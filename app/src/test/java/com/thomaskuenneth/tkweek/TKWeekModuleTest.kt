package com.thomaskuenneth.tkweek

import com.thomaskuenneth.tkweek.fragment.CalendarFragment
import com.thomaskuenneth.tkweek.fragment.MyDayFragment
import com.thomaskuenneth.tkweek.fragment.WeekFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TKWeekModuleTest {

    @Test
    fun find_returnsMatchingModuleForKnownFragmentClass() {
        assertEquals(TKWeekModule.Week, TKWeekModule.find(WeekFragment::class.java))
        assertEquals(TKWeekModule.MyDay, TKWeekModule.find(MyDayFragment::class.java))
        assertEquals(TKWeekModule.Calendar, TKWeekModule.find(CalendarFragment::class.java))
    }

    @Test
    fun find_returnsNullForUnknownClass() {
        assertNull(TKWeekModule.find(String::class.java))
    }

    @Test
    fun entries_exposeStableLauncherOrdering() {
        assertEquals(TKWeekModule.Week, TKWeekModule.entries.first())
        assertEquals(TKWeekModule.About, TKWeekModule.entries.last())
    }
}
