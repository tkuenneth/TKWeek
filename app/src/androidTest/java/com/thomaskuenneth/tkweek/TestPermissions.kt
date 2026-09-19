package com.thomaskuenneth.tkweek

import android.Manifest
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry

fun grantMyDayRuntimePermissions(packageName: String) {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val permissions = buildList {
        add(Manifest.permission.READ_CONTACTS)
        add(Manifest.permission.READ_CALENDAR)
        add(Manifest.permission.READ_CALL_LOG)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    permissions.forEach { permission ->
        instrumentation.uiAutomation.grantRuntimePermission(packageName, permission)
    }
}
