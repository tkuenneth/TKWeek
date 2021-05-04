/*
 * TKWeekActivity.kt
 *
 * TKWeek (c) Thomas Künneth 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.activity

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.DatePicker
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import com.thomaskuenneth.tkweek.ActivityDescription
import com.thomaskuenneth.tkweek.BootCompleteReceiver
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.databinding.TkweekBinding
import com.thomaskuenneth.tkweek.preference.WidgetPreference
import com.thomaskuenneth.tkweek.util.TKWeekUtils
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "TKWeekActivity"

/**
 * Wird verwendet, um den Willkommen-Dialog zu identifizieren.
 */
private const val MESSAGE = 1

private const val INFINITY_SYMBOL = "infinity_symbol"

private const val TKWEEK = "TKWeek"

/**
 * Der aktuelle versionCode. Wird verwendet, um ggf. beim Start ein README
 * anzuzeigen.
 */
private const val VERSION_CODE = "versionCode"

/**
 * Die Hauptactivity der Anwendung. Sie stellt eine Auswahlliste dar, die zu den
 * Funktionen der Anwendung verzweigt. Falls sich seit dem letzten Start die
 * Versionsnummer geändert hat (oder die App zum ersten Mal aufgerufen wird),
 * wird ein Willkommen-Dialog angezeigt.
 *
 * @author Thomas Künneth
 */
class TKWeekActivity : TKWeekBaseActivity(),
    OnItemClickListener {

    private var backing: TkweekBinding? = null
    private val binding get() = backing!!
    private var storedVersionCode = 0
    private var currentVersionCode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backing = TkweekBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            // ggf. einen Willkommen-Dialog anzeigen
            if (isNewVersion()) {
                showDialog(MESSAGE)
            } else {
                requestReadCalendar(this)
            }
        }
    }

    override fun wantsHomeItem() = false

    override fun onItemClick(
        parent: AdapterView<*>, view: View?, position: Int,
        id: Long
    ) {
        val o = parent.adapter.getItem(position)
        if (o is ActivityDescription) {
            startActivityClearTopNewTask(this, o.fragment)
        }
    }

    private fun isNewVersion(): Boolean {
        readCurrentVersionFromPrefs()
        return storedVersionCode < currentVersionCode
    }

    //    @Override
