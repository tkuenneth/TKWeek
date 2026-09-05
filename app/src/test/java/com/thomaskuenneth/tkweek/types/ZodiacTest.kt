package com.thomaskuenneth.tkweek.types

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.util.Calendar

class ZodiacTest {

    @Test
    fun getSign_returnsConfiguredBoundaryForEachMonth() {
        val march = requireNotNull(Zodiac.getSign(Calendar.MARCH))
        val september = requireNotNull(Zodiac.getSign(Calendar.SEPTEMBER))

        assertEquals(21, march.firstDayOfSecondSign)
        assertEquals(24, september.firstDayOfSecondSign)
    }

    @Test
    fun getSign_usesDistinctResourceIdsAroundBoundary() {
        val april = requireNotNull(Zodiac.getSign(Calendar.APRIL))

        assertNotEquals(april.firstSign, april.secondSign)
    }

    @Test
    fun getSign_returnsNullForUnknownMonthKey() {
        assertEquals(null, Zodiac.getSign(Calendar.UNDECIMBER))
    }
}
