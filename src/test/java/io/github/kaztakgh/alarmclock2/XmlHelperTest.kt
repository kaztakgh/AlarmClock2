package io.github.kaztakgh.alarmclock2

import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class XmlHelperTest
    : TestCase() {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * 該当する属性の値を配列で取得する
     */
    @Test
    fun getAttributeArrayByName() {
        val expectedArray = arrayListOf<String>()
        val minuteArray = arrayOf("1", "2", "5", "10", "15", "20", "30")
        expectedArray.addAll(minuteArray)
        val actualArray = XmlHelper.getAttributeArrayByNameFromAssets(context, "alarm_settings.xml", "data-minute")
        assertEquals(expectedArray, actualArray)
    }

    /**
     * 指定したファイルが存在しない場合、例外処理が行われるかをテストする
     */
    @Test(expected = IOException::class)
    fun getAttributeArrayByName_fileNotFound() {
        // FileNotFoundExceptionが発生する
        val expectedArray = arrayListOf<String>()
        val minuteArray = arrayOf("1", "2", "5", "10", "15", "20", "30")
        expectedArray.addAll(minuteArray)
        val actualArray = XmlHelper.getAttributeArrayByNameFromAssets(context, "settings.xml", "data-minute")
    }

    /**
     * 該当する属性が無い場合、空配列を返すことを確認する
     */
    @Test
    fun getAttributeArrayByName_attributeNotFound() {
        val expectedArray = arrayListOf<String>()
        val actualArray = XmlHelper.getAttributeArrayByNameFromAssets(context, "alarm_settings.xml", "data-second")
        assertEquals(expectedArray, actualArray)
    }
}