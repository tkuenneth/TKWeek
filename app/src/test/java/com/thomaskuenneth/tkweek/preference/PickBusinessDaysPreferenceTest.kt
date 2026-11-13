package com.thomaskuenneth.tkweek.preference

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceDataStore
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import java.util.Calendar
import androidx.preference.PreferenceManager as AndroidXPreferenceManager

@RunWith(RobolectricTestRunner::class)
class PickBusinessDaysPreferenceTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val storedByEarlierVersions = context.getSharedPreferences(
        "PickBusinessDaysPreference", Context.MODE_PRIVATE
    )

    private val preference
        get() = PickBusinessDaysPreference(context, Robolectric.buildAttributeSet().build())

    private val dialog: PreferenceDataStore
        get() = preference.preferenceDataStore!!

    private val workingWeek = setOf(
        Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY
    )

    @Test
    fun withNothingStored_theWorkingWeekCounts() {
        assertEquals(workingWeek, PickBusinessDaysPreference.getBusinessDays(context))
    }

    @Test
    fun theDaysStoredByEarlierVersionsAreStillHonoured() {
        storedByEarlierVersions.edit {
            putBoolean(Calendar.SATURDAY.toString(), true)
            putBoolean(Calendar.MONDAY.toString(), false)
        }

        assertEquals(
            workingWeek - Calendar.MONDAY + Calendar.SATURDAY,
            PickBusinessDaysPreference.getBusinessDays(context)
        )
    }

    @Test
    fun whatTheDialogSavesIsWhatTheModulesRead() {
        dialog.putStringSet("business_days", setOf("${Calendar.SUNDAY}", "${Calendar.SATURDAY}"))

        assertEquals(
            setOf(Calendar.SUNDAY, Calendar.SATURDAY),
            PickBusinessDaysPreference.getBusinessDays(context)
        )
    }

    @Test
    fun clearingEveryDayLeavesNoBusinessDay() {
        dialog.putStringSet("business_days", emptySet())

        assertTrue(PickBusinessDaysPreference.getBusinessDays(context).isEmpty())
    }

    @Test
    fun theDialogOpensOnTheDaysThatCount() {
        storedByEarlierVersions.edit { putBoolean(Calendar.SUNDAY.toString(), true) }

        assertEquals(
            (workingWeek + Calendar.SUNDAY).map(Int::toString).toSet(),
            dialog.getStringSet("business_days", null)
        )
    }

    @Test
    fun theDialogListsTheWholeWeekStartingOnItsFirstDay() {
        val listed = preference.entryValues.map(CharSequence::toString)

        assertEquals("${Calendar.getInstance().firstDayOfWeek}", listed.first())
        assertEquals(
            (Calendar.SUNDAY..Calendar.SATURDAY).map(Int::toString).toSet(), listed.toSet()
        )
    }

    @Test
    fun theDaysStayOutOfTheDefaultPreferences() {
        dialog.putStringSet("business_days", setOf("${Calendar.SUNDAY}"))

        assertFalse(
            AndroidXPreferenceManager.getDefaultSharedPreferences(context)
                .contains("business_days")
        )
    }
}
