/*
 * WeekInfoWidget.java
 *
 * TKWeek (c) Thomas Künneth 2010 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.thomaskuenneth.tkweek.R;
import com.thomaskuenneth.tkweek.activity.TKWeekActivity;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Dieses Widget zeigt Informationen über die aktuelle Woche an, zum Beispiel
 * deren Nummer, Datum des ersten und letzten Tages, sowie das aktuelle Datum.
 *
 * @author Thomas Künneth
 */
public class WeekInfoWidget extends AppWidgetProvider {

    private static final String TAG = WeekInfoWidget.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        updateWidgets(context, appWidgetManager, appWidgetIds);
    }

    public static void updateWidgets(Context context,
                                     AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews updateViews = new RemoteViews(context.getPackageName(),
                R.layout.weekinfowidget_layout);
        TKWeekActivity.setWidgetAppearance(context, updateViews, R.id.weekinfowidget_id);
        try {
            updateViews(updateViews, context);
        } catch (IOException e) {
            Log.e(TAG, "updateWidgets", e);
        }
        // FIXME: We need to update this
        Intent intent = new Intent(context, TKWeekActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_IMMUTABLE);
        updateViews.setOnClickPendingIntent(R.id.weekinfowidget_id,
                pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, updateViews);
    }

    private static void updateViews(RemoteViews updateViews, Context context)
            throws IOException {
        Calendar cal = Calendar.getInstance();
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean useISO = prefs.getBoolean("use_iso_weeks", false);
        String text1;
        if (useISO) {
            cal.setMinimalDaysInFirstWeek(4);
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
            text1 = context.getString(R.string.week_number_iso);
        } else {
            text1 = context.getString(R.string.week_number);
        }
        int maxWeeks = cal.getActualMaximum(Calendar.WEEK_OF_YEAR);
        String text6 = TKWeekActivity.FORMAT_FULL.format(cal.getTime());
        // Wochennummer
        updateViews.setTextViewText(R.id.text1, context.getString(
                R.string.weekinfowidget_template, text1, cal
                        .get(Calendar.WEEK_OF_YEAR), maxWeeks));
        Calendar temp = (Calendar) cal.clone();
        // Bis zum Wochenanfangs zurück gehen
        while (temp.get(Calendar.DAY_OF_WEEK) != temp.getFirstDayOfWeek()) {
            temp.add(Calendar.DAY_OF_MONTH, -1);
        }
        Date start = temp.getTime();
        temp.add(Calendar.DAY_OF_MONTH, 6);
        Date end = temp.getTime();
        String text2 = context.getString(R.string.weekinfowidget_from,
                TKWeekActivity.FORMAT_DAY_OF_WEEK.format(start));
        String text3 = TKWeekActivity.FORMAT_DEFAULT.format(start);
        String text4 = context.getString(R.string.weekinfowidget_to,
                TKWeekActivity.FORMAT_DAY_OF_WEEK.format(end));
        String text5 = TKWeekActivity.FORMAT_DEFAULT.format(end);
        updateViews.setTextViewText(R.id.text2, text2);
        updateViews.setTextViewText(R.id.text3, text3);
        updateViews.setTextViewText(R.id.text4, text4);
        updateViews.setTextViewText(R.id.text5, text5);
        updateViews.setTextViewText(R.id.text6, text6);
    }
}
