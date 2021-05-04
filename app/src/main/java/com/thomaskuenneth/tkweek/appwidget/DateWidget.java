/*
 * DateWidget.java
 *
 * TKWeek (c) Thomas Künneth 2011 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.thomaskuenneth.tkweek.R;
import com.thomaskuenneth.tkweek.activity.TKWeekActivity;

import java.util.Calendar;
import java.util.Date;

/**
 * Dieses Widget zeigt das aktuelle Datum an. Antippen, um den Kalender
 * anzuzeigen
 *
 * @author Thomas Künneth
 */
public class DateWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        updateWidgets(context, appWidgetManager, appWidgetIds);
    }

    public static void updateWidgets(final Context context,
                                     final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        final RemoteViews updateViews = new RemoteViews(
                context.getPackageName(), R.layout.datewidget_layout);
        TKWeekActivity.setWidgetAppearance(context, updateViews, R.id.datewidget_id);
        Calendar cal = Calendar.getInstance();
        String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
        Date date = cal.getTime();
        String weekday = TKWeekActivity.FORMAT_DAY_OF_WEEK_SHORT.format(date);
        String month = TKWeekActivity.FORMAT_MONTH_SHORT.format(date);
        // die Texte ausgeben
        updateViews.setTextViewText(R.id.text_month, month);
        updateViews.setTextViewText(R.id.text_day, day);
        updateViews.setTextViewText(R.id.text_weekday, weekday);
        // auf Antippen reagieren
        Intent intent = new Intent(context, TKWeekActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_IMMUTABLE);
        updateViews.setOnClickPendingIntent(R.id.datewidget_id, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, updateViews);
    }
}
