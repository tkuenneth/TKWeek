/*
 * WeekFragment.kt
 *
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

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.DatePicker
import android.widget.DatePicker.OnDateChangedListener
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.activity.TKWeekActivity
import com.thomaskuenneth.tkweek.appwidget.WeekInfoWidget
import com.thomaskuenneth.tkweek.databinding.WeekBinding
import com.thomaskuenneth.tkweek.util.TKWeekUtils
import java.util.*

class WeekFragment : TKWeekBaseFragment<WeekBinding>(), OnDateChangedListener,
    OnSeekBarChangeListener, View.OnClickListener {

    private val binding get() = backing!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        backing = WeekBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        TKWeekActivity.configureDatePicker(binding.dateWithinWeek)
        binding.weekSelection.setOnSeekBarChangeListener(this)
        binding.down.setOnClickListener(this)
        binding.up.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        prepareCalendar(cal, requireContext(), binding.labelWeekNumber, false)
        updateViewsFromCalendar()
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_today, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.today -> {
                cal.time = Date()
                updateViewsFromCalendar()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun preferencesFinished(resultCode: Int, data: Intent?) {
        super.preferencesFinished(resultCode, data)
        prepareCalendar(cal, requireContext(), binding.labelWeekNumber, false)
        updateViewsFromCalendar()
        updateWeekInfoWidgets(requireContext())
    }

    override fun onDateChanged(
        view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int
    ) {
        cal[Calendar.YEAR] = year
        cal[Calendar.MONTH] = monthOfYear
        cal[Calendar.DAY_OF_MONTH] = dayOfMonth
        updateViews()
    }

    override fun onProgressChanged(
        seekBar: SeekBar?, progress: Int, fromUser: Boolean
    ) {
        if (fromUser) {
            val dif = progress - (cal[Calendar.WEEK_OF_YEAR] - 1)
            if (dif != 0) {
                cal.add(Calendar.DAY_OF_MONTH, 7 * dif)
                updateViewsFromCalendar(updateWeekSelection = false)
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onClick(v: View) {
        var current = cal[Calendar.DAY_OF_MONTH]
        if (v === binding.down) {
            current -= 7
        } else if (v === binding.up) {
            current += 7
        }
        cal[Calendar.DAY_OF_MONTH] = current
        updateViewsFromCalendar()
    }

    private fun updateWeekInfoWidgets(context: Context) {
        val m = AppWidgetManager.getInstance(context)
        if (m != null) {
            val appWidgetIds = m.getAppWidgetIds(
                ComponentName(
                    context, WeekInfoWidget::class.java
                )
            )
            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                WeekInfoWidget.updateWidgets(context, m, appWidgetIds)
            }
        }
    }

    private fun updateViewsFromCalendar(updateWeekSelection: Boolean = true) {
        binding.dateWithinWeek.init(
            cal[Calendar.YEAR], cal[Calendar.MONTH], cal[Calendar.DAY_OF_MONTH], this
        )
        updateViews(updateWeekSelection)
    }

    private fun updateViews(updateWeekSelection: Boolean = true) {
        binding.day.text = TKWeekActivity.FORMAT_DAY_OF_WEEK.format(cal.time)
        val weekOfYear = cal[Calendar.WEEK_OF_YEAR]
        binding.weekNumber.text = TKWeekUtils.integerToString(weekOfYear)
        val temp = cal.clone() as Calendar
        if (updateWeekSelection) {
            binding.weekSelection.max = temp.getActualMaximum(Calendar.WEEK_OF_YEAR) - 1
            binding.weekSelection.progress = weekOfYear - 1
        }
        while (temp[Calendar.DAY_OF_WEEK] != temp.firstDayOfWeek) {
            temp.add(Calendar.DAY_OF_MONTH, -1)
        }
        val start = temp.time
        temp.add(Calendar.DAY_OF_MONTH, 6)
        val end = temp.time
        val text = getString(
            R.string.first_and_last_day_of_week,
            TKWeekActivity.FORMAT_DAY_OF_WEEK_SHORT.format(start),
            TKWeekActivity.FORMAT_DEFAULT.format(start),
            TKWeekActivity.FORMAT_DAY_OF_WEEK_SHORT.format(end),
            TKWeekActivity.FORMAT_DEFAULT.format(end)
        )
        binding.firstAndLastDayOfWeek.text = text
    }

    companion object {

        private const val START_OF_WEEK = "wochenanfang"
        private const val USE_ISO_WEEKS = "use_iso_weeks"

        private val cal = Calendar.getInstance()

        @JvmStatic
        fun prepareCalendar(cal: Calendar, context: Context) {
            prepareCalendar(cal, context, null, false)
        }

        @JvmStatic
        fun prepareCalendar(
            cal: Calendar, context: Context, label_week_number: TextView?, appendColon: Boolean
        ) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val useISO = prefs.getBoolean(USE_ISO_WEEKS, false)
            if (useISO) {
                cal.minimalDaysInFirstWeek = 4
                cal.firstDayOfWeek = Calendar.MONDAY
                label_week_number?.setText(R.string.week_number_iso)
            } else {
                val c = Calendar.getInstance()
                val s = prefs.getString(START_OF_WEEK, "-1")
                var start = -1
                try {
                    start = s!!.toInt()
                } catch (_: NumberFormatException) {
                    // no logging needed
                }
                if (start != -1) {
                    c.firstDayOfWeek = start
                }
                cal.minimalDaysInFirstWeek = c.minimalDaysInFirstWeek
                cal.firstDayOfWeek = c.firstDayOfWeek
                label_week_number?.setText(R.string.week_number)
            }
            cal[Calendar.DAY_OF_MONTH] = cal[Calendar.DAY_OF_MONTH]
            if (label_week_number != null && appendColon) {
                label_week_number.append(":")
            }
        }
    }
}