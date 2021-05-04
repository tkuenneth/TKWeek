/*
 * DayOfYearWidget.java
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

import com.thomaskuenneth.tkweek.activity.TKWeekActivity;
import com.thomaskuenneth.tkweek.util.DateUtilities;
import com.thomaskuenneth.tkweek.R;

import java.util.Calendar;

/**
 * This widget shows the current day of year, the current year and the total number
 * of days in the current year.
 *
 * @author Thomas Künneth
 */
public class DayOfYearWidget extends AppWidgetProvider {

    private static final int RQ_LAUNCH_MAY_DAY = 1;

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
        Calendar cal = DateUtilities.getCalendarClearTimeRelatedFields();
        String dayOfYear = Integer.toString(cal.get(Calendar.DAY_OF_YEAR));
        String year = Integer.toString(cal.get(Calendar.YEAR));
        String daysInYear = Integer.toString(cal.getActualMaximum(Calendar.DAY_OF_YEAR));
        updateViews.setTextViewText(R.id.text_month, daysInYear);
        updateViews.setTextViewText(R.id.text_day, dayOfYear);
        updateViews.setTextViewText(R.id.text_weekday, year);
        Intent intent = new Intent(context, TKWeekActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                RQ_LAUNCH_MAY_DAY,
                intent,
                PendingIntent.FLAG_IMMUTABLE);
        updateViews.setOnClickPendingIntent(R.id.datewidget_id, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, updateViews);
    }
}
