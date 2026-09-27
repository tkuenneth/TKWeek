package com.thomaskuenneth.tkweek

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.thomaskuenneth.tkweek.ui.TKWeekTestTags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ModuleListComposeTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<TKWeekCompose>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    /**
     * The title depends on the window, not just on the back stack: with room for two panes the
     * detail pane is filled with the default module straight away, so the bar names that module
     * rather than the app. Asserting the app name unconditionally only holds on a handset.
     */
    @Test
    fun launch_showsTheExpectedTitleAndPrimaryModules() {
        val weekTitle = composeRule.activity.getString(R.string.week_activity_text1)
        val expectedTitle = if (isTwoPane()) {
            weekTitle
        } else {
            composeRule.activity.getString(R.string.app_name)
        }

        composeRule.onNodeWithTag(TKWeekTestTags.TOP_APP_BAR_TITLE)
            .assertIsDisplayed()
            .assertTextEquals(expectedTitle)
        // By tag, not by text: in two-pane mode the module's name is also the bar's title, so
        // matching on text alone finds two nodes and the assertion becomes ambiguous.
        composeRule.onNodeWithTag(TKWeekTestTags.moduleListItem(TKWeekModule.Week.name))
            .assertIsDisplayed()
        composeRule.onNodeWithTag(TKWeekTestTags.moduleListItem(TKWeekModule.MyDay.name))
            .assertIsDisplayed()
    }

    /**
     * Mirrors what TKWeekCompose derives from the window: two panes once the window is wide
     * enough for the list-detail directive to allow more than one horizontal partition.
     */
    private fun isTwoPane(): Boolean {
        val configuration = composeRule.activity.resources.configuration
        return configuration.screenWidthDp >= TWO_PANE_MIN_WIDTH_DP
    }

    private companion object {
        /** calculatePaneScaffoldDirective splits into two panes from the expanded width class. */
        const val TWO_PANE_MIN_WIDTH_DP = 840
    }

    @Test
    fun selectingCalendar_updatesTopAppBarTitle() {
        val calendarTitle = composeRule.activity.getString(R.string.calendar_activity_text1)

        composeRule.onNodeWithText(calendarTitle).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TKWeekTestTags.TOP_APP_BAR_TITLE)
            .assertIsDisplayed()
            .assertTextEquals(calendarTitle)
    }
}
