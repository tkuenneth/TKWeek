package com.thomaskuenneth.tkweek.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar

class DateUtilitiesTest {

    @Test
    fun isSchaltjahr_identifiesGregorianLeapYears() {
        assertTrue(DateUtilities.isSchaltjahr(2024))
        assertTrue(DateUtilities.isSchaltjahr(2000))
        assertFalse(DateUtilities.isSchaltjahr(1900))
        assertFalse(DateUtilities.isSchaltjahr(2023))
    }

    @Test
    fun toRoman_convertsCommonValues() {
        assertEquals("I", DateUtilities.toRoman(1))
        assertEquals("IV", DateUtilities.toRoman(4))
        assertEquals("IX", DateUtilities.toRoman(9))
        assertEquals("XLII", DateUtilities.toRoman(42))
        assertEquals("MMXXVI", DateUtilities.toRoman(2026))
    }

    @Test
    fun getCalendar_yearMonthDay_setsExpectedFields() {
        val cal = DateUtilities.getCalendar(2026, Calendar.SEPTEMBER, 4)

        assertEquals(2026, cal.get(Calendar.YEAR))
        assertEquals(Calendar.SEPTEMBER, cal.get(Calendar.MONTH))
        assertEquals(4, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun clearTimeRelatedFields_normalizesToNoon() {
        val cal = GregorianCalendar(2026, Calendar.MARCH, 15, 23, 59, 59)
        cal.set(Calendar.MILLISECOND, 999)

        DateUtilities.clearTimeRelatedFields(cal)

        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(Calendar.MINUTE))
        assertEquals(0, cal.get(Calendar.SECOND))
        assertEquals(0, cal.get(Calendar.MILLISECOND))
    }

    @Test
    fun getFirstAdvent_2026_isNovember29() {
        val advent = DateUtilities.getFirstAdvent(2026)

        assertEquals(2026, advent.get(Calendar.YEAR))
        assertEquals(Calendar.NOVEMBER, advent.get(Calendar.MONTH))
        assertEquals(29, advent.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.SUNDAY, advent.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun getMothersDay_2026_isMay10() {
        val mothersDay = DateUtilities.getMothersDay(2026)

        assertEquals(2026, mothersDay.get(Calendar.YEAR))
        assertEquals(Calendar.MAY, mothersDay.get(Calendar.MONTH))
        assertEquals(10, mothersDay.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.SUNDAY, mothersDay.get(Calendar.DAY_OF_WEEK))
    }
}
