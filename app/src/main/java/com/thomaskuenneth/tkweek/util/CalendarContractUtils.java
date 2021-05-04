/*
 * CalendarContractUtils.java
 * 
 * TKWeek (c) Thomas Künneth 2012 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Instances;
import android.provider.CalendarContract.Reminders;
import android.util.Log;

import com.thomaskuenneth.tkweek.types.FixedEvent;
import com.thomaskuenneth.tkweek.R;
import com.thomaskuenneth.tkweek.types.Appointment;
import com.thomaskuenneth.tkweek.types.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Use this class to read calendar entries.
 *
 * @author Thomas Künneth
 */
public class CalendarContractUtils {

    private static final String TAG = CalendarContractUtils.class.getSimpleName();

    /**
     * Get a list of allday events between {@code calFrom}
     * and {@code calTo} (both included).
     *
     * @param context            context
     * @param calFrom            start date
     * @param calTo              end date
     * @param expandAllDayEvents Should events lasting several days be expanded?
     * @return list of allday events
     */
    @SuppressLint("NewApi")
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
        if (TKWeekUtils.getAPILevel() >= 15) {
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
        }
        return result;
    }

    /**
     * Liefert alle nicht ganztägigen Termine am übergebenen Datum.
     *
     * @param context Kontext
     * @param when    Datum
     * @return Liste mit den gefundenen Terminen
     */
    @SuppressLint("NewApi")
    public static List<Appointment> getAppointments(Context context,
                                                    Calendar when) {
        List<Appointment> result = new ArrayList<>();
        // ab Android 4.0.3
        if (TKWeekUtils.getAPILevel() >= 15) {
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
        }
        return result;
    }

    /**
     * Liefert alle Erinnerungen für das übergebene Datum
     *
     * @param context Kontext
     * @param when    Datum
     * @return Liste mit den gefundenen Erinnerungen
     */
    @SuppressLint("NewApi")
    public static List<Appointment> getReminders(Context context, Calendar when) {
        List<Appointment> result = new ArrayList<>();
        // ab Android 4.0.3
        if (TKWeekUtils.getAPILevel() >= 15) {
            ContentResolver contentResolver = context.getContentResolver();
            String[] projection = {Reminders.EVENT_ID, Reminders.MINUTES};
            Cursor c = null;
            try {
                // FIXME: gucken, welche Berechtigung das ist - READ_CALENDAR?
                c = contentResolver.query(Reminders.CONTENT_URI, projection,
                        null, null, null);
            } catch (SecurityException e) {
                Log.e(TAG, "getReminders()", e);
            }
            if (c != null) {
                while (c.moveToNext()) {
                    Log.d(TAG, "min.: " + c.getInt(1));
                    // CalendarContract.Instances.query(cr, projection, begin, end)
                    Uri uri = ContentUris.withAppendedId(Instances.CONTENT_URI,
                            c.getLong(0));
                    String[] projection2 = {Instances.TITLE, Instances.BEGIN};
                    Cursor c2 = contentResolver.query(uri, projection2, null, null,
                            null);
                    if (c2 != null) {
                        if (c.moveToNext()) {
                            Log.d(TAG, c2.getString(0));
                        }
                        c2.close();
                    }
                }
                c.close();
            }
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
