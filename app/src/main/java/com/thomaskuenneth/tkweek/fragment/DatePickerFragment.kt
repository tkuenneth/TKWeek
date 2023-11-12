/*
 * DatePickerFragment.kt
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

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.addDate
import com.thomaskuenneth.tkweek.databinding.DatepickerBinding
import com.thomaskuenneth.tkweek.updateRecents
import java.util.Calendar
import java.util.Date

const val RESULT_DATEPICKER = "resultDatePicker"
const val ARGS_PICKER = "picker"
const val ARGS_YEAR = "year"
const val ARGS_MONTH = "month"
const val ARGS_DAY_OF_MONTH = "day"

class DatePickerFragment : DialogFragment() {

    private lateinit var binding: DatepickerBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val cal = Calendar.getInstance()
        arguments?.run {
            cal.set(Calendar.YEAR, getInt(ARGS_YEAR, cal.get(Calendar.YEAR)))
            cal.set(Calendar.MONTH, getInt(ARGS_MONTH, cal.get(Calendar.MONTH)))
            cal.set(
                Calendar.DAY_OF_MONTH,
                getInt(ARGS_DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH))
            )
        }
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
                Bundle().also { bundle ->
                    bundle.putInt(ARGS_PICKER, requireArguments().getInt(ARGS_PICKER))
                    bundle.putInt(ARGS_YEAR, cal.get(Calendar.YEAR))
                    bundle.putInt(ARGS_MONTH, cal.get(Calendar.MONTH))
                    bundle.putInt(ARGS_DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH))
                    setFragmentResult(RESULT_DATEPICKER, bundle)
                }
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