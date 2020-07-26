/**
 * @file SettingItemsTest.kt
 */
package io.github.kaztakgh.alarmclock2

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import io.github.kaztakgh.settingitemsviewlibrary.SwitchItem
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class SettingItemsTest
    : TestCase() {
    private val context = InstrumentationRegistry.getInstrumentation().context

    @Test
    fun createSwitchItemDefault() {
        val expect = SwitchItem(
            title = "スイッチ",
            tag = "switchTest"
        )
        val actual = SettingItems.readXmlSettingsFromAssets(context, "switch_item_default.xml")[0]
        assertEquals(expect.title, actual.title)
    }
}