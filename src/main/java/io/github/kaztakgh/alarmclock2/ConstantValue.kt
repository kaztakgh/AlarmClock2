/**
 * @file ConstantValue.kt
 */
package io.github.kaztakgh.alarmclock2

/**
 * 使用するアラームのID
 */
const val ALARM_ID_KEY = "alarmId"

/**
 * 祝日データ取得サービス
 */
const val HOLIDAY_ACCESS_SERVICE = "HOLIDAY_ACCESS_SERVICE"
/**
 * 祝日データ作成サービス
 */
const val HOLIDAY_DATA_CREATE_SERVICE = "HOLIDAY_DATA_CREATE_SERVICE"
/**
 * 祝日データ更新サービス
 */
const val HOLIDAY_DATA_UPDATE_SERVICE = "HOLIDAY_DATA_UPDATE_SERVICE"
/**
 * 祝日データのキー
 */
const val HOLIDAY_DATA_KEY = "holidayData"
/**
 * 祝日データ更新結果
 */
const val HOLIDAY_DATA_UPDATE_RESULT_KEY = "holidayDataUpdateResult"
/**
 * サービス起動元
 */
const val HOLIDAY_DATA_SEND_ACTIVITY = "holidayDataSendActivity"
/**
 * テーマ デフォルト値
 */
const val THEME_DEFAULT = "system"
/**
 * 時計種類 デフォルト値
 */
const val CLOCK_TYPE_DEFAULT = "digital"
/**
 * アラーム発動時間 デフォルト値
 */
const val ALARM_RING_TIME_DEFAULT = 30
/**
 * マナーモード優先順位 デフォルト値
 */
const val ALARM_RING_MODE_DEFAULT = "normal"
/**
 * 曜日数
 */
const val WEEK_NUM = 7
/**
 * 最大アラーム数
 */
const val MAX_ALARM_NUM = 100