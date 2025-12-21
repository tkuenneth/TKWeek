/*
 * CalendarFragment.kt
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
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT of OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.thomaskuenneth.tkweek.fragment

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.color.MaterialColors
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.adapter.AnnualEventsListAdapter
import com.thomaskuenneth.tkweek.adapter.MonthsAdapter
import com.thomaskuenneth.tkweek.addDate
import com.thomaskuenneth.tkweek.databinding.CalendarBinding
import com.thomaskuenneth.tkweek.fragment.WeekFragment.Companion.prepareCalendar
import com.thomaskuenneth.tkweek.preference.PickBusinessDaysPreference
import com.thomaskuenneth.tkweek.types.Event
import com.thomaskuenneth.tkweek.updateRecents
import com.thomaskuenneth.tkweek.util.DateUtilities
import com.thomaskuenneth.tkweek.util.Helper
import com.thomaskuenneth.tkweek.util.Helper.DATE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

const val KEY_RECENT_DATES = "CalendarFragment"
private const val TAG = "CalendarFragment"

class CalendarFragment : TKWeekBaseFragment<CalendarBinding>(), View.OnClickListener {

    private val binding get() = backing!!

    private lateinit var days: MutableList<TextView>
    private lateinit var monthsAdapter: MonthsAdapter

    private var listAdapter: AnnualEventsListAdapter? = null
    private var loadEventsJob: Job? = null

    private val dayOffListener = OnLongClickListener { v: View ->
        (v.tag as? Date)?.let {
            setDayOff(
                requireContext(), it, !isDayOff(requireContext(), it)
            )
            updateCalendar()
        }
        true
    }

    private val dayClickedListener = View.OnClickListener { v: View ->
        (v.tag as? Date)?.let {
            addDate(requireContext(), KEY_RECENT_DATES, it)
            updateRecentDates()
            val payload = Bundle()
            payload.putLong(DATE, it.time)
            selectModule(MyDayFragment::class.java, payload)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        backing = CalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.calendarYear.setOnClickListener {
            var year = try {
                binding.calendarYear.text.toString().toInt()
            } catch (_: NumberFormatException) {
                cal.get(Calendar.YEAR)
            }
            if (year < 0) year = 0
            if (year > 2100) year = 2100
            binding.calendarYear.text = "$year"
            requireContext().getSystemService(InputMethodManager::class.java).run {
                hideSoftInputFromWindow(binding.calendarYear.windowToken, 0)
            }
            cal.set(Calendar.YEAR, year)
            update()
        }
        binding.calendarDown.setOnClickListener(this)
        binding.calendarUp.setOnClickListener(this)
        binding.calendarToday.setOnClickListener {
            cal.time = Date()
            update()
        }
        monthsAdapter = MonthsAdapter(requireContext()) { position ->
            cal[Calendar.MONTH] = position
            updateCalendar()
            binding.calendarGallery.smoothScrollToPosition(position)
        }
        binding.calendarGallery.adapter = monthsAdapter
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.calendarGallery.layoutManager = layoutManager
        days = mutableListOf()
        days.add(binding.calendar11)
        days.add(binding.calendar12)
        days.add(binding.calendar13)
        days.add(binding.calendar14)
        days.add(binding.calendar15)
        days.add(binding.calendar16)
        days.add(binding.calendar17)
        days.add(binding.calendar18)
        days.add(binding.calendar21)
        days.add(binding.calendar22)
        days.add(binding.calendar23)
        days.add(binding.calendar24)
        days.add(binding.calendar25)
        days.add(binding.calendar26)
        days.add(binding.calendar27)
        days.add(binding.calendar28)
        days.add(binding.calendar31)
        days.add(binding.calendar32)
        days.add(binding.calendar33)
        days.add(binding.calendar34)
        days.add(binding.calendar35)
        days.add(binding.calendar36)
        days.add(binding.calendar37)
        days.add(binding.calendar38)
        days.add(binding.calendar41)
        days.add(binding.calendar42)
        days.add(binding.calendar43)
        days.add(binding.calendar44)
        days.add(binding.calendar45)
        days.add(binding.calendar46)
        days.add(binding.calendar47)
        days.add(binding.calendar48)
        days.add(binding.calendar51)
        days.add(binding.calendar52)
        days.add(binding.calendar53)
        days.add(binding.calendar54)
        days.add(binding.calendar55)
        days.add(binding.calendar56)
        days.add(binding.calendar57)
        days.add(binding.calendar58)
        days.add(binding.calendar61)
        days.add(binding.calendar62)
        days.add(binding.calendar63)
        days.add(binding.calendar64)
        days.add(binding.calendar65)
        days.add(binding.calendar66)
        days.add(binding.calendar67)
        days.add(binding.calendar68)
        days.add(binding.calendar71)
        days.add(binding.calendar72)
        days.add(binding.calendar73)
        days.add(binding.calendar74)
        days.add(binding.calendar75)
        days.add(binding.calendar76)
        days.add(binding.calendar77)
        days.add(binding.calendar78)
        binding.calendarLayoutRecent.recent1.setOnClickListener(dayClickedListener)
        binding.calendarLayoutRecent.recent2.setOnClickListener(dayClickedListener)
        binding.calendarLayoutRecent.recent3.setOnClickListener(dayClickedListener)

        for (day in days) {
            with(day) {
                setOnClickListener(dayClickedListener)
                setOnLongClickListener(dayOffListener)
            }
        }
        prepareCalendar(cal, requireContext())
        update()
    }

    override fun onResume() {
        super.onResume()
        updateRecentDates()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(
            TAG, Helper.FORMAT_DEFAULT.format(cal.time)
        )
    }

    override fun onClick(v: View) {
        if (binding.calendarDown == v) {
            cal.add(Calendar.YEAR, -1)
        } else if (binding.calendarUp == v) {
            cal.add(Calendar.YEAR, 1)
        }
        update()
    }

    private fun update() {
        binding.calendarYear.text = "${cal[Calendar.YEAR]}"
        binding.calendarGallery.scrollToPosition(cal[Calendar.MONTH])
        updateCalendar()
    }

    private fun load(year: Int) {
        listAdapter?.run {
            if (year >= from[Calendar.YEAR] && year <= to[Calendar.YEAR]) {
                return
            }
        }
        loadEventsJob?.cancel()
        loadEventsJob = lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                val calFrom = DateUtilities.getCalendar(year, Calendar.JANUARY, 1)
                val calTo = DateUtilities.getCalendar(year, Calendar.DECEMBER, 31)
                AnnualEventsListAdapter(
                    requireContext(), calFrom, calTo, false, null
                )
            }
            listAdapter = result
            loadEventsJob = null
            updateFromListAdapter()
        }
    }

    private fun updateCalendar() {
        monthsAdapter.updateSelectedPosition(cal[Calendar.MONTH])
        val defaultColor = binding.calendarLayoutRecent.recent1.textColors
        val activeColor = MaterialColors.getColor(
            requireContext(), com.google.android.material.R.attr.colorOnBackground, Color.GREEN
        )
        val backgroundColor = MaterialColors.getColor(
            requireContext(), com.google.android.material.R.attr.colorSurface, Color.GREEN
        )
        val accentColor = MaterialColors.getColor(
            requireContext(), com.google.android.material.R.attr.colorSecondary, Color.GREEN
        )
        var businessDays = 0
        var daysOff = 0
        val prefs = requireContext().getSharedPreferences(
            PickBusinessDaysPreference.getTag(), Context.MODE_PRIVATE
        )
        val temp = DateUtilities.getCalendar(
            cal[Calendar.YEAR], cal[Calendar.MONTH], 1
        )
        prepareCalendar(temp, requireContext())
        DateUtilities.clearTimeRelatedFields(temp)
        // back to the beginning of the week
        while (temp[Calendar.DAY_OF_WEEK] != temp.firstDayOfWeek) {
            temp.add(Calendar.DAY_OF_MONTH, -1)
        }
        // go back 7 days to set the weekdays
        temp.add(Calendar.DAY_OF_MONTH, -7)
        for (i in 1..7) {
            days[i].tag = null
            days[i].text = "${
                Helper.FORMAT_DAY_OF_WEEK_SHORT.format(
                    temp.time
                )[0]
            }"
            val dayOfWeek = temp[Calendar.DAY_OF_WEEK]
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                days[i].setTextColor(Color.RED)
            } else {
                days[i].setTextColor(defaultColor)
            }
            temp.add(Calendar.DAY_OF_MONTH, 1)
        }
        var color: Int
        val month = cal[Calendar.MONTH]
        for (week in 1..6) {
            days[week * 8].tag = null
            days[week * 8].setTextColor(defaultColor)
            days[week * 8].text = "${temp[Calendar.WEEK_OF_YEAR]}"
            for (day in 1..7) {
                val pos = day + week * 8
                days[pos].tag = temp.time
                var dayOff: Boolean
                if (temp[Calendar.MONTH] == month) {
                    // business day?
                    val weekday = temp[Calendar.DAY_OF_WEEK]
                    if (prefs.getBoolean(
                            weekday.toString(), PickBusinessDaysPreference.getDefault(weekday)
                        )
                    ) {
                        businessDays += 1
                    }
                    // day off?
                    dayOff = isDayOff(requireContext(), temp.time)
                    if (dayOff) {
                        daysOff += 1
                        color = accentColor
                    } else {
                        color = activeColor
                    }
                } else {
                    color = defaultColor.defaultColor
                }
                if (DateUtilities.isToday(temp)) {
                    days[pos].background =
                        ResourcesCompat.getDrawable(resources, R.drawable.custom_list_item, null)
                    days[pos].backgroundTintList = ColorStateList.valueOf(color)
                    days[pos].setTextColor(backgroundColor)
                } else {
                    days[pos].setBackgroundColor(Color.TRANSPARENT)
                    days[pos].setTextColor(color)
                }
                days[pos].text = "${temp[Calendar.DAY_OF_MONTH]}"
                temp.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        binding.calendarNumberOfBusinessDays.text = getString(
            R.string.calendar_number_of_business_days, businessDays
        )
        binding.calendarNumberDaysOff.text = getString(
            R.string.calendar_number_days_off, daysOff
        )
        load(cal[Calendar.YEAR])
        updateFromListAdapter()
    }

    private fun updateFromListAdapter() {
        listAdapter?.also { adapter ->
            val holidayEvents = (0 until adapter.count)
                .map { adapter.getItem(it) as Event }
                .filter { AnnualEventsFragment.isHoliday(requireContext(), it) }
                .map { Helper.FORMAT_YYYYMMDD.format(DateUtilities.getCalendar(it).time) }
                .toSet()
            if (holidayEvents.isNotEmpty()) {
                for (i in 8 until days.size) {
                    val day = days[i]
                    (day.tag as? Date)?.let { date ->
                        if (holidayEvents.contains(Helper.FORMAT_YYYYMMDD.format(date))) {
                            day.setTextColor(Color.RED)
                        }
                    }
                }
            }
        }
    }

    companion object {

        val cal: Calendar = Calendar.getInstance()

        @JvmStatic
        fun setDayOff(context: Context, date: Date, dayOff: Boolean) {
            val prefs = context.getSharedPreferences(
                TAG, Context.MODE_PRIVATE
            )
            prefs.edit {
                putBoolean(Helper.FORMAT_YYYYMMDD.format(date), dayOff)
            }
        }

        /**
         * Check if a date is flagged as a day off
         *
         * @param context context
         * @param date    date
         * @return `true` if the date is flagged as a day off
         */
        @JvmStatic
        fun isDayOff(context: Context, date: Date): Boolean {
            val prefs = context.getSharedPreferences(
                TAG, Context.MODE_PRIVATE
            )
            return prefs.getBoolean(Helper.FORMAT_YYYYMMDD.format(date), false)
        }
    }

    private fun updateRecentDates() {
        updateRecents(
            requireContext(),
            KEY_RECENT_DATES,
            binding.calendarLayoutRecent.recent1,
            binding.calendarLayoutRecent.recent2,
            binding.calendarLayoutRecent.recent3
        )
    }
}
