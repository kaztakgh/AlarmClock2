/**
 * @file MainPagerFragment.kt
 */
package io.github.kaztakgh.alarmclock2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import kotlinx.android.synthetic.main.fragment_main_pager.view.*

/**
 * 各機能の画面を表示するFragment
 */
class MainPagerFragment
    : Fragment() {
    /**
     * タブのアイコンの配列
     */
    private val tabIconArray = arrayListOf(
        R.drawable.ic_clock_24dp,
        R.drawable.ic_alarm_24dp
    )

    /**
     * タブのテキストの配列
     */
    private val tabTextArray = arrayListOf(
        R.string.tab_clock,
        R.string.tab_alarm
    )

    /**
     * アプリが起動済みであるか
     */
    private var hasStarted = true

    /**
     * タブの位置の保存
     */
    private val mainPagerViewModel = MainPagerViewModel()

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 起動済みであるかの変数を取得する
        if (arguments?.getBoolean(MainActivity.KEY_IS_STARTED) != null) {
            hasStarted = arguments?.getBoolean(MainActivity.KEY_IS_STARTED, true)!!
        }
        // 初回起動時orストレージ消去後に起動時
        if (!hasStarted) {
            // 祝日データの読み込み
            val intent = Intent(requireActivity(), HolidayDataAccessJobService::class.java)
            intent.putExtra(HOLIDAY_DATA_SEND_ACTIVITY, this.javaClass.simpleName)
            HolidayDataAccessJobService.enqueueWork(requireActivity(), intent)
        }
    }

    /**
     * Viewを作成する
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return View fragment_main_pager.xmlに示すレイアウト画面
     * <br>クラス内のviewはonCreateViewで作成した画面を指す
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // このフラグメントで使用するレイアウトリソースの指定
        val view: View = inflater.inflate(R.layout.fragment_main_pager, container, false)
        // 前回のタブ位置を読み込む
        val initIndex = mainPagerViewModel.pageIndex.value
        // pagerの使用設定
        val pageAdapter = PageAdapter(childFragmentManager)
        view.pager.adapter = pageAdapter
        // 最初の表示タブの設定
        view.pager.currentItem = initIndex!!
        view.tab_top.setupWithViewPager(view.pager)

        // レイアウト(アイコン、テキスト)をセットする
        for (index in 0 until view.tab_top.tabCount) {
            val tab = view.tab_top.getTabAt(index)
            // タブにテーマカラーを使用させるため、変数にthisを代入
            val tabView = TabItemView(requireActivity())
            tabView.icon.setImageResource(tabIconArray[index])
            tabView.text.text = resources.getText(tabTextArray[index])
            // 再読み込み時に必ずindex=0の場合のみisSelected=trueになっている
            tabView.isSelectedMenu = tab!!.isSelected
            tab.customView = tabView
        }

        // ページ切り替え時のタブの絵画変更
        view.pager.addOnPageChangeListener(OnScreenChangeListener())
        // メニューの設定
        view.tb_main_pager.inflateMenu(R.menu.menu_main_pager)
        view.tb_main_pager.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                // 設定
                R.id.menu_general_setting -> {
                    // viewPagerの画面よりも上側に絵画するため、親フラグメントを変更する
                    val fragmentTransaction = parentFragmentManager.beginTransaction()
                    val fragment = GeneralSettingsFragment()
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.replace(R.id.main_container, fragment)
                    fragmentTransaction.commit()
                    return@setOnMenuItemClickListener true
                }
                // 祝日一覧
                R.id.menu_holiday -> {
                    // viewPagerの画面よりも上側に絵画するため、親フラグメントを変更する
                    val fragmentTransaction = parentFragmentManager.beginTransaction()
                    val fragment = HolidayListFragment()
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.replace(R.id.main_container, fragment)
                    fragmentTransaction.commit()
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener false
                }
            }
        }

        return view
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to [Activity.onResume] of the containing
     * Activity's lifecycle.
     */
    override fun onResume() {
        super.onResume()
        if (!hasStarted) {
            // 祝日データの取得
            val intentFilter = IntentFilter()
            intentFilter.addAction(HOLIDAY_DATA_CREATE_SERVICE)
            requireActivity().registerReceiver(holidayDataCreatedReceiver, intentFilter)
        }
    }

    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to [Activity.onPause] of the containing
     * Activity's lifecycle.
     */
    override fun onPause() {
        super.onPause()
        // 初回起動時orストレージ消去後に起動時
        if (!hasStarted) {
            requireActivity().unregisterReceiver(holidayDataCreatedReceiver)
        }
    }

    /**
     * 画面切り替え時のタブの絵画を変更する
     * @see [SimpleOnPageChangeListener]
     */
    inner class OnScreenChangeListener
        : SimpleOnPageChangeListener() {
        /**
         * ページが移動したときの処理
         *
         * @param state 移動状態のステータス
         * @see androidx.viewpager.widget.ViewPager
         */
        override fun onPageScrollStateChanged(
            state: Int
        ) {
            when(state) {
                // ドラッグ開始時、ドラッグ終了時
                ViewPager.SCROLL_STATE_DRAGGING,
                ViewPager.SCROLL_STATE_SETTLING -> {
                    for (index in 0 until requireView().tab_top.tabCount) {
                        val tabView = getTabItemView(index)
                        // タブの選択状態をOFFにする
                        if (tabView.isSelectedMenu) {
                            tabView.isSelectedMenu = false
                        }
                    }
                }
                else -> {}
            }
        }

        /**
         * ページがスクロールしているときの処理
         *
         * @param position 現在ページ
         * @param positionOffset 左から見た現在ページの画面左端の位置
         * @param positionOffsetPixels 移動距離
         */
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            // ページが端の場合
            if ((position == 0 || position == requireView().tab_top.tabCount - 1)
                && positionOffsetPixels == 0) {
                // 選択状態をONのまま保持する
                val tabView = getTabItemView(position)
                tabView.isSelectedMenu = true
            }
        }

        /**
         * 選択されたページが確定したときの処理
         *
         * @param position 選択したページ
         */
        override fun onPageSelected(
            position: Int
        ) {
            val tabView = getTabItemView(position)
            if (!tabView.isSelectedMenu) {
                // 選択状態をONにする
                tabView.isSelectedMenu = true
            }

            // ViewModelに値を保存
            val pageLiveData = MutableLiveData<Int>()
            pageLiveData.value = position
            mainPagerViewModel.pageIndex = pageLiveData
        }

        /**
         * タブの取得
         *
         * @param position ページ
         * @return ページに対応するTabItemView
         */
        private fun getTabItemView(
            position: Int
        ) : TabItemView {
            val tab = requireView().tab_top.getTabAt(position)
            return tab!!.customView as TabItemView
        }
    }

    /**
     * 祝日データを作成時に行う処理
     */
    private val holidayDataCreatedReceiver = object : BroadcastReceiver() {
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
            // 定期取得サービスの登録
            HolidayDataUpdateReceiver.setUpdate(context!!)
        }
    }
}
