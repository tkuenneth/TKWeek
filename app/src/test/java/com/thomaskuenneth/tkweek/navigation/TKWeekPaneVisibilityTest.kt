package com.thomaskuenneth.tkweek.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TKWeekPaneVisibilityTest {

    @Test
    fun twoPane_bothPanesAlwaysVisible_regardlessOfBackStackDepth() {
        for (backStackSize in 1..5) {
            assertTrue(isDetailPaneVisible(isTwoPane = true, backStackSize = backStackSize))
            assertTrue(isListPaneVisible(isTwoPane = true, backStackSize = backStackSize))
        }
    }

    @Test
    fun singlePane_onlyListVisible_whenNothingIsPushedYet() {
        assertFalse(isDetailPaneVisible(isTwoPane = false, backStackSize = 1))
        assertTrue(isListPaneVisible(isTwoPane = false, backStackSize = 1))
    }

    @Test
    fun singlePane_onlyDetailVisible_onceSomethingIsPushed() {
        assertTrue(isDetailPaneVisible(isTwoPane = false, backStackSize = 2))
        assertFalse(isListPaneVisible(isTwoPane = false, backStackSize = 2))

        assertTrue(isDetailPaneVisible(isTwoPane = false, backStackSize = 3))
        assertFalse(isListPaneVisible(isTwoPane = false, backStackSize = 3))
    }

    @Test
    fun canNavigateBack_singlePane_falseOnlyWhenOnlyTheModuleListRemains() {
        assertFalse(canNavigateBack(isTwoPane = false, backStackSize = 1))
        assertTrue(canNavigateBack(isTwoPane = false, backStackSize = 2))
        assertTrue(canNavigateBack(isTwoPane = false, backStackSize = 3))
    }

    @Test
    fun canNavigateBack_twoPane_falseUntilNestedBeyondTheTopLevelSelection() {
        assertFalse(canNavigateBack(isTwoPane = true, backStackSize = 1))
        assertFalse(canNavigateBack(isTwoPane = true, backStackSize = 2))
        assertTrue(canNavigateBack(isTwoPane = true, backStackSize = 3))
        assertTrue(canNavigateBack(isTwoPane = true, backStackSize = 4))
    }
}
