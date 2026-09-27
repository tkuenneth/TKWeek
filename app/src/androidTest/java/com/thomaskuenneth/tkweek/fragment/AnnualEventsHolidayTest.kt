package com.thomaskuenneth.tkweek.fragment

import android.content.Context
import androidx.core.content.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.thomaskuenneth.tkweek.types.Event
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Marking an annual event as a holiday.
 *
 * The flag is persisted per event and decides whether a day is treated as a day off, so the
 * cases worth pinning are: an unmarked event must not read as a holiday, marking must stick,
 * unmarking must actually clear it rather than leave the old value behind, and two different
 * events must not influence each other.
 *
 * The last test records something sharper: the preference key is derived from the event's
 * description alone, so two events sharing a description - or an event with no description at
 * all - share one flag. That is the current contract, not an endorsement of it.
 */
@SmallTest
@RunWith(AndroidJUnit4::class)
class AnnualEventsHolidayTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun clearBefore() = clearPreferences()

    @After
    fun clearAfter() = clearPreferences()

    private fun clearPreferences() {
        context.getSharedPreferences("AnnualEventsFragment", Context.MODE_PRIVATE)
            .edit { clear() }
    }

    private fun event(description: String?) = Event().apply { descr = description }

    @Test
    fun anEventNobodyMarked_isNotAHoliday() {
        assertFalse(AnnualEventsFragment.isHoliday(context, event("Ostersonntag")))
    }

    @Test
    fun markingAnEvent_makesItAHoliday() {
        val easter = event("Ostersonntag")

        AnnualEventsFragment.setHoliday(context, easter, true)

        assertTrue(AnnualEventsFragment.isHoliday(context, easter))
    }

    @Test
    fun unmarkingAnEvent_clearsItAgain() {
        val easter = event("Ostersonntag")
        AnnualEventsFragment.setHoliday(context, easter, true)

        AnnualEventsFragment.setHoliday(context, easter, false)

        assertFalse(AnnualEventsFragment.isHoliday(context, easter))
    }

    @Test
    fun markingOneEvent_leavesOtherEventsAlone() {
        val easter = event("Ostersonntag")
        val christmas = event("Weihnachten")

        AnnualEventsFragment.setHoliday(context, easter, true)

        assertTrue(AnnualEventsFragment.isHoliday(context, easter))
        assertFalse(AnnualEventsFragment.isHoliday(context, christmas))
    }

    /**
     * The flag lives in the preferences, not on the object, so a freshly built event with the
     * same description has to see it. This is how the list reads the flag back after a rebuild.
     */
    @Test
    fun aFreshlyBuiltEventWithTheSameDescription_seesTheStoredFlag() {
        AnnualEventsFragment.setHoliday(context, event("Pfingstmontag"), true)

        assertTrue(AnnualEventsFragment.isHoliday(context, event("Pfingstmontag")))
    }

    /** A null description collapses to the empty one, so both address the same key. */
    @Test
    fun anEventWithoutADescription_sharesTheFlagWithTheEmptyDescription() {
        AnnualEventsFragment.setHoliday(context, event(null), true)

        assertTrue(AnnualEventsFragment.isHoliday(context, event("")))
    }
}
