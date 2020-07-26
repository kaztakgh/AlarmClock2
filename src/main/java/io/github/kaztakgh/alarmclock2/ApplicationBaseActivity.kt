/**
 * @file ApplicationBaseActivity.kt
 */
package io.github.kaztakgh.alarmclock2

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate

/**
 * Activityに共通する処理
 */
open class ApplicationBaseActivity
    : AppCompatActivity() {
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        // テーマの読み込み
        val preferences = getSharedPreferences(
            GeneralSettingsFragment.GENERAL_SETTING_PREFERENCE,
            Context.MODE_PRIVATE
        )
        // 記録は数値なので、その値を検索する
        val attributeArray = XmlHelper.getAttributeArrayByNameFromAssets(
            this,
            GeneralSettingsFragment.FILE_GENERAL_SETTING,
            GeneralSettingsFragment.DATA_THEME
        )
        val themeIndex = attributeArray.indexOfFirst { s ->
            s == preferences.getString(GeneralSettingsFragment.DATA_THEME, THEME_DEFAULT)
        }
        // テーマの設定
        when(themeIndex) {
            // ライトモード
            GeneralSettingsFragment.THEME_LIGHT_INDEX -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            // ダークモード
            GeneralSettingsFragment.THEME_DARK_INDEX -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            // システムのテーマ
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }
}