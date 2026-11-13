package com.thomaskuenneth.tkweek

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar

class DaysBetweenDatesTest {

    private val workingWeek = setOf(
        Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY
    )

    private fun monday() = GregorianCalendar(2026, Calendar.NOVEMBER, 9)

    private fun theMondayAfter() = GregorianCalendar(2026, Calendar.NOVEMBER, 16)

    @Test
    fun aWeekHasFiveBusinessDaysAndOneWeekend() {
        val result = daysBetweenDates(monday(), theMondayAfter(), false, workingWeek)

        assertEquals(7, result.days)
        assertEquals(5, result.businessDays)
        assertEquals(1, result.weekends)
    }

    @Test
    fun withoutASingleBusinessDay_onlyTheBusinessDayCountStaysAtZero() {
        val result = daysBetweenDates(monday(), theMondayAfter(), false, emptySet())

        assertEquals(0, result.businessDays)
        assertEquals(7, result.days)
        assertEquals(1, result.weekends)
    }

    @Test
    fun anUnusualWorkingWeekIsCountedJustTheSame() {
        val result = daysBetweenDates(
            monday(), theMondayAfter(), false, setOf(Calendar.SATURDAY, Calendar.SUNDAY)
        )

        assertEquals(2, result.businessDays)
        assertEquals(7, result.days)
    }

    @Test
    fun includingTheFirstDateCountsItAsWell() {
        val result = daysBetweenDates(monday(), theMondayAfter(), true, workingWeek)

        assertEquals(8, result.days)
        assertEquals(6, result.businessDays)
    }

    @Test
    fun includingAFirstDateThatIsNoBusinessDayAddsOnlyToTheDays() {
        val saturday = GregorianCalendar(2026, Calendar.NOVEMBER, 14)

        val result = daysBetweenDates(saturday, theMondayAfter(), true, workingWeek)

        assertEquals(3, result.days)
        assertEquals(0, result.businessDays)
    }

    @Test
    fun crossingIntoTheNextMonthIsOneMonthTurn() {
        val result = daysBetweenDates(
            GregorianCalendar(2026, Calendar.JANUARY, 28),
            GregorianCalendar(2026, Calendar.FEBRUARY, 3),
            false,
            workingWeek
        )

        assertEquals(6, result.days)
        assertEquals(1, result.months)
        assertEquals(0, result.years)
    }

    @Test
    fun crossingIntoTheNextYearTurnsTheMonthAndTheYear() {
        val result = daysBetweenDates(
            GregorianCalendar(2026, Calendar.DECEMBER, 29),
            GregorianCalendar(2027, Calendar.JANUARY, 2),
            false,
            workingWeek
        )

        assertEquals(4, result.days)
        assertEquals(1, result.months)
        assertEquals(1, result.years)
    }

    private fun isoWeek(year: Int, month: Int, day: Int) =
        GregorianCalendar(year, month, day).apply {
            firstDayOfWeek = Calendar.MONDAY
            minimalDaysInFirstWeek = 4
        }

    @Test
    fun aFortnightTurnsTheWeekOnce() {
        val result = daysBetweenDates(
            isoWeek(2026, Calendar.NOVEMBER, 9),
            isoWeek(2026, Calendar.NOVEMBER, 23),
            false,
            workingWeek
        )

        assertEquals(14, result.days)
        assertEquals(1, result.weeks)
        assertEquals(2, result.weekends)
    }

    @Test
    fun theSameDateTwiceCountsNothing() {
        val result = daysBetweenDates(monday(), monday(), false, workingWeek)

        assertEquals(0, result.days)
        assertEquals(0, result.businessDays)
    }
}
