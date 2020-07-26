/**
 * @file SettingsHelper.kt
 */
package io.github.kaztakgh.alarmclock2

import android.content.Context
import android.text.format.DateFormat

/**
 * 設定に関するHelper
 */
object SettingsHelper {
    /**
     * 24時間表記を使用しているか
     * Systemから読み込む
     *
     * @param context Context
     * @return true:24時間表記, false:12時間表記
     */
    fun is24HFormatUsing(
        context: Context
    ) : Boolean = DateFormat.is24HourFormat(context)
}