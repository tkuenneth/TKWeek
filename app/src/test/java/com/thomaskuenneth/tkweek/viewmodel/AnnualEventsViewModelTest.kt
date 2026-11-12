package com.thomaskuenneth.tkweek.viewmodel

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AnnualEventsViewModelTest {

    private lateinit var viewModel: AnnualEventsViewModel

    @Before
    fun setUp() {
        viewModel = AnnualEventsViewModel()
    }

    @Test
    fun searchQuery_startsUnsetAndFollowsEveryChange() = runTest {
        viewModel.searchQuery.test {
            assertNull(awaitItem())

            viewModel.setSearchQuery("moon")
            assertEquals("moon", awaitItem())

            viewModel.setSearchQuery(null)
            assertNull(awaitItem())
        }
    }

    @Test
    fun setSearchQuery_leavesTheFabMenuAlone() = runTest {
        viewModel.setFabMenuExpanded(true)

        viewModel.setSearchQuery("moon")

        viewModel.isFabMenuExpanded.test {
            assertEquals(true, awaitItem())
        }
    }
}
