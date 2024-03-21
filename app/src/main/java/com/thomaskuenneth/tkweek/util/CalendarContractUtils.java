/*
 * CalendarContractUtils.java
 *
 * Copyright 2012 - 2020 Thomas Künneth
 *           2021 MATHEMA GmbH
 *           2022 - 2024 Thomas Künneth
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
package com.thomaskuenneth.tkweek.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract.Instances;

import com.thomaskuenneth.tkweek.R;
import com.thomaskuenneth.tkweek.types.Appointment;
import com.thomaskuenneth.tkweek.types.Event;
import com.thomaskuenneth.tkweek.types.FixedEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class CalendarContractUtils {

    public static List<Event> getAllDayEvents(Context context,
                                              Calendar calFrom,
                                              Calendar calTo,
                                              boolean expandAllDayEvents) {
        calFrom = DateUtilities.getCalendar(calFrom);
        calTo = DateUtilities.getCalendar(calTo);
        List<Event> result = new ArrayList<>();
        if (DateUtilities.diffDayPeriods(calFrom, calTo) == 0) {
            calTo.add(Calendar.DAY_OF_YEAR, 1);
        }
        ContentResolver contentResolver = context.getContentResolver();
        TimeZone timezoneDefault = TimeZone.getDefault();
        TimeZone timezoneUTC = TimeZone.getTimeZone("UTC");
        DateUtilities.setTimeRelatedFields(calFrom, 0, 0);
        DateUtilities.setTimeRelatedFields(calTo, 0, 0);
        Calendar eventFrom = (Calendar) calFrom.clone();
        Calendar eventTo = (Calendar) calTo.clone();
        // allday events begin at 0:00 UTC, hence one additional day
        eventFrom.add(Calendar.DAY_OF_YEAR, -1);
        eventTo.add(Calendar.DAY_OF_YEAR, 2);
        eventFrom.setTimeZone(timezoneUTC);
        eventTo.setTimeZone(timezoneUTC);
        String[] projection = {Instances.TITLE, Instances.BEGIN,
                Instances.END, Instances.CALENDAR_COLOR,
                Instances.EVENT_COLOR, Instances.ALL_DAY, Instances.CALENDAR_DISPLAY_NAME};
        Cursor c = null;
        if (TKWeekUtils.canReadCalendar(context)) {
            c = Instances.query(contentResolver, projection,
                    eventFrom.getTimeInMillis(), eventTo.getTimeInMillis());
        }
        if (c != null) {
            while (c.moveToNext()) {
                int allday = c.getInt(5);
                if (allday == 0) {
                    continue;
                }
                String calendarName = c.getString(6);
                eventFrom.setTimeInMillis(c.getLong(1));
                eventFrom.setTimeZone(timezoneDefault);
                eventTo.setTimeInMillis(c.getLong(2));
                eventTo.setTimeZone(timezoneDefault);
                String title = c.getString(0);
                int color = c.getInt(3);
                int eventColor = c.getInt(4);
                if (eventColor != 0) {
                    color = eventColor;
                }
                long days = DateUtilities.diffDayPeriods(
                        eventFrom, eventTo);
                if (days == 1) {
                    Event e = new FixedEvent(eventFrom, title);
                    e.color = color;
                    e.calendarName = calendarName;
                    add(result, calFrom, calTo, e);
                } else {
                    if (!expandAllDayEvents) {
                        Event e = new FixedEvent(eventFrom,
                                context.getString(R.string.begin, title));
                        e.color = color;
                        e.calendarName = calendarName;
                        add(result, calFrom, calTo, e);
                        eventTo.add(Calendar.DAY_OF_MONTH, -1);
                        e = new FixedEvent(eventTo, context.getString(
                                R.string.end, title));
                        e.color = color;
                        e.calendarName = calendarName;
                        add(result, calFrom, calTo, e);
                    } else {
                        Event e = new FixedEvent(eventFrom,
                                context.getString(R.string.begin, title));
                        e.color = color;
                        e.calendarName = calendarName;
                        add(result, calFrom, calTo, e);
                        for (long day = 2; day < days; day++) {
                            eventFrom.add(Calendar.DAY_OF_YEAR, 1);
                            e = new FixedEvent(eventFrom,
                                    context.getString(R.string.day_x_of_y,
                                            day, days, title));
                            e.color = color;
                            e.calendarName = calendarName;
                            add(result, calFrom, calTo, e);
                        }
                        eventTo.add(Calendar.DAY_OF_MONTH, -1);
                        e = new FixedEvent(eventTo, context.getString(
                                R.string.end, title));
                        e.color = color;
                        e.calendarName = calendarName;
                        add(result, calFrom, calTo, e);
                    }
                }
                eventFrom.setTimeZone(timezoneUTC);
                eventTo.setTimeZone(timezoneUTC);
            }
            c.close();
        }
        return result;
    }

    public static List<Appointment> getAppointments(Context context,
                                                    Calendar when) {
        List<Appointment> result = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        TimeZone timezoneDefault = TimeZone.getDefault();
        TimeZone timezoneUTC = TimeZone.getTimeZone("UTC");
        Calendar calFrom = DateUtilities.getCalendar(when);
        DateUtilities.setTimeRelatedFields(calFrom, 0, 0);
        calFrom.setTimeZone(timezoneUTC);
        Calendar calTo = DateUtilities.getCalendar(when);
        calTo.add(Calendar.DAY_OF_YEAR, 1);
        DateUtilities.setTimeRelatedFields(calTo, 0, 0);
        calTo.add(Calendar.SECOND, -1);
        calTo.setTimeZone(timezoneUTC);
        String[] projection = {Instances.TITLE, Instances.BEGIN,
                Instances.END, Instances._ID, Instances.CALENDAR_COLOR,
                Instances.EVENT_COLOR, Instances.ALL_DAY,
                Instances.DESCRIPTION};
        Cursor c = null;
        if (TKWeekUtils.canReadCalendar(context)) {
            c = Instances.query(contentResolver, projection,
                    calFrom.getTimeInMillis(), calTo.getTimeInMillis());
        }
        if (c != null) {
            while (c.moveToNext()) {
                int allday = c.getInt(6);
                if (allday != 0) {
                    continue;
                }
                String title = c.getString(0);
                String description = c.getString(7);
                // Beginn
                long dtstart = c.getLong(1);
                calFrom.setTimeInMillis(dtstart);
                calFrom.setTimeZone(timezoneDefault);
                dtstart = calFrom.getTimeInMillis();
                // Ende
                long dtend = c.getLong(2);
                calTo.setTimeInMillis(dtend);
                calFrom.setTimeZone(timezoneDefault);
                dtend = calTo.getTimeInMillis();
                long id = c.getLong(3);
                int color = c.getInt(4);
                // ggf. Event-Farbe verwenden
                int eventColor = c.getInt(5);
                if (eventColor != 0) {
                    color = eventColor;
                }
                result.add(new Appointment(title, description, dtstart,
                        dtend, id, color));
                calFrom.setTimeZone(timezoneUTC);
                calTo.setTimeZone(timezoneUTC);
            }
            c.close();
        }
        return result;
    }

    private static void add(List<Event> l, Calendar calFrom, Calendar calTo,
                            Event e) {
        Calendar calEvent = DateUtilities.getCalendar(e);
        DateUtilities.setTimeRelatedFields(calEvent, 0, 1);
        if (calEvent.after(calFrom) && calEvent.before(calTo)) {
            l.add(e);
        }
    }
}
