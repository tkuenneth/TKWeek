package com.thomaskuenneth.tkweek.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * The file storage behind the annual-events backup and restore.
 *
 * Whatever [TKWeekUtils.save] writes, [TKWeekUtils.load] has to give back unchanged - this is
 * user data, and a silent corruption here is only noticed when a restore produces nonsense.
 * The awkward cases are the ones that matter: umlauts, because the app is shipped in German;
 * characters outside the BMP, because load reads the file one char at a time; newlines,
 * because a backup is a multi-line document; and overwriting, because a second backup must
 * replace the first rather than append to it.
 */
@SmallTest
@RunWith(AndroidJUnit4::class)
class TKWeekUtilsStorageTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "tkweek_storage_test.txt"

    @Before
    fun clearBefore() = deleteTestFile()

    @After
    fun clearAfter() = deleteTestFile()

    private fun deleteTestFile() {
        File(context.filesDir, name).delete()
    }

    private fun saveAndLoad(content: String): String {
        assertTrue("save() reported failure", TKWeekUtils.save(context, name, content))
        return TKWeekUtils.load(context, name)
    }

    @Test
    fun plainText_survivesTheRoundTrip() {
        assertEquals("Hello", saveAndLoad("Hello"))
    }

    @Test
    fun germanUmlauts_surviveTheRoundTrip() {
        val content = "Fronleichnam, Christi Himmelfahrt, Buß- und Bettag, Größe: 3 Äpfel"

        assertEquals(content, saveAndLoad(content))
    }

    /** load() reads char by char, so anything built from a surrogate pair is the risky case. */
    @Test
    fun charactersOutsideTheBasicPlane_surviveTheRoundTrip() {
        val content = "Geburtstag 🎂 und Urlaub 🏖️"

        assertEquals(content, saveAndLoad(content))
    }

    @Test
    fun multipleLines_surviveTheRoundTrip() {
        val content = "first\nsecond\r\nthird\n"

        assertEquals(content, saveAndLoad(content))
    }

    @Test
    fun emptyContent_survivesTheRoundTrip() {
        assertEquals("", saveAndLoad(""))
    }

    /** A second backup has to replace the first; appending would corrupt every restore. */
    @Test
    fun savingTwice_replacesTheEarlierContent() {
        saveAndLoad("a much longer first backup")

        assertEquals("short", saveAndLoad("short"))
    }

    @Test
    fun loadingSomethingNeverSaved_returnsEmptyRatherThanNull() {
        val missing = TKWeekUtils.load(context, "tkweek_storage_test_absent.txt")

        assertEquals("", missing)
    }

    @Test
    fun largeContent_survivesTheRoundTrip() {
        val content = (1..2000).joinToString("\n") { "event $it: Beschreibung mit Ümlaut" }

        assertEquals(content, saveAndLoad(content))
    }
}
