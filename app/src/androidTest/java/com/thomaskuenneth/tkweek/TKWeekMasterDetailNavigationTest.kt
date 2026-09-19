package com.thomaskuenneth.tkweek

import android.content.pm.ActivityInfo
import androidx.activity.compose.setContent
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.WindowSize
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoActivityResumedException
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.thomaskuenneth.tkweek.ui.TKWeekTestTags
import com.thomaskuenneth.tkweek.viewmodel.TKWeekViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private val COMPACT_WINDOW = DpSize(400.dp, 900.dp)
private val EXPANDED_WINDOW = DpSize(1000.dp, 500.dp)

@LargeTest
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TKWeekMasterDetailNavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<TKWeekCompose>()

    private lateinit var viewModel: TKWeekViewModel

    @Before
    fun setUp() {
        hiltRule.inject()
        viewModel = ViewModelProvider(composeRule.activity)[TKWeekViewModel::class.java]
        grantMyDayRuntimePermissions(composeRule.activity.packageName)
    }

    @After
    fun tearDown() {
        if (composeRule.activityRule.scenario.state != Lifecycle.State.DESTROYED) {
            composeRule.activityRule.scenario.onActivity {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    private fun titleOf(module: TKWeekModule): String =
        composeRule.activity.getString(module.titleRes)

    private fun forceLandscape() {
        composeRule.activityRule.scenario.onActivity {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        composeRule.waitForIdle()
    }

    private fun setContentAt(size: DpSize) {
        composeRule.activity.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.WindowSize(size)) {
                TKWeekApp()
            }
        }
        composeRule.waitForIdle()
    }

    private fun awaitModuleListItem(module: TKWeekModule) {
        val index = TKWeekModule.entries.indexOf(module)
        composeRule.onNodeWithTag(TKWeekTestTags.MODULE_LIST).performScrollToIndex(index)
        composeRule.waitForIdle()
    }

    private fun selectTopLevel(module: TKWeekModule) {
        awaitModuleListItem(module)
        composeRule.onNodeWithTag(TKWeekTestTags.moduleListItem(module.name)).performClick()
        composeRule.waitForIdle()
    }

    private fun simulateNestedNavigation(module: TKWeekModule, date: Long? = null) {
        composeRule.runOnUiThread {
            viewModel.requestNavigation(module = module, date = date, topLevel = false)
        }
        composeRule.waitForIdle()
    }

    private fun pressSystemBack() {
        composeRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun backArrowNode() =
        composeRule.onNodeWithContentDescription(composeRule.activity.getString(R.string.navigate_up))

    @Test
    fun singlePane_tappingAModule_opensDetailAndHidesList() {
        setContentAt(COMPACT_WINDOW)

        val calendarTitle = titleOf(TKWeekModule.Calendar)
        composeRule.onNodeWithText(calendarTitle).assertIsDisplayed()

        selectTopLevel(TKWeekModule.Calendar)

        composeRule.onNodeWithTag(TKWeekTestTags.TOP_APP_BAR_TITLE).assertTextEquals(calendarTitle)
        composeRule.onNodeWithTag(TKWeekTestTags.moduleListItem(TKWeekModule.Calendar.name))
            .assertDoesNotExist()
    }

    @Test
    fun singlePane_backButton_returnsFromDetailToList() {
        setContentAt(COMPACT_WINDOW)
        selectTopLevel(TKWeekModule.Calendar)

        pressSystemBack()

        composeRule.onNodeWithTag(TKWeekTestTags.moduleListItem(TKWeekModule.Calendar.name))
            .assertIsDisplayed()
    }

    @Test
    fun twoPane_showsListAndPreselectedDetailSideBySide() {
        setContentAt(EXPANDED_WINDOW)

        val weekTitle = titleOf(TKWeekModule.Week)
        val calendarTitle = titleOf(TKWeekModule.Calendar)

        composeRule.onNodeWithTag(TKWeekTestTags.TOP_APP_BAR_TITLE).assertTextEquals(weekTitle)
        awaitModuleListItem(TKWeekModule.Calendar)
        composeRule.onNodeWithTag(TKWeekTestTags.moduleListItem(TKWeekModule.Calendar.name))
            .assertIsDisplayed()
        composeRule.onNodeWithText(calendarTitle).assertIsDisplayed()
    }

    @Test
    fun twoPane_reselectingModuleAlreadyActiveWhileNested_returnsToItsRoot() {
        setContentAt(EXPANDED_WINDOW)

        selectTopLevel(TKWeekModule.Calendar)
        simulateNestedNavigation(TKWeekModule.MyDay, date = System.currentTimeMillis())

        val myDayTitle = titleOf(TKWeekModule.MyDay)
        val calendarTitle = titleOf(TKWeekModule.Calendar)
        composeRule.onNodeWithTag(TKWeekTestTags.TOP_APP_BAR_TITLE).assertTextEquals(myDayTitle)

        selectTopLevel(TKWeekModule.Calendar)

        composeRule.onNodeWithTag(TKWeekTestTags.TOP_APP_BAR_TITLE).assertTextEquals(calendarTitle)
    }

    @Test
    fun twoPane_topLevelSelection_hasNoBackArrow() {
        setContentAt(EXPANDED_WINDOW)

        selectTopLevel(TKWeekModule.Calendar)

        backArrowNode().assertDoesNotExist()
    }

    @Test
    fun twoPane_systemBack_exitsWhenNothingBeyondTopLevelSelectionToPop() {
        setContentAt(EXPANDED_WINDOW)
        selectTopLevel(TKWeekModule.Calendar)

        assertThrows(NoActivityResumedException::class.java) { Espresso.pressBack() }
    }

    @Test
    fun singlePane_systemBack_exitsAtModuleListRoot() {
        setContentAt(COMPACT_WINDOW)

        assertThrows(NoActivityResumedException::class.java) { Espresso.pressBack() }
    }

    @Test
    fun twoPane_nestedNavigation_hasBackArrowAndReturnsToTopLevelSelection() {
        setContentAt(EXPANDED_WINDOW)

        selectTopLevel(TKWeekModule.Calendar)
        simulateNestedNavigation(TKWeekModule.MyDay, date = System.currentTimeMillis())
        val myDayTitle = titleOf(TKWeekModule.MyDay)
        val calendarTitle = titleOf(TKWeekModule.Calendar)
        composeRule.onNodeWithTag(TKWeekTestTags.TOP_APP_BAR_TITLE).assertTextEquals(myDayTitle)
        backArrowNode().assertIsDisplayed()

        pressSystemBack()

        composeRule.onNodeWithTag(TKWeekTestTags.TOP_APP_BAR_TITLE).assertTextEquals(calendarTitle)
    }

    @Test
    fun activityRecreation_preservesNestedNavigationState() {
        selectTopLevel(TKWeekModule.Calendar)
        simulateNestedNavigation(TKWeekModule.MyDay, date = 12345L)
        val myDayTitle = titleOf(TKWeekModule.MyDay)
        composeRule.onNodeWithTag(TKWeekTestTags.TOP_APP_BAR_TITLE).assertTextEquals(myDayTitle)

        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TKWeekTestTags.TOP_APP_BAR_TITLE).assertTextEquals(myDayTitle)
    }

    @Test
    fun activityRecreation_restoredBackStackStillAcceptsNewNavigation() {
        selectTopLevel(TKWeekModule.Calendar)
        simulateNestedNavigation(TKWeekModule.MyDay)

        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        viewModel = ViewModelProvider(composeRule.activity)[TKWeekViewModel::class.java]

        val dateCalculatorTitle = titleOf(TKWeekModule.DateCalculator)
        composeRule.runOnUiThread {
            viewModel.requestNavigation(module = TKWeekModule.DateCalculator, topLevel = true)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TKWeekTestTags.TOP_APP_BAR_TITLE).assertTextEquals(dateCalculatorTitle)
    }

    @Test
    fun activityRecreation_whileTwoPane_reselectingModuleAlreadyActiveWhileNested_returnsToItsRoot() {
        forceLandscape()

        selectTopLevel(TKWeekModule.Calendar)
        simulateNestedNavigation(TKWeekModule.MyDay, date = System.currentTimeMillis())
        val myDayTitle = titleOf(TKWeekModule.MyDay)
        composeRule.onNodeWithTag(TKWeekTestTags.TOP_APP_BAR_TITLE).assertTextEquals(myDayTitle)

        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TKWeekTestTags.TOP_APP_BAR_TITLE).assertTextEquals(myDayTitle)

        selectTopLevel(TKWeekModule.Calendar)

        val calendarTitle = titleOf(TKWeekModule.Calendar)
        composeRule.onNodeWithTag(TKWeekTestTags.TOP_APP_BAR_TITLE).assertTextEquals(calendarTitle)
    }
}
