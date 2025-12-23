/*
 * NewEventFragment.kt
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

import android.app.Dialog
import android.os.Bundle
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.util.DateUtilities
import com.thomaskuenneth.tkweek.util.Helper.DATE

const val REQUEST_KEY_NEW_EVENT_FRAGMENT = "NewEventFragment"
const val ANNUALLY_REPEATING = "annuallyRepeatring"
const val DESCR = "descr"

class NewEventFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.new_event, null)
        val picker = view.findViewById<DatePicker>(R.id.new_event_date)
        val descr = view.findViewById<EditText>(R.id.new_event_descr)
        val annuallyRepeating = view.findViewById<CheckBox>(R.id.new_event_annually_repeating)
        descr.requestFocus()
        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle(getString(R.string.new_event))
            .setPositiveButton(
                android.R.string.ok
            ) { _, _ ->
                val result = Bundle()
                result.putSerializable(
                    DATE,
                    DateUtilities.getCalendar(picker.year, picker.month, picker.dayOfMonth).time
                )
                val s = descr.text.toString()
                result.putString(
                    DESCR,
                    s.ifEmpty { getString(R.string.no_description) }
                )
                result.putBoolean(ANNUALLY_REPEATING, annuallyRepeating.isChecked)
                setFragmentResult(REQUEST_KEY_NEW_EVENT_FRAGMENT, result)
            }
            .setNegativeButton(
                android.R.string.cancel
            ) { _, _ -> }
            .create()
    }
}
