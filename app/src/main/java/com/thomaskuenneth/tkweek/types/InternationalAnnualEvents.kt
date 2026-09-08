/*
 * InternationalAnnualEvents.kt
 *
 * Copyright 2026 Thomas Künneth
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.thomaskuenneth.tkweek.types

import com.thomaskuenneth.tkweek.R
import java.util.Calendar

data class FixedDateObservance(
    val nameResId: Int,
    val month: Int,
    val dayOfMonth: Int,
)

object InternationalAnnualEvents {

    @JvmField
    val ALL: List<FixedDateObservance> = listOf(
        FixedDateObservance(R.string.weltfrauentag, Calendar.MARCH, 8),
        FixedDateObservance(R.string.weltkindertag, Calendar.NOVEMBER, 20),
        FixedDateObservance(R.string.tag_der_erde, Calendar.APRIL, 22),
        FixedDateObservance(R.string.halloween, Calendar.OCTOBER, 31),
        FixedDateObservance(R.string.neujahr, Calendar.JANUARY, 1),
        FixedDateObservance(R.string.silvester, Calendar.DECEMBER, 31),
        FixedDateObservance(R.string.erster_april, Calendar.APRIL, 1),
        FixedDateObservance(R.string.tag_der_arbeit, Calendar.MAY, 1),
        FixedDateObservance(R.string.valentinstag, Calendar.FEBRUARY, 14),
        FixedDateObservance(R.string.tolkien_reading_day, Calendar.MARCH, 25),
        FixedDateObservance(R.string.hobbit_day, Calendar.SEPTEMBER, 22),
        FixedDateObservance(R.string.world_seagrass_day, Calendar.MARCH, 1),
        FixedDateObservance(R.string.world_water_day, Calendar.MARCH, 22),
        FixedDateObservance(R.string.world_health_day, Calendar.APRIL, 7),
        FixedDateObservance(R.string.world_book_day, Calendar.APRIL, 23),
        FixedDateObservance(R.string.world_environment_day, Calendar.JUNE, 5),
        FixedDateObservance(R.string.world_oceans_day, Calendar.JUNE, 8),
        FixedDateObservance(R.string.nelson_mandela_day, Calendar.JULY, 18),
        FixedDateObservance(R.string.international_day_of_peace, Calendar.SEPTEMBER, 21),
        FixedDateObservance(R.string.world_teachers_day, Calendar.OCTOBER, 5),
        FixedDateObservance(R.string.world_food_day, Calendar.OCTOBER, 16),
        FixedDateObservance(R.string.human_rights_day, Calendar.DECEMBER, 10),
    )

    @JvmStatic
    fun findByNameResId(nameResId: Int): FixedDateObservance? {
        return ALL.find { it.nameResId == nameResId }
    }
}
