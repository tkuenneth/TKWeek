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

    private val workingWeek = setOf(
        Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY
    )

    private fun friday() = GregorianCalendar(2026, Calendar.NOVEMBER, 13)

    private fun firstOfNovember2026() = GregorianCalendar(2026, Calendar.NOVEMBER, 1)

    private fun lastOfNovember2026() = GregorianCalendar(2026, Calendar.NOVEMBER, 30)

    @Test
    fun countBusinessDays_countsTheWeekdaysOfAMonth() {
        val count = DateUtilities.countBusinessDays(
            firstOfNovember2026(), lastOfNovember2026(), workingWeek
        )

        assertEquals(21, count)
    }

    @Test
    fun countBusinessDays_withoutASingleBusinessDay_countsNothing() {
        val count = DateUtilities.countBusinessDays(
            firstOfNovember2026(), lastOfNovember2026(), emptySet()
        )

        assertEquals(0, count)
    }

    @Test
    fun countBusinessDays_followsAnUnusualWorkingWeek() {
        val count = DateUtilities.countBusinessDays(
            firstOfNovember2026(), lastOfNovember2026(),
            setOf(Calendar.SATURDAY, Calendar.SUNDAY)
        )

        assertEquals(9, count)
    }

    @Test
    fun countBusinessDays_includesBothEnds() {
        val friday = GregorianCalendar(2026, Calendar.NOVEMBER, 13)

        assertEquals(1, DateUtilities.countBusinessDays(friday, friday, workingWeek))
    }

    @Test
    fun countBusinessDays_leavesItsArgumentsAlone() {
        val from = firstOfNovember2026()
        val to = lastOfNovember2026()

        DateUtilities.countBusinessDays(from, to, workingWeek)

        assertEquals(1, from.get(Calendar.DAY_OF_MONTH))
        assertEquals(30, to.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun addBusinessDays_skipsTheWeekend() {
        val result = DateUtilities.addBusinessDays(friday(), 1, false, workingWeek)

        assertEquals(16, result.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.MONDAY, result.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun addBusinessDays_countsOnlyTheDaysThatWereSelected() {
        val result = DateUtilities.addBusinessDays(friday(), 5, false, workingWeek)

        assertEquals(20, result.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.FRIDAY, result.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun addBusinessDays_walksBackwardsWhenSubtracting() {
        val monday = GregorianCalendar(2026, Calendar.NOVEMBER, 16)

        val result = DateUtilities.addBusinessDays(monday, 1, true, workingWeek)

        assertEquals(13, result.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.FRIDAY, result.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun addBusinessDays_followsAnUnusualWorkingWeek() {
        val result = DateUtilities.addBusinessDays(
            friday(), 2, false, setOf(Calendar.SATURDAY, Calendar.SUNDAY)
        )

        assertEquals(15, result.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.SUNDAY, result.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun addBusinessDays_withoutASingleBusinessDay_leavesTheDateAlone() {
        val result = DateUtilities.addBusinessDays(friday(), 5, false, emptySet())

        assertEquals(13, result.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.NOVEMBER, result.get(Calendar.MONTH))
    }

    @Test
    fun addBusinessDays_withoutDays_leavesTheDateAlone() {
        val result = DateUtilities.addBusinessDays(friday(), 0, false, workingWeek)

        assertEquals(13, result.get(Calendar.DAY_OF_MONTH))
    }
}
