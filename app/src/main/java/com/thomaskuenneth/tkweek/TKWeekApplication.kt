package com.thomaskuenneth.tkweek

import android.app.Application
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
    }
}
