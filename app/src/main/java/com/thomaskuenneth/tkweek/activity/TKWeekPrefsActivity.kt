/*
 * TKWeekPrefsActivity.kt
 * 
 * TKWeek (c) Thomas Künneth 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.activity

import android.os.Bundle
import com.thomaskuenneth.tkweek.fragment.PreferencesFragment

class TKWeekPrefsActivity : TKWeekBaseActivity() {

    override fun wantsHomeItem() = true

    override fun wantsPreferencesItem() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, PreferencesFragment())
                .commit()
        }
    }
}