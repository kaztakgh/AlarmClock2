package io.github.kaztakgh.alarmclock2

import junit.framework.TestCase
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*
import kotlin.collections.ArrayList

@RunWith(RobolectricTestRunner::class)
class HolidayDataAccessJobServiceTest : TestCase() {
    private val jsonText = "{" +
            "    \"2019-01-01\": \"元日\"," +
            "    \"2019-01-14\": \"成人の日\"," +
            "    \"2019-02-11\": \"建国記念の日\"," +
            "    \"2019-03-21\": \"春分の日\"," +
            "    \"2019-04-29\": \"昭和の日\"," +
            "    \"2019-04-30\": \"祝日\"," +
            "    \"2019-05-01\": \"天皇の即位の日\"," +
            "    \"2019-05-02\": \"祝日\"," +
            "    \"2019-05-03\": \"憲法記念日\"," +
            "    \"2019-05-04\": \"みどりの日\"," +
            "    \"2019-05-05\": \"こどもの日\"," +
            "    \"2019-05-06\": \"こどもの日 振替休日\"," +
            "    \"2019-07-15\": \"海の日\"," +
            "    \"2019-08-11\": \"山の日\"," +
            "    \"2019-08-12\": \"山の日 振替休日\"," +
            "    \"2019-09-16\": \"敬老の日\"," +
            "    \"2019-09-23\": \"秋分の日\"," +
            "    \"2019-10-14\": \"体育の日\"," +
            "    \"2019-10-22\": \"即位礼正殿の儀の行われる日\"," +
            "    \"2019-11-03\": \"文化の日\"," +
            "    \"2019-11-04\": \"文化の日 振替休日\"," +
            "    \"2019-11-23\": \"勤労感謝の日\"," +
            "    \"2020-01-01\": \"元日\"," +
            "    \"2020-01-13\": \"成人の日\"," +
            "    \"2020-02-11\": \"建国記念の日\"," +
            "    \"2020-02-23\": \"天皇誕生日\"," +
            "    \"2020-02-24\": \"天皇誕生日 振替休日\"," +
            "    \"2020-03-20\": \"春分の日\"," +
            "    \"2020-04-29\": \"昭和の日\"," +
            "    \"2020-05-03\": \"憲法記念日\"," +
            "    \"2020-05-04\": \"みどりの日\"," +
            "    \"2020-05-05\": \"こどもの日\"," +
            "    \"2020-05-06\": \"憲法記念日 振替休日\"," +
            "    \"2020-07-23\": \"海の日\"," +
            "    \"2020-07-24\": \"体育の日\"," +
            "    \"2020-08-10\": \"山の日\"," +
            "    \"2020-09-21\": \"敬老の日\"," +
            "    \"2020-09-22\": \"秋分の日\"," +
            "    \"2020-11-03\": \"文化の日\"," +
            "    \"2020-11-23\": \"勤労感謝の日\"," +
            "    \"2021-01-01\": \"元日\"," +
            "    \"2021-01-11\": \"成人の日\"," +
            "    \"2021-02-11\": \"建国記念の日\"," +
            "    \"2021-02-23\": \"天皇誕生日\"," +
            "    \"2021-03-20\": \"春分の日\"," +
            "    \"2021-04-29\": \"昭和の日\"," +
            "    \"2021-05-03\": \"憲法記念日\"," +
            "    \"2021-05-04\": \"みどりの日\"," +
            "    \"2021-05-05\": \"こどもの日\"," +
            "    \"2021-07-19\": \"海の日\"," +
            "    \"2021-08-11\": \"山の日\"," +
            "    \"2021-09-20\": \"敬老の日\"," +
            "    \"2021-09-23\": \"秋分の日\"," +
            "    \"2021-10-11\": \"体育の日\"," +
            "    \"2021-11-03\": \"文化の日\"," +
            "    \"2021-11-23\": \"勤労感謝の日\"" +
            "}"

    @Test
    fun getHolidaysStringTest() {
        val expected = jsonText
        val method = HolidayDataAccessJobService::class.java.getDeclaredMethod("getHolidaysString")
        method.isAccessible = true
        val actual = method.invoke(HolidayDataAccessJobService)
        assertEquals(expected, actual)
    }
    @Test
    fun convertToHolidayDataModelTest() {
        val method = HolidayDataAccessJobService::class.java.getDeclaredMethod("convertToHolidayDataModel", String::class.java)
        method.isAccessible = true
        val actual = method.invoke(HolidayDataAccessJobService, jsonText)
        assertEquals(56, (actual as ArrayList<*>).size)
    }

    @Test
    fun convertToHolidayDataModelTest_fail() {
        val method = HolidayDataAccessJobService::class.java.getDeclaredMethod("convertToHolidayDataModel", String::class.java)
        method.isAccessible = true
        val actual = method.invoke(HolidayDataAccessJobService, "")
        assertEquals(0, (actual as ArrayList<*>).size)
    }

    @Test
    fun stringToDateTest() {
        val calendar = Calendar.getInstance()
        calendar.set(2020, 0, 15, 0, 0, 0)
        val expected = Date()
        expected.time = calendar.timeInMillis
        val method = HolidayDataAccessJobService::class.java.getDeclaredMethod("stringToDate", String::class.java)
        method.isAccessible = true
        val actual = method.invoke(HolidayDataAccessJobService, "2020-01-15")
        assertEquals(expected.toString(), actual.toString())
    }

    @Test
    fun stringToDateTest_wrongFormat() {
        val method = HolidayDataAccessJobService::class.java.getDeclaredMethod("stringToDate", String::class.java)
        method.isAccessible = true
        val actual = method.invoke(HolidayDataAccessJobService, "2020/01/15")
        assertNull(actual)
    }
}