package com.thomaskuenneth.tkweek

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.thomaskuenneth.tkweek.ui.TKWeekTestTags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertNotNull
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
    fun launch_showsNoAppBarAndThePrimaryModules() {
        composeRule.onNodeWithTag(TKWeekTestTags.TOP_APP_BAR_TITLE).assertDoesNotExist()

        composeRule.onNodeWithTag(TKWeekTestTags.moduleListItem(TKWeekModule.Week.name))
            .assertIsDisplayed()
        composeRule.onNodeWithTag(TKWeekTestTags.moduleListItem(TKWeekModule.MyDay.name))
            .assertIsDisplayed()
    }

    @Test
    fun selectingCalendar_showsThatModuleInTheDetailPane() {
        composeRule.onNodeWithTag(TKWeekTestTags.MODULE_LIST)
            .performScrollToIndex(TKWeekModule.entries.indexOf(TKWeekModule.Calendar))
        composeRule.onNodeWithTag(TKWeekTestTags.moduleListItem(TKWeekModule.Calendar.name))
            .performClick()
        composeRule.waitForIdle()

        assertNotNull(
            "Calendar is not the module in the detail pane",
            composeRule.activity.supportFragmentManager
                .findFragmentByTag(TKWeekModule.Calendar.clazz.name)
        )
    }
}
