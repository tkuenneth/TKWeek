/*
 * PreferencesFragment.kt
 *
 * TKWeek (c) Thomas Künneth 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.fragment

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.thomaskuenneth.tkweek.types.Schulferien
import com.thomaskuenneth.tkweek.R

class PreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.tkweek_preferences)
        val lp = findPreference(SCHULFERIEN_BUNDESLAND) as ListPreference?
        val laender = Schulferien.getLaender(context)
        val length = laender.size + 1
        val entries = arrayOfNulls<String>(length)
        val values = arrayOfNulls<String>(length)
        entries[0] = getString(R.string.hide)
        values[0] = getString(R.string.hide)
        for (i in 1..laender.size) {
            entries[i] = laender[i - 1]
            values[i] = entries[i]
        }
        lp?.entries = entries
        lp?.entryValues = values
    }

    companion object {
        const val SCHULFERIEN_BUNDESLAND = "schulferien_bundesland"
    }
}