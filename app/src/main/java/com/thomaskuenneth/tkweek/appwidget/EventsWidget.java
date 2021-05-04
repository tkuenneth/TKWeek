/*
 * EventsWidget.java
 * 
 * TKWeek (c) Thomas Künneth 2010 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.appwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.thomaskuenneth.tkweek.R;

/**
 * This widget is no longer supported.
 *
 * @author Thomas Künneth
 */
public class EventsWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        updateWidgets(context, appWidgetManager, appWidgetIds);
    }

    public static void updateWidgets(final Context context,
                                     final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        final RemoteViews updateViews = new RemoteViews(
                context.getPackageName(), R.layout.widget_retired);
        appWidgetManager.updateAppWidget(appWidgetIds, updateViews);
    }
}
