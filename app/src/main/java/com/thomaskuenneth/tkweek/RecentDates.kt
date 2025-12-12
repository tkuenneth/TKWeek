/*
 * RecentDates.kt
 *
 * Copyright 2021 MATHEMA GmbH
 *           2022 - 2025 Thomas Künneth
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
package com.thomaskuenneth.tkweek

import android.content.Context
import android.widget.TextView
import com.thomaskuenneth.tkweek.util.Helper
import java.text.ParseException
import java.util.*

private const val numRecents = 3
private val recents = arrayOfNulls<String>(numRecents)

fun updateRecents(
    context: Context,
    key: String,
    textview1: TextView,
    textview2: TextView,
    textview3: TextView
) {
    val prefs = context.getSharedPreferences(
        key,
        Context.MODE_PRIVATE
    )
    for (i in 0 until numRecents) {
        recents[i] = prefs.getString(getRecentKey(i), Helper.DASHES)
    }
    Arrays.sort(recents)
    populateRecent(textview1, 0)
    populateRecent(textview2, 1)
    populateRecent(textview3, 2)
}

fun addDate(context: Context, key: String, date: Date) {
    val string = Helper.FORMAT_YYYYMMDD.format(date)
    var found = false
    var i = 0
    while (i < numRecents) {
        if (string == recents[i]) {
            found = true
            break
        }
        i += 1
    }
    if (!found) {
        val prefs = context.getSharedPreferences(
            key,
            Context.MODE_PRIVATE
        )
        val e = prefs.edit()
        var pos = prefs.getInt("recent_next", 0)
        e.putString(getRecentKey(pos), string)
        if (++pos >= 3) {
            pos = 0
        }
        e.putInt("recent_next", pos)
        e.apply()
    }
}

private fun populateRecent(tv: TextView, pos: Int) {
    var date: Date? = null
    recents[pos]?.let { str ->
        try {
            date = Helper.FORMAT_YYYYMMDD.parse(str)
            date?.let {
                tv.text = Helper.FORMAT_DATE_SHORT.format(it)
            }
        } catch (_: ParseException) {
            tv.text = Helper.DASHES
        }
    }
    tv.tag = date
    tv.isClickable = date != null
}

private fun getRecentKey(pos: Int): String {
    return "recent_$pos"
}
