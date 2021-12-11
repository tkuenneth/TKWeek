/*
 * DateWidget.java
 *
 * Copyright 2011 - 2020 Thomas Künneth
 * Copyright 2021 MATHEMA GmbH
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
import android.widget.RemoteViews;

import com.thomaskuenneth.tkweek.R;
import com.thomaskuenneth.tkweek.activity.TKWeekActivity;
import com.thomaskuenneth.tkweek.fragment.MyDayFragment;

import java.util.Calendar;
import java.util.Date;

public class DateWidget extends AppWidgetProvider {

    private static final int REQUEST_CODE_DATE_WIDGET = 3;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        updateWidgets(context, appWidgetManager, appWidgetIds);
    }

    public static void updateWidgets(final Context context,
                                     final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        final RemoteViews updateViews = new RemoteViews(
                context.getPackageName(), R.layout.datewidget_layout);
        Calendar cal = Calendar.getInstance();
        String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
        Date date = cal.getTime();
        String weekday = TKWeekActivity.FORMAT_DAY_OF_WEEK_SHORT.format(date);
        String month = TKWeekActivity.FORMAT_MONTH_SHORT.format(date);
        updateViews.setTextViewText(R.id.text_month, month);
        updateViews.setTextViewText(R.id.text_day, day);
        updateViews.setTextViewText(R.id.text_weekday, weekday);
        updateViews.setOnClickPendingIntent(R.id.datewidget_id, TKWeekActivity.createPendingIntentToLaunchTKWeek(context, REQUEST_CODE_DATE_WIDGET, MyDayFragment.class));
        appWidgetManager.updateAppWidget(appWidgetIds, updateViews);
    }
}
