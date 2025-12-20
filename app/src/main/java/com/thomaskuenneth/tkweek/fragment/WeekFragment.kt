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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.slider.Slider
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.appwidget.WeekInfoWidget
import com.thomaskuenneth.tkweek.databinding.WeekBinding
import com.thomaskuenneth.tkweek.util.Helper
import com.thomaskuenneth.tkweek.util.TKWeekUtils
import java.util.Calendar
import java.util.Date

class WeekFragment : TKWeekBaseFragment<WeekBinding>(),
    View.OnClickListener {

    private val binding get() = backing!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        backing = WeekBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.dateWithinWeek.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setSelection(cal.timeInMillis)
                .build()
            picker.addOnPositiveButtonClickListener { selection ->
                cal.timeInMillis = selection
                updateViewsFromCalendar()
            }
            picker.show(parentFragmentManager, "date_picker")
        }
        binding.dateToday.setOnClickListener {
            cal.time = Date()
            updateViewsFromCalendar()
        }
        binding.weekSelection.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val newWeek = value.toInt()
                val currentWeek = cal[Calendar.WEEK_OF_YEAR]
                val diff = newWeek - currentWeek
                if (diff != 0) {
                    cal.add(Calendar.DAY_OF_MONTH, 7 * diff)
                    updateViewsFromCalendar(updateWeekSelection = false)
                }
            }
        }
        binding.weekSelection.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                updateViewsFromCalendar(updateWeekSelection = true)
            }
        })
        binding.down.setOnClickListener(this)
        binding.up.setOnClickListener(this)
        TKWeekUtils.linkToSettings(
            binding.weekControlsContainer,
            requireActivity(),
            R.string.week_calculation_message
        )
    }

    override fun onStart() {
        super.onStart()
        prepareCalendar(cal, requireContext(), binding.labelWeekNumber, false)
        updateViewsFromCalendar()
        updateWeekInfoWidgets(requireContext())
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
        updateViews(updateWeekSelection)
    }

    private fun updateViews(updateWeekSelection: Boolean = true) {
        binding.dateWithinWeek.text = Helper.FORMAT_FULL.format(cal.time)
        val weekOfYear = cal[Calendar.WEEK_OF_YEAR]
        binding.weekNumber.text = TKWeekUtils.integerToString(weekOfYear)
        val temp = cal.clone() as Calendar
        if (updateWeekSelection) {
            binding.weekSelection.valueFrom = 1f
            binding.weekSelection.valueTo = temp.getActualMaximum(Calendar.WEEK_OF_YEAR).toFloat()
            binding.weekSelection.value = weekOfYear.toFloat()
        }
        while (temp[Calendar.DAY_OF_WEEK] != temp.firstDayOfWeek) {
            temp.add(Calendar.DAY_OF_MONTH, -1)
        }
        val start = temp.time
        temp.add(Calendar.DAY_OF_MONTH, 6)
        val end = temp.time
        val text = getString(
            R.string.first_and_last_day_of_week,
            Helper.FORMAT_DAY_OF_WEEK_SHORT.format(start),
            Helper.FORMAT_DEFAULT.format(start),
            Helper.FORMAT_DAY_OF_WEEK_SHORT.format(end),
            Helper.FORMAT_DEFAULT.format(end)
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
            cal: Calendar, context: Context, labelWeekNumber: TextView?, appendColon: Boolean
        ) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val useISO = prefs.getBoolean(USE_ISO_WEEKS, false)
            if (useISO) {
                cal.minimalDaysInFirstWeek = 4
                cal.firstDayOfWeek = Calendar.MONDAY
                labelWeekNumber?.setText(R.string.week_number_iso)
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
                labelWeekNumber?.setText(R.string.week_number)
            }
            cal[Calendar.DAY_OF_MONTH] = cal[Calendar.DAY_OF_MONTH]
            if (labelWeekNumber != null && appendColon) {
                labelWeekNumber.append(":")
            }
        }
    }
}
