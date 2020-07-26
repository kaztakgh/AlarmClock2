/**
 * @file HolidayUtil.kt
 */
package io.github.kaztakgh.alarmclock2

import android.content.Context
import android.text.format.DateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * HolidayModelを用いた共通処理
 */
object HolidayUtil {
    /**
     * 今日を基準とした祝日データの取得
     *
     * @param context Context
     * @return 祝日データのArrayList
     */
    fun getHolidayList(
        context: Context
    ): ArrayList<HolidayModel> {
        // DBからデータの取得
        val holidayDatabase = HolidayDatabaseHelper(context)
        val holidayArray = arrayListOf<HolidayModel>()
        val currentWeekCalendar = Calendar.getInstance()
        // ループ終了フラグ
        var isEndLoop = false

        // ループ終了指示が無い場合
        while (!isEndLoop) {
            val currentDate = DateFormat.format("yyyy-MM-dd", currentWeekCalendar) as String
            // calendar.addに戻り値が無いため、ここで1週間分の日を足す
            currentWeekCalendar.add(Calendar.DAY_OF_MONTH, WEEK_NUM)
            val nextWeekDate = DateFormat.format("yyyy-MM-dd", currentWeekCalendar) as String

            // 1週間の範囲での祝日データの検索
            val weekHolidayArray = holidayDatabase.readHoliday(
                "${HolidayTableObject.RecordColumns.COLUMN_DATE} >= ? and ${HolidayTableObject.RecordColumns.COLUMN_DATE} < ?",
                arrayOf(currentDate, nextWeekDate)
            )

            // 取得できた祝日が無い場合
            if (weekHolidayArray.size == 0) {
                // ループ終了フラグを立てる
                isEndLoop = true
            } else {
                // データの追加
                holidayArray.addAll(weekHolidayArray)
            }
        }
        // DBをクローズする
        holidayDatabase.close()

        return holidayArray
    }
}