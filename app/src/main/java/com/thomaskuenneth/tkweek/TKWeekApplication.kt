package com.thomaskuenneth.tkweek

import android.app.Application
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import net.time4j.android.ApplicationStarter

@HiltAndroidApp
class TKWeekApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        ApplicationStarter.initialize(this, true)
        BootCompleteReceiver.startAlarm(this, true)
        // Clean up some settings from older versions
        val prefs =
            getSharedPreferences("PickCountriesPreference", MODE_PRIVATE)
        prefs.edit {
            prefs.all.forEach { (key: String?, _: Any?) -> remove(key) }
        }
        PreferenceManager.getDefaultSharedPreferences(this).edit {
            remove("hide_nameday")
        }
    }
}
