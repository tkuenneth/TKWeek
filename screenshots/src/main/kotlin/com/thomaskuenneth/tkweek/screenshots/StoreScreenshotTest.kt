package com.thomaskuenneth.tkweek.screenshots

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.thomaskuenneth.tkweek.TKWeekCompose
import com.thomaskuenneth.tkweek.TKWeekModule
import com.thomaskuenneth.tkweek.viewmodel.TKWeekViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import java.io.File

private val SCREENSHOT_SEQUENCE = listOf(
    "module_list" to null,
    "week" to TKWeekModule.Week,
    "my_day" to TKWeekModule.MyDay,
    "days_between_dates" to TKWeekModule.DaysBetweenDates,
    "date_calculator" to TKWeekModule.DateCalculator,
    "events" to TKWeekModule.AnnualEvents,
    "about_a_year" to TKWeekModule.AboutAYear,
    "calendar" to TKWeekModule.Calendar,
    "settings" to TKWeekModule.Prefs,
    "about" to TKWeekModule.About,
)

@LargeTest
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StoreScreenshotTest {

    // Must run before composeRule launches the activity, so that TKWeekCompose's
    // one-shot enableEdgeToEdge() reads dark mode from the very first onCreate.
    @get:Rule(order = 0)
    val darkModeRule = object : ExternalResource() {
        override fun before() {
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                .executeShellCommand("cmd uimode night yes")
        }

        override fun after() {
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                .executeShellCommand("cmd uimode night no")
        }
    }

    @get:Rule(order = 1)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<TKWeekCompose>()

    private lateinit var viewModel: TKWeekViewModel
    private lateinit var uiDevice: UiDevice
    private lateinit var outputDir: File

    @Before
    fun setUp() {
        hiltRule.inject()
        grantMyDayRuntimePermissions(composeRule.activity.packageName)
        viewModel = ViewModelProvider(composeRule.activity)[TKWeekViewModel::class.java]
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        outputDir = File(composeRule.activity.getExternalFilesDir(null), "store-screenshots").apply {
            deleteRecursively()
            mkdirs()
        }
    }

    @Test
    fun captureAllScreens() {
        File(outputDir, "device-class.txt").writeText(detectDeviceClass())
        SCREENSHOT_SEQUENCE.forEachIndexed { index, (name, module) ->
            module?.let { selectTopLevel(it) }
            composeRule.waitForIdle()
            capture("%02d_%s.png".format(index, name))
        }
    }

    private fun detectDeviceClass(): String {
        val activity = composeRule.activity
        val layoutInfo = runBlocking {
            WindowInfoTracker.getOrCreate(activity).windowLayoutInfo(activity).first()
        }
        val hasHinge = layoutInfo.displayFeatures.any { it is FoldingFeature }
        return when {
            hasHinge -> "fold"
            activity.resources.configuration.smallestScreenWidthDp >= 600 -> "tablet"
            else -> "phone"
        }
    }

    private fun selectTopLevel(module: TKWeekModule) {
        composeRule.runOnUiThread {
            viewModel.requestNavigation(module = module, topLevel = true)
        }
        composeRule.waitForIdle()
    }

    private fun capture(fileName: String) {
        uiDevice.takeScreenshot(File(outputDir, fileName))
    }

    private fun grantMyDayRuntimePermissions(packageName: String) {
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
}
