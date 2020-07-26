/**
 * @file SettingsFragment.kt
 */
package io.github.kaztakgh.alarmclock2

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.fragment.app.Fragment
import io.github.kaztakgh.dialogfragmentslibrary.SingleSelectorDialog
import io.github.kaztakgh.settingitemsviewlibrary.*
import kotlinx.android.synthetic.main.fragment_setting.view.*
import java.util.ArrayList

/**
 * 設定画面
 */
class GeneralSettingsFragment
    : BaseSettingFragment() {
    /**
     * インスタンス作成
     */
    companion object {
        /**
         * XML ファイル名
         * assetsフォルダに配置
         */
        const val FILE_GENERAL_SETTING = "general_settings.xml"

        /**
         * SharedPreference ファイル名
         */
        const val GENERAL_SETTING_PREFERENCE = "GeneralSettings"

        /**
         * XML 属性名
         * 時計種類
         */
        const val DATA_CLOCK_TYPE = "clockType"

        /**
         * XML 属性名
         * 秒表示
         */
        const val DATA_CLOCK_SECOND = "clockSecond"

        /**
         * XML 属性名
         * テーマ
         */
        const val DATA_THEME = "theme"

        /**
         * XML 属性名
         * アラーム発動時間
         */
        const val DATA_ALARM_RING_TIME = "ringSec"

        /**
         * XML 属性名
         * マナーモード優先順位
         */
        const val DATA_MODE_PRIORITY = "mode"

        /**
         * XML タグ名
         * 時計種類
         */
        const val TAG_CLOCK_TYPE = "clockType"

        /**
         * XML タグ名
         * 秒表示
         */
        const val TAG_CLOCK_SECOND = "clockSecond"

        /**
         * XML タグ名
         * テーマ
         */
        const val TAG_THEME = "theme"

        /**
         * XML タグ名
         * アラーム発動時間
         */
        const val TAG_ALARM_RING_TIME = "alarmRingTime"

        /**
         * XML タグ名
         * マナーモード優先順位
         */
        const val TAG_MODE_PRIORITY = "modePriority"

        /**
         * SharedPreference 記録用パラメータ
         * デジタル時計
         */
        const val CLOCK_TYPE_DIGITAL = "digital"

        /**
         * SharedPreference 記録用パラメータ
         * アナログ時計
         */
        const val CLOCK_TYPE_ANALOG = "analog"

        /**
         * LightMode
         */
        const val THEME_LIGHT_INDEX = 1

        /**
         * DarkMode
         */
        const val THEME_DARK_INDEX = 2

        /**
         * マナーモード優先OFF
         */
        const val MODE_RING_ALARM_SOUND = "normal"

        /**
         * マナーモード優先ON
         */
        const val MODE_SILENT = "silent"
    }

    /**
     * SettingItemsListに紐づけるFragment
     * このクラス自身を指定する
     */
    override val settingListTargetFragment : Fragment
        get() = this

    /**
     * 環境設定のパラメータ群
     */
    private lateinit var preferences: SharedPreferences

    /**
     * Called when a fragment is first attached to its context.
     * [.onCreate] will be called after this.
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        preferences = requireActivity().getSharedPreferences(
            GENERAL_SETTING_PREFERENCE,
            Context.MODE_PRIVATE
        )
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
        // ツールバーのタイトルとメニューの設定
        view.tb_setting.title = requireActivity().resources.getText(R.string.menu_general_setting)
    }

    /**
     * setting_itemsの初期表示の設定を行う
     * 必要なパラメータは外部から読み込むこと
     *
     * @param [.onCreateView]で返すViewにあるsetting_items
     */
    override fun createSettingItemArray(
        view: SettingItemsView
    ) : ArrayList<ItemInterface> {
        // ファイルから読み込む
        val itemsInitList = SettingItems.readXmlSettingsFromAssets(
            requireActivity(),
            FILE_GENERAL_SETTING
        )

        // 設定したタグをもとに変更を行っていく
        itemsInitList.forEach {
            when(it.tag) {
                // 時計種類
                TAG_CLOCK_TYPE -> {
                    // 記録は文字列なので、その文字列の位置を検索する
                    var initSelect: Int = getInitSelectFromAttribute(DATA_CLOCK_TYPE, CLOCK_TYPE_DEFAULT)
                    // 該当箇所が存在しない場合はデジタル時計を選択する
                    if (initSelect == -1)
                        initSelect = 0
                    (it as SpinnerItem).select = initSelect
                    it.selectChangeListener = OnClockTypeChangeListener()
                }
                // 秒表示
                TAG_CLOCK_SECOND -> {
                    val displaySec = preferences.getBoolean(DATA_CLOCK_SECOND, false)
                    (it as SwitchItem).checked = displaySec
                    it.stateChangedListener = OnDisplaySecondChangeListener()
                }
                // テーマ
                TAG_THEME -> {
                    // 記録は文字列なので、その文字列の位置を検索する
                    var initSelect: Int = getInitSelectFromAttribute(DATA_THEME, THEME_DEFAULT)
                    // 該当箇所が存在しない場合はシステムのテーマを選択する
                    if (initSelect == -1)
                        initSelect = 0
                    (it as SingleSelectItem).select = initSelect
                }
                // アラームの動作時間
                TAG_ALARM_RING_TIME -> {
                    // 記録は数値なので、その値を検索する
                    var initSelect: Int = getInitSelectFromAttribute(DATA_ALARM_RING_TIME, ALARM_RING_TIME_DEFAULT.toString())
                    // 該当箇所が存在しない場合は30秒を選択する
                    if (initSelect == -1)
                        initSelect = 0
                    (it as SpinnerItem).select = initSelect
                    it.selectChangeListener = OnAlarmTimeChangeListener()
                }
                // マナーモード優先順位
                TAG_MODE_PRIORITY -> {
                    var initSelect: Int = getInitSelectFromAttribute(DATA_MODE_PRIORITY, ALARM_RING_MODE_DEFAULT)
                    // 該当箇所が存在しない場合は、通常を選択する
                    if (initSelect == -1)
                        initSelect = 0
                    (it as SpinnerItem).select = initSelect
                    it.selectChangeListener = OnModeChangeListener()
                }
                else -> {}
            }
        }

        // 内容を確定
        return itemsInitList
    }

    /**
     * setting_itemsのSingleSelectItemを更新する
     *
     * @param item SingleSelectItem
     * @param params ダイアログから送られてきたパラメータ
     */
    override fun updateSingleSelectItem(
        adapter: SettingItemsAdapter,
        item: SingleSelectItem,
        params: Bundle
    ) {
        super.updateSingleSelectItem(adapter, item, params)
        if (item.tag == TAG_THEME) {
            // SharedPreferenceへデータを上書きする
            savePreference(DATA_THEME, params.getInt(SingleSelectorDialog.DIALOG_SELECTED_ITEM_POS))

            // テーマの変更
            when(params.getInt(SingleSelectorDialog.DIALOG_SELECTED_ITEM_POS)) {
                THEME_LIGHT_INDEX -> {
                    setDefaultNightMode(MODE_NIGHT_NO)
                }
                THEME_DARK_INDEX -> {
                    setDefaultNightMode(MODE_NIGHT_YES)
                }
                else -> {
                    setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
        }
    }

    /**
     * その他のアイテムまたはFragmentの更新
     * setting_itemsでは対応できない範囲のダイアログ処理
     *
     * @param requestCode
     * @param resultCode
     * @param params
     */
    override fun updateOtherItemOnDialog(
        requestCode: Int,
        resultCode: Int,
        params: Bundle
    ) {}

    /**
     * XMLとSharedPreferenceから初期位置の取得を行う
     *
     * @param attribute 属性
     * @param defValue 属性が存在しない場合の検索値
     * @return 初期位置(見つからない場合は-1)
     */
    private fun getInitSelectFromAttribute(
        attribute: String,
        defValue: String = ""
    ): Int {
        // 記録は数値なので、その値を検索する
        val attributeArray = XmlHelper.getAttributeArrayByNameFromAssets(
            requireActivity(),
            FILE_GENERAL_SETTING,
            attribute
        )

        // 初期位置について、記録された文字列と同じ値が出てくる最初の要素位置を返す
        return attributeArray.indexOfFirst { s ->
            s == preferences.getString(attribute, defValue)
        }
    }

    /**
     * SharedPreferenceへの書込
     *
     * @param attributeName 属性
     * @param position 位置
     */
    private fun savePreference(
        attributeName: String,
        position: Int
    ) {
        // 記録用の配列を作成
        val attributeArray = XmlHelper.getAttributeArrayByNameFromAssets(
            requireActivity(),
            FILE_GENERAL_SETTING,
            attributeName
        )

        // データの書き込み
        val editor = preferences.edit()
        editor.putString(
            attributeName,
            attributeArray[position]
        )
        editor.apply()
    }

    /**
     * 時計の種類を変更したときの処理
     */
    inner class OnClockTypeChangeListener
        : SpinnerItem.OnItemStateChangeListener {
        override fun onItemSelectChanged(
            adapter: SettingItemsAdapter,
            position: Int
        ) {
            // SharedPreferenceへデータを上書きする
            savePreference(DATA_CLOCK_TYPE, position)
        }
    }

    /**
     * 秒表示の設定を変更したときの処理
     */
    inner class OnDisplaySecondChangeListener
        : SwitchItem.OnItemStateChangeListener {
        override fun onSwitchCheckChange(
            adapter: SettingItemsAdapter,
            checked: Boolean
        ) {
            // データの書き込み
            val editor = preferences.edit()
            editor.putBoolean(DATA_CLOCK_SECOND, checked)
            editor.apply()
        }
    }

    /**
     * アラーム時間の設定を変更したときの処理
     */
    inner class OnAlarmTimeChangeListener
        : SpinnerItem.OnItemStateChangeListener {
        override fun onItemSelectChanged(
            adapter: SettingItemsAdapter,
            position: Int
        ) {
            // SharedPreferenceへデータを上書きする
            savePreference(DATA_ALARM_RING_TIME, position)
        }
    }

    /**
     * マナーモード優先順位を変更したときの処理
     */
    inner class OnModeChangeListener
        : SpinnerItem.OnItemStateChangeListener {
        override fun onItemSelectChanged(
            adapter: SettingItemsAdapter,
            position: Int
        ) {
            // SharedPreferenceへデータを上書きする
            savePreference(DATA_MODE_PRIORITY, position)
        }
    }
}