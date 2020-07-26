/**
 * @file AlarmListFragment.kt
 */
package io.github.kaztakgh.alarmclock2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.alarm_list_screen.view.*

/**
 * アラーム一覧の画面
 */
class AlarmListFragment
    : Fragment(), OnAlarmDeleteDialogSelectListener {
    /**
     * インスタンス作成
     * MainPagerで使用するため
     */
    companion object {
        fun newInstance() : AlarmListFragment {
            return AlarmListFragment()
        }
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null (which
     * is the default implementation).  This will be called between
     * [.onCreate] and [.onActivityCreated].
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
        val view: View = inflater.inflate(R.layout.alarm_list_screen, container, false)

        // アラーム追加ボタンを押下したときの処理
        view.fab_add_alarm.setOnClickListener {
            // 新規データを作成し、AlarmSettingFragmentへ移動する
            val newModel = AlarmRecordModel(
                volume = 50
            )
            moveToSetting(newModel)
        }

        // アイコンをクリックしたときの動作
        view.isFocusableInTouchMode = true
        view.requestFocus()
        return view
    }

    /**
     * Called immediately after [.onCreateView]
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     * @param view The View returned by [.onCreateView].
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        // アラームのデータを読み込む
        val alarmRecordDatabase = AlarmRecordDatabaseHelper(requireActivity())
        val recordList = alarmRecordDatabase.readRecordModels(null, null)
        alarmRecordDatabase.close()

        // アダプターの設定
        view.rv_alarm_list.layoutManager = LinearLayoutManager(view.context)
        val listAdapter = AlarmListViewAdapter(recordList)
        listAdapter.recordClickListener = ItemClickListener(recordList)
        listAdapter.recordLongClickListener = ItemLongClickListener()
        listAdapter.recordStateChangeListener = StateSwitchClickListener(recordList)
        view.rv_alarm_list.adapter = listAdapter
        // リストにアイテムがない場合
        if (recordList.size == 0) {
            // アラームがないテキストを表示
            view.rv_alarm_list.visibility = View.GONE
            view.tv_no_items.visibility = View.VISIBLE
        }
        // リストの登録数が最大値の場合
        else if (recordList.size == MAX_ALARM_NUM) {
            // 追加ボタンを表示しない
            view.fab_add_alarm.visibility = View.GONE
        }
    }

    /**
     * アラーム設定画面へ移動する
     *
     * @param model AlarmRecordModel
     */
    private fun moveToSetting(
        model: AlarmRecordModel
    ) {
        val args = Bundle()
        // AlarmSettingFragmentへ移動する
        val settingFragment = AlarmSettingFragment()
        // 移動するときにデータを同時に送付する
        args.putParcelable(AlarmSettingFragment.KEY_INPUT_MODEL, model)
        settingFragment.arguments = args

        // viewPagerの画面よりも上側に絵画するため、親フラグメントを変更する
        val fragmentTransaction = requireParentFragment().requireFragmentManager().beginTransaction()
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.replace(R.id.main_container, settingFragment)
        fragmentTransaction.commit()
    }

    /**
     * 削除を実行する場合
     *
     * @param reqCode 一覧のindex
     * @param resultCode ダイアログの押下内容
     */
    override fun onExecuteDelete(
        reqCode: Int,
        resultCode: Int
    ) {
        val adapter = requireView().rv_alarm_list.adapter as AlarmListViewAdapter
        val removeItem : AlarmRecordModel = adapter.recordList[reqCode]
        adapter.recordList.remove(removeItem)
        // データの表示
        adapter.notifyItemRemoved(reqCode)
        // 残り0件の場合はNo Alarms.を表示する
        if (adapter.recordList.size == 0) {
            requireView().rv_alarm_list.visibility = View.GONE
            requireView().tv_no_items.visibility = View.VISIBLE
        }

        // アラームがONの場合は登録されたアラームをキャンセルする
        if (removeItem.state) {
            // アラームのキャンセル
            removeItem.state = false
        }
        RingAlarmReceiver.cancelAlarm(requireActivity(), removeItem)

        // DB処理
        val alarmRecordDatabase = AlarmRecordDatabaseHelper(requireActivity())
        // DBからデータを消去する
        val delResult = alarmRecordDatabase.deleteRecordModel(removeItem.id)
        // DBをクローズする
        alarmRecordDatabase.close()

        // 消去のメッセージを表示する
        // 消去できなかった場合、失敗したことを表示する
        val toastText = if (delResult == 0) resources.getText(R.string.alarm_record_delete_failed)
        else resources.getText(R.string.alarm_delete)
        Toast.makeText(requireActivity(), toastText, Toast.LENGTH_SHORT).show()
    }

    /**
     * アイテムをクリックしたときの動作
     * アラームの編集画面へ移動する
     *
     * @property recordList アラームデータのリスト
     */
    inner class ItemClickListener(
        private val recordList: ArrayList<AlarmRecordModel>
    ) : AlarmListItemViewHolder.ItemClickListener {
        /**
         * アイテムをクリックしたときの処理
         *
         * @param view レイアウトビュー
         * @param position アダプター内のアイテムの位置
         */
        override fun onItemClick(
            view: View,
            position: Int
        ) {
            moveToSetting(recordList[position])
        }
    }

    /**
     * アイテムを長押ししたときの動作
     * アラームのデータの削除ダイアログを出す
     */
    inner class ItemLongClickListener
        : AlarmListItemViewHolder.ItemLongClickListener {
        /**
         * アイテムを長押ししたときの処理
         *
         * @param view レイアウトビュー
         * @param position アダプター内のアイテムの位置
         */
        override fun onItemLongClick(
            view: View,
            position: Int
        ) {
            // 消去確認ダイアログの作成
            val dialog = AlarmDeleteDialog()
            val args = Bundle()
            args.putInt(AlarmDeleteDialog.DIALOG_REQUEST_CODE, position)
            dialog.arguments = args
            dialog.setTargetFragment(this@AlarmListFragment, position)
            dialog.show(requireFragmentManager(), "alarmDelete")
        }
    }

    /**
     * スイッチを押したときの動作
     * アラームのON/OFFを切り替える
     *
     * @property recordList アラームデータのリスト
     */
    inner class StateSwitchClickListener(
        private val recordList: ArrayList<AlarmRecordModel>
    ) : AlarmListItemViewHolder.StateSwitchClickListener {
        /**
         * スイッチを押したときの処理
         * アラームをONにした場合は、アラームをセットする
         *
         * @param view レイアウトビュー
         * @param position アダプター内のアイテムの位置
         * @param state スイッチの状態
         */
        override fun onCheckChanged(
            view: View,
            position: Int,
            state: Boolean
        ) {
            val context = requireActivity()
            val updateModel = recordList[position]
            updateModel.state = state

            val alarmRecordDatabase = AlarmRecordDatabaseHelper(context)
            // DBに登録
            val result = alarmRecordDatabase.updateRecordModel(updateModel)
            // DBをクローズする
            alarmRecordDatabase.close()

            when {
                // 登録失敗時
                result <= 0 -> {
                    Toast.makeText(context, context.resources.getText(R.string.alarm_record_register_failed), Toast.LENGTH_SHORT).show()
                }
                // アラームONの場合
                state -> {
                    // アラームのセット
                    RingAlarmReceiver.setAlarm(context, updateModel)
                    // セット時のテキストを表示
                    Toast.makeText(context, resources.getText(R.string.alarm_set), Toast.LENGTH_SHORT).show()
                }
                // アラームOFFの場合
                else -> {
                    // アラームのキャンセル
                    RingAlarmReceiver.cancelAlarm(context, updateModel)
                    // キャンセル時のテキストを表示
                    Toast.makeText(context, resources.getText(R.string.alarm_cancel), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}