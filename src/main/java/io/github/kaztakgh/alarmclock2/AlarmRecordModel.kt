/**
 * @file AlarmRecordModel.kt
 */
package io.github.kaztakgh.alarmclock2

import android.annotation.TargetApi
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

/**
 * AlarmRecordのテーブルモデル
 * DBにデータを挿入・変更などを行うときにこのクラスのオブジェクトを渡す
 *
 * @property id ID(初期値0、0の場合は挿入、0以外の場合は変更のみ使用可)
 * @property state アラームのスイッチ
 * @property name アラーム名
 * @property hour 時(0～23)
 * @property min 分(0～59)
 * @property repeatWeek 繰り返し対象の曜日
 * @property holidayCount 繰り返しに祝日を含めるか(0:曜日のみ、1:祝日を含める、2:祝日を除外する)
 * @property useFile アラーム音をファイルから使用するか(0:プリセット、1:ファイルから)
 * @property selectSound プリセット音の選択
 * @property soundFileName ファイル名の選択
 * @property volume 音量(0～100)
 * @property vibrate バイブレーション
 * @property snoozeTime スヌーズ時間間隔
 * @property snoozeCount スヌーズ回数
 * @property snoozeRepeated スヌーズ繰り返し済み回数(0～設定値)
 */
class AlarmRecordModel(
    val id: Int = 0,
    var state: Boolean = false,
    var name: String = "",
    var hour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
    var min: Int = Calendar.getInstance().get(Calendar.MINUTE),
    var repeatWeek: BooleanArray = BooleanArray(WEEK_NUM){ false },
    var holidayCount: Int = HOLIDAY_NO_USE,
    var useFile: Int = 0,
    var selectSound: Int = 0,
    var soundFileName: String = "",
    var volume: Int = 0,
    var vibrate: Boolean = false,
    var snoozeTime: Int = 0,
    var snoozeCount: Int = 0,
    var snoozeRepeated: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readString() as String,
        parcel.readInt(),
        parcel.readInt(),
        parcel.createBooleanArray() as BooleanArray,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() as String,
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeByte(if (state) 1 else 0)
        parcel.writeString(name)
        parcel.writeInt(hour)
        parcel.writeInt(min)
        parcel.writeBooleanArray(repeatWeek)
        parcel.writeInt(holidayCount)
        parcel.writeInt(useFile)
        parcel.writeInt(selectSound)
        parcel.writeString(soundFileName)
        parcel.writeInt(volume)
        parcel.writeByte(if (vibrate) 1 else 0)
        parcel.writeInt(snoozeTime)
        parcel.writeInt(snoozeCount)
        parcel.writeInt(snoozeRepeated)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AlarmRecordModel> {
        override fun createFromParcel(parcel: Parcel): AlarmRecordModel {
            return AlarmRecordModel(parcel)
        }

        override fun newArray(size: Int): Array<AlarmRecordModel?> {
            return arrayOfNulls(size)
        }

        /**
         * 曜日のみで判断する
         */
        const val HOLIDAY_NO_USE = 0
        /**
         * 祝日を含める
         */
        const val HOLIDAY_INCLUDE = 1
        /**
         * 祝日を除外する
         */
        const val HOLIDAY_EXCLUDE = 2
    }

    /**
     * 時刻の表記
     *
     * @param is24HFormatUsing 24時間表記の使用
     * @param locale 表記言語
     * @return 時刻の文字列
     */
    fun getAlarmTimeString(
        is24HFormatUsing: Boolean = true,
        locale: Locale = Locale.getDefault()
    ): String {
        // 入力した時刻を2桁、0埋め形式の文字列に変換する
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, min)
        // 時間表記の形式によって表記の仕方を変える
        var timeFormat = "HH:mm"
        // 12時間表記の場合
        if (!is24HFormatUsing) {
            // 日本語環境のみ午前午後の表記を前にする
            timeFormat = if (locale == Locale.JAPAN || locale == Locale.JAPANESE) {
                "a hh:mm"
            } else {
                "hh:mm a"
            }
        }
        return android.text.format.DateFormat.format(timeFormat, calendar.timeInMillis).toString()
    }

    /**
     * 時刻の表記
     * ApiLevel:26以上
     *
     * @param is24HFormatUsing 24時間表記の使用
     * @param locale 表記言語
     * @return 時刻の文字列
     */
    @TargetApi(Build.VERSION_CODES.O)
    fun getAlarmDateTimeString(
        is24HFormatUsing: Boolean = true,
        locale: Locale = Locale.getDefault()
    ) : String {
        val currentDate = LocalDate.now()
        val dateTime = LocalDateTime.of(
            currentDate.year,
            currentDate.month,
            currentDate.dayOfMonth,
            hour,
            min,
            0,
            0
        )
        // 時間表記の形式によって表記の仕方を変える
        var timeFormat = "HH:mm"
        // 12時間表記の場合
        if (!is24HFormatUsing) {
            // 日本語環境のみ午前午後の表記を前にする
            timeFormat = if (locale == Locale.JAPAN || locale == Locale.JAPANESE) {
                "a hh:mm"
            } else {
                "hh:mm a"
            }
        }
        return dateTime.format(DateTimeFormatter.ofPattern(timeFormat, locale))
    }

    /**
     * 次のアラームの時刻を取得する
     *
     * @param holidayArray 祝日データの配列
     * @return [java.util.Calendar] 次のアラーム時刻をCalendar型で返す。<br>
     *     ただし、アラームがOFFの場合はnullで返す
     */
    fun getNextAlarmTime(
        holidayArray: ArrayList<HolidayModel> = arrayListOf()
    ): Calendar? {
        // アラームがOFFの場合はnullを返す
        if (!state) return null

        // 現在の日付・時刻の取得
        val calendar = Calendar.getInstance()
        // 時刻の設定
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, min)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        // 繰り返し曜日がない場合の配列
        val repeatNgArray = BooleanArray(WEEK_NUM) { false }
        // アラームの時刻を過ぎているか
        val hasPast = calendar.timeInMillis < System.currentTimeMillis()
        // アラームの時刻を過ぎた場合
        if(hasPast) {
            // 1日を加算する
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        // 曜日の繰り返しが無い場合はここで終了
        if (repeatWeek.contentEquals(repeatNgArray)) {
            return calendar
        }

        var repeatArrayFromToday: BooleanArray = repeatNgArray
        // 週のカウント
        // 先に週のカウントの追加を行うため、初期値を-1に設定
        var weekCount = -1
        while (repeatArrayFromToday.contentEquals(repeatNgArray)) {
            // 週のカウントを+1する
            weekCount++
            // 開始日からのアラーム発動の有無の配列を取得する
            repeatArrayFromToday =
                getRepeatArrayFromToday(calendar, weekCount, holidayArray)
        }
        // 何日後かを探す
        val nextDayNum = repeatArrayFromToday.indexOfFirst { it }
        // 指定した日数を加算する
        // calendarの週が進んでいる状態のため、日数のみを加算
        if (nextDayNum > 0) calendar.add(Calendar.DAY_OF_YEAR, nextDayNum)

        return calendar
    }

    /**
     * 次のアラームの時刻を取得する
     * ApiLevel:26以上
     *
     * @param holidayArray 祝日データの配列
     * @return [java.time.LocalDateTime] 次のアラーム時刻をLocalDateTime型で返す。<br>
     *     ただし、アラームがOFFの場合はnullで返す
     */
    @TargetApi(Build.VERSION_CODES.O)
    fun getNextAlarmDateTime(
        holidayArray: ArrayList<HolidayModel> = arrayListOf()
    ) : LocalDateTime? {
        // アラームがOFFの場合はnullを返す
        if (!state) return null

        val currentDate = LocalDate.now()
        var dateTime = LocalDateTime.of(
            currentDate.year,
            currentDate.month,
            currentDate.dayOfMonth,
            hour,
            min,
            0,
            0
        )
        // 繰り返し曜日がない場合の配列
        val repeatNgArray = BooleanArray(WEEK_NUM) { false }
        // アラームの時刻を過ぎているか
        val hasPast = dateTime.isBefore(LocalDateTime.now())
        // アラームの時刻を過ぎた場合
        if (hasPast) {
            // 1日を加算する
            dateTime = dateTime.plusDays(1)
        }
        // 曜日の繰り返しが無い場合はここで終了
        if (repeatWeek.contentEquals(repeatNgArray)) {
            return dateTime
        }

        var repeatArrayFromToday: BooleanArray = repeatNgArray
        // 週のカウント
        // 先に週のカウントの追加を行うため、初期値を-1に設定
        var weekCount = -1
        while (repeatArrayFromToday.contentEquals(repeatNgArray)) {
            // 週のカウントを+1する
            weekCount++
            // 開始日からのアラーム発動の有無の配列を取得する
            repeatArrayFromToday =
                getRepeatArrayFromToday(dateTime.toLocalDate(), weekCount, holidayArray)
        }
        // 何日後かを探す
        val nextDayNum = repeatArrayFromToday.indexOfFirst { it }
        // 指定した日数を加算する
        if (nextDayNum > 0 || weekCount >= 0) {
            dateTime = dateTime.plusWeeks(weekCount.toLong())
            dateTime = dateTime.plusDays(nextDayNum.toLong())
        }

        return dateTime
    }

    /**
     * true=1, false=0とするアラームスイッチの値の取得
     */
    fun getStateVal() = convertToInteger(state)

    /**
     * true=1, false=0とする繰り返し対象の曜日の値の配列の取得
     *
     * @return 繰り返し対象の曜日の値の配列
     */
    fun getRepeatWeekValArray() : IntArray {
        val repeatValArray = IntArray(WEEK_NUM)
        // 数値配列への変換
        repeatWeek.forEachIndexed { index, b ->
            // true=1、false=0に置き換える
            repeatValArray[index] = convertToInteger(b)
        }
        return repeatValArray
    }

    /**
     * true=1, false=0とするバイブレーションの値の取得
     */
    fun getVibrateVal() = convertToInteger(vibrate)

    /**
     * DBへの記録を行うためにBooleanをIntに変換する
     *
     * @param value Boolean
     * @return true=1, false=0
     */
    private fun convertToInteger(value: Boolean) = if (value) 1 else 0

    /**
     * 指定した日からの繰り返しのフラグの配列を作成
     *
     * @param startDate [Calendar] 起点日
     * @param weekCount 経過週数
     * @param holidayArray 祝日の配列データ
     * @return 繰り返しのフラグの配列
     */
    private fun getRepeatArrayFromToday(
        startDate: Calendar,
        weekCount: Int,
        holidayArray: ArrayList<HolidayModel>
    ): BooleanArray {
        // 今日からのリピート設定に並び替え
        val weekDay = startDate.get(Calendar.DAY_OF_WEEK)
        val repeatArrayFromToday = createRepeatArrayFromToday(repeatWeek, weekDay)
        startDate.add(Calendar.DAY_OF_MONTH, weekCount * WEEK_NUM)
        val oneWeekDateArray = createOneWeekDateArray(startDate)

        // 各要素について祝日の有無で繰り返しの設定を変更する
        oneWeekDateArray.forEachIndexed { index, date ->
            // 祝日があるかを調べる
            // 年・月・日が一致する箇所を調べる
            val isContainHoliday = holidayArray.indexOfFirst {
                it.date.get(Calendar.YEAR) == date.get(Calendar.YEAR)
                && it.date.get(Calendar.MONTH) == date.get(Calendar.MONTH)
                && it.date.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)
            } > -1

            // 設定対象日が祝日で、祝日を含める場合
            if (isContainHoliday && holidayCount == HOLIDAY_INCLUDE) {
                // 繰り返しのフラグをONにする
                repeatArrayFromToday[index] = true
            }
            // 設定対象日が祝日で、祝日を含めない場合
            else if (isContainHoliday && holidayCount == HOLIDAY_EXCLUDE) {
                repeatArrayFromToday[index] = false
            }
        }

        return repeatArrayFromToday
    }

    /**
     * 指定した日からの繰り返しのフラグの配列を作成
     * ApiLevel:26以上
     *
     * @param startDate [LocalDate] 起点日
     * @param weekCount 経過週数
     * @param holidayArray 祝日の配列データ
     * @return 繰り返しのフラグの配列
     */
    @TargetApi(Build.VERSION_CODES.O)
    private fun getRepeatArrayFromToday(
        startDate: LocalDate,
        weekCount: Int,
        holidayArray: ArrayList<HolidayModel>
    ): BooleanArray {
        // 今日からのリピート設定に並び替え
        // LocalDateでは月曜日を1とするため、数値の変換が必要
        val weekDay = (startDate.dayOfWeek.value) % WEEK_NUM + 1
        val repeatArrayFromToday = createRepeatArrayFromToday(repeatWeek, weekDay)

        // 開始日を設定
        val weekStartDate = startDate.plusWeeks(weekCount.toLong())
        val oneWeekDateArray = createOneWeekDateArray(weekStartDate)

        // 各要素について祝日の有無で繰り返しの設定を変更する
        oneWeekDateArray.forEachIndexed { index, date ->
            // 祝日があるかを調べる
            // 祝日の配列データの日付をLocalDateに変換し、年・月・日が一致する箇所を調べる
            val isContainHoliday = holidayArray.indexOfFirst {
                calendarToLocalDate(it.date).isEqual(date)
            } > -1

            // 設定対象日が祝日で、祝日を含める場合
            if (isContainHoliday && holidayCount == HOLIDAY_INCLUDE) {
                // 繰り返しのフラグをONにする
                repeatArrayFromToday[index] = true
            }
            // 設定対象日が祝日で、祝日を含めない場合
            else if (isContainHoliday && holidayCount == HOLIDAY_EXCLUDE) {
                repeatArrayFromToday[index] = false
            }
        }

        return repeatArrayFromToday
    }

    /**
     * Calendar型をLocalDate型に変更する
     * ApiLevel:26以上
     *
     * @param calendar [Calendar]
     * @return [LocalDate] 入力値のLocalDate型
     */
    @TargetApi(Build.VERSION_CODES.O)
    private fun calendarToLocalDate(
        calendar: Calendar
    ) : LocalDate {
        val instant: Instant = calendar.toInstant()
        val zoneId = ZoneId.systemDefault()

        return instant.atZone(zoneId).toLocalDate()
    }

    /**
     * 今日の曜日を起点とした繰り返し曜日配列の作成
     *
     * @param repeatWeekArray 繰り返し曜日配列
     * @param startDay [Calendar.DAY_OF_WEEK] 開始曜日
     * @return [BooleanArray] 今日の曜日を起点とした繰り返し曜日配列
     */
    private fun createRepeatArrayFromToday(
        repeatWeekArray: BooleanArray,
        startDay: Int
    ) : BooleanArray {
        // 今週
        // 繰り返し曜日配列の後ろ側を取る
        val thisWeekArray = repeatWeekArray.drop(startDay - 1)
        // 来週
        // 繰り返し曜日配列の前側を取る
        val nextWeekArray = repeatWeekArray.take(startDay - 1)

        // 来週の配列を今週の配列の後ろに追加する
        return (thisWeekArray.plus(nextWeekArray)).toBooleanArray()
    }

    /**
     * 指定した日から連続した1週間の日付の配列を作成する
     *
     * @param startDate 開始日
     * @return 開始日から次の週の同じ曜日の前日までの配列
     */
    private fun createOneWeekDateArray(
        startDate: Calendar
    ) : ArrayList<Calendar> {
        // 空の配列の作成
        val dateArray = arrayListOf<Calendar>()

        // 1週間分の日付を挿入する
        for (i in 0 until WEEK_NUM) {
            val date = Calendar.getInstance()
            date.set(
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DAY_OF_MONTH) + i,
                0,
                0,
                0
            )
            date.set(Calendar.MILLISECOND, 0)
            dateArray.add(date)
        }

        return dateArray
    }

    /**
     * 指定した日から連続した1週間の日付の配列を作成する
     * ApiLevel:26以上
     *
     * @param startDate 開始日
     * @return 開始日から次の週の同じ曜日の前日までの配列
     */
    @TargetApi(Build.VERSION_CODES.O)
    private fun createOneWeekDateArray(
        startDate: LocalDate
    ): ArrayList<LocalDate> {
        // 空の配列の作成
        val dateArray = arrayListOf<LocalDate>()

        // 1週間分の日付を挿入する
        for (i in 0 until WEEK_NUM) {
            val date = startDate.plusDays(i.toLong())
            dateArray.add(date)
        }

        return dateArray
    }
}