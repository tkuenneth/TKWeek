/*
 * DatePickerFragment.kt
 *
 * Copyright 2021 MATHEMA GmbH
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

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.addDate
import com.thomaskuenneth.tkweek.databinding.DatepickerBinding
import com.thomaskuenneth.tkweek.updateRecents
import java.util.*

class DatePickerFragment(private val callback: DatePicker.OnDateChangedListener) :
    DialogFragment() {

    private lateinit var binding: DatepickerBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val cal = Calendar.getInstance()
        val listener: DatePicker.OnDateChangedListener =
            DatePicker.OnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
                cal.set(year, monthOfYear, dayOfMonth)
                addDate(
                    requireContext(),
                    RECENTS_KEY, cal.time
                )
                updateRecents()
            }
        binding = DatepickerBinding.inflate(layoutInflater, null, false)
        binding.datepicker.init(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH),
            listener
        )
        val ocl = View.OnClickListener {
            (it.tag as Date).let { date ->
                cal.time = date
                binding.datepicker.init(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH),
                    listener
                )
            }
        }
        binding.recentlyClicked.recent1.setOnClickListener(ocl)
        binding.recentlyClicked.recent2.setOnClickListener(ocl)
        binding.recentlyClicked.recent3.setOnClickListener(ocl)
        updateRecents()
        return AlertDialog.Builder(requireContext())
            .setView(binding.datepickerRoot)
            .setTitle(R.string.pick_a_date)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                callback.onDateChanged(
                    binding.datepicker,
                    binding.datepicker.year,
                    binding.datepicker.month,
                    binding.datepicker.dayOfMonth
                )
            }
            .setNegativeButton(android.R.string.cancel) { _: DialogInterface, _: Int -> }
            .create()
    }

    private fun updateRecents() {
        updateRecents(
            requireContext(),
            RECENTS_KEY,
            binding.recentlyClicked.recent1,
            binding.recentlyClicked.recent2,
            binding.recentlyClicked.recent3
        )
    }

    companion object {
        val TAG = DatePickerFragment::class.simpleName
    }
}