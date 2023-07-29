/*
 * TKWeekBaseActivity.kt
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
package com.thomaskuenneth.tkweek.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.fragment.TKWeekBaseFragment
import com.thomaskuenneth.tkweek.util.TKWeekUtils.RQ_TKWEEK_PREFS

abstract class TKWeekBaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.module_container)
        setSupportActionBar(findViewById(R.id.actionBar))
        supportActionBar?.setDisplayHomeAsUpEnabled(wantsHomeItem())
    }

    @Deprecated("Deprecated in Java")
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
            supportFragmentManager.findFragmentByTag(getString(R.string.tag_module_fragment)) as TKWeekBaseFragment<*>?
        fragment?.preferencesFinished(resultCode, data)
    }
}
