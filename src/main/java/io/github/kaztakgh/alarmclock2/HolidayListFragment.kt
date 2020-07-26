/**
 * @file HolidayListFragment.kt
 */
package io.github.kaztakgh.alarmclock2


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.kaztakgh.viewhelper.ViewHelper
import kotlinx.android.synthetic.main.holiday_list_screen.view.*
import kotlin.collections.ArrayList

/**
 * 祝日データの表示と更新を行う
 */
class HolidayListFragment
    : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Viewのロード
        val view: View = inflater.inflate(R.layout.holiday_list_screen, container, false)
        // navigationIconで使用する画像をテキストで使用している色で塗りつぶす
        val arrowBackIcon = view.tb_holiday_list.navigationIcon
        val fillColor = ViewHelper.getColorAttr(requireActivity(), android.R.attr.textColorPrimary)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrowBackIcon!!.colorFilter = BlendModeColorFilter(fillColor, BlendMode.SRC_IN)
        }
        else {
            arrowBackIcon!!.setColorFilter(fillColor, PorterDuff.Mode.SRC_IN)
        }
        view.tb_holiday_list.navigationIcon = arrowBackIcon

        // メニューの設定
        view.tb_holiday_list.inflateMenu(R.menu.menu_holiday_list)
        view.tb_holiday_list.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                // データの更新
                R.id.update_holiday -> {
                    val intent = Intent(requireActivity(), HolidayDataAccessJobService::class.java)
                    intent.putExtra(HOLIDAY_DATA_SEND_ACTIVITY, this.javaClass.simpleName)
                    HolidayDataAccessJobService.enqueueWork(requireActivity(), intent)
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener false
                }
            }
        }

        // navigationIconをクリックした場合
        view.tb_holiday_list.setNavigationOnClickListener {
            requireFragmentManager().popBackStack()
        }

        // Backボタンが押されたときの処理
        view.setOnKeyListener { _, keyCode, event ->
            if(event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                // アラーム一覧画面に戻る
                requireFragmentManager().popBackStack()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // データの表示
        // DBから取得する処理を追加
        val holidayDatabase = HolidayDatabaseHelper(requireActivity())
        val holidayDataArray = holidayDatabase.readHoliday(null, null)
        showHolidayDataList(view, holidayDataArray)
        // DBをクローズする
        holidayDatabase.close()
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to [Activity.onResume] of the containing
     * Activity's lifecycle.
     */
    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(HOLIDAY_ACCESS_SERVICE)
        requireActivity().registerReceiver(holidayListDataReceiver, intentFilter)
    }

    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to [Activity.onPause] of the containing
     * Activity's lifecycle.
     */
    override fun onPause() {
        requireActivity().unregisterReceiver(holidayListDataReceiver)
        super.onPause()
    }

    /**
     * 読み込んだ祝日データの表示
     *
     * @param view 表示画面
     * @param modelArray 祝日データの配列
     */
    private fun showHolidayDataList(
        view: View,
        modelArray: ArrayList<HolidayModel>
    ) {
        view.rv_holiday.layoutManager = LinearLayoutManager(view.context)
        val listAdapter = HolidayListViewAdapter(modelArray)
        view.rv_holiday.adapter = listAdapter

        // 区切り線のセット
        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        view.rv_holiday.addItemDecoration(itemDecoration)
    }

    /**
     * 祝日データを更新したときに行う処理
     */
    private val holidayListDataReceiver = object : BroadcastReceiver() {
        /**
         * This method is called when the BroadcastReceiver is receiving an Intent
         * broadcast.  During this time you can use the other methods on
         * BroadcastReceiver to view/modify the current result values.  This method
         * is always called within the main thread of its process, unless you
         * explicitly asked for it to be scheduled on a different thread using
         * [android.content.Context.registerReceiver]. When it runs on the main
         * thread you should
         * never perform long-running operations in it (there is a timeout of
         * 10 seconds that the system allows before considering the receiver to
         * be blocked and a candidate to be killed). You cannot launch a popup dialog
         * in your implementation of onReceive().
         *
         *
         * **If this BroadcastReceiver was launched through a &lt;receiver&gt; tag,
         * then the object is no longer alive after returning from this
         * function.** This means you should not perform any operations that
         * return a result to you asynchronously. If you need to perform any follow up
         * background work, schedule a [android.app.job.JobService] with
         * [android.app.job.JobScheduler].
         *
         * If you wish to interact with a service that is already running and previously
         * bound using [bindService()][android.content.Context.bindService],
         * you can use [.peekService].
         *
         *
         * The Intent filters used in [android.content.Context.registerReceiver]
         * and in application manifests are *not* guaranteed to be exclusive. They
         * are hints to the operating system about how to find suitable recipients. It is
         * possible for senders to force delivery to specific recipients, bypassing filter
         * resolution.  For this reason, [onReceive()][.onReceive]
         * implementations should respond only to known actions, ignoring any unexpected
         * Intents that they may receive.
         *
         * @param context The Context in which the receiver is running.
         * @param intent The Intent being received.
         */
        override fun onReceive(
            context: Context?,
            intent: Intent?
        ) {
            // DBからデータを取得
            val holidayArray = intent!!.getParcelableArrayListExtra<HolidayModel>(HOLIDAY_DATA_KEY)
            showHolidayDataList(requireView(), holidayArray!!)
            Toast.makeText(context, resources.getText(R.string.holiday_list_downloaded), Toast.LENGTH_SHORT).show()
        }
    }
}
