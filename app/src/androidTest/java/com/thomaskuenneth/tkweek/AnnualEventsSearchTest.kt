package com.thomaskuenneth.tkweek

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.widget.AdapterView
import android.widget.EditText
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.textfield.TextInputLayout
import com.thomaskuenneth.tkweek.fragment.AnnualEventsFragment
import com.thomaskuenneth.tkweek.types.Event
import com.thomaskuenneth.tkweek.util.Helper
import com.thomaskuenneth.tkweek.viewmodel.AnnualEventsViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val LOAD_TIMEOUT_MILLIS = 15_000L
private const val QUERY = "moon"

@LargeTest
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AnnualEventsSearchTest {

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

    private fun launchAnnualEvents(): ActivityScenario<TKWeekCompose> {
        val intent = Intent(context, TKWeekCompose::class.java)
            .putExtra(Helper.CLAZZ, AnnualEventsFragment::class.java.name)
        return ActivityScenario.launch(intent)
    }

    private fun List<String>.matchingTheQuery() =
        filter { it.contains(QUERY, ignoreCase = true) }

    private fun descriptions(scenario: ActivityScenario<TKWeekCompose>): List<String> {
        var result = emptyList<String>()
        scenario.onActivity { activity ->
            val adapter =
                activity.findViewById<AdapterView<*>>(R.id.listView)?.adapter ?: return@onActivity
            result = (0 until adapter.count).map { (adapter.getItem(it) as Event).descr }
        }
        return result
    }

    private fun awaitEvents(scenario: ActivityScenario<TKWeekCompose>): List<String> {
        var events = emptyList<String>()
        composeRule.waitUntil("events are loaded", LOAD_TIMEOUT_MILLIS) {
            events = descriptions(scenario)
            events.isNotEmpty()
        }
        return events
    }

    private fun searchQuery(scenario: ActivityScenario<TKWeekCompose>): String? {
        var query: String? = null
        scenario.onActivity { activity ->
            query = ViewModelProvider(activity)[AnnualEventsViewModel::class.java]
                .searchQuery.value
        }
        return query
    }

    private fun fieldText(scenario: ActivityScenario<TKWeekCompose>): String {
        var text = ""
        scenario.onActivity { activity ->
            text = activity.findViewById<EditText>(R.id.search_field).text.toString()
        }
        return text
    }

    private fun startIcon(scenario: ActivityScenario<TKWeekCompose>): Int {
        var signature = 0
        scenario.onActivity { activity ->
            val shown = activity.findViewById<TextInputLayout>(R.id.search_field_layout)
                .startIconDrawable ?: return@onActivity
            val icon = shown.constantState?.newDrawable()?.mutate() ?: return@onActivity
            val bitmap = createBitmap(icon.intrinsicWidth, icon.intrinsicHeight)
            icon.setBounds(0, 0, bitmap.width, bitmap.height)
            icon.draw(Canvas(bitmap))
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            signature = pixels.contentHashCode()
        }
        return signature
    }

    private fun awaitEventCount(scenario: ActivityScenario<TKWeekCompose>, count: Int) {
        composeRule.waitUntil("the list holds $count events", LOAD_TIMEOUT_MILLIS) {
            descriptions(scenario).size == count
        }
    }

    @Test
    fun typingAQuery_keepsOnlyTheMatchingEvents() {
        launchAnnualEvents().use { scenario ->
            val all = awaitEvents(scenario)
            val expected = all.matchingTheQuery()
            assertTrue("No event matches $QUERY, the query proves nothing", expected.isNotEmpty())
            assertTrue("Every event matches $QUERY already", expected.size < all.size)

            onView(withId(R.id.search_field)).perform(typeText(QUERY), closeSoftKeyboard())

            awaitEventCount(scenario, expected.size)
            assertEquals(expected, descriptions(scenario))
        }
    }

    @Test
    fun pressingDone_keepsTheQuery() {
        launchAnnualEvents().use { scenario ->
            val all = awaitEvents(scenario)
            val expected = all.matchingTheQuery()
            onView(withId(R.id.search_field)).perform(typeText(QUERY))
            awaitEventCount(scenario, expected.size)

            onView(withId(R.id.search_field)).perform(pressImeActionButton())

            assertEquals(QUERY, searchQuery(scenario))
            assertEquals(QUERY, fieldText(scenario))
            assertEquals(expected, descriptions(scenario))
        }
    }

    @Test
    fun theStartIcon_followsWhetherAQueryIsActive() {
        launchAnnualEvents().use { scenario ->
            val all = awaitEvents(scenario)
            val resting = startIcon(scenario)

            onView(withId(R.id.search_field)).perform(typeText(QUERY), closeSoftKeyboard())
            awaitEventCount(scenario, all.matchingTheQuery().size)
            assertNotEquals(resting, startIcon(scenario))

            onView(withId(R.id.search_field)).perform(clearText())

            awaitEventCount(scenario, all.size)
            assertEquals(resting, startIcon(scenario))
        }
    }

    @Test
    fun theQueryAndTheFilteredList_surviveRecreation() {
        launchAnnualEvents().use { scenario ->
            val all = awaitEvents(scenario)
            val expected = all.matchingTheQuery()
            onView(withId(R.id.search_field)).perform(typeText(QUERY), closeSoftKeyboard())
            awaitEventCount(scenario, expected.size)

            scenario.recreate()

            awaitEventCount(scenario, expected.size)
            assertEquals(QUERY, fieldText(scenario))
            assertEquals(expected, descriptions(scenario))
        }
    }

    @Test
    fun clearingTheQuery_bringsEveryEventBack() {
        launchAnnualEvents().use { scenario ->
            val all = awaitEvents(scenario)
            onView(withId(R.id.search_field)).perform(typeText(QUERY), closeSoftKeyboard())
            awaitEventCount(scenario, all.matchingTheQuery().size)

            onView(withId(R.id.search_field)).perform(clearText())

            awaitEventCount(scenario, all.size)
            assertEquals(all, descriptions(scenario))
        }
    }
}
