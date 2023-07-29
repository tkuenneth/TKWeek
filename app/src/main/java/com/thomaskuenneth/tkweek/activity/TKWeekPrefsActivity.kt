/*
 * TKWeekPrefsActivity.kt
 * 
 * TKWeek (c) Thomas Künneth 2021 - 2023
 * All rights reserved.
 */
package com.thomaskuenneth.tkweek.activity

import android.os.Bundle
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.fragment.PreferencesFragment

class TKWeekPrefsActivity : TKWeekBaseActivity() {

    override fun wantsHomeItem() = true

    override fun wantsPreferencesItem() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
            .replace(R.id.module_content, PreferencesFragment())
            .commit()
    }
}
