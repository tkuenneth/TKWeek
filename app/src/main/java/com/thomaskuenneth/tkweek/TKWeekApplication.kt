package com.thomaskuenneth.tkweek

import android.app.Application
import com.google.android.material.color.DynamicColors

class TKWeekApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
