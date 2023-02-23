/*
 * DateCalculatorFragment.kt
 *
 * TKWeek (c) Thomas Künneth 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.DatePicker
import android.widget.DatePicker.OnDateChangedListener
import android.widget.EditText
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.activity.TKWeekActivity
import com.thomaskuenneth.tkweek.databinding.DateCalculatorActivityBinding
import com.thomaskuenneth.tkweek.preference.PickBusinessDaysPreference
import com.thomaskuenneth.tkweek.util.DateUtilities.setMinDate
import java.util.*

class DateCalculatorFragment : TKWeekBaseFragment<DateCalculatorActivityBinding>(),
    OnDateChangedListener {

    private val binding get() = backing!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        backing = DateCalculatorActivityBinding.inflate(inflater, container, false)
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
        view: DatePicker?, year: Int, monthOfYear: Int,
        dayOfMonth: Int
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
            for (i in 0 until numberOfDays) {
                do {
                    temp.add(Calendar.DAY_OF_MONTH, offset)
                    weekday = temp[Calendar.DAY_OF_WEEK]
                } while (!prefs.getBoolean(
                        weekday.toString(),
                        PickBusinessDaysPreference.getDefault(weekday)
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
            cal[Calendar.YEAR], cal[Calendar.MONTH],
            cal[Calendar.DAY_OF_MONTH], this
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
        }
        return result
    }

    companion object {
        val cal: Calendar = Calendar.getInstance()
    }
}