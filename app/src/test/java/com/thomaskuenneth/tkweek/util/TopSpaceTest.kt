package com.thomaskuenneth.tkweek.util

import android.content.Context
import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ApplicationProvider
import com.thomaskuenneth.tkweek.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val STATUS_BAR_HEIGHT = 100

@RunWith(RobolectricTestRunner::class)
class TopSpaceTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val contentPadding =
        context.resources.getDimensionPixelSize(R.dimen.content_padding)

    private fun heightWithStatusBar(statusBarHeight: Int): Int {
        val space = TopSpace(context, null)
        ViewCompat.dispatchApplyWindowInsets(
            space,
            WindowInsetsCompat.Builder()
                .setInsets(
                    WindowInsetsCompat.Type.statusBars(),
                    Insets.of(0, statusBarHeight, 0, 0)
                )
                .build()
        )
        space.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        return space.measuredHeight
    }

    @Test
    fun theMarginStaysBelowTheStatusBar() {
        assertEquals(STATUS_BAR_HEIGHT + contentPadding, heightWithStatusBar(STATUS_BAR_HEIGHT))
    }

    @Test
    fun theMarginRemainsWhenAnAppBarHasConsumedTheStatusBar() {
        assertEquals(contentPadding, heightWithStatusBar(0))
    }
}
