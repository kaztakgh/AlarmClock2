/**
 * @file AlarmRecordTableObject.kt
 */
package io.github.kaztakgh.alarmclock2

import android.provider.BaseColumns

/**
 * AlarmRecordテーブルのファイル名、カラム名の定義を行うクラス
 */
object AlarmRecordTableObject {
    /**
     * テーブル名、カラム名の定義クラス
     * @see BaseColumns
     */
    class RecordColumns
        : BaseColumns {
        companion object {
            /**
             * テーブル名
             */
            const val TABLE_NAME = "alarm_records"

            /**
             * ID(PK)
             * Integer
             */
            const val COLUMN_ID = "_id"

            /**
             * アラームのON/OFF
             * Integer(0 or 1)
             */
            const val COLUMN_STATE = "state"

            /**
             * アラーム名
             * Text
             */
            const val COLUMN_NAME = "name"

            /**
             * 時刻(時)
             * Integer(0～23)
             */
            const val COLUMN_HOUR = "hour"

            /**
             * 時刻(分)
             * Integer(0～59)
             */
            const val COLUMN_MIN = "min"

            /**
             * 繰り返し対象の曜日: 日曜日
             * Integer(0 or 1)
             */
            const val COLUMN_WEEK_SUN = "week_sun"

            /**
             * 繰り返し対象の曜日: 月曜日
             * Integer(0 or 1)
             */
            const val COLUMN_WEEK_MON = "week_mon"

            /**
             * 繰り返し対象の曜日: 火曜日
             * Integer(0 or 1)
             */
            const val COLUMN_WEEK_TUE = "week_tue"

            /**
             * 繰り返し対象の曜日: 水曜日
             * Integer(0 or 1)
             */
            const val COLUMN_WEEK_WED = "week_wed"

            /**
             * 繰り返し対象の曜日: 木曜日
             * Integer(0 or 1)
             */
            const val COLUMN_WEEK_THU = "week_thu"

            /**
             * 繰り返し対象の曜日: 金曜日
             * Integer(0 or 1)
             */
            const val COLUMN_WEEK_FRI = "week_fri"

            /**
             * 繰り返し対象の曜日: 土曜日
             * Integer(0 or 1)
             */
            const val COLUMN_WEEK_SAT = "week_sat"

            /**
             * 繰り返しに祝日を入れるか
             * Integer(0～2)
             */
            const val COLUMN_HOLIDAY_COUNT = "holiday_count"

            /**
             * 音楽ファイルを使用するか
             * Integer(0 or 1)
             */
            const val COLUMN_USE_HDD_FILE = "use_file"

            /**
             * プリセット音の選択番号
             * Integer
             */
            const val COLUMN_SOUND_SELECT = "sound_select"

            /**
             * 音楽ファイル名
             * Text
             */
            const val COLUMN_SOUND_FILENAME = "sound_file_name"

            /**
             * 音量
             * Integer(0～100)
             */
            const val COLUMN_VOLUME = "volume"

            /**
             * バイブレーション
             * Integer(0 or 1)
             */
            const val COLUMN_VIBRATE = "vibrate"

            /**
             * スヌーズタイマー
             * Integer
             */
            const val COLUMN_SNOOZE_TIMER = "snooze_timer"

            /**
             * スヌーズ繰り返し回数
             * Integer
             */
            const val COLUMN_SNOOZE_COUNT = "snooze_count"

            /**
             * スヌーズ実行済み回数
             * Integer
             */
            const val COLUMN_SNOOZE_REPEATED = "snooze_repeated"

            /**
             * 作成日時
             * Text
             */
            const val COLUMN_CREATED_DATETIME = "create_datetime"

            /**
             * 更新日時
             * Text
             */
            const val COLUMN_UPDATED_DATETIME = "update_datetime"
        }
    }
}