/**
 * @file RingAlarmFragment.kt
 */
package io.github.kaztakgh.alarmclock2

import android.content.Context
import android.content.Intent
import android.os.*
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_ring_alarm.view.*
import java.util.*

/**
 * アラームが発動しているときの画面
 * モーダルウィンドウ形式で表示
 */
class RingAlarmFragment
    : Fragment() {
    /**
     * アラームの発動時間
     * スヌーズで使用する
     */
    private val alarmRingTime : Calendar = Calendar.getInstance()

    /**
     * 起動中は常に表示するためのHandler
     */
    private val handler = Handler()
    private val runnable = object : Runnable {
        /**
         * When an object implementing interface `Runnable` is used
         * to create a thread, starting the thread causes the object's
         * `run` method to be called in that separately executing
         * thread.
         *
         *
         * The general contract of the method `run` is that it may
         * take any action whatsoever.
         *
         * @see java.lang.Thread.run
         */
        override fun run() {
            // 一定時間経過後にアラームを止める
            stopAlarm()
        }
    }

    /**
     * Called to do initial creation of a fragment.  This is called after
     * [.onAttach] and before
     * [.onCreateView].
     *
     *
     * Note that this can be called while the fragment's activity is
     * still in the process of being created.  As such, you can not rely
     * on things like the activity's content view hierarchy being initialized
     * at this point.  If you want to do work once the activity itself is
     * created, see [.onActivityCreated].
     *
     *
     * Any restored child fragments will be created before the base
     * `Fragment.onCreate` method returns.
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        // 変数の設定
        val preferences = requireActivity().getSharedPreferences(GeneralSettingsFragment.GENERAL_SETTING_PREFERENCE, Context.MODE_PRIVATE)
        val alarmRingTimeArray = XmlHelper.getAttributeArrayByNameFromAssets(
            requireActivity(),
            GeneralSettingsFragment.FILE_GENERAL_SETTING,
            GeneralSettingsFragment.DATA_ALARM_RING_TIME
        )

        // アラーム停止時間の設定
        val autoStopSecond : Int = alarmRingTimeArray.find {
            it == preferences.getString(GeneralSettingsFragment.DATA_ALARM_RING_TIME, "30")
        }!!.toInt()
        handler.postDelayed(runnable, (autoStopSecond * 1000).toLong())
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null. This will be called between
     * [.onCreate] and [.onActivityCreated].
     *
     * A default View can be returned by calling [.Fragment] in your
     * constructor. Otherwise, this method returns null.
     *
     *
     * It is recommended to **only** inflate the layout in this method and move
     * logic that operates on the returned View to [.onViewCreated].
     *
     *
     * If you return a View from here, you will later be called in
     * [.onDestroyView] when the view is being released.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return Return the View for the fragment's UI, or null.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Viewのロード
        val view: View = inflater.inflate(R.layout.fragment_ring_alarm, container, false)
        // アラームのデータ取得
        val model = getAlarmRecord()

        // アラーム名表示
        when {
            model.name.isNotEmpty() -> {
                view.tv_name.text = model.name
            }
            else -> {
                view.tv_name.visibility = View.GONE
            }
        }

        // ボタンが押された時の挙動
        view.btn_stop.setOnClickListener {
            // handlerの解除
            handler.removeCallbacks(runnable)
            stopAlarm()
        }
        // SNOOZEボタンは設定がONかつ上限回数に達していない場合のみ使用できるようにする
        if (model.snoozeCount > 0
            && model.snoozeRepeated < model.snoozeCount) {
            view.btn_snooze.setOnClickListener {
                // handlerの解除
                handler.removeCallbacks(runnable)
                setSnooze()
            }
        }
        else {
            // SNOOZEボタンを非表示にする
            view.btn_snooze.visibility = View.GONE
        }

        // 戻るボタンが押された時の動作
        // ここでは終了しないようにする
        view.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                // return trueで何もしない
                when (keyCode) {
                    KeyEvent.KEYCODE_BACK ->
                        return@OnKeyListener true
                    else -> return@OnKeyListener false
                }
            }
            false
        })
        view.isFocusableInTouchMode = true

        return view
    }

    /**
     * 送信されたアラームIDからデータを取得
     *
     * @return アラームIDに該当するAlarmRecordModel
     */
    private fun getAlarmRecord() : AlarmRecordModel {
        // DBからのデータ読込
        val alarmId = requireArguments().getInt(ALARM_ID_KEY, 0)
        val alarmRecordObject = AlarmRecordDatabaseHelper(requireContext())
        val model = alarmRecordObject.readRecordModels(
            "${AlarmRecordTableObject.RecordColumns.COLUMN_ID} = ?",
            arrayOf(alarmId.toString())
        ).first()
        // DBをクローズする
        alarmRecordObject.close()

        return model
    }

    /**
     * アラームの停止
     * アラームの更新も行う
     */
    private fun stopAlarm() {
        // DBからデータを取得
        val model = getAlarmRecord()

        // スヌーズ回数を0にリセットする
        model.snoozeRepeated = 0

        // 繰り返し設定がある場合
        if (!model.repeatWeek.contentEquals(BooleanArray(WEEK_NUM) {false})) {
            // アラームのセット
            RingAlarmReceiver.setAlarm(requireActivity(), model)
        }
        else {
            // アラームをOFFにする
            model.state = false
            // アラームの解除
            RingAlarmReceiver.cancelAlarm(requireActivity(), model)
        }

        // DB更新
        val alarmRecordObject = AlarmRecordDatabaseHelper(requireActivity())
        alarmRecordObject.updateRecordModel(model)
        alarmRecordObject.close()

        // アラーム終了処理
        finishAlarmService()
    }

    /**
     * スヌーズのセット
     * アラームの更新も行う
     */
    private fun setSnooze() {
        // DBからデータを取得
        val model = getAlarmRecord()

        // スヌーズ実行回数のカウントを増やす
        model.snoozeRepeated++

        // DB更新
        val alarmRecordObject = AlarmRecordDatabaseHelper(requireContext())
        alarmRecordObject.updateRecordModel(model)
        alarmRecordObject.close()

        // 次回のアラーム発動時刻を指定
        alarmRingTime.set(Calendar.SECOND, 0)
        alarmRingTime.set(Calendar.MILLISECOND, 0)
        alarmRingTime.add(Calendar.MINUTE, model.snoozeTime)
        // アラームのセット
        RingAlarmReceiver.setAlarm(requireActivity(), model, alarmRingTime)

        // アラーム終了処理
        finishAlarmService()
    }

    /**
     * アラームの終了時の処理
     */
    private fun finishAlarmService() {
        // サービスの終了
        val intent = Intent(requireActivity(), RingAlarmService::class.java)
        requireActivity().stopService(intent)

        // ダイアログを閉じる
        requireActivity().finish()
    }
}