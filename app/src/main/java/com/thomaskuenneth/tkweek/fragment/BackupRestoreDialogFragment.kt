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