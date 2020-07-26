/**
 * @file HolidayDataTableObject.kt
 */
package io.github.kaztakgh.alarmclock2

import android.provider.BaseColumns

/**
 * Holidayテーブルのファイル名、カラム名の定義を行うクラス
 */
object HolidayTableObject {
    class RecordColumns : BaseColumns {
        companion object {
            /**
             * テーブル名
             */
            const val TABLE_NAME = "holiday_data"

            /**
             * 日付(PK)
             * Text
             */
            const val COLUMN_DATE = "date"

            /**
             * 祝日名
             * Text
             */
            const val COLUMN_NAME = "name"
        }
    }
}