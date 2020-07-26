/**
 * @file AlarmListItemViewHolder.kt
 */
package io.github.kaztakgh.alarmclock2

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.view.View
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.alarm_list_item.view.*

/**
 * アラーム情報の表示に使用する
 *
 * @constructor
 *
 * @param view viewのレイアウトリソース<br>alarm_list_itemを使用する
 */
class AlarmListItemViewHolder(view: View)
    : RecyclerView.ViewHolder(view) {

    interface ItemClickListener {
        /**
         * アイテムをクリックしたときの処理
         *
         * @param view レイアウトビュー
         * @param position アダプター内のアイテムの位置
         */
        fun onItemClick(view: View, position: Int)
    }
    interface ItemLongClickListener {
        /**
         * アイテムを長押ししたときの処理
         *
         * @param view レイアウトビュー
         * @param position アダプター内のアイテムの位置
         */
        fun onItemLongClick(view: View, position: Int)
    }
    interface StateSwitchClickListener {
        /**
         * スイッチを押したときの処理
         *
         * @param view レイアウトビュー
         * @param position アダプター内のアイテムの位置
         * @param state スイッチの状態
         */
        fun onCheckChanged(view: View, position: Int, state: Boolean)
    }
    private val alarmTimeTextView: TextView = view.tv_time
    private val repeatWeekTextView: TextView = view.tv_repeat
    private val alarmNameTextView: TextView = view.tv_name
    private val stateSwitch: Switch = view.sw_state

    /**
     * アイテムをクリックしたときのリスナー
     */
    var itemClickListener: ItemClickListener? = null

    /**
     * アイテムを長押ししたときのリスナー
     */
    var itemLongClickListener: ItemLongClickListener? = null

    /**
     * スイッチを切り替えたときの処理
     */
    var stateSwitchClickListener: StateSwitchClickListener? = null

    /**
     * データを挿入したときにその内容を表示する
     *
     * @param context Context
     * @param recordModel アラームのデータ
     */
    fun bind(
        context: Context,
        recordModel: AlarmRecordModel
    ) {
        // データの表示
        // 時刻
        alarmTimeTextView.text = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            recordModel.getAlarmDateTimeString(is24HFormatUsing = SettingsHelper.is24HFormatUsing(context))
        else recordModel.getAlarmTimeString(is24HFormatUsing = SettingsHelper.is24HFormatUsing(context))

        // 繰り返し対象の曜日をカンマで繋げた文字列を作成する
        // 祝日の繰り返し設定をカッコ内に追加
        val repeatWeekTextBuilder = StringBuilder()
        repeatWeekTextBuilder.append(createRepeatDayText(recordModel.repeatWeek))
        // 繰り返し対象がある場合
        if (!recordModel.repeatWeek.contentEquals(BooleanArray(WEEK_NUM) {false})) {
            repeatWeekTextBuilder.append("(")
                .append(createRepeatHolidayText(recordModel.holidayCount))
                .append(")")
        }
        repeatWeekTextView.text = repeatWeekTextBuilder.toString()

        // アラーム名
        if (recordModel.name.isNotEmpty()) {
            alarmNameTextView.text = recordModel.name
        }
        else {
            alarmNameTextView.visibility = View.GONE
        }

        // スイッチ
        // アラームのON/OFF
        stateSwitch.isChecked = recordModel.state
    }

    /**
     * 繰り返し曜日の文字列を作成する
     *
     * @param repeatWeekArray 繰り返し曜日(true:ON, false:OFF)
     * @return 繰り返す曜日をカンマで接続した文字列(repeatWeekArrayにtrueが1つ以上ある場合)
     * @return 繰り返し無しを示す文字列(repeatWeekArrayが全てfalseの場合)
     */
    private fun createRepeatDayText(
        repeatWeekArray: BooleanArray
    ): String {
        val resources: Resources = itemView.resources
        val weekDayList = resources.getStringArray(R.array.week_array)

        // 表記する曜日と繰り返し設定を合わせたMapを作成する
        val alarmRepeatMap: Map<String, Boolean> = weekDayList.zip(repeatWeekArray.toList()).toMap()

        // 繰り返し曜日がtrueの要素の曜日を取り出し、文字列を作成する
        val repeatWeekday = alarmRepeatMap.filter { it.value }.keys.joinToString()
        return if (repeatWeekday.isNotEmpty()) repeatWeekday else resources.getString(R.string.no_repeat_week)
    }

    /**
     * 祝日繰り返しの出力
     *
     * @param select 選択パラメータ
     * @return 祝日繰り返しの文字列
     */
    private fun createRepeatHolidayText(
        select: Int
    ): String {
        val holidayRepeatTextArray = arrayOf(
            itemView.resources.getString(R.string.holiday_no_use),
            itemView.resources.getString(R.string.holiday_include),
            itemView.resources.getString(R.string.holiday_exclude)
        )
        return holidayRepeatTextArray[select]
    }
}