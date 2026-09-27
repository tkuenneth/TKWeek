package com.thomaskuenneth.tkweek

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.thomaskuenneth.tkweek.fragment.MyDayFragment
import com.thomaskuenneth.tkweek.ui.TKWeekTestTags
import com.thomaskuenneth.tkweek.util.Helper
import com.thomaskuenneth.tkweek.viewmodel.MyDayViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The two assist chips above My Day's content.
 *
 * They replaced the module's overflow menu entries, so what has to hold is that both are
 * visible without opening anything and that each hands off the right intent. The calendar chip
 * additionally has to carry the day being shown - dropping that extra is the failure that looks
 * fine on screen and then creates the appointment on the wrong date.
 *
 * Every intent leaving the app is stubbed, so the assertions are about what TKWeek *sends*.
 * Without that the tests would need a browser and a calendar installed and configured, and
 * would hand the foreground to whichever app answered.
 */
@LargeTest
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MyDayActionChipsTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    private val chosenDate = 1_234_567_890_000L

    @Before
    fun setUp() {
        hiltRule.inject()
        grantMyDayRuntimePermissions(
            ApplicationProvider.getApplicationContext<Context>().packageName
        )
        Intents.init()
        // Nothing may actually leave the app: the device need not have a browser or a calendar,
        // and a real hand-off would take the foreground away from the test.
        Intents.intending(not(isInternal()))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    private fun launchMyDay(): ActivityScenario<TKWeekCompose> {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            TKWeekCompose::class.java
        ).putExtra(Helper.CLAZZ, MyDayFragment::class.java.name)
        return ActivityScenario.launch(intent)
    }

    /** The day shown is owned by the view model, so that is where the test sets it. */
    private fun ActivityScenario<TKWeekCompose>.showDay(time: Long) {
        onActivity { activity ->
            ViewModelProvider(activity)[MyDayViewModel::class.java].setCalendarTime(time)
        }
        composeRule.waitForIdle()
    }

    @Test
    fun bothChips_areVisibleWithoutOpeningAMenu() {
        launchMyDay().use {
            composeRule.waitForIdle()

            onView(withId(R.id.my_day_chip_new_appointment))
                .check(matches(allOf(isDisplayed(), withText(R.string.new_appointment))))
            onView(withId(R.id.my_day_chip_wikipedia))
                .check(matches(allOf(isDisplayed(), withText(R.string.look_up_in_wikipedia))))
        }
    }

    /**
     * The chips replaced the menu rather than joining it: My Day contributes no app bar action,
     * so the overflow button must not be there at all.
     */
    @Test
    fun myDay_contributesNoOverflowMenu() {
        launchMyDay().use {
            composeRule.waitForIdle()

            composeRule.onNodeWithTag(TKWeekTestTags.APP_BAR_OVERFLOW).assertDoesNotExist()
        }
    }

    @Test
    fun newAppointmentChip_asksTheCalendarToInsertTheDayOnShow() {
        launchMyDay().use { scenario ->
            composeRule.waitForIdle()
            scenario.showDay(chosenDate)

            onView(withId(R.id.my_day_chip_new_appointment)).perform(click())

            Intents.intended(
                allOf(
                    hasAction(Intent.ACTION_INSERT),
                    hasExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, chosenDate)
                )
            )
        }
    }

    @Test
    fun wikipediaChip_leavesTheAppWithAViewIntent() {
        launchMyDay().use {
            composeRule.waitForIdle()

            onView(withId(R.id.my_day_chip_wikipedia)).perform(click())

            Intents.intended(hasAction(Intent.ACTION_VIEW))
        }
    }
}
