/*
 * CalendarAsyncTask.java
 *
 * Copyright 2021 MATHEMA GmbH 2021
 *           2022 - 2025 Thomas Künneth
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
package com.thomaskuenneth.tkweek

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import com.thomaskuenneth.tkweek.databinding.DaysBetweenDatesActivityBinding
import com.thomaskuenneth.tkweek.preference.PickBusinessDaysPreference
import com.thomaskuenneth.tkweek.util.DateUtilities
import java.util.Calendar

private const val YEARS = "years"
private const val MONTHS = "months"
private const val WEEKENDS = "weekends"
private const val WEEKS = "weeks"
private const val BUSINESS_DAYS = "business_days"
private const val DAYS = "days"

data class DaysBetweenDates(
    val days: Int,
    val businessDays: Int,
    val weekends: Int,
    val weeks: Int,
    val months: Int,
    val years: Int
)

fun daysBetweenDates(
    from: Calendar,
    to: Calendar,
    includeFirstDate: Boolean,
    businessDaysOfWeek: Set<Int>
): DaysBetweenDates {
    val c1 = DateUtilities.clearTimeRelatedFields(from)
    val c2 = DateUtilities.clearTimeRelatedFields(to)
    var days = 0
    var businessDays = 0
    var sat = false
    var weekends = 0
    var weeks = 0
    var lastWeek = c1[Calendar.WEEK_OF_YEAR]
    var monthTurns = 0
    var lastMonth = c1[Calendar.MONTH]
    var yearTurns = 0
    var lastYear = c1[Calendar.YEAR]
    var temp: Int
    if (includeFirstDate) {
        days += 1
        if (businessDaysOfWeek.contains(c1[Calendar.DAY_OF_WEEK])) {
            businessDays += 1
        }
    }
    while (c2.after(c1)) {
        val weekday = c1[Calendar.DAY_OF_WEEK]
        if (businessDaysOfWeek.contains(weekday)) {
            businessDays += 1
        }
        if (weekday == Calendar.SATURDAY) {
            sat = true
        } else if (weekday == Calendar.SUNDAY) {
            if (sat) {
                weekends += 1
            }
            sat = false
        }
        days += 1
        if (c1[Calendar.WEEK_OF_YEAR].also { temp = it } != lastWeek) {
            weeks += 1
            lastWeek = temp
        }
        if (c1[Calendar.MONTH].also { temp = it } != lastMonth) {
            monthTurns += 1
            lastMonth = temp
        }
        if (c1[Calendar.YEAR].also { temp = it } != lastYear) {
            yearTurns += 1
            lastYear = temp
        }
        c1.add(Calendar.DAY_OF_YEAR, 1)
    }
    if (c1.get(Calendar.DAY_OF_MONTH) == 1) {
        monthTurns += 1
        if (c1.get(Calendar.MONTH) == Calendar.JANUARY) yearTurns += 1
    }
    return DaysBetweenDates(days, businessDays, weekends, weeks, monthTurns, yearTurns)
}

class CalendarAsyncTask(
    private val context: Context,
    private val binding: DaysBetweenDatesActivityBinding
) : AsyncTask<Calendar, Void, Bundle>() {

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: Calendar): Bundle {
        val result = daysBetweenDates(
            from = params[0],
            to = params[1],
            includeFirstDate = binding.checkboxIncludeFirstDate.isChecked,
            businessDaysOfWeek = PickBusinessDaysPreference.getBusinessDays(context)
        )
        val b = Bundle()
        b.putInt(DAYS, result.days)
        b.putInt(BUSINESS_DAYS, result.businessDays)
        b.putInt(WEEKENDS, result.weekends)
        b.putInt(WEEKS, result.weeks)
        b.putInt(MONTHS, result.months)
        b.putInt(YEARS, result.years)
        return b
    }

    @Deprecated("Deprecated in Java")
    override fun onPostExecute(b: Bundle) {
        binding.firstDate.isEnabled = true
        binding.secondDate.isEnabled = true
        val days = b.getInt(DAYS)
        binding.daysBetweenDatesTotal1.text = context.getString(
            R.string.days_between_dates_days, days
        )
        binding.daysBetweenDatesTotal2.text = context.getString(
            R.string.days_between_dates_business_days, b.getInt(BUSINESS_DAYS)
        )
        binding.daysBetweenDatesWeekends.text = context.getString(
            R.string.days_between_dates_weekends, b.getInt(WEEKENDS)
        )
        binding.daysBetweenDatesWeeks.text = if (days >= 7)
            context.getString(R.string.days_between_dates_weeks, b.getInt(WEEKS))
        else
            context.getString(R.string.less_than_a_week)
        binding.daysBetweenDatesMonthTurns.text = context.getString(
            R.string.days_between_dates_month_turns, b.getInt(MONTHS)
        )
        binding.daysBetweenDatesYearTurns.text = context.getString(
            R.string.days_between_dates_year_turns, b.getInt(YEARS)
        )
    }
}
