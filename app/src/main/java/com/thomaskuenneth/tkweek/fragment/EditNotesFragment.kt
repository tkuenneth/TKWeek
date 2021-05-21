/*
 * EditNotesFragment.kt
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
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.thomaskuenneth.tkweek.R

class EditNotesFragment(val initial: String, private val callback: (String) -> Unit) :
    DialogFragment() {

    private lateinit var notes: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.enter_notes, null)
        notes = view.findViewById(R.id.notes)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.notes)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int -> callback(notes.text.toString()) }
            .setNegativeButton(android.R.string.cancel) { _: DialogInterface, _: Int -> }
            .create()
        notes.doAfterTextChanged {
            updateButton(dialog)
        }
        dialog.setOnShowListener {
            notes.setText(initial)
            updateButton(dialog)
        }
        return dialog
    }

    private fun updateButton(dialog: AlertDialog) {
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = notes.text.isNotEmpty()
    }

    companion object {
        val TAG = EditNotesFragment::class.simpleName
    }
}