package io.github.kaztakgh.alarmclock2

import android.os.Build
import junit.framework.TestCase

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.lang.reflect.Method
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

/**
 * AlarmRecordModelのfunction単体試験
 */
@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class AlarmRecordModelTest : TestCase() {
    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    @Before
    public override fun setUp() {
        super.setUp()
        ShadowLog.stream = System.out
    }

    @Test
    fun createRepeatArrayFromToday_Sunday() {
        // modelの設定
        val model = AlarmRecordModel(
            hour = 8,
            min = 30,
            repeatWeek = booleanArrayOf(false, true, true, true, true, true, false)
        )
        val today = Calendar.SUNDAY
        val expectedArray = model.repeatWeek
        val method: Method = model.javaClass.getDeclaredMethod("createRepeatArrayFromToday", BooleanArray::class.java, Int::class.java)
        method.isAccessible = true
        val actualArray = method.invoke(model, model.repeatWeek, today)
        assertArrayEquals(expectedArray, actualArray as BooleanArray)
    }

    @Test
    fun createRepeatArrayFromToday_Wednesday() {
        // modelの設定
        val model = AlarmRecordModel(
            hour = 8,
            min = 30,
            repeatWeek = booleanArrayOf(false, true, true, true, true, true, false)
        )
        val today = Calendar.WEDNESDAY
        val expectedArray = booleanArrayOf(true, true, true, false, false, true, true)
        val method: Method = model.javaClass.getDeclaredMethod("createRepeatArrayFromToday", BooleanArray::class.java, Int::class.java)
        method.isAccessible = true
        val actualArray = method.invoke(model, model.repeatWeek, today)
        assertArrayEquals(expectedArray, actualArray as BooleanArray)
    }

    @Test
    fun createRepeatArrayFromToday_Saturday() {
        // modelの設定
        val model = AlarmRecordModel(
            hour = 8,
            min = 30,
            repeatWeek = booleanArrayOf(false, true, true, true, true, true, false)
        )
        val today = Calendar.SATURDAY
        val expectedArray = booleanArrayOf(false, false, true, true, true, true, true)
        val method: Method = model.javaClass.getDeclaredMethod("createRepeatArrayFromToday", BooleanArray::class.java, Int::class.java)
        method.isAccessible = true
        val actualArray = method.invoke(model, model.repeatWeek, today)
        assertArrayEquals(expectedArray, actualArray as BooleanArray)
    }

    @Test
    fun convertToInteger_True() {
        val model = AlarmRecordModel()
        val method = model.javaClass.getDeclaredMethod("convertToInteger", Boolean::class.java)
        method.isAccessible = true
        val result = method.invoke(model, true)
        assertEquals(1, result)
    }

    @Test
    fun convertToInteger_False() {
        val model = AlarmRecordModel()
        val method = model.javaClass.getDeclaredMethod("convertToInteger", Boolean::class.java)
        method.isAccessible = true
        val result = method.invoke(model, false)
        assertEquals(0, result)
    }

    @Test
    fun createOneWeekDateArray() {
        val model = AlarmRecordModel()
        val method = model.javaClass.getDeclaredMethod("createOneWeekDateArray", Calendar::class.java)
        method.isAccessible = true
        val date1 = Calendar.getInstance()
        date1.set(2020, 0, 19, 0, 0, 0)
        val date2 = Calendar.getInstance()
        date2.set(2020, 0, 20, 0, 0, 0)
        val date3 = Calendar.getInstance()
        date3.set(2020, 0, 21, 0, 0, 0)
        val date4 = Calendar.getInstance()
        date4.set(2020, 0, 22, 0, 0, 0)
        val date5 = Calendar.getInstance()
        date5.set(2020, 0, 23, 0, 0, 0)
        val date6 = Calendar.getInstance()
        date6.set(2020, 0, 24, 0, 0, 0)
        val date7 = Calendar.getInstance()
        date7.set(2020, 0, 25, 0, 0, 0)
        val expectArray : ArrayList<Calendar> = arrayListOf(
            date1, date2, date3, date4, date5, date6, date7
        )
        val actualArray = method.invoke(model, date1) as ArrayList<Calendar>
        for (i in 0 until 7) {
            assertEquals(expectArray[i].get(Calendar.MONTH), actualArray[i].get(Calendar.MONTH))
            assertEquals(expectArray[i].get(Calendar.DAY_OF_MONTH), actualArray[i].get(Calendar.DAY_OF_MONTH))
        }
    }

    @Test
    fun createOneWeekDateArray_Api26() {
        val model = AlarmRecordModel()
        val method = model.javaClass.getDeclaredMethod("createOneWeekDateArray", LocalDate::class.java)
        method.isAccessible = true
        val date1 = LocalDate.of(2020, 1, 19)
        val date2 = LocalDate.of(2020, 1, 20)
        val date3 = LocalDate.of(2020, 1, 21)
        val date4 = LocalDate.of(2020, 1, 22)
        val date5 = LocalDate.of(2020, 1, 23)
        val date6 = LocalDate.of(2020, 1, 24)
        val date7 = LocalDate.of(2020, 1, 25)
        val expectArray: ArrayList<LocalDate> = arrayListOf(
            date1, date2, date3, date4, date5, date6, date7
        )
        val actualArray = method.invoke(model, date1) as ArrayList<*>
        for (i in 0 until 7) {
            assertEquals(expectArray[i], actualArray[i])
        }
    }

    @Test
    fun getRepeatArrayFromToday_NoHolidayData() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30,
            repeatWeek = booleanArrayOf(false, true, true, true, true, true, false)
        )
        val method = model.javaClass.getDeclaredMethod("getRepeatArrayFromToday", Calendar::class.java, Int::class.java, ArrayList<HolidayModel>().javaClass)
        method.isAccessible = true
        val date1 = Calendar.getInstance()
        // 2020-01-19(Sun)
        date1.set(2020, 0, 19)
        val expectArray = model.repeatWeek
        val actualArray = method.invoke(model, date1, 0, ArrayList<HolidayModel>(0)) as BooleanArray
        assertArrayEquals(expectArray, actualArray)
    }

    @Test
    fun getRepeatArrayFromToday_NoHolidayData_Api26() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30,
            repeatWeek = booleanArrayOf(false, true, true, true, true, true, false),
            holidayCount = AlarmRecordModel.HOLIDAY_NO_USE
        )
        val method = model.javaClass.getDeclaredMethod("getRepeatArrayFromToday", LocalDate::class.java, Int::class.java, ArrayList<HolidayModel>().javaClass)
        method.isAccessible = true
        val date = LocalDate.of(2020, 1, 19)
        date.atTime(7, 30, 0, 0)
        val expectArray = model.repeatWeek
        val actualArray = method.invoke(model, date, 0, ArrayList<HolidayModel>(0)) as BooleanArray
        assertArrayEquals(expectArray, actualArray)
    }

    @Test
    fun getRepeatArrayFromToday_NoHolidayData_TimePast_Api26() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30,
            repeatWeek = booleanArrayOf(false, true, true, true, true, true, false),
            holidayCount = AlarmRecordModel.HOLIDAY_NO_USE
        )
        val method = model.javaClass.getDeclaredMethod("getRepeatArrayFromToday", LocalDate::class.java, Int::class.java, ArrayList<HolidayModel>().javaClass)
        method.isAccessible = true
        val date = LocalDate.of(2020, 1, 19)
        date.atTime(12, 0, 0, 0)
        val expectArray = booleanArrayOf(true, true, true, true, true, false, false)
        val actualArray = method.invoke(model, date, 0, ArrayList<HolidayModel>(0)) as BooleanArray
        assertArrayEquals(expectArray, actualArray)
    }

    @Test
    fun getRepeatArrayFromToday_HolidayNoUse() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30,
            repeatWeek = booleanArrayOf(false, true, true, true, true, true, false),
            holidayCount = AlarmRecordModel.HOLIDAY_NO_USE
        )
        val method = model.javaClass.getDeclaredMethod("getRepeatArrayFromToday", Calendar::class.java, Int::class.java, ArrayList<HolidayModel>().javaClass)
        method.isAccessible = true
        val date = Calendar.getInstance()
        // 2020-02-10(Mon)
        date.set(2020, 1, 10)
        // 2020-02-11(Tue)
        val holiday1 = Calendar.getInstance()
        holiday1.set(2020, 1, 11)
        // 2020-02-23(Sun)
        val holiday2 = Calendar.getInstance()
        holiday1.set(2020, 1, 23)
        // 2020-02-24(Mon)
        val holiday3 = Calendar.getInstance()
        holiday1.set(2020, 1, 24)
        val holidayArray = arrayListOf(
            HolidayModel(holiday1, "建国記念日"),
            HolidayModel(holiday2, "天皇誕生日"),
            HolidayModel(holiday3, "天皇誕生日 振替休日")
        )
        val expectArray = booleanArrayOf(true, true, true, true, true, false, false)
        val actualArray = method.invoke(model, date, 0, holidayArray) as BooleanArray
        assertArrayEquals(expectArray, actualArray)
    }
    @Test
    fun getRepeatArrayFromToday_HolidayNoUse_Api26() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30,
            repeatWeek = booleanArrayOf(false, true, true, true, true, true, false),
            holidayCount = AlarmRecordModel.HOLIDAY_NO_USE
        )
        val method = model.javaClass.getDeclaredMethod("getRepeatArrayFromToday", LocalDate::class.java, Int::class.java, ArrayList<HolidayModel>().javaClass)
        method.isAccessible = true
        // 2020-02-10(Mon)
        val date = LocalDate.of(2020, 2, 10)
        // 2020-02-11(Tue)
        val holiday1 = Calendar.getInstance()
        holiday1.set(2020, 1, 11)
        // 2020-02-23(Sun)
        val holiday2 = Calendar.getInstance()
        holiday1.set(2020, 1, 23)
        // 2020-02-24(Mon)
        val holiday3 = Calendar.getInstance()
        holiday1.set(2020, 1, 24)
        val holidayArray = arrayListOf(
            HolidayModel(holiday1, "建国記念日"),
            HolidayModel(holiday2, "天皇誕生日"),
            HolidayModel(holiday3, "天皇誕生日 振替休日")
        )
        val expectArray = booleanArrayOf(true, true, true, true, true, false, false)
        val actualArray = method.invoke(model, date, 0, holidayArray) as BooleanArray
        assertArrayEquals(expectArray, actualArray)
    }

    @Test
    fun getRepeatArrayFromToday_HolidayInclude() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30,
            repeatWeek = booleanArrayOf(false, true, false, true, false, true, false),
            holidayCount = AlarmRecordModel.HOLIDAY_INCLUDE
        )
        val method = model.javaClass.getDeclaredMethod("getRepeatArrayFromToday", Calendar::class.java, Int::class.java, ArrayList<HolidayModel>().javaClass)
        method.isAccessible = true
        val date = Calendar.getInstance()
        // 2020-02-10(Mon)
        date.set(2020, 1, 10)
        // 2020-02-11(Tue)
        val holiday1 = Calendar.getInstance()
        holiday1.set(2020, 1, 11)
        // 2020-02-23(Sun)
        val holiday2 = Calendar.getInstance()
        holiday2.set(2020, 1, 23)
        // 2020-02-24(Mon)
        val holiday3 = Calendar.getInstance()
        holiday3.set(2020, 1, 24)
        val holidayArray = arrayListOf(
            HolidayModel(holiday1, "建国記念日"),
            HolidayModel(holiday2, "天皇誕生日"),
            HolidayModel(holiday3, "天皇誕生日 振替休日")
        )
        val expectArray = booleanArrayOf(true, true, true, false, true, false, false)
        val actualArray = method.invoke(model, date, 0, holidayArray) as BooleanArray
        assertArrayEquals(expectArray, actualArray)
    }

    @Test
    fun getRepeatArrayFromToday_HolidayInclude_Api26() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30,
            repeatWeek = booleanArrayOf(false, true, false, true, false, true, false),
            holidayCount = AlarmRecordModel.HOLIDAY_INCLUDE
        )
        val method = model.javaClass.getDeclaredMethod("getRepeatArrayFromToday", LocalDate::class.java, Int::class.java, ArrayList<HolidayModel>().javaClass)
        method.isAccessible = true
        // 2020-02-10(Mon)
        val date = LocalDate.of(2020, 2, 10)
        // 2020-02-11(Tue)
        val holiday1 = Calendar.getInstance()
        holiday1.set(2020, 1, 11)
        // 2020-02-23(Sun)
        val holiday2 = Calendar.getInstance()
        holiday2.set(2020, 1, 23)
        // 2020-02-24(Mon)
        val holiday3 = Calendar.getInstance()
        holiday3.set(2020, 1, 24)
        val holidayArray = arrayListOf(
            HolidayModel(holiday1, "建国記念日"),
            HolidayModel(holiday2, "天皇誕生日"),
            HolidayModel(holiday3, "天皇誕生日 振替休日")
        )
        val expectArray = booleanArrayOf(true, true, true, false, true, false, false)
        val actualArray = method.invoke(model, date, 0, holidayArray) as BooleanArray
        assertArrayEquals(expectArray, actualArray)
    }

    @Test
    fun getRepeatArrayFromToday_HolidayExclude() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30,
            repeatWeek = booleanArrayOf(false, true, true, true, true, true, false),
            holidayCount = AlarmRecordModel.HOLIDAY_EXCLUDE
        )
        val method = model.javaClass.getDeclaredMethod("getRepeatArrayFromToday", Calendar::class.java, Int::class.java, ArrayList<HolidayModel>().javaClass)
        method.isAccessible = true
        val date = Calendar.getInstance()
        // 2020-02-10(Mon)
        date.set(2020, 1, 10)
        // 2020-02-11(Tue)
        val holiday1 = Calendar.getInstance()
        holiday1.set(2020, 1, 11)
        // 2020-02-23(Sun)
        val holiday2 = Calendar.getInstance()
        holiday2.set(2020, 1, 23)
        // 2020-02-24(Mon)
        val holiday3 = Calendar.getInstance()
        holiday3.set(2020, 1, 24)
        val holidayArray = arrayListOf(
            HolidayModel(holiday1, "建国記念日"),
            HolidayModel(holiday2, "天皇誕生日"),
            HolidayModel(holiday3, "天皇誕生日 振替休日")
        )
        val expectArray = booleanArrayOf(true, false, true, true, true, false, false)
        val actualArray = method.invoke(model, date, 0, holidayArray) as BooleanArray
        assertArrayEquals(expectArray, actualArray)
    }

    @Test
    fun getRepeatArrayFromToday_HolidayExclude_Api26() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30,
            repeatWeek = booleanArrayOf(false, true, true, true, true, true, false),
            holidayCount = AlarmRecordModel.HOLIDAY_EXCLUDE
        )
        val method = model.javaClass.getDeclaredMethod("getRepeatArrayFromToday", LocalDate::class.java, Int::class.java, ArrayList<HolidayModel>().javaClass)
        method.isAccessible = true
        // 2020-02-10(Mon)
        val date = LocalDate.of(2020, 2, 10)
        // 2020-02-11(Tue)
        val holiday1 = Calendar.getInstance()
        holiday1.set(2020, 1, 11)
        // 2020-02-23(Sun)
        val holiday2 = Calendar.getInstance()
        holiday2.set(2020, 1, 23)
        // 2020-02-24(Mon)
        val holiday3 = Calendar.getInstance()
        holiday3.set(2020, 1, 24)
        val holidayArray = arrayListOf(
            HolidayModel(holiday1, "建国記念日"),
            HolidayModel(holiday2, "天皇誕生日"),
            HolidayModel(holiday3, "天皇誕生日 振替休日")
        )
        val expectArray = booleanArrayOf(true, false, true, true, true, false, false)
        val actualArray = method.invoke(model, date, 0, holidayArray) as BooleanArray
        assertArrayEquals(expectArray, actualArray)
    }

    @Test
    fun getRepeatArrayFromToday_HolidayExclude_2WeekLater() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30,
            repeatWeek = booleanArrayOf(false, true, false, false, false, false, false),
            holidayCount = AlarmRecordModel.HOLIDAY_EXCLUDE
        )
        val method = model.javaClass.getDeclaredMethod("getRepeatArrayFromToday", Calendar::class.java, Int::class.java, ArrayList<HolidayModel>().javaClass)
        method.isAccessible = true
        val date = Calendar.getInstance()
        // 2020-02-10(Mon)
        date.set(2020, 1, 10)
        // 2020-02-11(Tue)
        val holiday1 = Calendar.getInstance()
        holiday1.set(2020, 1, 11)
        // 2020-02-23(Sun)
        val holiday2 = Calendar.getInstance()
        holiday2.set(2020, 1, 23)
        // 2020-02-24(Mon)
        val holiday3 = Calendar.getInstance()
        holiday3.set(2020, 1, 24)
        val holidayArray = arrayListOf(
            HolidayModel(holiday1, "建国記念日"),
            HolidayModel(holiday2, "天皇誕生日"),
            HolidayModel(holiday3, "天皇誕生日 振替休日")
        )
        val expectArray = BooleanArray(WEEK_NUM){ false }
        val actualArray = method.invoke(model, date, 2, holidayArray) as BooleanArray
        assertArrayEquals(expectArray, actualArray)
    }

    @Test
    fun getRepeatArrayFromToday_HolidayExclude_2WeekLater_Api26() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30,
            repeatWeek = booleanArrayOf(false, true, false, false, false, false, false),
            holidayCount = AlarmRecordModel.HOLIDAY_EXCLUDE
        )
        val method = model.javaClass.getDeclaredMethod("getRepeatArrayFromToday", LocalDate::class.java, Int::class.java, ArrayList<HolidayModel>().javaClass)
        method.isAccessible = true
        // 2020-02-10(Mon)
        val date = LocalDate.of(2020, 2, 10)
        // 2020-02-11(Tue)
        val holiday1 = Calendar.getInstance()
        holiday1.set(2020, 1, 11)
        // 2020-02-23(Sun)
        val holiday2 = Calendar.getInstance()
        holiday2.set(2020, 1, 23)
        // 2020-02-24(Mon)
        val holiday3 = Calendar.getInstance()
        holiday3.set(2020, 1, 24)
        val holidayArray = arrayListOf(
            HolidayModel(holiday1, "建国記念日"),
            HolidayModel(holiday2, "天皇誕生日"),
            HolidayModel(holiday3, "天皇誕生日 振替休日")
        )
        val expectArray = BooleanArray(WEEK_NUM){ false }
        val actualArray = method.invoke(model, date, 2, holidayArray) as BooleanArray
        assertArrayEquals(expectArray, actualArray)
    }

    @Test
    fun getNextAlarmTime_NoHolidayArray() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30,
            repeatWeek = booleanArrayOf(false, true, true, true, true, true, false),
            state = true
        )
        val expectedDate = Calendar.getInstance()
        expectedDate.set(2020, 0, 23, model.hour, model.min, 0)
        val actualDate = model.getNextAlarmTime()!!
        // Calendarオブジェクトの比較ではNG(全て合致する必要があるため)
        assertEquals(expectedDate.get(Calendar.YEAR), actualDate.get(Calendar.YEAR))
        assertEquals(expectedDate.get(Calendar.MONTH), actualDate.get(Calendar.MONTH))
        assertEquals(expectedDate.get(Calendar.DAY_OF_MONTH), actualDate.get(Calendar.DAY_OF_MONTH))
        assertEquals(expectedDate.get(Calendar.HOUR_OF_DAY), actualDate.get(Calendar.HOUR_OF_DAY))
        assertEquals(expectedDate.get(Calendar.MINUTE), actualDate.get(Calendar.MINUTE))
        assertEquals(expectedDate.get(Calendar.SECOND), actualDate.get(Calendar.SECOND))
    }

    @Test
    fun getNextAlarmTime_HolidayOn() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30,
            repeatWeek = booleanArrayOf(false, true, true, true, true, true, false),
            state = true
        )
        // 2020-02-10(Mon)
        val date = Calendar.getInstance()
        date.set(2020, 1, 10, 12, 0, 0)
        // 2020-02-11(Tue)
        val holiday1 = Calendar.getInstance()
        holiday1.set(2020, 1, 11)
        // 2020-02-23(Sun)
        val holiday2 = Calendar.getInstance()
        holiday2.set(2020, 1, 23)
        // 2020-02-24(Mon)
        val holiday3 = Calendar.getInstance()
        holiday3.set(2020, 1, 24)
        val holidayArray = arrayListOf(
            HolidayModel(holiday1, "建国記念日"),
            HolidayModel(holiday2, "天皇誕生日"),
            HolidayModel(holiday3, "天皇誕生日 振替休日")
        )
        // TODO: 現在日時を調整
        val expectedDate = Calendar.getInstance()
        expectedDate.set(2020, 1, 12, model.hour, model.min, 0)
        val actualDate = model.getNextAlarmTime(holidayArray)!!
        assertEquals(expectedDate.get(Calendar.YEAR), actualDate.get(Calendar.YEAR))
        assertEquals(expectedDate.get(Calendar.MONTH), actualDate.get(Calendar.MONTH))
        assertEquals(expectedDate.get(Calendar.DAY_OF_MONTH), actualDate.get(Calendar.DAY_OF_MONTH))
        assertEquals(expectedDate.get(Calendar.HOUR_OF_DAY), actualDate.get(Calendar.HOUR_OF_DAY))
        assertEquals(expectedDate.get(Calendar.MINUTE), actualDate.get(Calendar.MINUTE))
        assertEquals(expectedDate.get(Calendar.SECOND), actualDate.get(Calendar.SECOND))
    }

    @Test
    fun getNextAlarmTime__StateFalse() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30,
            repeatWeek = booleanArrayOf(false, true, true, true, true, true, false),
            state = false
        )
        assertNull(model.getNextAlarmTime())
    }

    @Test
    fun getNextAlarmDateTime_NoHolidayArray() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30,
            repeatWeek = booleanArrayOf(false, true, true, true, true, true, false),
            state = true
        )
        // 2020-02-10(Mon)
        val date = LocalDate.of(2020, 2, 10)
        date.atTime(12, 0, 0, 0)
        val expectedDateTime = LocalDateTime.of(2020, 2, 11, model.hour, model.min, 0)
        val actualDateTime = model.getNextAlarmDateTime()
        assertEquals(expectedDateTime, actualDateTime)
    }

    @Test
    fun getNextAlarmDateTime_HolidayOn() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30,
            repeatWeek = booleanArrayOf(false, true, true, true, true, true, false),
            state = true
        )
        // 2020-02-10(Mon)
        val date = LocalDate.of(2020, 2, 10)
        date.atTime(12, 0, 0, 0)
        // 2020-02-11(Tue)
        val holiday1 = Calendar.getInstance()
        holiday1.set(2020, 1, 11)
        // 2020-02-23(Sun)
        val holiday2 = Calendar.getInstance()
        holiday1.set(2020, 1, 23)
        // 2020-02-24(Mon)
        val holiday3 = Calendar.getInstance()
        holiday1.set(2020, 1, 24)
        val holidayArray = arrayListOf(
            HolidayModel(holiday1, "建国記念日"),
            HolidayModel(holiday2, "天皇誕生日"),
            HolidayModel(holiday3, "天皇誕生日 振替休日")
        )
        val expectedDateTime = LocalDateTime.of(2020, 2, 12, model.hour, model.min, 0)
        val actualDateTime = model.getNextAlarmDateTime(holidayArray)
        assertEquals(expectedDateTime, actualDateTime)
    }

    @Test
    fun getNextAlarmDateTime_StateFalse() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30,
            repeatWeek = booleanArrayOf(false, true, true, true, true, true, false),
            state = false
        )
        assertNull(model.getNextAlarmDateTime())
    }

    @Test
    fun getAlarmTimeString_24H_AM() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30
        )
        val expectedTimeString = "08:30"
        val actualTimeString = model.getAlarmTimeString()
        assertEquals(expectedTimeString, actualTimeString)
    }

    @Test
    fun getAlarmTimeString_24H_PM() {
        val model = AlarmRecordModel(
            hour = 20,
            min = 30
        )
        val expectedTimeString = "20:30"
        val actualTimeString = model.getAlarmTimeString()
        assertEquals(expectedTimeString, actualTimeString)
    }

    @Test
    fun getAlarmTimeString_12H_AM_EN() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30
        )
        val expectedTimeString = "08:30 AM"
        val actualTimeString = model.getAlarmTimeString(false, Locale.ENGLISH)
        assertEquals(expectedTimeString, actualTimeString)
    }

    @Test
    fun getAlarmTimeString_12H_PM_EN() {
        val model = AlarmRecordModel(
            hour = 20,
            min = 30
        )
        val expectedTimeString = "08:30 PM"
        val actualTimeString = model.getAlarmTimeString(false, Locale.ENGLISH)
        assertEquals(expectedTimeString, actualTimeString)
    }

    @Test
    fun getAlarmTimeString_12H_AM_JP() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30
        )
        // TODO: Locale設定
        val expectedTimeString = "午前 08:30"
        val actualTimeString = model.getAlarmTimeString(false, Locale.JAPANESE)
        assertEquals(expectedTimeString, actualTimeString)
    }

    @Test
    fun getAlarmTimeString_12H_PM_JP() {
        val model = AlarmRecordModel(
            hour = 20,
            min = 30
        )
        // TODO: Locale設定
        val expectedTimeString = "午後 08:30"
        val actualTimeString = model.getAlarmTimeString(false, Locale.JAPANESE)
        assertEquals(expectedTimeString, actualTimeString)
    }

    @Test
    fun getAlarmDateTimeString_24H_AM() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30
        )
        val expectedTimeString = "08:30"
        val actualTimeString = model.getAlarmDateTimeString()
        assertEquals(expectedTimeString, actualTimeString)
    }

    @Test
    fun getAlarmDateTimeString_24H_PM() {
        val model = AlarmRecordModel(
            hour = 20,
            min = 30
        )
        val expectedTimeString = "20:30"
        val actualTimeString = model.getAlarmDateTimeString()
        assertEquals(expectedTimeString, actualTimeString)
    }
    @Test
    fun getAlarmDateTimeString_12H_AM_EN() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30
        )
        val expectedTimeString = "08:30 AM"
        val actualTimeString = model.getAlarmDateTimeString(false, Locale.ENGLISH)
        assertEquals(expectedTimeString, actualTimeString)
    }

    @Test
    fun getAlarmDateTimeString_12H_PM_EN() {
        val model = AlarmRecordModel(
            hour = 20,
            min = 30
        )
        val expectedTimeString = "08:30 PM"
        val actualTimeString = model.getAlarmDateTimeString(false, Locale.ENGLISH)
        assertEquals(expectedTimeString, actualTimeString)
    }

    @Test
    fun getAlarmDateTimeString_12H_AM_JP() {
        val model = AlarmRecordModel(
            hour = 8,
            min = 30
        )
        val expectedTimeString = "午前 08:30"
        val actualTimeString = model.getAlarmDateTimeString(false, Locale.JAPANESE)
        assertEquals(expectedTimeString, actualTimeString)
    }

    @Test
    fun getAlarmDateTimeString_12H_PM_JP() {
        val model = AlarmRecordModel(
            hour = 20,
            min = 30
        )
        val expectedTimeString = "午後 08:30"
        val actualTimeString = model.getAlarmDateTimeString(false, Locale.JAPANESE)
        assertEquals(expectedTimeString, actualTimeString)
    }
}