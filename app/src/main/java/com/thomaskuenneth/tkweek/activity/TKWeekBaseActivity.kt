/*
 * TKWeekBaseActivity.kt
 *
 * TKWeek (c) Thomas Künneth 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.fragment.TAG_MODULE_FRAGMENT
import com.thomaskuenneth.tkweek.fragment.TKWeekBaseFragment
import com.thomaskuenneth.tkweek.util.TKWeekUtils.RQ_TKWEEK_PREFS

abstract class TKWeekBaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(wantsHomeItem())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RQ_TKWEEK_PREFS) {
            preferencesFinished(resultCode, data)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (wantsPreferencesItem()) {
            menuInflater.inflate(R.menu.menu_preferences, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                TKWeekActivity.backToMain(this)
                true
            }
            R.id.annual_event_prefs -> {
                val i = Intent(this, TKWeekPrefsActivity::class.java)
                startActivityForResult(i, RQ_TKWEEK_PREFS)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    protected open fun wantsPreferencesItem(): Boolean {
        return true
    }

    protected abstract fun wantsHomeItem(): Boolean

    private fun preferencesFinished(resultCode: Int, data: Intent?) {
        val fragment =
            supportFragmentManager.findFragmentByTag(TAG_MODULE_FRAGMENT) as TKWeekBaseFragment<*>?
        fragment?.preferencesFinished(resultCode, data)
    }
}