package com.thomaskuenneth.tkweek.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.viewmodel.AppBarAction
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Behaviour of the app bar's action area.
 *
 * An action carrying an icon belongs in the bar; one without has nothing to draw there and
 * therefore has to be reachable through the overflow menu; a hidden action must be reachable
 * from neither. The two kinds can appear together, and selecting a menu entry has to close
 * the menu as well as run the action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@MediumTest
@RunWith(AndroidJUnit4::class)
class TKWeekAppBarActionsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val overflowLabel = context.getString(R.string.more_options)
    private val iconLabel = context.getString(R.string.today)
    private val otherIconLabel = context.getString(R.string.delete)
    private val textLabel = context.getString(R.string.settings)
    private val otherTextLabel = context.getString(R.string.new_appointment)

    private fun setActions(actions: List<AppBarAction>) {
        composeRule.setContent {
            Box {
                TopAppBar(
                    title = { Text("TKWeek") },
                    actions = { TKWeekAppBarActions(appBarActions = actions) }
                )
            }
        }
    }

    private fun overflowButton() = composeRule.onNodeWithTag(TKWeekTestTags.APP_BAR_OVERFLOW)

    @Test
    fun iconAction_isShownInTheBarAndInvokesItsClick() {
        var clicks = 0
        setActions(listOf(iconAction(R.string.today) { clicks++ }))

        val action = composeRule.onNodeWithContentDescription(iconLabel)
        action.assertIsDisplayed()
        action.performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun actionWithoutIcon_isReachableOnlyThroughTheOverflowMenu() {
        var clicks = 0
        setActions(listOf(textAction(R.string.settings) { clicks++ }))

        // Nothing to draw in the bar, so it must not be there before the menu is opened.
        composeRule.onNodeWithText(textLabel).assertDoesNotExist()

        overflowButton().performClick()
        composeRule.onNodeWithText(textLabel).assertIsDisplayed()
        composeRule.onNodeWithText(textLabel).performClick()

        assertEquals(1, clicks)
    }

    /** The menu closes itself on selection; without that it stays over the content. */
    @Test
    fun selectingAMenuEntry_closesTheMenu() {
        setActions(listOf(textAction(R.string.settings)))

        overflowButton().performClick()
        composeRule.onNodeWithText(textLabel).assertIsDisplayed()
        composeRule.onNodeWithText(textLabel).performClick()

        composeRule.onNodeWithText(textLabel).assertDoesNotExist()
    }

    @Test
    fun severalActionsWithoutIcons_allAppearInTheMenu() {
        setActions(
            listOf(
                textAction(R.string.settings),
                textAction(R.string.new_appointment),
            )
        )

        overflowButton().performClick()

        composeRule.onNodeWithText(textLabel).assertIsDisplayed()
        composeRule.onNodeWithText(otherTextLabel).assertIsDisplayed()
    }

    @Test
    fun iconAndTextActionsTogether_appearInTheirOwnPlaces() {
        var iconClicks = 0
        var textClicks = 0
        setActions(
            listOf(
                iconAction(R.string.today) { iconClicks++ },
                textAction(R.string.settings) { textClicks++ },
            )
        )

        composeRule.onNodeWithContentDescription(iconLabel).performClick()
        overflowButton().performClick()
        composeRule.onNodeWithText(textLabel).performClick()

        assertEquals(1, iconClicks)
        assertEquals(1, textClicks)
    }

    @Test
    fun invisibleIconAction_isNotReachableAtAll() {
        setActions(listOf(iconAction(R.string.today, isVisible = false)))

        composeRule.onNodeWithContentDescription(iconLabel).assertDoesNotExist()
        overflowButton().assertDoesNotExist()
    }

    @Test
    fun invisibleTextAction_leavesNoEmptyOverflowMenu() {
        setActions(listOf(textAction(R.string.settings, isVisible = false)))

        overflowButton().assertDoesNotExist()
    }

    @Test
    fun hidingOneOfTwoTextActions_keepsOnlyTheVisibleOne() {
        setActions(
            listOf(
                textAction(R.string.settings, isVisible = false),
                textAction(R.string.new_appointment),
            )
        )

        overflowButton().performClick()

        composeRule.onNodeWithText(textLabel).assertDoesNotExist()
        composeRule.onNodeWithText(otherTextLabel).assertIsDisplayed()
    }

    @Test
    fun noVisibleActions_showNoOverflowIndicator() {
        setActions(emptyList())

        overflowButton().assertDoesNotExist()
    }

    @Test
    fun overflowIndicator_isLabelledForAccessibility() {
        setActions(listOf(textAction(R.string.settings)))

        composeRule.onNodeWithContentDescription(overflowLabel).assertIsDisplayed()
    }

    @Test
    fun iconAction_carriesItsContentDescription() {
        setActions(listOf(iconAction(R.string.delete)))

        composeRule.onNodeWithContentDescription(otherIconLabel).assertIsDisplayed()
    }

    private fun iconAction(
        titleRes: Int,
        isVisible: Boolean = true,
        onClick: () -> Unit = {},
    ) = AppBarAction(
        title = titleRes,
        icon = R.drawable.ic_baseline_add_24,
        contentDescription = titleRes,
        onClick = onClick,
        isVisible = isVisible,
    )

    private fun textAction(
        titleRes: Int,
        isVisible: Boolean = true,
        onClick: () -> Unit = {},
    ) = AppBarAction(
        title = titleRes,
        icon = null,
        onClick = onClick,
        isVisible = isVisible,
    )
}