//    protected Dialog onCreateDialog(int id) {
//        if (id == MESSAGE) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(TKWeekOldActivity.this);
//            builder.setTitle(R.string.welcome);
//            builder.setIcon(R.drawable.ic_tkweek_no_gradients);
//            View textView = getLayoutInflater().inflate(R.layout.welcome, null);
//            TextView tv1 = textView.findViewById(R.id.welcome_tv1);
//            tv1.setText(getString(R.string.welcome_text,
//                    getString(R.string.my_email)));
//            builder.setView(textView);
//            builder.setPositiveButton(R.string.alert_dialog_continue,
//                    (dialog, which) -> {
//                        TKWeekActivity.writeToPreferences(TKWeekOldActivity.this);
//                        requestReadCalendar();
//                    });
//            builder.setNegativeButton(R.string.alert_dialog_abort,
//                    (dialog, which) -> finish());
//            builder.setCancelable(false);
//            return builder.create();
//        }
//        return null;
//    }

    private fun readCurrentVersionFromPrefs() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        storedVersionCode = prefs.getInt(VERSION_CODE, 0)
        currentVersionCode = try {
            val info = packageManager.getPackageInfo(
                packageName, 0
            )
            info.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            // da es nur ein Versionscheck ist, ignorieren wir den Fehler
            0
        }
    }

    private fun writeToPreferences(context: Context) {
        val prefs = context.getSharedPreferences(
            TKWEEK,
            MODE_PRIVATE
        )
        val editor = prefs.edit()
        editor.putInt(VERSION_CODE, currentVersionCode)
        editor.apply()
    }

    companion object {

        // Diverses
        const val DASHES = "---"

        // Uhrzeit, Datum, ...
        const val MINUTES_PER_DAY = 24 * 60

        /**
         * Datum im Format tt.mm.jj, also 29.08.70
         */
        // @JvmStatic
        @JvmField
        val FORMAT_DDMMYY = SimpleDateFormat(
            "dd.MM.yy", Locale.US
        )

        /**
         * Datum im Format jjjjmmtt, also 19700829
         */
        @JvmField
        val FORMAT_YYYYMMDD: DateFormat = SimpleDateFormat(
            "yyyyMMdd", Locale.US
        )

        /**
         * Datum als Text (sehr ausführlich)
         */
        @JvmField
        val FORMAT_FULL = DateFormat
            .getDateInstance(DateFormat.FULL)

        /**
         * Wird beispielsweise für die Ausgabe des ersten und letzten Tages einer
         * Woche verwendet.
         */
        @JvmField
        val FORMAT_DEFAULT = DateFormat
            .getDateInstance()

        /**
         * Datum als Text (kurz)
         */
        @JvmField
        val FORMAT_DATE_SHORT = DateFormat
            .getDateInstance(DateFormat.SHORT)

        /**
         * Zeit als Text (kurz)
         */
        @JvmField
        val FORMAT_TIME_SHORT = SimpleDateFormat
            .getTimeInstance(SimpleDateFormat.SHORT)

        /**
         * Wochentag
         */
        @JvmField
        val FORMAT_DAY_OF_WEEK: DateFormat = SimpleDateFormat(
            "EEEE", Locale.getDefault()
        )

        /**
         * Wochentag kurz
         */
        @JvmField
        val FORMAT_DAY_OF_WEEK_SHORT: DateFormat = SimpleDateFormat(
            "EEE", Locale.getDefault()
        )

        /**
         * Monat
         */
        @JvmField
        val FORMAT_MONTH: DateFormat = SimpleDateFormat(
            "MMMM",
            Locale.getDefault()
        )

        /**
         * Monat kurz
         */
        @JvmField
        val FORMAT_MONTH_SHORT: DateFormat = SimpleDateFormat(
            "MMM", Locale.getDefault()
        )

        /**
         * Monat und Tag (jeweils zweistellig)
         */
        @JvmField
        val FORMAT_YYMM: DateFormat = SimpleDateFormat("MMdd", Locale.US)

        /**
         * Beginn und Ende eines Termins (Datum und Uhrzeit)
         */
        @JvmField
        val FORMAT_DATE_TIME_SHORT = DateFormat
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

        @JvmStatic
        fun backToMain(activity: Activity) {
            startActivityClearTopNewTask(activity, TKWeekActivity::class.java)
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
        fun setInfinitySymbol(context: Context?, value: String?) {
            putString(context, INFINITY_SYMBOL, value)
        }

        @JvmStatic
        fun putString(context: Context?, key: String?, value: String?) {
            val prefs = PreferenceManager
                .getDefaultSharedPreferences(context)
            val editor = prefs.edit()
            editor.putString(key, value)
            editor.apply()
        }

        @JvmStatic
        fun putInt(context: Context?, key: String?, value: Int) {
            val prefs = PreferenceManager
                .getDefaultSharedPreferences(context)
            val editor = prefs.edit()
            editor.putInt(key, value)
            editor.apply()
        }

        @JvmStatic
        fun setWidgetAppearance(
            context: Context?, views: RemoteViews,
            resid: Int
        ) {
            var opacity = WidgetPreference.getOpacity(context)
            val color = 0x000000
            opacity = opacity shl 24
            views.setInt(resid, "setBackgroundColor", opacity or color)
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

        /**
         * Liefert den Wert eines SharedPreferences-Schlüssels als int.
         *
         * @param prefs        SharedPreferences
         * @param key          Schlüssel
         * @param defaultValue Standardwert
         * @return Wert eines SharedPreferences-Schlüssels als int oder der
         * Standardwert
         */
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

        fun requestReadCalendar(activity: Activity) {
            if (!TKWeekUtils.canReadCalendar(activity)) {
                TKWeekUtils.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.READ_CALENDAR),
                    0
                )
            }
            BootCompleteReceiver.startAlarm(activity, true)
        }
    }
}