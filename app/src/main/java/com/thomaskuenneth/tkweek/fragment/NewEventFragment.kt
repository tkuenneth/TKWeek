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
                    if (s.isNotEmpty()) s else getString(R.string.no_description)
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