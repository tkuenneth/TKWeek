package com.thomaskuenneth.tkweek.util

import com.thomaskuenneth.tkweek.util.CalendarCondition.CONDITION
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar

class CalendarLogicTest {

    @Test
    fun matches_equalDayOfWeek_returnsTrueForSunday() {
        val condition = CalendarCondition.createCalendarCondition(
            CONDITION.EQUAL,
            Calendar.DAY_OF_WEEK,
            Calendar.SUNDAY,
            false
        )
        val sunday = GregorianCalendar(2026, Calendar.SEPTEMBER, 6)

        assertTrue(condition.matches(sunday))
    }

    @Test
    fun matches_notEqual_returnsFalseWhenValuesMatch() {
        val condition = CalendarCondition.createCalendarCondition(
            CONDITION.NOT_EQUAL,
            Calendar.DAY_OF_MONTH,
            4,
            false
        )
        val fourth = GregorianCalendar(2026, Calendar.SEPTEMBER, 4)

        assertFalse(condition.matches(fourth))
    }

    @Test
    fun matches_requiredCondition_throwsWhenUnmet() {
        val condition = CalendarCondition.createCalendarCondition(
            CONDITION.EQUAL,
            Calendar.DAY_OF_WEEK,
            Calendar.MONDAY,
            true
        )
        val sunday = GregorianCalendar(2026, Calendar.SEPTEMBER, 6)

        assertThrows(RequiredCalendarConditionException::class.java) {
            condition.matches(sunday)
        }
    }

    @Test
    fun iterateUntil_advancesForwardToMatchingDay() {
        val start = GregorianCalendar(2026, Calendar.SEPTEMBER, 1)
        val untilSunday = CalendarCondition.createCalendarCondition(
            CONDITION.EQUAL,
            Calendar.DAY_OF_WEEK,
            Calendar.SUNDAY,
            true
        )

        val result = CalendarIterator.iterateUntil(
            start,
            untilSunday,
            Calendar.DAY_OF_MONTH,
            1
        )

        assertEquals(Calendar.SUNDAY, result.get(Calendar.DAY_OF_WEEK))
        assertEquals(6, result.get(Calendar.DAY_OF_MONTH))
    }
}
