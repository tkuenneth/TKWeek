/*
 * EditNotesFragment.kt
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
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.thomaskuenneth.tkweek.R

const val ARGS_NOTES = "initial"
const val RESULT_NOTES = "resultNotes"

class EditNotesFragment : DialogFragment() {

    private lateinit var notes: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.enter_notes, null)
        notes = view.findViewById(R.id.notes)
        val initialNote = requireArguments().getString(ARGS_NOTES, "")
        val title = if (initialNote.isEmpty()) R.string.add_note else R.string.edit_note
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                Bundle().also {
                    it.putString(ARGS_NOTES, notes.text.toString())
                    setFragmentResult(RESULT_NOTES, it)
                }
            }
            .setNegativeButton(android.R.string.cancel) { _: DialogInterface, _: Int -> }
            .create()
        notes.doAfterTextChanged {
            updateButton(dialog)
        }
        dialog.setOnShowListener {
            notes.setText(initialNote)
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
