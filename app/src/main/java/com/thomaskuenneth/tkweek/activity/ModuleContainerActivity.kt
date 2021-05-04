/*
 * ModuleContainerActivity.kt
 *
 * TKWeek (c) Thomas Künneth 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.activity

import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.thomaskuenneth.tkweek.fragment.*

class ModuleContainerActivity : TKWeekBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.configuration.orientation
            == Configuration.ORIENTATION_LANDSCAPE
        ) {
            finish()
        }
        if (savedInstanceState == null) {
            intent?.extras?.run {
                get(CLAZZ)?.let {
                    val clazz = it as Class<*>
                    val fragment = clazz.newInstance() as Fragment
                    fragment.arguments = intent?.extras?.getBundle(PAYLOAD)
                    supportFragmentManager.beginTransaction()
                        .replace(
                            android.R.id.content,
                            fragment,
                            TAG_MODULE_FRAGMENT
                        )
                        .commit()
                }
                supportActionBar?.title = getString(TITLE)
            }
        }
    }

    override fun wantsHomeItem() = true
}
