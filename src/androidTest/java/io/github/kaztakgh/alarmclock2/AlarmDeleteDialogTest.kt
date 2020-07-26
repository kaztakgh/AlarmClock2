package io.github.kaztakgh.alarmclock2

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import java.util.regex.Pattern.matches

class AlarmDeleteDialogTest {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun onScreenEvent() {
        val scenario = launchFragmentInContainer<AlarmDeleteDialog>()
        scenario.moveToState(Lifecycle.State.RESUMED)
        // ビューの確認
        onView(withText(R.string.alarm_record_delete_dialog_text)).check(ViewAssertions.matches(withId(android.R.id.message)))
    }
}