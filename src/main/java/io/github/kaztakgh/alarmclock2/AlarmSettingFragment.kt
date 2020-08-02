/**
 * @file AlarmSettingFragment.kt
 */
package io.github.kaztakgh.alarmclock2

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_ID
import io.github.kaztakgh.dialogfragmentslibrary.MessageDialog
import io.github.kaztakgh.settingitemsviewlibrary.*
import io.github.kaztakgh.viewhelper.ViewHelper
import kotlinx.android.synthetic.main.fragment_setting.view.*
import kotlinx.android.synthetic.main.fragment_setting.view.setting_items
import java.util.*
import kotlin.collections.ArrayList

/**
 * アラームの設定を行う画面を表示
 */
class AlarmSettingFragment
    : BaseSettingFragment() {
    /**
     * 定数
     */
    companion object {
        /**
         * 入力モデル キー名
         */
        const val KEY_INPUT_MODEL = "inputModel"

        /**
         * XML ファイル名
         */
        const val FILE_ALARM_SETTING = "alarm_settings.xml"

        /**
         * XML 属性名
         * スヌーズ時に待つ時間(分)
         */
        const val DATA_SNOOZE_MINUTE = "data-minute"

        /**
         * XML 属性名
         * スヌーズ実行回数
         */
        const val DATA_SNOOZE_TIME = "data-time"

        /**
         * XML タグ名
         * アラームON/OFF
         */
        const val TAG_ALARM_ON_OFF = "alarmOnOff"

        /**
         * XML タグ名
         * アラーム名
         */
        const val TAG_ALARM_NAME = "alarmName"

        /**
         * XML タグ名
         * アラーム発動時間
         */
        const val TAG_ALARM_TIME = "alarmTime"

        /**
         * XML タグ名
         * 繰り返し曜日
         */
        const val TAG_REPEAT_WEEK = "repeatWeek"

        /**
         * XML タグ名
         * 祝日の繰り返し設定
         */
        const val TAG_HOLIDAY_REPEAT = "holidayRepeat"

        /**
         * XML タグ名
         * ダウンロードしたファイルの使用
         */
        const val TAG_USE_FILE = "useFile"

        /**
         * XML タグ名
         * プリセット音選択肢
         */
        const val TAG_SOUND_SELECT = "soundSelect"

        /**
         * XML タグ名
         * アラームに使用するファイル名
         */
        const val TAG_SOUND_FILE = "alarmSoundFile"

        /**
         * XML タグ名
         * 音量
         */
        const val TAG_VOLUME = "volume"

        /**
         * XML タグ名
         * バイブレーション
         */
        const val TAG_VIBRATE = "vibrate"

        /**
         * XML タグ名
         * スヌーズ時間間隔
         */
        const val TAG_SNOOZE_DURATION = "snoozeTimeDuration"

        /**
         * XML タグ名
         * スヌーズ実行回数
         */
        const val TAG_SNOOZE_COUNT = "snoozeCount"

        /**
         * プリセット音から選択の選択肢位置
         */
        const val VAL_SELECT_PRESET = 0

        /**
         * ファイルから選択の選択肢位置
         */
        const val VAL_SELECT_FILE = 1
    }

    /**
     * SettingItemsListに紐づけるFragment
     * このクラス自身を指定する
     */
    override val settingListTargetFragment : Fragment
        get() = this

    /**
     * アラーム追加フラグ
     */
    private var isAddAlarm: Boolean = false

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
        // データのチェック
        val inputModel: AlarmRecordModel = arguments?.getParcelable<AlarmRecordModel>(KEY_INPUT_MODEL) as AlarmRecordModel
        // 入力したモデルのIDが0(アラーム追加ボタンから移動した)の場合、追加フラグをONにする
        if (inputModel.id == 0)
            isAddAlarm = true
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
        var toolbarStringId = R.string.title_alarm_modify
        var menuId = R.menu.menu_alarm_modify
        val fillColor = ViewHelper.getColorAttr(requireActivity(), android.R.attr.textColorPrimary)
        // 追加画面の場合
        if (isAddAlarm) {
            // 表示内容を変更
            toolbarStringId = R.string.title_alarm_add
            menuId = R.menu.menu_alarm_add
        }
        view.tb_setting.title = requireActivity().resources.getText(toolbarStringId)
        view.tb_setting.inflateMenu(menuId)

        // アイコンのカラーの変更
        view.tb_setting.menu.forEach {
            // セットされているアイコンの取得
            val icon = it.icon
            // 取得したアイコンがnullではない場合、カラーを変更して再度セットする
            if (icon !== null) {
                icon.mutate()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    icon.colorFilter = BlendModeColorFilter(fillColor, BlendMode.SRC_IN)
                }
                else {
                    icon.setColorFilter(fillColor, PorterDuff.Mode.SRC_IN)
                }
                it.icon = icon
            }
        }

        // メニューを押したときの処理
        view.tb_setting.setOnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                // 追加
                R.id.menu_add -> {
                    val model = convertListValueToModel()
                    addAlarm(requireActivity(), model)
                    return@setOnMenuItemClickListener true
                }
                // 変更
                R.id.menu_modify -> {
                    val model = convertListValueToModel()
                    modifyAlarm(requireActivity(), model)
                    return@setOnMenuItemClickListener true
                }
                // 削除
                R.id.menu_delete -> {
                    val model = convertListValueToModel()
                    deleteAlarm(model.id)
                    return@setOnMenuItemClickListener true
                }
                // それ以外
                else -> {
                    return@setOnMenuItemClickListener false
                }
            }
        }
    }

    /**
     * Receive the result from a previous call to
     * [.startActivityForResult].  This follows the
     * related Activity API as described there in
     * [Activity.onActivityResult].
     *
     * @param requestCode The integer request code originally supplied to
     * startActivityForResult(), allowing you to identify who this
     * result came from.
     * @param resultCode The integer result code returned by the child activity
     * through its setResult().
     * @param data An Intent, which can return result data to the caller
     * (various data can be attached to Intent "extras").
     */
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        val fileSelectItem = requireView().setting_items.getSettingItemsAdapter().getItemFromRequestCode(requestCode) as StorageFileSelectItem
        // ファイルを選択したとき
        if (resultCode == RESULT_OK) {
            // permission永続的許可の付与
            val takeFlags = data!!.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            requireActivity().contentResolver.takePersistableUriPermission(data.data!!, takeFlags)

            // 設定の更新
            val adapter = requireView().setting_items.getSettingItemsAdapter()
            fileSelectItem.uri = data.data
            adapter.update(fileSelectItem.tag, fileSelectItem)
        }
    }

    /**
     * setting_itemsの初期表示の設定を行う
     * 必要なパラメータは外部から読み込むこと
     *
     * @param [.onCreateView]で返すView
     */
    override fun createSettingItemArray(
        view: SettingItemsView
    ): ArrayList<ItemInterface> {
        val itemsInitList = SettingItems.readXmlSettingsFromAssets(
            requireActivity(),
            FILE_ALARM_SETTING
        )
        val inputModel: AlarmRecordModel = arguments?.getParcelable<AlarmRecordModel>(KEY_INPUT_MODEL) as AlarmRecordModel
        // 設定したタグをもとに変更を行っていく
        itemsInitList.forEach {
            when (it.tag) {
                // アラームON/OFF
                TAG_ALARM_ON_OFF -> {
                    (it as SwitchItem).checked = inputModel.state
                }
                // アラーム名
                TAG_ALARM_NAME -> {
                    (it as InputTextItem).text = inputModel.name
                }
                // アラームの時刻
                TAG_ALARM_TIME -> {
                    // Calendar型に変換
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, inputModel.hour)
                    calendar.set(Calendar.MINUTE, inputModel.min)
                    (it as TimeItem).time = calendar
                }
                // 繰り返し曜日
                TAG_REPEAT_WEEK -> {
                    (it as MultiSelectItem).select = inputModel.repeatWeek
                }
                // 祝日の繰り返し設定
                TAG_HOLIDAY_REPEAT -> {
                    (it as SpinnerItem).select = inputModel.holidayCount
                    // 繰り返し無しの場合、または祝日データが存在しない場合は設定できないようにする
                    val holidayDatabase = HolidayDatabaseHelper(requireActivity())
                    val holidayArray = holidayDatabase.readHoliday(null, null)
                    holidayDatabase.close()
                    it.enabled = !inputModel.repeatWeek.contentEquals(BooleanArray(WEEK_NUM) {false})
                            && holidayArray.size > 0
                }
                // ファイルからの選択をするか
                TAG_USE_FILE -> {
                    (it as SpinnerItem).select = inputModel.useFile
                }
                // プリセット音からの選択
                TAG_SOUND_SELECT -> {
                    // useFileが0(VAL_SELECT_PRESET)の場合に選択可能
                    (it as SingleSelectItem).enabled = inputModel.useFile == VAL_SELECT_PRESET
                    it.select = inputModel.selectSound
                }
                // ファイルからの選択
                TAG_SOUND_FILE -> {
                    // useFileが1(VAL_SELECT_FILE)の場合に選択可能
                    (it as StorageFileSelectItem).enabled = inputModel.useFile == VAL_SELECT_FILE
                    // 空白の文字列の場合、処理不可能であるためスキップする
                    if (inputModel.soundFileName.isEmpty()) return@forEach
                    // 記録したファイルのpermissionの許可を取得する
                    val soundFileUri = Uri.parse(inputModel.soundFileName)
                    requireActivity().contentResolver.takePersistableUriPermission(
                        soundFileUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    // 読み取った値をUriとして扱う
                    it.uri = soundFileUri
                }
                // 音量
                TAG_VOLUME -> {
                    (it as SeekBarItem).state = inputModel.volume
                }
                // バイブレーション
                TAG_VIBRATE -> {
                    (it as SwitchItem).checked = inputModel.vibrate
                }
                // スヌーズ間隔
                TAG_SNOOZE_DURATION -> {
                    // 記録は数値なので、その値を検索する
                    val durationMinArray = XmlHelper.getAttributeArrayByNameFromAssets(
                        requireActivity(),
                        FILE_ALARM_SETTING,
                        DATA_SNOOZE_MINUTE
                    )
                    // 初期位置の検索
                    // 見つからない場合は-1を返す
                    val initSelect: Int = durationMinArray.indexOfFirst {
                            s -> s == inputModel.snoozeTime.toString()
                    }
                    (it as SpinnerItem).select = initSelect
                }
                // スヌーズ繰り返し回数
                TAG_SNOOZE_COUNT -> {
                    // 記録は数値なので、その値を検索する
                    val snoozeCountArray = XmlHelper.getAttributeArrayByNameFromAssets(
                        requireActivity(),
                        FILE_ALARM_SETTING,
                        DATA_SNOOZE_TIME
                    )
                    // 初期位置の検索
                    // 見つからない場合は-1を返す
                    val initSelect: Int = snoozeCountArray.indexOfFirst {
                            s -> s == inputModel.snoozeCount.toString()
                    }
                    (it as SpinnerItem).select = initSelect
                }
            }
        }
        // クリックしたときの処理
        (itemsInitList.find {
            it.tag == TAG_USE_FILE
        } as SpinnerItem).selectChangeListener = OnUseFileStateChangeListener(itemsInitList)

        // 内容を確定
        return itemsInitList
    }

    /**
     * setting_itemsのMultiSelectItemを更新する
     *
     * @param adapter SettingItemsAdapter(setting_itemsの表示を行っているアダプター)
     * @param item MultiSelectItem
     * @param params ダイアログから送られてきたパラメータ
     */
    override fun updateMultiSelectItem(
        adapter: SettingItemsAdapter,
        item: MultiSelectItem,
        params: Bundle
    ) {
        super.updateMultiSelectItem(adapter, item, params)
        // 繰り返し設定
        if (item.tag == TAG_REPEAT_WEEK) {
            // ダイアログで選択したアイテムの状態を取得する
            val selectParams : BooleanArray = item.select!!
            val repeatHolidayItem: SpinnerItem = requireView().setting_items.itemsList.find {
                it.tag == TAG_HOLIDAY_REPEAT
            } as SpinnerItem

            // 祝日の繰り返し設定
            // 全てfalseではない場合に設定可能にする
            repeatHolidayItem.enabled = !selectParams.contentEquals(BooleanArray(WEEK_NUM){ false })
            adapter.update(repeatHolidayItem.tag, repeatHolidayItem)
        }
    }

    /**
     * その他のアイテムまたはFragmentの更新
     * setting_itemsでは対応できない範囲のダイアログ処理
     *
     * @param requestCode リクエストコード
     * @param resultCode 結果
     * @param params ダイアログから送信された値
     */
    override fun updateOtherItemOnDialog(
        requestCode: Int,
        resultCode: Int,
        params: Bundle
    ) {
        // DB処理
        val alarmRecordDatabase = AlarmRecordDatabaseHelper(requireActivity())
        // 削除対象のデータの読み込み
        val removeItem = alarmRecordDatabase.readRecordModels(
            "$COLUMN_ID = ?",
            arrayOf(requestCode.toString())
        ).first()

        // アラームがONの場合は登録されたアラームをキャンセルする
        if (removeItem.state) {
            // アラームのキャンセル
            removeItem.state = false
            RingAlarmReceiver.cancelAlarm(requireActivity(), removeItem)
        }

        // requestCodeに対応するIDのアラームのデータを消去する
        // DBからデータを消去する
        val delResult = alarmRecordDatabase.deleteRecordModel(requestCode)
        // DBをクローズする
        alarmRecordDatabase.close()

        // 消去したことを表示する
        // 1件も消去できなかった場合、失敗したことを表示する
        val toastText = if (delResult > 0) resources.getText(R.string.alarm_delete)
        else resources.getText(R.string.alarm_record_delete_failed)
        Toast.makeText(requireActivity(), toastText, Toast.LENGTH_SHORT).show()

        // アラーム一覧画面に戻る
        parentFragmentManager.popBackStack()
    }

    /**
     * データを挿入する
     * アラームをONにしている場合は、アラームをセットする
     *
     * @param context Context
     * @param model 記録用データ
     */
    private fun addAlarm(
        context: Context,
        model: AlarmRecordModel
    ) {
        // ファイルから再生する場合で、ファイルを指定していないときは登録しない
        if (!isValidSoundFile(model)) {
            Toast.makeText(context, resources.getText(R.string.alarm_record_invalid_filename), Toast.LENGTH_SHORT).show()
            return
        }

        val alarmRecordDatabase = AlarmRecordDatabaseHelper(context)
        // DBに登録
        val result = alarmRecordDatabase.addRecordModel(model)

        when {
            // 登録失敗時
            result == (-1).toLong() -> {
                // 登録に失敗したときのテキストを表示する
                Toast.makeText(context, context.resources.getText(R.string.alarm_record_register_failed), Toast.LENGTH_SHORT).show()
            }
            // アラームがONの場合
            model.state -> {
                // 挿入したデータの取得(既存データのIDは変更できないため)
                val modelId = alarmRecordDatabase.getLastInsertRowId()
                val addedModel = alarmRecordDatabase.readRecordModels(
                    "$COLUMN_ID = ?",
                    arrayOf(modelId.toString())
                ).first()

                // アラームのセット
                RingAlarmReceiver.setAlarm(context, addedModel)

                // セット時のテキストを表示する
                Toast.makeText(context, context.resources.getText(R.string.alarm_set), Toast.LENGTH_SHORT).show()
            }
            else -> {
                // セット時のテキストを表示する
                Toast.makeText(context, context.resources.getText(R.string.alarm_record_register), Toast.LENGTH_SHORT).show()
            }
        }
        // DBをクローズする
        alarmRecordDatabase.close()

        // アラーム一覧画面に戻る
        parentFragmentManager.popBackStack()
    }

    /**
     * データを更新する
     * アラームをONにしている場合は、アラームをセットする
     *
     * @param context Context
     * @param model 記録用データ
     */
    private fun modifyAlarm(
        context: Context,
        model: AlarmRecordModel
    ) {
        // ファイルから再生する場合で、ファイルを指定していないときは登録しない
        if (!isValidSoundFile(model)) {
            Toast.makeText(context, resources.getText(R.string.alarm_record_invalid_filename), Toast.LENGTH_SHORT).show()
            return
        }

        val alarmRecordDatabase = AlarmRecordDatabaseHelper(context)
        // DBに登録
        val result = alarmRecordDatabase.updateRecordModel(model)
        // DBをクローズする
        alarmRecordDatabase.close()

        when {
            // 登録失敗時
            result <= 0 -> {
                // 登録に失敗したときのテキストを表示する
                Toast.makeText(context, context.resources.getText(R.string.alarm_record_register_failed), Toast.LENGTH_SHORT).show()
            }
            model.state -> {
                // アラームのセット
                RingAlarmReceiver.setAlarm(context, model)
                // セット時のテキストを表示
                Toast.makeText(context, resources.getText(R.string.alarm_set), Toast.LENGTH_SHORT).show()
            }
            else -> {
                // アラームの解除
                RingAlarmReceiver.cancelAlarm(context, model)

                // 解除時のテキストを表示
                Toast.makeText(context, resources.getText(R.string.alarm_cancel), Toast.LENGTH_SHORT).show()
            }
        }

        // アラーム一覧画面に戻る
        parentFragmentManager.popBackStack()
    }

    /**
     * データを削除する
     *
     * @param id modelのid
     */
    private fun deleteAlarm(
        id: Int
    ) {
        // 消去確認ダイアログの作成
        val dialog = MessageDialog.Builder()
            .text(requireActivity().resources.getString(R.string.alarm_record_delete_dialog_text))
            .positiveLabel(requireActivity().resources.getString(R.string.alarm_record_delete_yes))
            .negativeLabel(requireActivity().resources.getString(R.string.alarm_record_delete_no))
            .requestCode(id)
            .build()
        dialog.show(this, requireActivity().supportFragmentManager)
    }

    /**
     * setting_itemsの設定値から記録用の変数を設定する
     *
     * @return [AlarmRecordModel] 記録用データ
     */
    private fun convertListValueToModel(): AlarmRecordModel {
        val inputModel: AlarmRecordModel = arguments?.getParcelable<AlarmRecordModel>(KEY_INPUT_MODEL) as AlarmRecordModel
        val itemsList = requireView().setting_items.itemsList

        // スヌーズ間隔の値の配列
        val durationMinArray = XmlHelper.getAttributeArrayByNameFromAssets(
            requireActivity(),
            FILE_ALARM_SETTING,
            DATA_SNOOZE_MINUTE
        )
        val durationMinIndex = (itemsList.find { it.tag == TAG_SNOOZE_DURATION } as SpinnerItem).select
        // スヌーズ回数の値の配列
        val snoozeCountArray = XmlHelper.getAttributeArrayByNameFromAssets(
            requireActivity(),
            FILE_ALARM_SETTING,
            DATA_SNOOZE_TIME
        )
        val snoozeCountIndex = (itemsList.find { it.tag == TAG_SNOOZE_COUNT } as SpinnerItem).select
        var alarmName = (itemsList.find { it.tag == TAG_ALARM_NAME } as InputTextItem).text
        // アラーム名が空の場合、空文字として登録
        if (alarmName.isEmpty()) {
            alarmName = ""
        }
        val soundFileUri = (itemsList.find { it.tag == TAG_SOUND_FILE } as StorageFileSelectItem).uri
        // 音楽ファイルを指定していない場合、空文字として登録
        val soundFileName = soundFileUri?.toString() ?: ""

        // 記録用の変数を設定する
        return AlarmRecordModel(
            id = inputModel.id,
            state = (itemsList.find { it.tag == TAG_ALARM_ON_OFF } as SwitchItem).checked,
            name = alarmName,
            hour = (itemsList.find { it.tag == TAG_ALARM_TIME } as TimeItem).time.get(Calendar.HOUR_OF_DAY),
            min = (itemsList.find { it.tag == TAG_ALARM_TIME } as TimeItem).time.get(Calendar.MINUTE),
            repeatWeek = (itemsList.find { it.tag == TAG_REPEAT_WEEK } as MultiSelectItem).select!!,
            holidayCount = (itemsList.find { it.tag == TAG_HOLIDAY_REPEAT } as SpinnerItem).select,
            useFile = (itemsList.find { it.tag == TAG_USE_FILE } as SpinnerItem).select,
            selectSound = (itemsList.find { it.tag == TAG_SOUND_SELECT } as SingleSelectItem).select,
            soundFileName = soundFileName,
            volume = (itemsList.find { it.tag == TAG_VOLUME } as SeekBarItem).state,
            vibrate = (itemsList.find { it.tag == TAG_VIBRATE } as SwitchItem).checked,
            snoozeTime = durationMinArray[durationMinIndex].toInt(),
            snoozeCount = snoozeCountArray[snoozeCountIndex].toInt()
        )
    }

    /**
     * ファイル指定時に登録できるかの確認
     *
     * @param model 入力するAlarmRecordModel
     */
    private fun isValidSoundFile(
        model: AlarmRecordModel
    ) = model.useFile != VAL_SELECT_FILE || model.soundFileName != ""

    /**
     * アラーム音に使用するファイルで値を変更したときに行う処理
     *
     * @property itemsInitList 表示に使用するリスト
     */
    inner class OnUseFileStateChangeListener(
        private val itemsInitList: ArrayList<ItemInterface>
    ) : SpinnerItem.OnItemStateChangeListener {
        override fun onItemSelectChanged(
            adapter: SettingItemsAdapter,
            position: Int
        ) {
            // プリセット音からの選択アイテム
            // 値が合っているかを調べるので==による検索をしている(ファイルからの選択も同様)
            val soundSelectItem: SingleSelectItem = itemsInitList.find {
                it.tag == TAG_SOUND_SELECT
            } as SingleSelectItem
            // ファイルからの選択アイテム
            val soundFileItem: StorageFileSelectItem = itemsInitList.find {
                it.tag == TAG_SOUND_FILE
            } as StorageFileSelectItem
            // 各アイテムのenabledを設定
            soundSelectItem.enabled = position == VAL_SELECT_PRESET
            soundFileItem.enabled = position == VAL_SELECT_FILE

            // 表示の更新
            adapter.update(TAG_SOUND_SELECT, soundSelectItem)
            adapter.update(TAG_SOUND_FILE, soundFileItem)
        }
    }
}