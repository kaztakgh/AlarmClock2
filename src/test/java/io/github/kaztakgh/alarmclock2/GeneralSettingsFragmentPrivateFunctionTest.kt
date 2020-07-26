package io.github.kaztakgh.alarmclock2

import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowPreference
import org.robolectric.shadows.ShadowSharedPreferences
import java.lang.reflect.Method

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class GeneralSettingsFragmentPrivateFunctionTest
    : TestCase() {
    // TODO: Fragmentをテストする方法とSharedPreferenceをテストする方法を探す
    private val context = InstrumentationRegistry.getInstrumentation().context
    private val activity = Robolectric.buildActivity(MainActivity::class.java).create().start().get()
    private val generalSettingsFragment = GeneralSettingsFragment()
    private val genenalSettingsFragmentScenario = launchFragmentInContainer<GeneralSettingsFragment>()
    private val sharedPreferences = context.getSharedPreferences(
        GeneralSettingsFragment.GENERAL_SETTING_PREFERENCE,
        Context.MODE_PRIVATE
    )

    @Test
    fun getInitSelectFromAttributeTest_snoozeDuration0() {
        //
        val expectedPos = 0
        val attribute = "data-minute"
        sharedPreferences.edit()
            .putString(attribute, "1")
            .commit()
        val method: Method = generalSettingsFragment.javaClass.getDeclaredMethod("getInitSelectFromAttribute", String::class.java, String::class.java)
        method.isAccessible = true
        val actualPos = method.invoke(generalSettingsFragment, attribute, "")
        assertEquals(expectedPos, actualPos)
    }
}