package com.thomaskuenneth.tkweek

import android.Manifest
import android.content.Intent
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.thomaskuenneth.tkweek.fragment.MyDayFragment
import com.thomaskuenneth.tkweek.fragment.WeekFragment
import com.thomaskuenneth.tkweek.util.Helper
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ViewModuleEspressoTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    @Before
    fun setUp() {
        hiltRule.inject()
        grantMyDayRuntimePermissions()
    }

    @Test
    fun myDayDeepLink_showsTodayButton() {
        launchModule(MyDayFragment::class.java).use {
            composeRule.waitForIdle()

            onView(withId(R.id.my_day_today))
                .check(matches(allOf(isDisplayed(), withText(R.string.today))))
        }
    }

    @Test
    fun weekDeepLink_showsWeekNumberLabel() {
        launchModule(WeekFragment::class.java).use {
            composeRule.waitForIdle()

            onView(withId(R.id.label_week_number))
                .check(matches(allOf(isDisplayed(), withText(R.string.week_number))))
        }
    }

    private fun grantMyDayRuntimePermissions() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val packageName = ApplicationProvider.getApplicationContext<android.content.Context>().packageName
        listOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.READ_CALL_LOG,
        ).forEach { permission ->
            instrumentation.uiAutomation.grantRuntimePermission(packageName, permission)
        }
    }

    private fun launchModule(fragmentClass: Class<*>): ActivityScenario<TKWeekCompose> {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            TKWeekCompose::class.java
        ).putExtra(Helper.CLAZZ, fragmentClass.name)
        return ActivityScenario.launch(intent)
    }
}
