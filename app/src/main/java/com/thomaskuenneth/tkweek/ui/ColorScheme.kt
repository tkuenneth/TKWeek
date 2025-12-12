package com.thomaskuenneth.tkweek.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun colorScheme(): ColorScheme {
    val context = LocalContext.current
    val light = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        dynamicLightColorScheme(context) else lightColorScheme()
    val dark = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        dynamicDarkColorScheme(context) else darkColorScheme()
    return if (isSystemInDarkTheme()) dark else light
}
