package io.github.kaztakgh.alarmclock2

import android.os.Bundle
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import io.github.kaztakgh.alarmclock2.AlarmSettingFragment.Companion.KEY_INPUT_MODEL
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test

class AlarmSettingFragmentTest {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun onAddScreenEvent() {
        val args = Bundle()
        val factory = FragmentFactory()
        val scenario = launchFragmentInContainer<AlarmSettingFragment>()
        scenario.moveToState(Lifecycle.State.STARTED)
        // ビューの確認
        Espresso.onView(ViewMatchers.withId(R.id.tb_alarm_setting))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withText(R.string.menu_title_alarm_add)).check(
            ViewAssertions.matches(
                ViewMatchers.withParent(ViewMatchers.withId(R.id.tb_alarm_setting))
            )
        )
    }

    @Test
    fun onModScreenEvent() {
        val model = AlarmRecordModel(
            id = 1,
            hour = 8,
            min = 30
        )
        val args = Bundle().apply {
            putParcelable(KEY_INPUT_MODEL, model)
        }
        val factory = FragmentFactory()
        val scenario = launchFragmentInContainer<AlarmSettingFragment>(args, factory.hashCode())
        scenario.moveToState(Lifecycle.State.STARTED)
        // ビューの確認
        Espresso.onView(ViewMatchers.withId(R.id.tb_alarm_setting))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withText(R.string.menu_title_alarm_modify)).check(
            ViewAssertions.matches(
                ViewMatchers.withParent(ViewMatchers.withId(R.id.tb_alarm_setting))
            )
        )
    }
}