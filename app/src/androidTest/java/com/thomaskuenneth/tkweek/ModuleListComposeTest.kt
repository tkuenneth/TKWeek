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

    @Test
    fun launch_showsAppNameInTopAppBarAndPrimaryModules() {
        val appName = composeRule.activity.getString(R.string.app_name)

        composeRule.onNodeWithTag(TKWeekTestTags.TOP_APP_BAR_TITLE)
            .assertIsDisplayed()
            .assertTextEquals(appName)
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.week_activity_text1))
            .assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.myday_activity_text1))
            .assertIsDisplayed()
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
