/*
 * Helper.kt
 *
 * Copyright 2022 - 2025 Thomas Künneth
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
package com.thomaskuenneth.tkweek.util

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import android.widget.DatePicker
import androidx.preference.PreferenceManager
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.ui.TKWeekCompose
import com.thomaskuenneth.tkweek.fragment.CLAZZ
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

private const val TAG = "Helper"

private const val INFINITY_SYMBOL = "infinity_symbol"

object Helper {

    const val EXTRA_MODULE = "module"

    const val DASHES = "---"

    const val MINUTES_PER_DAY = 24 * 60

    @JvmField
    val FORMAT_DDMMYY = SimpleDateFormat(
        "dd.MM.yy", Locale.US
    )

    @JvmField
    val FORMAT_YYYYMMDD: DateFormat = SimpleDateFormat(
        "yyyyMMdd", Locale.US
    )

    @JvmField
    val FORMAT_FULL: DateFormat = DateFormat.getDateInstance(DateFormat.FULL)

    @JvmField
    val FORMAT_DEFAULT: DateFormat = DateFormat.getDateInstance()

    @JvmField
    val FORMAT_DATE_SHORT: DateFormat = DateFormat.getDateInstance(DateFormat.SHORT)

    @JvmField
    val FORMAT_TIME_SHORT: DateFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)

    @JvmField
    val FORMAT_DAY_OF_WEEK: DateFormat = SimpleDateFormat(
        "EEEE", Locale.getDefault()
    )

    @JvmField
    val FORMAT_DAY_OF_WEEK_SHORT: DateFormat = SimpleDateFormat(
        "EEE", Locale.getDefault()
    )

    @JvmField
    val FORMAT_MONTH: DateFormat = SimpleDateFormat(
        "MMMM",
        Locale.getDefault()
    )

    @JvmField
    val FORMAT_MONTH_SHORT: DateFormat = SimpleDateFormat(
        "MMM", Locale.getDefault()
    )

    @JvmField
    val FORMAT_YYMM: DateFormat = SimpleDateFormat("MMdd", Locale.US)

    @JvmField
    val FORMAT_DATE_TIME_SHORT: DateFormat = DateFormat
        .getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

    @JvmStatic
    fun startActivityClearTopNewTask(
        context: Context,
        clazz: Class<*>?
    ) {
        val intent = Intent(context, clazz)
        intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP
                or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun configureDatePicker(dp: DatePicker) {
        dp.minDate = dp.minDate
        dp.maxDate = dp.maxDate
    }

    @JvmStatic
    fun getInfinitySymbol(context: Context): String? {
        val prefs = PreferenceManager
            .getDefaultSharedPreferences(context)
        return prefs.getString(
            INFINITY_SYMBOL,
            context.getString(R.string.infinity)
        )
    }

    @JvmStatic
    fun setInfinitySymbol(context: Context, value: String?) {
        putString(context, INFINITY_SYMBOL, value)
    }

    @JvmStatic
    fun putString(context: Context, key: String?, value: String?) {
        val prefs = PreferenceManager
            .getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.putString(key, value)
        editor.apply()
    }

    @JvmStatic
    fun putInt(context: Context, key: String?, value: Int) {
        val prefs = PreferenceManager
            .getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    @JvmStatic
    fun updateWidgets(context: Context?, widgetClasses: Array<Class<*>>) {
        val m = AppWidgetManager.getInstance(context)
        if (m != null) {
            for (widgetClass in widgetClasses) {
                val appWidgetIds = m.getAppWidgetIds(
                    ComponentName(
                        context!!, widgetClass
                    )
                )
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                    try {
                        val method = widgetClass.getMethod(
                            "updateWidgets",
                            Context::class.java,
                            AppWidgetManager::class.java, IntArray::class.java
                        )
                        method.invoke(null, context, m, appWidgetIds)
                    } catch (t: Throwable) {
                        Log.e(TAG, "updateWidgets()", t)
                    }
                }
            }
        }
    }

    @JvmStatic
    fun getIntFromSharedPreferences(
        prefs: SharedPreferences,
        key: String?, defaultValue: Int
    ): Int {
        var result = defaultValue
        val string = prefs.getString(key, defaultValue.toString())
        try {
            result = string!!.toInt()
        } catch (tr: Throwable) {
            Log.e(TAG, "getIntFromSharedPreferences()", tr)
        }
        return result
    }

    @JvmStatic
    fun createPendingIntentToLaunchTKWeek(
        context: Context,
        requestCode: Int,
        clazz: Class<*>
    ): PendingIntent {
        val intent = Intent(context, TKWeekCompose::class.java)
        intent.putExtra(CLAZZ, clazz.name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent.identifier = UUID.randomUUID().toString()
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        return PendingIntent.getActivity(
            context, requestCode,
            intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
