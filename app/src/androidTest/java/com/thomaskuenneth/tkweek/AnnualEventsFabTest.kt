package com.thomaskuenneth.tkweek

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.thomaskuenneth.tkweek.fragment.AnnualEventsFragment
import com.thomaskuenneth.tkweek.fragment.BackupRestoreDialogFragment
import com.thomaskuenneth.tkweek.fragment.MyDayFragment
import com.thomaskuenneth.tkweek.fragment.NewEventFragment
import com.thomaskuenneth.tkweek.ui.TKWeekTestTags
import com.thomaskuenneth.tkweek.util.Helper
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val SCROLL_TIMEOUT_MILLIS = 10_000L
private const val FAB_MARGIN_DP = 16
private const val FAB_MARGIN_LARGE_WINDOW_DP = 24
private const val LARGE_WINDOW_WIDTH_DP = 1200
private const val MARGIN_TOLERANCE_PX = 2f

@LargeTest
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AnnualEventsFabTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        hiltRule.inject()
        grantMyDayRuntimePermissions(context.packageName)
    }

    private fun launch(clazz: Class<*>): ActivityScenario<TKWeekCompose> {
        val intent = Intent(context, TKWeekCompose::class.java)
            .putExtra(Helper.CLAZZ, clazz.name)
        return ActivityScenario.launch(intent)
    }

    private fun launchAnnualEvents() = launch(AnnualEventsFragment::class.java)

    private fun toggle() = composeRule.onNodeWithTag(TKWeekTestTags.FAB_MENU_TOGGLE)

    private fun newEventItem() =
        composeRule.onNodeWithTag(TKWeekTestTags.FAB_MENU_NEW_EVENT)

    private fun backupRestoreItem() =
        composeRule.onNodeWithTag(TKWeekTestTags.FAB_MENU_BACKUP_RESTORE)

    private fun openMenu() {
        toggle().performClick()
        composeRule.waitForIdle()
    }

    private fun assertMenuCollapsed() {
        newEventItem().assertIsNotDisplayed()
        backupRestoreItem().assertIsNotDisplayed()
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.more_options))
            .assertIsDisplayed()
    }

    private fun assertMenuExpanded() {
        newEventItem().assertIsDisplayed()
        backupRestoreItem().assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.hide_actions))
            .assertIsDisplayed()
    }

    private fun dialogOf(scenario: ActivityScenario<TKWeekCompose>): Any? {
        var dialog: Any? = null
        scenario.onActivity { dialog = it.supportFragmentManager.findFragmentByTag("dialog") }
        return dialog
    }

    private fun scrollViewOf(activity: TKWeekCompose): NestedScrollView =
        activity.findViewById(R.id.events_scroll)

    private fun View.bottomInWindow(): Int {
        val location = IntArray(2)
        getLocationInWindow(location)
        return location[1] + height
    }

    @Test
    fun menuStartsCollapsed_showingOnlyTheToggle() {
        launchAnnualEvents().use { scenario ->
            composeRule.waitForIdle()

            assertMenuCollapsed()
        }
    }

    @Test
    fun openingTheMenu_revealsBothActions() {
        launchAnnualEvents().use { scenario ->
            composeRule.waitForIdle()

            openMenu()

            assertMenuExpanded()
        }
    }

    @Test
    fun theMenuCollapsesFromTheToggleAndFromBack() {
        launchAnnualEvents().use { scenario ->
            composeRule.waitForIdle()

            openMenu()
            toggle().performClick()
            composeRule.waitForIdle()
            assertMenuCollapsed()

            openMenu()
            Espresso.pressBack()
            composeRule.waitForIdle()
            assertMenuCollapsed()
            assertEquals(
                "Back should collapse the menu, not leave the screen",
                Lifecycle.State.RESUMED,
                scenario.state
            )
        }
    }

    private fun ActivityScenario<TKWeekCompose>.isShowingTheFabMenu(): Boolean =
        state == Lifecycle.State.RESUMED &&
            composeRule.onAllNodesWithTag(TKWeekTestTags.FAB_MENU_TOGGLE)
                .fetchSemanticsNodes().isNotEmpty()

    @Test
    fun pressingBackWithTheMenuCollapsed_leavesTheEventsScreen() {
        launchAnnualEvents().use { scenario ->
            composeRule.waitForIdle()

            Espresso.pressBackUnconditionally()
            composeRule.waitForIdle()

            assertFalse(
                "Back must not be swallowed while the menu is collapsed",
                scenario.isShowingTheFabMenu()
            )
        }
    }

    @Test
    fun anOpenMenuSurvivesRecreation() {
        launchAnnualEvents().use { scenario ->
            composeRule.waitForIdle()
            openMenu()

            scenario.recreate()
            composeRule.waitForIdle()

            assertMenuExpanded()
        }
    }

    @Test
    fun newEventItem_opensTheDialogAndClosesTheMenu() {
        launchAnnualEvents().use { scenario ->
            composeRule.waitForIdle()
            openMenu()

            newEventItem().performClick()
            composeRule.waitForIdle()

            val dialog = dialogOf(scenario)
            assertTrue("Expected a NewEventFragment, found $dialog", dialog is NewEventFragment)
            assertMenuCollapsed()
        }
    }

    @Test
    fun backupRestoreItem_opensTheDialogAndClosesTheMenu() {
        launchAnnualEvents().use { scenario ->
            composeRule.waitForIdle()
            openMenu()

            backupRestoreItem().performClick()
            composeRule.waitForIdle()

            val dialog = dialogOf(scenario)
            assertTrue(
                "Expected a BackupRestoreDialogFragment, found $dialog",
                dialog is BackupRestoreDialogFragment
            )
            assertMenuCollapsed()
        }
    }

    private fun scrollToEnd(scenario: ActivityScenario<TKWeekCompose>) {
        composeRule.waitUntil(SCROLL_TIMEOUT_MILLIS) {
            var atEnd = false
            scenario.onActivity { activity ->
                val scroll = scrollViewOf(activity)
                scroll.fullScroll(View.FOCUS_DOWN)
                atEnd = !scroll.canScrollVertically(1)
            }
            atEnd
        }
    }

    private fun assertLastEventClears(
        scenario: ActivityScenario<TKWeekCompose>,
        top: Float,
        what: String
    ) {
        scenario.onActivity { activity ->
            val lastEventBottom = activity.findViewById<View>(R.id.listView).bottomInWindow()

            assertTrue(
                "The last event ends at $lastEventBottom, behind $what at $top",
                lastEventBottom <= top
            )
        }
    }

    private fun awaitScrollable(scenario: ActivityScenario<TKWeekCompose>) {
        composeRule.waitUntil(SCROLL_TIMEOUT_MILLIS) {
            var scrollable = false
            scenario.onActivity { scrollable = scrollViewOf(it).canScrollVertically(1) }
            scrollable
        }
    }

    private fun assertTheListClearsTheMenu(scenario: ActivityScenario<TKWeekCompose>) {
        awaitScrollable(scenario)
        scrollToEnd(scenario)
        assertLastEventClears(
            scenario,
            toggle().fetchSemanticsNode().boundsInWindow.top,
            "the closed FAB"
        )

        openMenu()
        scrollToEnd(scenario)
        assertLastEventClears(
            scenario,
            minOf(
                newEventItem().fetchSemanticsNode().boundsInWindow.top,
                backupRestoreItem().fetchSemanticsNode().boundsInWindow.top
            ),
            "the open menu"
        )
    }

    @Test
    fun theListCanAlwaysScrollClearOfTheMenu() {
        launchAnnualEvents().use { scenario ->
            composeRule.waitForIdle()

            assertTheListClearsTheMenu(scenario)
        }
    }

    @Test
    fun theMenuOverlayDoesNotSwallowScrolling() {
        launchAnnualEvents().use { scenario ->
            composeRule.waitForIdle()
            awaitScrollable(scenario)

            Espresso.onView(withId(R.id.events_scroll)).perform(
                GeneralSwipeAction(
                    Swipe.FAST, GeneralLocation.CENTER, GeneralLocation.TOP_CENTER, Press.FINGER
                )
            )
            composeRule.waitForIdle()

            scenario.onActivity { activity ->
                assertTrue(
                    "The menu overlay swallowed the swipe, the list never moved",
                    scrollViewOf(activity).scrollY > 0
                )
            }
        }
    }

    private fun assertTheToggleKeepsTheMaterialMargin(scenario: ActivityScenario<TKWeekCompose>) {
        val toggleBounds = toggle().fetchSemanticsNode().boundsInWindow
        scenario.onActivity { activity ->
            val root = activity.findViewById<View>(android.R.id.content)
            val insets = requireNotNull(ViewCompat.getRootWindowInsets(root))
                .getInsets(
                    WindowInsetsCompat.Type.systemBars() or
                        WindowInsetsCompat.Type.displayCutout()
                )
            val density = activity.resources.displayMetrics.density
            val marginDp = if (root.width / density >= LARGE_WINDOW_WIDTH_DP) {
                FAB_MARGIN_LARGE_WINDOW_DP
            } else {
                FAB_MARGIN_DP
            }
            val margin = marginDp * density

            assertEquals(
                "The toggle ends at ${toggleBounds.bottom}, not $margin px above the safe area",
                root.height - insets.bottom - margin,
                toggleBounds.bottom,
                MARGIN_TOLERANCE_PX
            )
            assertEquals(
                "The toggle ends at ${toggleBounds.right}, not $margin px inside the safe area",
                root.width - insets.right - margin,
                toggleBounds.right,
                MARGIN_TOLERANCE_PX
            )
        }
    }

    @Test
    fun theToggleKeepsTheMaterialMarginFromTheSafeArea() {
        launchAnnualEvents().use { scenario ->
            composeRule.waitForIdle()

            assertTheToggleKeepsTheMaterialMargin(scenario)
        }
    }

    @Test
    fun theMenuIsAbsentFromOtherModules() {
        launch(MyDayFragment::class.java).use {
            composeRule.waitForIdle()

            composeRule.onAllNodesWithTag(TKWeekTestTags.FAB_MENU_TOGGLE).assertCountEquals(0)
        }
    }

}
