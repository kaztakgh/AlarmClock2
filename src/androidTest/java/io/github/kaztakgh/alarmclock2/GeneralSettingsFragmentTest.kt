package io.github.kaztakgh.alarmclock2

import android.os.Bundle
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GeneralSettingsFragmentTest {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun onScreenEvent() {
        val args = Bundle()
        val factory = FragmentFactory()
        val scenario = launchFragmentInContainer<GeneralSettingsFragment>()
        scenario.moveToState(Lifecycle.State.STARTED)
        // ビューの確認
        onView(withId(R.id.tb_setting)).check(matches(isDisplayed()))
        onView(withText(R.string.menu_general_setting)).check(matches(withParent(withId(R.id.tb_setting))))
    }
}