/*
 * DateCalculatorFragment.kt
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

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.DatePicker
import android.widget.DatePicker.OnDateChangedListener
import android.widget.EditText
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.activity.TKWeekActivity
import com.thomaskuenneth.tkweek.databinding.DateCalculatorBinding
import com.thomaskuenneth.tkweek.preference.PickBusinessDaysPreference
import com.thomaskuenneth.tkweek.util.DateUtilities.setMinDate
import java.util.*

class DateCalculatorFragment : TKWeekBaseFragment<DateCalculatorBinding>(), OnDateChangedListener {

    private val binding get() = backing!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        backing = DateCalculatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateDatePicker()
        binding.dateCalculatorResult.setText(R.string.date_calculator_info)
        binding.dateCalculatorAdd.setOnClickListener { update(false) }
        binding.dateCalculatorSubtract.setOnClickListener { update(true) }
        binding.dateCalculatorClear.setOnClickListener {
            binding.days.text = null
            binding.weeks.text = null
            binding.months.text = null
            binding.years.text = null
        }
        setMinDate(binding.dateCalculatorDatepicker)
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
                updateDatePicker()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDateChanged(
        view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int
    ) {
        cal[Calendar.YEAR] = year
        cal[Calendar.MONTH] = monthOfYear
        cal[Calendar.DAY_OF_MONTH] = dayOfMonth
    }

    private fun update(subtract: Boolean) {
        val temp = cal.clone() as Calendar
        temp.add(Calendar.WEEK_OF_YEAR, getInt(binding.weeks, subtract))
        temp.add(Calendar.MONTH, getInt(binding.months, subtract))
        temp.add(Calendar.YEAR, getInt(binding.years, subtract))
        if (binding.dateCalculatorCountBusinessDaysOnly.isChecked) {
            val prefs: SharedPreferences = requireContext().getSharedPreferences(
                PickBusinessDaysPreference.getTag(), Context.MODE_PRIVATE
            )
            val numberOfDays = getInt(binding.days, false)
            val offset = if (subtract) -1 else 1
            var weekday: Int
            (0 until numberOfDays).forEach { i ->
                do {
                    temp.add(Calendar.DAY_OF_MONTH, offset)
                    weekday = temp[Calendar.DAY_OF_WEEK]
                } while (!prefs.getBoolean(
                        weekday.toString(), PickBusinessDaysPreference.getDefault(weekday)
                    )
                )
            }
        } else {
            temp.add(Calendar.DAY_OF_MONTH, getInt(binding.days, subtract))
        }
        binding.dateCalculatorResult.text = TKWeekActivity.FORMAT_FULL.format(temp.time)
        if (binding.dateCalculatorReuseResult.isChecked) {
            cal.time = temp.time
            updateDatePicker()
        }
    }

    private fun updateDatePicker() {
        binding.dateCalculatorDatepicker.init(
            cal[Calendar.YEAR], cal[Calendar.MONTH], cal[Calendar.DAY_OF_MONTH], this
        )
    }

    private fun getInt(view: EditText, subtract: Boolean): Int {
        var result = 0
        try {
            result = view.text.toString().toInt()
            if (subtract) {
                result *= -1
            }
        } catch (thr: Throwable) {
            Log.e(TAG, "getInt()", thr)
        }
        return result
    }

    companion object {
        val TAG: String = DateCalculatorFragment::class.java.simpleName
        val cal: Calendar = Calendar.getInstance()
    }
}