/*
 * EventsListWidget.java
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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.thomaskuenneth.tkweek.R;
import com.thomaskuenneth.tkweek.activity.TKWeekActivity;
import com.thomaskuenneth.tkweek.adapter.AnnualEventsListAdapter;
import com.thomaskuenneth.tkweek.types.Event;
import com.thomaskuenneth.tkweek.util.DateUtilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Diese Klasse implementiert ein Widget, das kommende Ereignisse anzeigt.
 * Wird das Widget angetippt, öffnet sich die App.
 *
 * @author Thomas Künneth
 * @see AnnualEventsListAdapter
 */
public class EventsListWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        updateWidgets(context, appWidgetManager, appWidgetIds);
    }

    public static void updateWidgets(final Context context,
                                     final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        final int numberOfPastDays = AnnualEventsListAdapter
                .getNumberOfPastDays(prefs);
        final RemoteViews updateViews = new RemoteViews(
                context.getPackageName(), R.layout.eventslistwidget_layout);
        TKWeekActivity.setWidgetAppearance(context, updateViews, R.id.eventslistwidget_id);
        AsyncTask<Void, Void, AnnualEventsListAdapter> task = new AsyncTask<Void, Void, AnnualEventsListAdapter>() {

            @Override
            protected AnnualEventsListAdapter doInBackground(Void... params) {
                return AnnualEventsListAdapter.create(context,
                        false,
                        null);
            }

            @Override
            protected void onPostExecute(AnnualEventsListAdapter adapter) {
                String text_1r = "";
                String text_1l = context
                        .getString(R.string.eventswidget_no_events);
                String text_2r = "";
                String text_2l = "";
                String text_3r = "";
                String text_3l = "";
                String text_4r = "";
                String text_4l = "";
                if (adapter != null) {
                    int numberOfEvents = adapter.getCount();
                    if (numberOfEvents > 0) {
                        Calendar calYesterday = DateUtilities
                                .getCalendarClearTimeRelatedFields();
                        calYesterday.add(Calendar.DAY_OF_YEAR,
                                numberOfPastDays - 1);
                        int startIndex = -1;
                        for (int pos = 0; pos < numberOfEvents; pos++) {
                            Event current = (Event) adapter.getItem(pos);
                            Calendar cal = DateUtilities.getCalendar(current);
                            DateUtilities.clearTimeRelatedFields(cal);
                            if (cal.after(calYesterday)) {
                                startIndex = pos;
                                break;
                            }
                        }
                        if (startIndex < 0) {
                            // kein Datum ist neuer als gestern
                            startIndex = numberOfEvents - 4;
                            if (startIndex < 0) {
                                startIndex = 0;
                            }
                        }
                        List<Event> arrayList = new ArrayList<>();
                        for (int i = startIndex; i < numberOfEvents; i++) {
                            Event event = (Event) adapter.getItem(i);
                            arrayList.add(event);
                            if (arrayList.size() == 4) {
                                break;
                            }
                        }
                        int arrayListSize = arrayList.size();
                        startIndex = 0;
                        if (startIndex < arrayListSize) {
                            Event event = arrayList.get(startIndex);
                            text_1l = adapter.getDescription(event, context);
                            text_1r = adapter.getDaysAsString(event);
                        }
                        if (++startIndex < arrayListSize) {
                            Event event = arrayList.get(startIndex);
                            text_2l = adapter.getDescription(event, context);
                            text_2r = adapter.getDaysAsString(event);
                        }
                        if (++startIndex < arrayListSize) {
                            Event event = arrayList.get(startIndex);
                            text_3l = adapter.getDescription(event, context);
                            text_3r = adapter.getDaysAsString(event);
                        }
                        if (++startIndex < arrayListSize) {
                            Event event = arrayList.get(startIndex);
                            text_4l = adapter.getDescription(event, context);
                            text_4r = adapter.getDaysAsString(event);
                        }
                    }
                }
                updateViews.setTextViewText(R.id.text_1r, text_1r);
                updateViews.setTextViewText(R.id.text_1l, text_1l);
                updateViews.setTextViewText(R.id.text_2r, text_2r);
                updateViews.setTextViewText(R.id.text_2l, text_2l);
                updateViews.setTextViewText(R.id.text_3r, text_3r);
                updateViews.setTextViewText(R.id.text_3l, text_3l);
                updateViews.setTextViewText(R.id.text_4r, text_4r);
                updateViews.setTextViewText(R.id.text_4l, text_4l);
                Intent intent = new Intent(context, TKWeekActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
                updateViews.setOnClickPendingIntent(R.id.eventslistwidget_id,
                        pendingIntent);
                appWidgetManager.updateAppWidget(appWidgetIds, updateViews);
            }
        };
        task.execute();
    }
}
