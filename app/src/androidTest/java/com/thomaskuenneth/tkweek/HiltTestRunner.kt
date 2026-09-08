package com.thomaskuenneth.tkweek

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        val hiltAppName = "${TKWeekHiltTestApp::class.java.name}_Application"
        return super.newApplication(cl, hiltAppName, context)
    }
}
