package com.thomaskuenneth.tkweek.types

import com.thomaskuenneth.tkweek.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar

class InternationalAnnualEventsTest {

    @Test
    fun findByNameResId_worldWaterDay_isMarch22() {
        assertObservanceOn(R.string.world_water_day, Calendar.MARCH, 22)
    }

    @Test
    fun findByNameResId_worldHealthDay_isApril7() {
        assertObservanceOn(R.string.world_health_day, Calendar.APRIL, 7)
    }

    @Test
    fun findByNameResId_worldBookDay_isApril23() {
        assertObservanceOn(R.string.world_book_day, Calendar.APRIL, 23)
    }

    @Test
    fun findByNameResId_worldEnvironmentDay_isJune5() {
        assertObservanceOn(R.string.world_environment_day, Calendar.JUNE, 5)
    }

    @Test
    fun findByNameResId_worldOceansDay_isJune8() {
        assertObservanceOn(R.string.world_oceans_day, Calendar.JUNE, 8)
    }

    @Test
    fun findByNameResId_nelsonMandelaDay_isJuly18() {
        assertObservanceOn(R.string.nelson_mandela_day, Calendar.JULY, 18)
    }

    @Test
    fun findByNameResId_internationalDayOfPeace_isSeptember21() {
        assertObservanceOn(R.string.international_day_of_peace, Calendar.SEPTEMBER, 21)
    }

    @Test
    fun findByNameResId_worldTeachersDay_isOctober5() {
        assertObservanceOn(R.string.world_teachers_day, Calendar.OCTOBER, 5)
    }

    @Test
    fun findByNameResId_worldFoodDay_isOctober16() {
        assertObservanceOn(R.string.world_food_day, Calendar.OCTOBER, 16)
    }

    @Test
    fun findByNameResId_humanRightsDay_isDecember10() {
        assertObservanceOn(R.string.human_rights_day, Calendar.DECEMBER, 10)
    }

    @Test
    fun findByNameResId_earthDay_isApril22() {
        assertObservanceOn(R.string.tag_der_erde, Calendar.APRIL, 22)
    }

    @Test
    fun findByNameResId_internationalWomensDay_isMarch8() {
        assertObservanceOn(R.string.weltfrauentag, Calendar.MARCH, 8)
    }

    @Test
    fun findByNameResId_unknownResource_returnsNull() {
        assertNull(InternationalAnnualEvents.findByNameResId(R.string.app_name))
    }

    @Test
    fun all_usesUniqueNameResourceIds() {
        val ids = InternationalAnnualEvents.ALL.map { it.nameResId }

        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun all_usesValidGregorianMonthAndDay() {
        InternationalAnnualEvents.ALL.forEach { observance ->
            val calendar = GregorianCalendar(2026, observance.month, 1)
            val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            assertTrue(observance.month in Calendar.JANUARY..Calendar.DECEMBER)
            assertTrue(observance.dayOfMonth in 1..lastDayOfMonth)
        }
    }

    private fun assertObservanceOn(nameResId: Int, month: Int, dayOfMonth: Int) {
        val observance = requireNotNull(InternationalAnnualEvents.findByNameResId(nameResId))

        assertEquals(month, observance.month)
        assertEquals(dayOfMonth, observance.dayOfMonth)
    }
}
