package com.thomaskuenneth.tkweek

import android.content.Intent
import android.widget.ListView
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.thomaskuenneth.tkweek.fragment.AnnualEventsFragment
import com.thomaskuenneth.tkweek.fragment.MyDayFragment
import com.thomaskuenneth.tkweek.fragment.WeekFragment
import com.thomaskuenneth.tkweek.util.Helper
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anything
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
        grantMyDayRuntimePermissions(
            ApplicationProvider.getApplicationContext<android.content.Context>().packageName
        )
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

    @Test
    fun onNewIntentWithDifferentTarget_navigatesToNewTopLevelModule() {
        launchModule(WeekFragment::class.java).use { scenario ->
            composeRule.waitForIdle()

            var activity: TKWeekCompose? = null
            scenario.onActivity { activity = it }
            val newIntent = Intent(
                ApplicationProvider.getApplicationContext(),
                TKWeekCompose::class.java
            ).putExtra(Helper.CLAZZ, MyDayFragment::class.java.name)
            InstrumentationRegistry.getInstrumentation()
                .callActivityOnNewIntent(requireNotNull(activity), newIntent)
            composeRule.waitForIdle()

            onView(withId(R.id.my_day_today))
                .check(matches(allOf(isDisplayed(), withText(R.string.today))))
        }
    }

    @Test
    fun eventsListItemClick_navigatesToMyDay() {
        launchModule(AnnualEventsFragment::class.java).use { scenario ->
            composeRule.waitForIdle()

            var activity: TKWeekCompose? = null
            scenario.onActivity { activity = it }
            val nonNullActivity = requireNotNull(activity)
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.runOnUiThread {
                    (nonNullActivity.findViewById<ListView>(R.id.listView).adapter?.count
                        ?: 0) > 0
                }
            }

            onData(anything())
                .inAdapterView(withId(R.id.listView))
                .atPosition(0)
                .perform(click())
            composeRule.waitForIdle()

            onView(withId(R.id.my_day_today))
                .check(matches(allOf(isDisplayed(), withText(R.string.today))))
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
