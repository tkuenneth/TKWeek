/*
 * StartActivityCatchingTest.kt
 *
 * Copyright 2026 Thomas Künneth
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.thomaskuenneth.tkweek.util

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import com.thomaskuenneth.tkweek.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
class StartActivityCatchingTest {

    private lateinit var application: Application
    private val intent = Intent(Intent.ACTION_VIEW, "https://example.com".toUri())

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        shadowOf(application).checkActivities(true)
    }

    @Test
    fun startsTheActivityWhenSomethingHandlesTheIntent() {
        val browser = ComponentName("com.example.browser", "BrowserActivity")
        shadowOf(application.packageManager).addActivityIfNotPresent(browser)
        shadowOf(application.packageManager).addIntentFilterForActivity(
            browser,
            IntentFilter(Intent.ACTION_VIEW).apply {
                addDataScheme("https")
                addCategory(Intent.CATEGORY_DEFAULT)
            }
        )

        Helper.startActivityCatching(application, intent)

        assertEquals(intent.data, shadowOf(application).nextStartedActivity?.data)
        assertNull(ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun showsAMessageInsteadOfCrashingWhenNothingHandlesTheIntent() {
        Helper.startActivityCatching(application, intent)

        assertEquals(
            application.getString(R.string.something_went_wrong),
            ShadowToast.getTextOfLatestToast()
        )
    }

    @Test
    fun showsTheCallersOwnMessageWhenOneIsGiven() {
        Helper.startActivityCatching(application, intent, R.string.error_call_log)

        assertEquals(
            application.getString(R.string.error_call_log),
            ShadowToast.getTextOfLatestToast()
        )
    }
}
