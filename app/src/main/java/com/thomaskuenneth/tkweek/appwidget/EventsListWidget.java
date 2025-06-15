/*
 * EventsListWidget.java
 *
 * Copyright 2011 - 2020 Thomas Künneth
 *           2021 MATHEMA GmbH
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
package com.thomaskuenneth.tkweek.appwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.thomaskuenneth.tkweek.R;
import com.thomaskuenneth.tkweek.activity.TKWeekActivity;
import com.thomaskuenneth.tkweek.adapter.AnnualEventsListAdapter;
import com.thomaskuenneth.tkweek.fragment.AnnualEventsFragment;
import com.thomaskuenneth.tkweek.types.Event;
import com.thomaskuenneth.tkweek.util.DateUtilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EventsListWidget extends AppWidgetProvider {

    private static final int REQUEST_CODE_EVENTS_LIST_WIDGET = 1;

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
                context.getPackageName(), R.layout.events_list_widget_layout);
        AsyncTask<Void, Void, AnnualEventsListAdapter> task = new AsyncTask<Void, Void, AnnualEventsListAdapter>() {

            @Override
            protected AnnualEventsListAdapter doInBackground(Void... params) {
                return AnnualEventsListAdapter.create(context,
                        null);
            }

            @Override
            protected void onPostExecute(AnnualEventsListAdapter adapter) {
                String text_1r = "";
                String text_1l = context
                        .getString(R.string.events_list_widget_no_events);
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
                updateViews.setOnClickPendingIntent(R.id.events_list_widget_id,
                        TKWeekActivity.createPendingIntentToLaunchTKWeek(context,
                                REQUEST_CODE_EVENTS_LIST_WIDGET,
                                AnnualEventsFragment.class));
                appWidgetManager.updateAppWidget(appWidgetIds, updateViews);
            }
        };
        task.execute();
    }
}
