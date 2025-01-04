/*
 * AboutYearFragment.kt
 *
 * Copyright 2009 - 2020 Thomas Künneth
 * Copyright 2021 MATHEMA GmbH
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
package com.thomaskuenneth.tkweek.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.activity.TKWeekActivity
import com.thomaskuenneth.tkweek.databinding.AboutAYearBinding
import com.thomaskuenneth.tkweek.types.Seasons
import com.thomaskuenneth.tkweek.types.Seasons.SEASON
import com.thomaskuenneth.tkweek.util.*
import com.thomaskuenneth.tkweek.util.CalendarCondition.CONDITION
import java.util.*

private const val TAG = "AboutYearFragment"
private const val YEAR_KEY = "year"

class AboutYearFragment : TKWeekBaseFragment<AboutAYearBinding>(), View.OnClickListener {

    private val binding get() = backing!!

    private lateinit var cal: Calendar
    private lateinit var seasons: Seasons
    private lateinit var sb: StringBuilder
    private lateinit var cc: CalendarCondition

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        backing = AboutAYearBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(YEAR_KEY, cal.get(Calendar.YEAR))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        cal = Calendar.getInstance()
        WeekFragment.prepareCalendar(cal, requireContext())
        seasons = Seasons(context)
        sb = StringBuilder()
        cc = CalendarCondition.createCalendarCondition(
            CONDITION.EQUAL, Calendar.DAY_OF_WEEK, Calendar.FRIDAY, false
        )
        binding.aboutAYearDown.setOnClickListener(this)
        binding.aboutAYearUp.setOnClickListener(this)
        binding.leapyearPreviousYear.setOnClickListener(this)
        binding.leapyearNextYear.setOnClickListener(this)
        savedInstanceState?.run {
            val year = getInt(YEAR_KEY, cal.get(Calendar.YEAR))
            cal.set(Calendar.YEAR, year)
        }
        update()
    }

    override fun onClick(v: View) {
        when {
            binding.aboutAYearDown == v -> {
                cal.add(Calendar.YEAR, -1)
            }

            binding.aboutAYearUp == v -> {
                cal.add(Calendar.YEAR, 1)
            }

            binding.leapyearPreviousYear == v -> {
                jumpToLeapYear(-1)
            }

            binding.leapyearNextYear == v -> {
                jumpToLeapYear(1)
            }
        }
        update()
    }

    private fun update() {
        val year = cal[Calendar.YEAR]
        season(year, SEASON.SPRING)
        season(year, SEASON.SUMMER)
        season(year, SEASON.AUTUMN)
        season(year, SEASON.WINTER)
        // Beginn und Ende der Sommerzeit
        val dst = DaylightSavingTime(year)
        val strFrom = dst.begin.let {
            if (it != null) TKWeekActivity.FORMAT_FULL.format(it)
            else TKWeekActivity.DASHES
        }
        val strTo = dst.end.let {
            if (it != null) TKWeekActivity.FORMAT_FULL.format(it)
            else TKWeekActivity.DASHES
        }
        binding.aboutAYearDaylightSavingsFromTo.text =
            if (strFrom == TKWeekActivity.DASHES || strTo == TKWeekActivity.DASHES) getString(R.string.no_daylight_savings)
            else getString(
                R.string.string1_dash_string2, strFrom, strTo
            )
        // Jahr
        binding.aboutAYearYear.text = TKWeekUtils.integerToString(year)
        cal[Calendar.DAY_OF_MONTH] = 13
        cal[Calendar.MONTH] = Calendar.JANUARY
        val temp = cal.clone() as Calendar
        sb.setLength(0)
        (0..11).forEach { i ->
            try {
                if (cc.matches(temp)) {
                    if (sb.isNotEmpty()) {
                        sb.append("\n")
                    }
                    sb.append(TKWeekActivity.FORMAT_MONTH.format(temp.time))
                }
            } catch (e: RequiredCalendarConditionException) {
                Log.e(TAG, "update()", e)
            }
            temp.add(Calendar.MONTH, 1)
        }
        binding.aboutAYearMonths.text = sb.toString()
        val s = "${DateUtilities.toRoman(year)}${
            if (DateUtilities.isSchaltjahr(cal[Calendar.YEAR])) ", ${requireContext().getString(R.string.leap_year)}" else ""
        }"
        binding.leapyearIsLeapYear.text = s
        updateWeekInfo()
    }

    private fun season(year: Int, season: SEASON) {
        val label: TextView
        val value: TextView
        when (season) {
            SEASON.SPRING -> {
                label = binding.aboutAYearLabelSpring
                value = binding.aboutAYearSpring
                label.setText(R.string.spring)
            }

            SEASON.SUMMER -> {
                label = binding.aboutAYearLabelSummer
                value = binding.aboutAYearSummer
                label.setText(R.string.summer)
            }

            SEASON.AUTUMN -> {
                label = binding.aboutAYearLabelAutumn
                value = binding.aboutAYearAutumn
                label.setText(R.string.autumn)
            }

            else -> {
                label = binding.aboutAYearLabelWinter
                value = binding.aboutAYearWinter
                label.setText(R.string.winter)
            }
        }
        label.append(":")
        val cal = seasons.getCalendar(season, year)
        value.text = if (cal != null) TKWeekActivity.FORMAT_FULL.format(cal.time) else "???"
    }

    private fun getTextViews(index: Int): List<TextView> {
        val row = binding.aboutAYearLayoutWeekinfo.root.getChildAt(index) as TableRow
        val result = mutableListOf<TextView>()
        for (i in 0..8) result.add(row.getChildAt(i) as TextView)
        return result
    }

    private fun updateWeekInfo() {
        val count = Array(12) { IntArray(8) }
        val temp = cal.clone() as Calendar
        WeekFragment.prepareCalendar(temp, requireContext())
        temp[Calendar.DAY_OF_MONTH] = 1
        temp[Calendar.MONTH] = Calendar.JANUARY
        // bis zum Wochenanfang zurück
        while (temp[Calendar.DAY_OF_WEEK] != temp.firstDayOfWeek) {
            temp.add(Calendar.DAY_OF_MONTH, -1)
        }
        // 7 Tage zurück, um Wochentage zu setzen
        temp.add(Calendar.DAY_OF_MONTH, -7)
        val dayOfWeekToPos = IntArray(8) // Index 0 bleibt leer
        var info = getTextViews(0)
        for (pos in 1..7) {
            info[pos].text = TKWeekActivity.FORMAT_DAY_OF_WEEK_SHORT.format(
                temp.time
            ).substring(0, 1)
            val dayOfWeek = temp[Calendar.DAY_OF_WEEK]
            dayOfWeekToPos[pos] = dayOfWeek
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                info[pos].setTextColor(Color.RED)
            } else {
                info[pos].setTextColor(info[8].textColors)
            }
            temp.add(Calendar.DAY_OF_MONTH, 1)
        }
        info[8].setText(R.string.sigma)
        temp[Calendar.YEAR] = cal[Calendar.YEAR]
        for (month in 0..11) {
            info = getTextViews(month + 1)
            temp[Calendar.DAY_OF_MONTH] = 1
            temp[Calendar.MONTH] = month
            info[0].text = TKWeekActivity.FORMAT_MONTH_SHORT.format(temp.time)
            for (day in 1..temp.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                temp[Calendar.DAY_OF_MONTH] = day
                val weekDay = temp[Calendar.DAY_OF_WEEK]
                count[month][weekDay] += 1
            }
            var sum = 0
            for (weekday in 1..7) {
                val amount = count[month][dayOfWeekToPos[weekday]]
                sum += amount
                info[weekday].text = TKWeekUtils.integerToString(
                    amount
                )
            }
            info[8].text = TKWeekUtils.integerToString(sum)
        }
    }

    /**
     * Setzt die Calendar-Instanz der Activity auf das nächste oder vorherige
     * Schaltjahr. Die Richtung wird durch `offset` gesteuert.
     *
     * @param offset Richtung, also 1 oder -1
     */
    private fun jumpToLeapYear(offset: Int) {
        do {
            cal.add(Calendar.YEAR, offset)
        } while (!DateUtilities.isSchaltjahr(cal[Calendar.YEAR]))
    }

    override fun preferencesFinished(resultCode: Int, data: Intent?) {
        update()
    }
}