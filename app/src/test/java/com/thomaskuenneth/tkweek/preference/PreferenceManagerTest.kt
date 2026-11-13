package com.thomaskuenneth.tkweek.preference

import android.content.Context
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.thomaskuenneth.tkweek.R
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import androidx.preference.PreferenceManager as AndroidXPreferenceManager

@RunWith(RobolectricTestRunner::class)
class PreferenceManagerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val sharedPreferences = AndroidXPreferenceManager.getDefaultSharedPreferences(context)

    @Test
    fun avoidHinge_isOnUntilTheUserTurnsItOff() = runTest {
        PreferenceManager(context).avoidHinge.test {
            assertEquals(true, awaitItem())

            sharedPreferences.edit { putBoolean(PreferenceManager.AVOID_HINGE, false) }
            assertEquals(false, awaitItem())

            sharedPreferences.edit { putBoolean(PreferenceManager.AVOID_HINGE, true) }
            assertEquals(true, awaitItem())
        }
    }

    @Test
    fun theSettingsScreenStartsWithTheSameDefault() {
        AndroidXPreferenceManager.setDefaultValues(context, R.xml.tkweek_preferences, true)

        assertEquals(
            PreferenceManager.AVOID_HINGE_DEFAULT,
            sharedPreferences.getBoolean(PreferenceManager.AVOID_HINGE, false)
        )
    }
}
