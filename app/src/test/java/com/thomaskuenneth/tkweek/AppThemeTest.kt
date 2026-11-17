package com.thomaskuenneth.tkweek

import android.content.Context
import android.util.TypedValue
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppThemeTest {

    @Test
    fun theAppTheme_bringsTheExpressiveComponentStyles() {
        listOf(
            com.google.android.material.R.attr.sliderStyle,
            com.google.android.material.R.attr.materialButtonOutlinedStyle,
            androidx.appcompat.R.attr.toolbarStyle,
            androidx.appcompat.R.attr.buttonBarButtonStyle,
        ).forEach { attr ->
            val style = styleOf(themed(R.style.AppTheme), attr)
            assertTrue(style, style.startsWith("Widget.Material3Expressive"))
        }
    }

    @Test
    fun theWidgetTheme_takesItsDynamicColoursFromExpressiveToo() {
        val overlay = styleOf(
            themed(R.style.TKWeekWidgetTheme),
            com.google.android.material.R.attr.dynamicColorThemeOverlay,
        )

        assertTrue(overlay, overlay.startsWith("ThemeOverlay.Material3Expressive"))
    }

    private fun themed(theme: Int): Context =
        ContextThemeWrapper(ApplicationProvider.getApplicationContext(), theme)

    private fun styleOf(context: Context, attr: Int): String {
        val value = TypedValue()
        context.theme.resolveAttribute(attr, value, true)
        return context.resources.getResourceEntryName(value.resourceId)
    }
}
