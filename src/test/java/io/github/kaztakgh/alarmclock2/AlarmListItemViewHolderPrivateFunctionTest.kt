package io.github.kaztakgh.alarmclock2

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import java.lang.reflect.Method
import java.util.*

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class AlarmListItemViewHolderPrivateFunctionTest
    : TestCase() {
    private val activityController: ActivityController<Activity> = Robolectric.buildActivity(
        Activity::class.java
    )
    private val view = LayoutInflater.from(activityController.get()).inflate(R.layout.alarm_list_item, null)
    private val viewHolder: AlarmListItemViewHolder = AlarmListItemViewHolder(view)

    @Test
    fun createRepeatDayTextTest() {
        val repeatWeek = booleanArrayOf(false, true, true, true, true, true, false)
        val expected = "月曜日, 火曜日, 水曜日, 木曜日, 金曜日"
        val method: Method = viewHolder.javaClass.getDeclaredMethod("createRepeatDayText", BooleanArray::class.java)
        method.isAccessible = true
        val actual: String = method.invoke(viewHolder, repeatWeek) as String
        assertEquals(expected, actual)
    }

    @Test
    fun createNoRepeatDayTextTest() {
        val repeatWeek = BooleanArray(7) {false}
        val expected = "繰り返し無し"
        val method: Method = viewHolder.javaClass.getDeclaredMethod("createRepeatDayText", BooleanArray::class.java)
        method.isAccessible = true
        val actual: String = method.invoke(viewHolder, repeatWeek) as String
        assertEquals(expected, actual)
    }
}