/**
 * @file BaseSettingFragment.kt
 */
package io.github.kaztakgh.alarmclock2

import android.content.DialogInterface
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import io.github.kaztakgh.dialogfragmentslibrary.DateTimeDialog
import io.github.kaztakgh.dialogfragmentslibrary.MultiSelectorDialog
import io.github.kaztakgh.dialogfragmentslibrary.OnDialogSelectListener
import io.github.kaztakgh.dialogfragmentslibrary.SingleSelectorDialog
import io.github.kaztakgh.settingitemsviewlibrary.*
import io.github.kaztakgh.viewhelper.ViewHelper
import kotlinx.android.synthetic.main.fragment_setting.view.*
import kotlinx.android.synthetic.main.fragment_setting.view.setting_items
import java.util.*

/**
 * SettingItemsViewLibraryを使用する設定画面の共通項目
 * それぞれの設定項目は、継承したクラスで記述する
 */
abstract class BaseSettingFragment
    : Fragment(), OnDialogSelectListener {
    /**
     * SettingItemsListに紐づけるFragment
     * このクラス自身を指定する
     */
    protected open val settingListTargetFragment : Fragment
        get() = this

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
        val view: View = inflater.inflate(R.layout.fragment_setting, container, false)

        // navigationIconで使用する画像をテキストで使用している色で塗りつぶす
        val arrowBackIcon = view.tb_setting.navigationIcon
        val fillColor = ViewHelper.getColorAttr(requireActivity(), android.R.attr.textColorPrimary)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrowBackIcon!!.colorFilter = BlendModeColorFilter(fillColor, BlendMode.SRC_IN)
        }
        else {
            arrowBackIcon!!.setColorFilter(fillColor, PorterDuff.Mode.SRC_IN)
        }
        view.tb_setting.navigationIcon = arrowBackIcon

        // navigationIconをクリックした場合
        view.tb_setting.setNavigationOnClickListener {
            // アラーム一覧画面に戻る
            parentFragmentManager.popBackStack()
        }

        // Backボタンが押されたときの処理
        view.setOnKeyListener { _, keyCode, event ->
            if(event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                // アラーム一覧画面に戻る
                parentFragmentManager.popBackStack()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        val settingItemsParameters = createSettingItemArray(view.setting_items)
        // SettingItemsViewにデータを挿入
        view.setting_items.fragment = settingListTargetFragment
        view.setting_items.fragmentManager = fragmentManager
        view.setting_items.itemsList = settingItemsParameters

        return view
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on [.requestPermissions].
     *
     *
     * **Note:** It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     *
     *
     * @param requestCode The request code passed in [.requestPermissions].
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     * which is either [android.content.pm.PackageManager.PERMISSION_GRANTED]
     * or [android.content.pm.PackageManager.PERMISSION_DENIED]. Never null.
     *
     * @see .requestPermissions
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0] == PermissionChecker.PERMISSION_GRANTED){
            Toast.makeText(
                requireActivity(),
                requireActivity().resources.getText(R.string.request_permission),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDialogCancel(
        reqCode: Int
    ) {}

    override fun onDialogSuccess(
        reqCode: Int,
        resultCode: Int,
        params: Bundle
    ) {
        if (resultCode == DialogInterface.BUTTON_POSITIVE) {
            val adapter = requireView().setting_items.getSettingItemsAdapter()
            // requestCodeのみの配列を取得して確認する
            // 現在のリストの状態から型を取得する
            when (val selectedItem = adapter.getItemFromRequestCode(reqCode)) {
                // 単一選択
                is SingleSelectItem -> {
                    // SingleSelectItemの更新
                    updateSingleSelectItem(adapter, selectedItem, params)
                }
                // 複数選択
                is MultiSelectItem -> {
                    // MultiSelectItemの更新
                    updateMultiSelectItem(adapter, selectedItem, params)
                }
                // 日付
                is DateItem -> {
                    // DateItemの更新
                    updateDateItem(adapter, selectedItem, params)
                }
                // 時刻
                is TimeItem -> {
                    // TimeItemの更新
                    updateTimeItem(adapter, selectedItem, params)
                }
                // その他の場合は必要な処理のみを行うようにする
                else -> {
                    updateOtherItemOnDialog(reqCode, resultCode, params)
                }
            }
        }
    }

    /**
     * setting_itemsのSingleSelectItemを更新する
     *
     * @param adapter SettingItemsAdapter(setting_itemsの表示を行っているアダプター)
     * @param item SingleSelectItem
     * @param params ダイアログから送られてきたパラメータ
     */
    open fun updateSingleSelectItem(
        adapter: SettingItemsAdapter,
        item: SingleSelectItem,
        params: Bundle
    ) {
        // ダイアログで選択したアイテムの位置を取得する
        val pos : Int = params.getInt(SingleSelectorDialog.DIALOG_SELECTED_ITEM_POS)
        // 対応するアイテムから選択位置を更新
        item.select = pos
        // ビューの更新
        adapter.update(item.tag, item)
    }

    /**
     * setting_itemsのMultiSelectItemを更新する
     *
     * @param adapter SettingItemsAdapter(setting_itemsの表示を行っているアダプター)
     * @param item MultiSelectItem
     * @param params ダイアログから送られてきたパラメータ
     */
    open fun updateMultiSelectItem(
        adapter: SettingItemsAdapter,
        item: MultiSelectItem,
        params: Bundle
    ) {
        // ダイアログで選択したアイテムの状態を取得する
        val selectParams : BooleanArray = params.getBooleanArray(MultiSelectorDialog.DIALOG_SELECTED_ITEM_STATE)!!
        // 対応するアイテムから選択位置を更新
        item.select = selectParams
        // ビューの更新
        adapter.update(item.tag, item)
    }

    /**
     * setting_itemsのDateItemを更新する
     *
     * @param adapter SettingItemsAdapter(setting_itemsの表示を行っているアダプター)
     * @param item DateItem
     * @param params ダイアログから送られてきたパラメータ
     */
    open fun updateDateItem(
        adapter: SettingItemsAdapter,
        item: DateItem,
        params: Bundle
    ) {
        // ダイアログで選択した日付をCalendar型の変数にセットする
        val date : Calendar = Calendar.getInstance()
        date.set(
            params.getInt(DateTimeDialog.DIALOG_CALENDAR_YEAR),
            params.getInt(DateTimeDialog.DIALOG_CALENDAR_MONTH),
            params.getInt(DateTimeDialog.DIALOG_CALENDAR_DAY)
        )
        // 対応するアイテムから日付を更新
        item.date = date
        // ビューの更新
        adapter.update(item.tag, item)
    }

    /**
     * setting_itemsのTimeItemを更新する
     *
     * @param adapter SettingItemsAdapter(setting_itemsの表示を行っているアダプター)
     * @param item TimeItem
     * @param params ダイアログから送られてきたパラメータ
     */
    open fun updateTimeItem(
        adapter: SettingItemsAdapter,
        item: TimeItem,
        params: Bundle
    ) {
        // ダイアログで選択した時刻をCalendar型の変数にセットする
        val time : Calendar = Calendar.getInstance()
        time.set(Calendar.HOUR_OF_DAY, params.getInt(DateTimeDialog.DIALOG_CLOCK_HOUR))
        time.set(Calendar.MINUTE, params.getInt(DateTimeDialog.DIALOG_CLOCK_MINUTE))
        time.set(Calendar.SECOND, params.getInt(DateTimeDialog.DIALOG_CLOCK_SECOND))
        time.set(Calendar.MILLISECOND, params.getInt(DateTimeDialog.DIALOG_CLOCK_MILLISECOND))
        // 対応するアイテムから時刻を更新
        item.time = time
        // ビューの更新
        adapter.update(item.tag, item)
    }

    /**
     * setting_itemsの初期表示の設定を行う
     * 必要なパラメータは外部から読み込むこと
     *
     * @param [.onCreateView]で返すView
     */
    abstract fun createSettingItemArray(
        view: SettingItemsView
    ) : ArrayList<ItemInterface>

    /**
     * その他のアイテムまたはFragmentの更新
     * setting_itemsでは対応できない範囲のダイアログ処理
     *
     * @param requestCode リクエストコード
     * @param resultCode 結果
     * @param params ダイアログから送信された値
     */
    abstract fun updateOtherItemOnDialog(
        requestCode: Int,
        resultCode: Int,
        params: Bundle
    )
}