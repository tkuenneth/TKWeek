package com.thomaskuenneth.tkweek.adapter

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.thomaskuenneth.tkweek.types.Event
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.StringWriter
import java.util.Calendar
import java.util.GregorianCalendar

private const val MATCHING = "Moon party"
private const val OTHER = "Garden day"

@RunWith(RobolectricTestRunner::class)
class AnnualEventsListAdapterTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AnnualEventsListAdapter.create(context, null).apply {
            addEventNoCheck(event(MATCHING, 3))
            addEventNoCheck(event(OTHER, 4))
            save(context)
        }
    }

    private fun event(description: String, daysFromToday: Int) = Event(
        description,
        GregorianCalendar().apply { add(Calendar.DAY_OF_YEAR, daysFromToday) }.time,
        true
    )

    private fun persistedBy(search: String?): String {
        val writer = StringWriter()
        AnnualEventsListAdapter.create(context, search).saveUserEvents(writer)
        return writer.toString()
    }

    @Test
    fun anUnfilteredAdapterPersistsEveryUserEvent() {
        val persisted = persistedBy(null)

        assertTrue(persisted, persisted.contains(MATCHING))
        assertTrue(persisted, persisted.contains(OTHER))
    }

    @Test
    fun aFilteredAdapterPersistsOnlyWhatItShows() {
        val persisted = persistedBy("moon")

        assertTrue(persisted, persisted.contains(MATCHING))
        assertFalse(persisted, persisted.contains(OTHER))
    }
}
