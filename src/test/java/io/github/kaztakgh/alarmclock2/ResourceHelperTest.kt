package io.github.kaztakgh.alarmclock2

import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ResourceHelperTest
    : TestCase() {
    /**
     * 該当するリソースとファイル名が分割できるかをテスト
     */
    @Test
    fun splitClassAndId() {
        val expectedArray: List<String> = listOf("@drawable", "ic_alarm_24dp")
        val actualArray: List<String> = ResourceHelper.splitClassAndId("@drawable/ic_alarm_24dp")
        assertEquals(expectedArray, actualArray)
    }

    @Test
    fun splitClassAndId_false() {
        val expectedArray: List<String> = emptyList()
        val actualArray: List<String> = ResourceHelper.splitClassAndId("ic_alarm_24dp")
        assertEquals(expectedArray, actualArray)
    }
}