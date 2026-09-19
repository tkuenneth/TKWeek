package com.thomaskuenneth.tkweek.viewmodel

import app.cash.turbine.test
import com.thomaskuenneth.tkweek.MainDispatcherRule
import com.thomaskuenneth.tkweek.TKWeekModule
import com.thomaskuenneth.tkweek.preference.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class TKWeekViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var avoidHinge: MutableStateFlow<Boolean>
    private lateinit var viewModel: TKWeekViewModel

    @Before
    fun setUp() {
        avoidHinge = MutableStateFlow(false)
        val preferenceManager: PreferenceManager = mock {
            whenever(it.avoidHinge).thenReturn(avoidHinge)
        }
        viewModel = TKWeekViewModel(preferenceManager)
    }

    @Test
    fun uiState_reflectsAvoidHingePreference() = runTest {
        viewModel.uiState.test {
            assertFalse(awaitItem().avoidHinge)

            avoidHinge.value = true

            assertTrue(awaitItem().avoidHinge)
        }
    }

    @Test
    fun setListScrolled_updatesOnlyThatFlag() = runTest {
        viewModel.uiState.test {
            assertFalse(awaitItem().isListScrolled)

            viewModel.setListScrolled(true)

            val updated = awaitItem()
            assertTrue(updated.isListScrolled)
            assertFalse(updated.isDetailScrolled)
        }
    }

    @Test
    fun setDetailScrolled_updatesOnlyThatFlag() = runTest {
        viewModel.uiState.test {
            assertFalse(awaitItem().isDetailScrolled)

            viewModel.setDetailScrolled(true)

            val updated = awaitItem()
            assertTrue(updated.isDetailScrolled)
            assertFalse(updated.isListScrolled)
        }
    }

    @Test
    fun setShouldShowProgressIndicator_updatesState() = runTest {
        viewModel.uiState.test {
            assertFalse(awaitItem().shouldShowProgressIndicator)

            viewModel.setShouldShowProgressIndicator(true)

            assertTrue(awaitItem().shouldShowProgressIndicator)
        }
    }

    @Test
    fun setAppBarActions_replacesTheActionList() = runTest {
        viewModel.appBarActions.test {
            assertEquals(emptyList<AppBarAction>(), awaitItem())

            val action = AppBarAction(title = 1, onClick = {})
            viewModel.setAppBarActions(listOf(action))

            assertEquals(listOf(action), awaitItem())
        }
    }

    @Test
    fun requestNavigation_topLevel_emitsMatchingRequest() = runTest {
        viewModel.navigationRequests.test {
            viewModel.requestNavigation(module = TKWeekModule.Calendar, topLevel = true)

            assertEquals(
                NavigationRequest(module = TKWeekModule.Calendar, date = null, topLevel = true),
                awaitItem()
            )
        }
    }

    @Test
    fun requestNavigation_nestedWithDate_emitsMatchingRequest() = runTest {
        viewModel.navigationRequests.test {
            viewModel.requestNavigation(module = TKWeekModule.MyDay, date = 42L, topLevel = false)

            assertEquals(
                NavigationRequest(module = TKWeekModule.MyDay, date = 42L, topLevel = false),
                awaitItem()
            )
        }
    }

    @Test
    fun requestNavigation_multipleRequests_areDeliveredInOrder() = runTest {
        viewModel.navigationRequests.test {
            viewModel.requestNavigation(module = TKWeekModule.Week, topLevel = true)
            viewModel.requestNavigation(module = TKWeekModule.MyDay, topLevel = false)

            assertEquals(TKWeekModule.Week, awaitItem().module)
            assertEquals(TKWeekModule.MyDay, awaitItem().module)
        }
    }
}
