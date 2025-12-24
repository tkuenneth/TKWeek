/*
 * DaysBetweenDatesFragment.kt
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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.thomaskuenneth.tkweek.CalendarAsyncTask
import com.thomaskuenneth.tkweek.databinding.DaysBetweenDatesActivityBinding
import com.thomaskuenneth.tkweek.util.DateUtilities
import com.thomaskuenneth.tkweek.util.Helper
import com.thomaskuenneth.tkweek.util.Helper.DATE
import java.util.Calendar
import java.util.Date

class DaysBetweenDatesFragment : TKWeekBaseFragment<DaysBetweenDatesActivityBinding>() {

    private val binding get() = backing!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        backing = DaysBetweenDatesActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.checkboxIncludeFirstDate
            .setOnCheckedChangeListener { _, _ -> update() }
        arguments?.run {
            if (containsKey(DATE)) {
                calFirstDate.time = Date(getLong(DATE))
                calSecondDate.time = Date()
            }
        }
        DateUtilities.clearTimeRelatedFields(calFirstDate)
        DateUtilities.clearTimeRelatedFields(calSecondDate)
        if (calFirstDate.after(calSecondDate)) {
            val temp: Calendar = calFirstDate
            calFirstDate = calSecondDate
            calSecondDate = temp
        }
        binding.firstDate.setOnClickListener {
            showDatePicker(calFirstDate) { time ->
                calFirstDate.timeInMillis = time
                update()
            }
        }
        binding.firstDateToday.setOnClickListener {
            calFirstDate.timeInMillis = MaterialDatePicker.todayInUtcMilliseconds()
            update()
        }
        binding.secondDate.setOnClickListener {
            showDatePicker(calSecondDate) { time ->
                calSecondDate.timeInMillis = time
                update()
            }
        }
        binding.secondDateToday.setOnClickListener {
            calSecondDate.timeInMillis = MaterialDatePicker.todayInUtcMilliseconds()
            update()
        }
        update()
    }

    private fun update() {
        binding.firstDate.text = Helper.FORMAT_EEE_D_MMM_YYYY.format(calFirstDate.time)
        binding.secondDate.text = Helper.FORMAT_EEE_D_MMM_YYYY.format(calSecondDate.time)
        var c1 = calFirstDate.clone() as Calendar
        var c2 = calSecondDate.clone() as Calendar
        if (c2.before(c1)) {
            val temp = c1
            c1 = c2
            c2 = temp
        }
        binding.firstDate.isEnabled = true
        binding.secondDate.isEnabled = true
        binding.firstDateToday.isEnabled = !DateUtilities.isToday(calFirstDate)
        binding.secondDateToday.isEnabled = !DateUtilities.isToday(calSecondDate)
        CalendarAsyncTask(requireContext(), binding).execute(c1, c2)
    }

    private fun showDatePicker(cal: Calendar, onDateSelected: (Long) -> Unit) {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setSelection(cal.timeInMillis)
            .build()
        picker.addOnPositiveButtonClickListener(onDateSelected)
        picker.show(parentFragmentManager, "date_picker")
    }

    companion object {
        private var calFirstDate = Calendar.getInstance()
        private var calSecondDate = Calendar.getInstance()
    }
}
