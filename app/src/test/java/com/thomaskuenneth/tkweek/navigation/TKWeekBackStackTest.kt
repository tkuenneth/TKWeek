package com.thomaskuenneth.tkweek.navigation

import androidx.navigation3.runtime.NavKey
import com.thomaskuenneth.tkweek.TKWeekModule
import com.thomaskuenneth.tkweek.types.TKWeekDestination
import com.thomaskuenneth.tkweek.viewmodel.NavigationRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TKWeekBackStackTest {

    @Test
    fun initialTKWeekBackStack_startsOnModuleListOnly() {
        val backStack = initialTKWeekBackStack()

        assertEquals(listOf(TKWeekDestination.ModuleList), backStack)
    }

    @Test
    fun applyNavigation_topLevelFromModuleListOnly_pushesDetail() {
        val backStack = mutableBackStackOf(TKWeekDestination.ModuleList)

        backStack.applyNavigation(NavigationRequest(TKWeekModule.Calendar, topLevel = true))

        assertEquals(
            listOf(TKWeekDestination.ModuleList, TKWeekDestination.Detail(TKWeekModule.Calendar)),
            backStack
        )
    }

    @Test
    fun applyNavigation_nested_pushesOnTopWithoutTouchingExistingEntries() {
        val backStack = mutableBackStackOf(
            TKWeekDestination.ModuleList,
            TKWeekDestination.Detail(TKWeekModule.AnnualEvents),
        )

        backStack.applyNavigation(
            NavigationRequest(TKWeekModule.MyDay, date = 12345L, topLevel = false)
        )

        assertEquals(
            listOf(
                TKWeekDestination.ModuleList,
                TKWeekDestination.Detail(TKWeekModule.AnnualEvents),
                TKWeekDestination.Detail(TKWeekModule.MyDay, date = 12345L),
            ),
            backStack
        )
    }

    @Test
    fun applyNavigation_reselectingModuleAlreadyActiveWhileNested_returnsToItsRoot() {
        val backStack = mutableBackStackOf(
            TKWeekDestination.ModuleList,
            TKWeekDestination.Detail(TKWeekModule.Calendar),
            TKWeekDestination.Detail(TKWeekModule.MyDay, date = 999L),
        )

        backStack.applyNavigation(NavigationRequest(TKWeekModule.Calendar, topLevel = true))

        assertEquals(
            listOf(TKWeekDestination.ModuleList, TKWeekDestination.Detail(TKWeekModule.Calendar)),
            backStack
        )
    }

    @Test
    fun applyNavigation_topLevelWhileDeeplyNested_dropsEveryNestedEntry() {
        val backStack = mutableBackStackOf(
            TKWeekDestination.ModuleList,
            TKWeekDestination.Detail(TKWeekModule.AnnualEvents),
            TKWeekDestination.Detail(TKWeekModule.DaysBetweenDates),
            TKWeekDestination.Detail(TKWeekModule.MyDay),
        )

        backStack.applyNavigation(NavigationRequest(TKWeekModule.Week, topLevel = true))

        assertEquals(
            listOf(TKWeekDestination.ModuleList, TKWeekDestination.Detail(TKWeekModule.Week)),
            backStack
        )
    }

    @Test
    fun applyNavigation_topLevelClearsDateFromPreviousSelection() {
        val backStack = mutableBackStackOf(
            TKWeekDestination.ModuleList,
            TKWeekDestination.Detail(TKWeekModule.MyDay, date = 111L),
        )

        backStack.applyNavigation(NavigationRequest(TKWeekModule.MyDay, topLevel = true))

        assertEquals(
            listOf(TKWeekDestination.ModuleList, TKWeekDestination.Detail(TKWeekModule.MyDay, date = null)),
            backStack
        )
    }

    @Test
    fun applyNavigation_onEmptyBackStack_throws() {
        val backStack = mutableBackStackOf()

        assertThrows(IllegalArgumentException::class.java) {
            backStack.applyNavigation(NavigationRequest(TKWeekModule.Week, topLevel = true))
        }
    }

    private fun mutableBackStackOf(vararg elements: NavKey): MutableList<NavKey> =
        mutableListOf(*elements)
}
