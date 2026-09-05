/*
 * BackupRestoreDialogFragment.kt
 *
 * Copyright 2022 - 2026 Thomas Künneth
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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.thomaskuenneth.tkweek.R

const val REQUEST_KEY_BACKUP_RESTORE_FRAGMENT = "BackupRestoreDialogFragment"
const val BACKUP_RESTORE = "backupRestore"
const val RESTORE = 5
const val BACKUP = 6

class BackupRestoreDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.annual_event_backup_restore)
            .setItems(
                R.array.annual_event_backup_restore
            ) { _, which ->
                when (which) {
                    0 -> backup()
                    1 -> restore()
                }
            }.create()
    }

    private fun backup() {
        val result = Bundle()
        result.putInt(BACKUP_RESTORE, BACKUP)
        setFragmentResult(REQUEST_KEY_BACKUP_RESTORE_FRAGMENT, result)
    }

    private fun restore() {
        val result = Bundle()
        result.putInt(BACKUP_RESTORE, RESTORE)
        setFragmentResult(REQUEST_KEY_BACKUP_RESTORE_FRAGMENT, result)
    }
}