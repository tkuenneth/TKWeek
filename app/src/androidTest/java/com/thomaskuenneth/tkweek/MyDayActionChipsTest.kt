package com.thomaskuenneth.tkweek

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.provider.CalendarContract
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.core.graphics.ColorUtils
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
import androidx.appcompat.view.ContextThemeWrapper
import com.google.android.material.chip.Chip
import com.thomaskuenneth.tkweek.fragment.MyDayFragment
import com.thomaskuenneth.tkweek.util.Helper
import com.thomaskuenneth.tkweek.viewmodel.MyDayViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val MIN_GRAPHICAL_CONTRAST = 3.0

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
        Intents.intending(
            allOf(
                not(isInternal()),
                anyOf(hasAction(Intent.ACTION_INSERT), hasAction(Intent.ACTION_VIEW))
            )
        ).respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))
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

    private fun ActivityScenario<TKWeekCompose>.showDay(time: Long) {
        onActivity { activity ->
            ViewModelProvider(activity)[MyDayViewModel::class.java].setCalendarTime(time)
        }
        composeRule.waitForIdle()
    }

    @Test
    fun bothChips_areVisibleWithoutOpeningAMenu() {
        launchMyDay().use { scenario ->
            composeRule.waitForIdle()

            onView(withId(R.id.my_day_chip_new_appointment))
                .check(matches(allOf(isDisplayed(), withText(R.string.new_appointment))))
            onView(withId(R.id.my_day_chip_wikipedia))
                .check(matches(allOf(isDisplayed(), withText(R.string.look_up_in_wikipedia))))

            scenario.onActivity { activity ->
                listOf(R.id.my_day_chip_new_appointment, R.id.my_day_chip_wikipedia).forEach { id ->
                    val chip = activity.findViewById<Chip>(id)
                    assertNotNull(
                        "${activity.resources.getResourceEntryName(id)} would draw its icon " +
                            "in the colour baked into the vector",
                        chip.chipIconTint
                    )
                }
            }
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

    @Test
    fun chipIconsStayLegibleInBothThemes() {
        listOf(
            "light" to Configuration.UI_MODE_NIGHT_NO,
            "dark" to Configuration.UI_MODE_NIGHT_YES
        ).forEach { (name, nightMode) ->
            val contrast = chipIconContrast(nightMode)
            assertTrue(
                "Chip icons only reach a contrast of $contrast against the $name background",
                contrast >= MIN_GRAPHICAL_CONTRAST
            )
        }
    }

    private fun chipIconContrast(nightMode: Int): Double {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val configuration = Configuration(context.resources.configuration).apply {
            uiMode = nightMode or (uiMode and Configuration.UI_MODE_TYPE_MASK)
        }
        val themed = ContextThemeWrapper(
            context.createConfigurationContext(configuration), R.style.AppTheme
        )
        val icon = themed.themeColor(androidx.appcompat.R.attr.colorPrimary)
        val background = themed.themeColor(com.google.android.material.R.attr.colorSurface)
        return ColorUtils.calculateContrast(icon, background)
    }

    private fun Context.themeColor(attribute: Int): Int {
        val attributes = obtainStyledAttributes(intArrayOf(attribute))
        try {
            return attributes.getColor(0, 0)
        } finally {
            attributes.recycle()
        }
    }
}
