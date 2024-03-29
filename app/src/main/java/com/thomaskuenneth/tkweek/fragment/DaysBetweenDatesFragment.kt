/*
 * DaysBetweenDatesFragment.kt
 *
 * Copyright 2021 MATHEMA GmbH
 *           2022 - 2023 Thomas Künneth
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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import com.thomaskuenneth.tkweek.CalendarAsyncTask
import com.thomaskuenneth.tkweek.activity.TKWeekActivity
import com.thomaskuenneth.tkweek.databinding.DaysBetweenDatesActivityBinding
import com.thomaskuenneth.tkweek.util.DateUtilities
import java.util.Calendar
import java.util.Date

private const val FIRST_PICKER = 1
private const val SECOND_PICKER = 2

class DaysBetweenDatesFragment : TKWeekBaseFragment<DaysBetweenDatesActivityBinding>() {

    private val binding get() = backing!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(RESULT_DATEPICKER) { _, bundle ->
            val date = DateUtilities.getCalendar(
                bundle.getInt(ARGS_YEAR),
                bundle.getInt(ARGS_MONTH),
                bundle.getInt(ARGS_DAY_OF_MONTH)
            )
            when (bundle.getInt(ARGS_PICKER)) {
                FIRST_PICKER -> calFirstDate = date
                SECOND_PICKER -> calSecondDate = date
            }
            update()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        backing = DaysBetweenDatesActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
        binding.firstDateToday.setOnClickListener {
            calFirstDate.time = Date()
            update()
        }
        binding.firstDatePick.setOnClickListener {
            val datePickerFragment = DatePickerFragment().also {
                it.arguments = Bundle().also { bundle ->
                    bundle.putInt(ARGS_PICKER, FIRST_PICKER)
                    bundle.putInt(ARGS_YEAR, calFirstDate.get(Calendar.YEAR))
                    bundle.putInt(ARGS_MONTH, calFirstDate.get(Calendar.MONTH))
                    bundle.putInt(ARGS_DAY_OF_MONTH, calFirstDate.get(Calendar.DAY_OF_MONTH))
                }
            }
            datePickerFragment.show(
                parentFragmentManager,
                DatePickerFragment.TAG
            )
        }
        binding.secondDateToday.setOnClickListener {
            calSecondDate.time = Date()
            update()
        }
        binding.secondDatePick.setOnClickListener {
            val datePickerFragment = DatePickerFragment().also {
                it.arguments = Bundle().also { bundle ->
                    bundle.putInt(ARGS_PICKER, SECOND_PICKER)
                    bundle.putInt(ARGS_YEAR, calSecondDate.get(Calendar.YEAR))
                    bundle.putInt(ARGS_MONTH, calSecondDate.get(Calendar.MONTH))
                    bundle.putInt(ARGS_DAY_OF_MONTH, calSecondDate.get(Calendar.DAY_OF_MONTH))
                }
            }
            datePickerFragment.show(
                parentFragmentManager,
                DatePickerFragment.TAG
            )
        }
        update()
    }

    override fun preferencesFinished(resultCode: Int, data: Intent?) {
        update()
    }

    private fun update() {
        binding.firstDate.text = TKWeekActivity.FORMAT_DEFAULT.format(calFirstDate.time)
        binding.secondDate.text = TKWeekActivity.FORMAT_DEFAULT.format(calSecondDate.time)
        var c1 = calFirstDate.clone() as Calendar
        var c2 = calSecondDate.clone() as Calendar
        if (c2.before(c1)) {
            val temp = c1
            c1 = c2
            c2 = temp
        }
        binding.firstDatePick.isEnabled = false
        binding.firstDateToday.isEnabled = false
        binding.secondDatePick.isEnabled = false
        binding.secondDateToday.isEnabled = false
        CalendarAsyncTask(requireContext(), binding).execute(c1, c2)
    }

    companion object {
        private var calFirstDate = Calendar.getInstance()
        private var calSecondDate = Calendar.getInstance()
    }
}
