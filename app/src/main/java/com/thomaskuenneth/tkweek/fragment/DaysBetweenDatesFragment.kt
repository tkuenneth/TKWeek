/*
 * DaysBetweenDatesFragment.kt
 *
 * TKWeek (c) Thomas Künneth 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thomaskuenneth.tkweek.CalendarAsyncTask
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.activity.TKWeekActivity
import com.thomaskuenneth.tkweek.databinding.DaysBetweenDatesActivityBinding
import com.thomaskuenneth.tkweek.util.DateUtilities
import java.util.*

class DaysBetweenDatesFragment : TKWeekBaseFragment<DaysBetweenDatesActivityBinding>() {

    private val binding get() = backing!!

    private var includeFirstDay = false

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
            val datePickerFragment =
                DatePickerFragment { _, year, month, dayOfMonth ->
                    calFirstDate = DateUtilities.getCalendar(year, month, dayOfMonth)
                    update()
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
            val datePickerFragment =
                DatePickerFragment { _, year, month, dayOfMonth ->
                    calSecondDate = DateUtilities.getCalendar(year, month, dayOfMonth)
                    update()
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
        binding.daysBetweenDatesTotal.visibility = View.INVISIBLE
        binding.daysBetweenDatesProgressbar.visibility = View.VISIBLE
        binding.daysBetweenDatesFromTo.text = getString(
            R.string.days_from_to,
            TKWeekActivity.FORMAT_DEFAULT.format(c1.time),
            TKWeekActivity.FORMAT_DEFAULT.format(c2.time)
        )
        includeFirstDay = binding.checkboxIncludeFirstDate.isChecked
        CalendarAsyncTask(requireContext(), binding).execute(c1, c2)
    }

    companion object {
        private var calFirstDate = Calendar.getInstance()
        private var calSecondDate = Calendar.getInstance()
    }
}
