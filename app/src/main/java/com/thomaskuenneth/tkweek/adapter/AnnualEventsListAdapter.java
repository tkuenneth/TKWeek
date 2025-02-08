/*
 * AnnualEventsListAdapter.java
 *
 * Copyright 2009 - 2020 Thomas Künneth
 *           2021 MATHEMA GmbH
 *           2022 - 2025 Thomas Künneth
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
package com.thomaskuenneth.tkweek.adapter;

import static com.thomaskuenneth.tkweek.util.DateUtilities.NO_CHURCHES;
import static com.thomaskuenneth.tkweek.util.DateUtilities.OSTERSONNTAG;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.thomaskuenneth.tkweek.R;
import com.thomaskuenneth.tkweek.activity.TKWeekActivity;
import com.thomaskuenneth.tkweek.appwidget.EventsListWidget;
import com.thomaskuenneth.tkweek.fragment.CalendarFragment;
import com.thomaskuenneth.tkweek.preference.MoonPhasesPreference;
import com.thomaskuenneth.tkweek.preference.PickCountriesPreference;
import com.thomaskuenneth.tkweek.types.Anniversary;
import com.thomaskuenneth.tkweek.types.Birthday;
import com.thomaskuenneth.tkweek.types.Event;
import com.thomaskuenneth.tkweek.types.FixedEvent;
import com.thomaskuenneth.tkweek.types.Mondphasen;
import com.thomaskuenneth.tkweek.types.Seasons;
import com.thomaskuenneth.tkweek.util.CalendarCondition;
import com.thomaskuenneth.tkweek.util.CalendarCondition.CONDITION;
import com.thomaskuenneth.tkweek.util.CalendarContractUtils;
import com.thomaskuenneth.tkweek.util.CalendarIterator;
import com.thomaskuenneth.tkweek.util.ContactsUtils;
import com.thomaskuenneth.tkweek.util.DateUtilities;
import com.thomaskuenneth.tkweek.util.DaylightSavingTime;
import com.thomaskuenneth.tkweek.util.TKWeekUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AnnualEventsListAdapter extends BaseAdapter implements Comparator<Event> {

    private static final String TAG = AnnualEventsListAdapter.class.getSimpleName();

    private static final String FIXED_EVENT = "_fixed_event";
    private static final String FILENAME = "AnnualEvents.txt";

    private static final int[] birthdays = {
            R.string.charles_dickens, Calendar.FEBRUARY, 7, 1812,
            R.string.abraham_lincoln, Calendar.FEBRUARY, 12, 1809,
            R.string.charles_babbage, Calendar.DECEMBER, 26, 1791
    };

    private static final int[] internationalEvents = {
            R.string.weltfrauentag, Calendar.MARCH, 8,
            R.string.weltkindertag, Calendar.NOVEMBER, 20,
            R.string.tag_der_erde, Calendar.APRIL, 22,
            R.string.halloween, Calendar.OCTOBER, 31,
            R.string.neujahr, Calendar.JANUARY, 1,
            R.string.silvester, Calendar.DECEMBER, 31,
            R.string.erster_april, Calendar.APRIL, 1,
            R.string.tag_der_arbeit, Calendar.MAY, 1,
            R.string.valentinstag, Calendar.FEBRUARY, 14,
            R.string.tolkien_reading_day, Calendar.MARCH, 25,
            R.string.hobbit_day, Calendar.SEPTEMBER, 22,
            R.string.world_seagrass_day, Calendar.MARCH, 1,
    };

    private static final int[] nationalEvents_FR = {
            R.string.nationalfeiertag_fr, Calendar.JULY, 14
    };

    private static final int[] nationalEvents_AU = {
            R.string.australia_day, Calendar.JANUARY, 26
    };

    private static final int[] nationalEvents_SG = {
            R.string.sg_national_day, Calendar.AUGUST, 9
    };

    private static final int[] nationalEvents_IE = {
            R.string.st_patricks_day, Calendar.MARCH, 17,
            R.string.bloomsday, Calendar.JUNE, 16
    };

    private static final int[] nationalEvents_AT = {
            R.string.nationalfeiertag_at, Calendar.OCTOBER, 26,
            R.string.leopolditag_at, Calendar.NOVEMBER, 15
    };

    private static final int[] nationalEvents_DE = {
            R.string.augsburger_friedensfest, Calendar.AUGUST, 8,
            R.string.tag_der_deutschen_einheit, Calendar.OCTOBER, 3
    };

    private static final int[] nationalEvents_US = {
            R.string.veterans_day_us, Calendar.NOVEMBER, 11,
            R.string.groundhog_day_us, Calendar.FEBRUARY, 2,
            R.string.flag_day_usa, Calendar.JUNE, 14,
            R.string.independence_day_usa, Calendar.JULY, 4
    };

    private static final int[] nationalEvents_NL = {
            R.string.pakjesavond, Calendar.DECEMBER, 5
    };

    private static final int[] nationalEvents_NO = {
            R.string.nationalfeiertag_no, Calendar.MAY, 17
    };

    private static final int[] nationalEvents_CH = {
            R.string.nationalfeiertag_sc, Calendar.AUGUST, 1
    };

    private static final int[] nationalEvents_RU = {
            R.string.national_flag_day_ru, Calendar.AUGUST, 22
    };

    private static final int[] nationalEvents_SE = {
            R.string.nationalfeiertag_se, Calendar.JUNE, 6,
            R.string.gustav_adolf_tag, Calendar.NOVEMBER, 6
    };

    private static final int[] christianEvents = {
            R.string.josefstag, Calendar.MARCH, 19,
            R.string.maria_himmelfahrt, Calendar.AUGUST, 15,
            R.string.peter_und_paul, Calendar.JUNE, 29,
            R.string.martini, Calendar.NOVEMBER, 11,
            R.string.heiliger_abend, Calendar.DECEMBER, 24,
            R.string.erster_weihnachtstag, Calendar.DECEMBER, 25,
            R.string.zweiter_weihnachtstag, Calendar.DECEMBER, 26,
            R.string.mariae_lichtmess, Calendar.FEBRUARY, 2,
            R.string.siebenschlaefer, Calendar.JUNE, 27,
            R.string.johanni, Calendar.JUNE, 26,
            R.string.nikolaus, Calendar.DECEMBER, 6,
            R.string.erscheinung_des_herrn, Calendar.JANUARY, 6,
            R.string.reformationstag, Calendar.OCTOBER, 31
    };

    private static final int[] iceSaints = {
            R.string.mamertus, Calendar.MAY, 11,
            R.string.pankratius, Calendar.MAY, 12,
            R.string.servatius, Calendar.MAY, 13,
            R.string.bonifatius, Calendar.MAY, 14,
            R.string.sophie, Calendar.MAY, 15
    };

    private final String search;
    private final Seasons seasons;
    private final List<Event> data;
    private final LayoutInflater mInflater;
    private final Calendar today_cal;
    private final Calendar calFrom;
    private final Calendar calTo;
    private final SharedPreferences prefs;

    public static AnnualEventsListAdapter create(Context context, String search) {
        SharedPreferences prefs = context.getSharedPreferences(PreferenceManager.getDefaultSharedPreferencesName(context), Context.MODE_PRIVATE);
        Calendar calFrom = Calendar.getInstance();
        calFrom.add(Calendar.DAY_OF_YEAR, getNumberOfPastDays(prefs));
        Calendar calTo = Calendar.getInstance();
        calTo.add(Calendar.YEAR, 1);
        return new AnnualEventsListAdapter(context, calFrom, calTo, false, search);
    }

    public AnnualEventsListAdapter(Context context, Calendar calFrom, Calendar calTo, boolean expandAllDayEvents, String search) {
        assert calFrom != null;
        assert calTo != null;
        this.calFrom = DateUtilities.clearTimeRelatedFields(calFrom);
        this.calTo = DateUtilities.clearTimeRelatedFields(calTo);
        if (search != null) {
            search = search.toLowerCase();
        }
        this.search = search;
        data = new ArrayList<>();
        seasons = new Seasons(context);
        mInflater = LayoutInflater.from(context);
        today_cal = Calendar.getInstance();
        prefs = context.getSharedPreferences(PreferenceManager.getDefaultSharedPreferencesName(context), Context.MODE_PRIVATE);
        int yearFrom = calFrom.get(Calendar.YEAR);
        int yearTo = calTo.get(Calendar.YEAR);
        for (int year = yearFrom; year <= yearTo; year++) {
            addBuiltinEvents(context, prefs, year);
            if (!prefs.getBoolean("hide_birthdays", false)) {
                loadBirthdays(context, year);
                addBirthdays(context, year);
            }
        }
        loadUserEvents(context, getUserEventsFile(context), yearFrom, yearTo);
        if (!prefs.getBoolean("hide_allday_events", false)) {
            List<Event> alldayEvents = CalendarContractUtils.getAllDayEvents(context, calFrom, calTo, expandAllDayEvents);
            addAll(alldayEvents);
        }
        data.sort(this);
    }

    public static int getNumberOfPastDays(SharedPreferences prefs) {
        String days = prefs.getString("recent_events", "-7");
        int _numberOfPastDays = -7;
        try {
            _numberOfPastDays = Integer.parseInt(days);
        } catch (NumberFormatException e) {
            Log.e(TAG, "parseInt()", e);
        }
        return _numberOfPastDays;
    }

    public Calendar getFrom() {
        return calFrom;
    }

    public Calendar getTo() {
        return calTo;
    }

    public void deleteSimilar(Event event) {
        String runtimeID = event.runtimeID;
        List<Event> events = new ArrayList<>();
        events.add(event);
        if (runtimeID != null) {
            for (Event e : data) {
                if (runtimeID.equals(e.runtimeID)) {
                    events.add(e);
                }
            }
        }
        data.removeAll(events);
        notifyDataSetChanged();
    }

    public void addEventNoCheck(Event event) {
        data.add(event);
        notifyDataSetChanged();
    }

    public boolean save(Context context) {
        boolean result = saveUserEvents(getUserEventsFile(context));
        updateEventsListWidgets(context);
        return result;
    }

    public void updateEventsListWidgets(Context context) {
        AppWidgetManager m = AppWidgetManager.getInstance(context);
        if (m != null) {
            int[] appWidgetIds = m.getAppWidgetIds(new ComponentName(context, EventsListWidget.class));
            if ((appWidgetIds != null) && (appWidgetIds.length > 0)) {
                EventsListWidget.updateWidgets(context, m, appWidgetIds);
            }
        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.two_line_item, parent, false);
            holder = new ViewHolder();
            holder.text1 = convertView.findViewById(R.id.text1);
            holder.text2 = convertView.findViewById(R.id.text2);
            holder.text3 = convertView.findViewById(R.id.text3);
            holder.text4 = convertView.findViewById(R.id.text4);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Event event = (Event) getItem(position);
        Context context = convertView.getContext();
        holder.text1.setText(getDescription(event, context));
        var strDate = getDateAsString(event, context);
        if (CalendarFragment.isDayOff(context, DateUtilities.getCalendar(event).getTime())) {
            holder.text2.setText(context.getString(R.string.string1_string2, strDate, context.getString(R.string.day_off)));
        } else {
            holder.text2.setText(strDate);
        }
        holder.text3.setText(getDaysAsString(event));
        String calendarName = getCalendarName(event);
        holder.text4.setText(calendarName);
        holder.text4.setVisibility(View.VISIBLE);
        holder.text4.setVisibility(!calendarName.isEmpty() ? View.VISIBLE : View.GONE);
        return convertView;
    }

    public String getCalendarName(Event event) {
        return TKWeekUtils.getStringNotNull(event.calendarName);
    }

    public String getDaysAsString(Event event) {
        Calendar cal = DateUtilities.getCalendar(event);
        return getDaysAsString(mInflater, today_cal, cal);
    }

    public static String getDaysAsString(LayoutInflater mInflater, Calendar today, Calendar date) {
        long days = DateUtilities.diffDayPeriods(today, date);
        return getDaysAsString(mInflater, days);
    }

    public static String getDaysAsString(LayoutInflater mInflater, long days) {
        if (days == 0) {
            return mInflater.getContext().getString(R.string.today);
        } else if (days == 1) {
            return mInflater.getContext().getString(R.string.tomorrow);
        } else if (days == -1) {
            return mInflater.getContext().getString(R.string.yesterday);
        } else if (days < 0) {
            return mInflater.getContext().getString(R.string.x_days_ago, -days);
        } else {
            return mInflater.getContext().getString(R.string.in_x_days, days);
        }
    }

    public String getDateAsString(Event event, Context context) {
        Calendar cal = DateUtilities.getCalendar(event);
        Date calTime = cal.getTime();
        String dateAsString = TKWeekActivity.FORMAT_FULL.format(calTime);
        if (event.occurrences > 0) {
            if (event instanceof Birthday) {
                long diffDays = DateUtilities.diffDayPeriods(today_cal, cal);
                return context.getString(diffDays < 0 ? R.string.birthday_past : R.string.birthday_future, event.occurrences, TKWeekActivity.FORMAT_DAY_OF_WEEK.format(calTime), TKWeekActivity.FORMAT_MONTH.format(calTime), event.getDay(), event.getYear() - event.occurrences);
            } else if (event instanceof Anniversary) {
                return context.getString(R.string.template_anniversary, event.occurrences, TKWeekActivity.FORMAT_DAY_OF_WEEK.format(calTime), TKWeekActivity.FORMAT_MONTH.format(calTime), event.getDay(), event.getYear() - event.occurrences);
            }
        }
        return dateAsString;
    }

    public String getDescription(Event event, Context context) {
        if (event instanceof Birthday) {
            return context.getString(R.string.birthday, event.descr);
        } else if (event instanceof Anniversary) {
            return context.getString(R.string.event, ((Anniversary) event).getText(), event.descr);
        }
        if (event.annuallyRepeating) {
            return context.getString(R.string.string1_string2, event.descr, TKWeekActivity.getInfinitySymbol(context));
        }
        return event.descr;
    }

    public int compare(Event o1, Event o2) {
        if ((o1 == null) && (o2 == null)) {
            return 0;
        } else if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        }
        long days1 = o1.getYear() * 10000L + o1.getMonth() * 100L + o1.getDay();
        long days2 = o2.getYear() * 10000L + o2.getMonth() * 100L + o2.getDay();
        return Long.compare(days1, days2);
    }

    public static File getUserEventsFile(Context context) {
        return new File(context.getFilesDir(), FILENAME);
    }

    public boolean saveUserEvents(File file) {
        FileWriter fw = null;
        boolean success = false;
        try {
            fw = new FileWriter(file);
            success = saveUserEvents(fw);
        } catch (IOException e) {
            Log.e(TAG, "saveUserEvents()", e);
        }
        if (fw != null) {
            try {
                fw.close();
            } catch (IOException e) {
                // should move to try-with-resources
            }
        }
        return success;
    }

    public boolean saveUserEvents(Writer writer) {
        BufferedWriter bw = null;
        boolean success = false;
        try {
            bw = new BufferedWriter(writer);
            for (int i = 0; i < getCount(); i++) {
                Event event = (Event) getItem(i);
                if (!event.builtin && !event.cloned) {
                    String description = event.descr;
                    if (!event.annuallyRepeating) {
                        description += FIXED_EVENT;
                    }
                    bw.write(description);
                    bw.newLine();
                    bw.write(Integer.toString(event.getYear()));
                    bw.newLine();
                    bw.write(Integer.toString(event.getMonth()));
                    bw.newLine();
                    bw.write(Integer.toString(event.getDay()));
                    bw.newLine();
                }
            }
            success = true;
        } catch (IOException e) {
            Log.e(TAG, "saveUserEvents()", e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    // should move to try-with-resources
                }
            }
        }
        return success;
    }

    private void addBuiltinEvents(Context context, SharedPreferences prefs, int year) {
        if (!prefs.getBoolean("hide_seasons", false)) {
            addSeasons(context, year);
        }
        if (!prefs.getBoolean(MoonPhasesPreference.HIDE_MOONPHASES, false)) {
            List<Event> moonPhases = Mondphasen.getMoonPhases(context, year);
            for (Event e : moonPhases) {
                String key;
                if (e.descr.equals(context.getString(R.string.new_moon))) {
                    key = MoonPhasesPreference.SHOW_NEW_MOON;
                } else if (e.descr.equals(context.getString(R.string.full_moon))) {
                    key = MoonPhasesPreference.SHOW_FULL_MOON;
                } else if (e.descr.equals(context.getString(R.string.first_quarter))) {
                    key = MoonPhasesPreference.SHOW_FIRST_QUARTER;
                } else {
                    key = MoonPhasesPreference.SHOW_LAST_QUARTER;
                }
                if (prefs.getBoolean(key, true)) {
                    add(e, false);
                }
            }
        }
        if (!prefs.getBoolean("hide_builtin_events", false)) {
            if (addSimpleEvents(context, TKWeekUtils.NETHERLANDS, nationalEvents_NL, year)) {
                // until 2013 Koninginnedag, then Koningsdag
                int day;
                int resId;
                if (year <= 2013) {
                    day = 30;
                    resId = R.string.koninginnedag;
                } else {
                    day = 27;
                    resId = R.string.koningsdag;
                }
                Calendar cal = DateUtilities.getCalendarFromCalendarCondition(year, Calendar.APRIL, day, CalendarCondition.createCalendarCondition(CONDITION.NOT_EQUAL, Calendar.DAY_OF_WEEK, Calendar.SUNDAY, true), Calendar.DAY_OF_MONTH, -1);
                add(new FixedEvent(cal, context.getString(resId), true), false);
            }
            addSimpleEvents(context, Locale.FRANCE, nationalEvents_FR, year);
            addSimpleEvents(context, TKWeekUtils.SINGAPORE, nationalEvents_SG, year);
            addSimpleEvents(context, TKWeekUtils.AUSTRALIA, nationalEvents_AU, year);
            addSimpleEvents(context, TKWeekUtils.AUSTRIA, nationalEvents_AT, year);
            addSimpleEvents(context, TKWeekUtils.SWITZERLAND, nationalEvents_CH, year);
            addSimpleEvents(context, TKWeekUtils.NORWAY, nationalEvents_NO, year);
            addSimpleEvents(context, TKWeekUtils.IRELAND, nationalEvents_IE, year);
            addSimpleEvents(context, TKWeekUtils.RUSSIA, nationalEvents_RU, year);
            if (addSimpleEvents(context, TKWeekUtils.SWEDEN, nationalEvents_SE, year)) {
                Calendar cal = DateUtilities.getCalendarFromCalendarCondition(year, Calendar.JUNE, 20, CalendarCondition.createCalendarCondition(CONDITION.EQUAL, Calendar.DAY_OF_WEEK, Calendar.SATURDAY, true), Calendar.DAY_OF_MONTH, 1);
                add(new FixedEvent(cal, context.getString(R.string.midsommar), true), false);
            }
            if (addSimpleEvents(context, Locale.US, nationalEvents_US, year)) {
                Calendar cal = DateUtilities.getCalendarFromCalendarCondition(year, Calendar.MAY, 31, CalendarCondition.createCalendarCondition(CONDITION.EQUAL, Calendar.DAY_OF_WEEK, Calendar.MONDAY, true), Calendar.DAY_OF_MONTH, -1);
                add(new FixedEvent(cal, context.getString(R.string.memorial_day), true), false);
                cal = CalendarIterator.iterateUntil(DateUtilities.getCalendar(year, Calendar.JANUARY, 1), CalendarCondition.createCalendarCondition(CONDITION.EQUAL, Calendar.DAY_OF_WEEK, Calendar.MONDAY, true), Calendar.DAY_OF_MONTH, 1);
                cal.add(Calendar.DAY_OF_MONTH, 14);
                add(new FixedEvent(cal, context.getString(R.string.mlk_day), true), false);
                cal = CalendarIterator.iterateUntil(DateUtilities.getCalendar(year, Calendar.FEBRUARY, 1), CalendarCondition.createCalendarCondition(CONDITION.EQUAL, Calendar.DAY_OF_WEEK, Calendar.MONDAY, true), Calendar.DAY_OF_MONTH, 1);
                cal.add(Calendar.DAY_OF_MONTH, 14);
                add(new FixedEvent(cal, context.getString(R.string.presidents_day), true), false);
                cal = CalendarIterator.iterateUntil(DateUtilities.getCalendar(year, Calendar.NOVEMBER, 1), CalendarCondition.createCalendarCondition(CONDITION.EQUAL, Calendar.DAY_OF_WEEK, Calendar.TUESDAY, true), Calendar.DAY_OF_MONTH, 1);
                add(new FixedEvent(cal, context.getString(R.string.election_day_us), true), false);
                cal = CalendarIterator.iterateUntil(DateUtilities.getCalendar(year, Calendar.SEPTEMBER, 1), CalendarCondition.createCalendarCondition(CONDITION.EQUAL, Calendar.DAY_OF_WEEK, Calendar.MONDAY, true), Calendar.DAY_OF_MONTH, 1);
                add(new FixedEvent(cal, context.getString(R.string.labour_day_usa), true), false);
                cal = CalendarIterator.iterateUntil(DateUtilities.getCalendar(year, Calendar.OCTOBER, 1), CalendarCondition.createCalendarCondition(CONDITION.EQUAL, Calendar.DAY_OF_WEEK, Calendar.MONDAY, true), Calendar.DAY_OF_MONTH, 1);
                cal.add(Calendar.WEEK_OF_MONTH, 1);
                add(new FixedEvent(cal, context.getString(R.string.columbus_day), true), false);
                cal = CalendarIterator.iterateUntil(DateUtilities.getCalendar(year, Calendar.JUNE, 1), CalendarCondition.createCalendarCondition(CONDITION.EQUAL, Calendar.DAY_OF_WEEK, Calendar.FRIDAY, true), Calendar.DAY_OF_MONTH, 1);
                add(new FixedEvent(cal, context.getString(R.string.national_doughnut_day), true), false);
            }
            if (addSimpleEvents(context, Locale.GERMANY, nationalEvents_DE, year)) {
                Calendar cal = DateUtilities.getCalendarFromCalendarCondition(year, Calendar.OCTOBER, 1, CalendarCondition.createCalendarCondition(CONDITION.EQUAL, Calendar.DAY_OF_WEEK, Calendar.SUNDAY, true), Calendar.DAY_OF_MONTH, 1);
                add(new FixedEvent(cal, context.getString(R.string.thanksgiving), true), false);
                cal = CalendarIterator.iterateUntil(DateUtilities.getCalendar(year, Calendar.NOVEMBER, 22), CalendarCondition.createCalendarCondition(CONDITION.EQUAL, Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY, true), Calendar.DAY_OF_MONTH, -1);
                add(new FixedEvent(cal, context.getString(R.string.buss_und_bettag_de), true), false);
            }
            Calendar thanksgiving = DateUtilities.getThanksgiving(context, year);
            if (thanksgiving != null) {
                add(new FixedEvent(thanksgiving, context.getString(R.string.thanksgiving), true), false);
                if (PickCountriesPreference.isSelected(context, Locale.US)) {
                    thanksgiving.add(Calendar.DAY_OF_YEAR, 1);
                    add(new FixedEvent(thanksgiving, context.getString(R.string.black_friday), true), false);
                }
            }
            String churches = prefs.getString(OSTERSONNTAG, NO_CHURCHES);
            if (!NO_CHURCHES.equals(churches)) {
                addChristianEvents(context, year);
                addEasterEvents(context, year);
                addSimpleEvents(context, iceSaints, year);
            }
            addSimpleEvents(context, internationalEvents, year);
            DaylightSavingTime dst = new DaylightSavingTime(year);
            Date dst_begin = dst.getBegin();
            if (dst_begin != null) {
                add(new FixedEvent(DateUtilities.getCalendar(dst_begin), context.getString(R.string.dst_begin), true), false);
            }
            Date dst_end = dst.getEnd();
            if (dst_end != null) {
                add(new FixedEvent(DateUtilities.getCalendar(dst_end), context.getString(R.string.dst_end), true), false);
            }
            add(new FixedEvent(DateUtilities.getMothersDay(year), context.getString(R.string.muttertag), true), false);
            int mode = TKWeekActivity.getIntFromSharedPreferences(prefs, "fathersday", 0);
            if (mode == 1) {
                Calendar cal = CalendarIterator.iterateUntil(DateUtilities.getCalendar(year, Calendar.JUNE, 1), CalendarCondition.createCalendarCondition(CONDITION.EQUAL, Calendar.DAY_OF_WEEK, Calendar.SUNDAY, true), Calendar.DAY_OF_MONTH, 1);
                cal.add(Calendar.WEEK_OF_MONTH, 2);
                add(new FixedEvent(cal, context.getString(R.string.fathersday), true), false);
            } else if (mode == 2) {
                Calendar cal = CalendarIterator.iterateUntil(DateUtilities.getCalendar(year, Calendar.SEPTEMBER, 1), CalendarCondition.createCalendarCondition(CONDITION.EQUAL, Calendar.DAY_OF_WEEK, Calendar.SUNDAY, true), Calendar.DAY_OF_MONTH, 1);
                add(new FixedEvent(cal, context.getString(R.string.fathersday), true), false);
            }
        }
    }

    private void addSeasons(Context context, int year) {
        final Calendar spring = seasons.getCalendar(Seasons.SEASON.SPRING, year);
        if (spring != null) {
            add(new FixedEvent(spring, context.getString(R.string.spring), true), false);
        }
        final Calendar summer = seasons.getCalendar(Seasons.SEASON.SUMMER, year);
        if (summer != null) {
            add(new FixedEvent(summer, context.getString(R.string.summer), true), false);
        }
        final Calendar autumn = seasons.getCalendar(Seasons.SEASON.AUTUMN, year);
        if (autumn != null) {
            add(new FixedEvent(autumn, context.getString(R.string.autumn), true), false);
        }
        final Calendar winter = seasons.getCalendar(Seasons.SEASON.WINTER, year);
        if (winter != null) {
            add(new FixedEvent(winter, context.getString(R.string.winter), true), false);
        }
    }

    private void addEasterEvents(Context context, int year) {
        Calendar easter = DateUtilities.getEasterForYear(context, year);
        if (easter != null) {
            add(new FixedEvent(easter, context.getString(R.string.ostersonntag), true), false);
            easter.add(Calendar.DAY_OF_YEAR, 1);
            add(new FixedEvent(easter, context.getString(R.string.ostermontag), true), false);
            easter.add(Calendar.DAY_OF_YEAR, -3);
            add(new FixedEvent(easter, context.getString(R.string.karfreitag), true), false);
            // Palm sunday: easter - 7 days (-2 - 5)
            easter.add(Calendar.DAY_OF_YEAR, -5);
            add(new FixedEvent(easter, context.getString(R.string.palmsonntag), true), false);
            // Thursday before Shrove Tuesday (Shrove Monday would be -41)
            easter.add(Calendar.DAY_OF_YEAR, -45);
            add(new FixedEvent(easter, context.getString(R.string.weiberfastnacht), true), false);
            // Shrove Monday (4 days from Thursday til Monday)
            easter.add(Calendar.DAY_OF_YEAR, 4);
            add(new FixedEvent(easter, context.getString(R.string.rosenmontag), true), false);
            // Ash Wednesday
            easter.add(Calendar.DAY_OF_YEAR, 2);
            if (DateUtilities.isWesternChurchesSet(context)) {
                add(new FixedEvent(easter, context.getString(R.string.aschermittwoch), true), false);
            }
            // Ascension of Jesus
            easter.add(Calendar.DAY_OF_YEAR, 85); // 39 + 46
            add(new FixedEvent(easter, context.getString(R.string.christi_himmelfahrt), true), false);
            // Pentecost
            easter.add(Calendar.DAY_OF_YEAR, 10);
            add(new FixedEvent(easter, context.getString(R.string.pfingsten), true), false);
            // All Saints: in orthodox churches after Pentecost, otherwise Nov. 1
            if (DateUtilities.isWesternChurchesSet(context)) {
                add(new FixedEvent(DateUtilities.getCalendar(year, Calendar.NOVEMBER, 1), context.getString(R.string.allerheiligen), true), false);
            } else {
                Calendar cal = (Calendar) easter.clone();
                cal.add(Calendar.DAY_OF_YEAR, 7);
                add(new FixedEvent(cal, context.getString(R.string.allerheiligen), true), false);
            }
            // Whit Monday
            easter.add(Calendar.DAY_OF_YEAR, 1);
            add(new FixedEvent(easter, context.getString(R.string.pfingstmontag), true), false);
            // Corpus Christi (10 instead of 11 due to Whit Monday)
            easter.add(Calendar.DAY_OF_YEAR, 10);
            add(new FixedEvent(easter, context.getString(R.string.fronleichnam), true), false);
        }
    }

    private void loadBirthdays(Context context, int year) {
        if (TKWeekUtils.canReadContacts(context)) {
            List<Event> birthdays = ContactsUtils.queryContacts(context);
            for (Event e : birthdays) {
                if (e.getYear() != Event.NOT_SPECIFIED) {
                    Calendar birthday = DateUtilities.getCalendar(e);
                    Calendar temp = DateUtilities.getCalendar(birthday);
                    temp.set(Calendar.YEAR, year);
                    e.occurrences = DateUtilities.getAge(birthday.getTime(), temp);
                }
                e.setYear(year);
                add(e, false);
            }
        }
    }

    private void add(Event e, boolean ignoreDateRange) {
        assert calFrom != null;
        assert calTo != null;
        Calendar cal = DateUtilities.getCalendar(e);
        DateUtilities.clearTimeRelatedFields(cal);
        Date d = cal.getTime();
        if (!ignoreDateRange) {
            if (d.before(calFrom.getTime())) {
                return;
            }
            if (d.after(calTo.getTime())) {
                return;
            }
        }
        if ((search == null) || e.descr.toLowerCase().contains(search)) {
            data.add(e);
        }
    }

    private void addAll(List<Event> l) {
        for (Event e : l) {
            add(e, false);
        }
    }

    private void loadUserEvents(Context context, final File file, final int yearFrom, final int yearTo) {
        FileReader fr = null;
        BufferedReader br = null;
        try {
            if (file.exists()) {
                fr = new FileReader(file);
                br = new BufferedReader(fr);
                String descr;
                while ((descr = br.readLine()) != null) {
                    if (descr.startsWith("$$$INFINITY$$$=")) {
                        String value = descr.substring(15);
                        if (TKWeekUtils.length(value) > 0) {
                            TKWeekActivity.setInfinitySymbol(context, value);
                        }
                        continue;
                    } else if (descr.startsWith("$$$TKWEEK_PREFS_")) {
                        int pos = descr.indexOf("=");
                        if (pos > 17) {
                            String key = descr.substring(16, pos++);
                            if (pos < descr.length()) {
                                try {
                                    int value = Integer.parseInt(descr.substring(pos));
                                    TKWeekActivity.putInt(context, key, value);
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "could not convert to int", e);
                                }
                            }
                        }
                        continue;
                    }
                    boolean fixedEvent = descr.endsWith(FIXED_EVENT);
                    if (fixedEvent) {
                        descr = descr.substring(0, descr.indexOf(FIXED_EVENT));
                    }
                    String strYear = br.readLine();
                    if (strYear == null) {
                        break;
                    }
                    String month = br.readLine();
                    if (month == null) {
                        break;
                    }
                    String day = br.readLine();
                    if (day == null) {
                        break;
                    }
                    boolean annuallyRepeating = !fixedEvent;
                    boolean forceLoaded = false;
                    if (!annuallyRepeating) {
                        if (prefs.getBoolean("load_all_user_events", false)) {
                            forceLoaded = true;
                        }
                    }
                    String runtimeID = annuallyRepeating ? UUID.randomUUID().toString() : null;
                    int intYear = Integer.parseInt(strYear);
                    int _from;
                    int _to;
                    if (annuallyRepeating) {
                        _from = Math.max(yearFrom, intYear);
                        _to = yearTo;
                    } else {
                        _from = intYear;
                        _to = intYear;
                    }
                    for (int year = _from; year <= _to; year++) {
                        add(new Event(descr, year, Integer.parseInt(month), Integer.parseInt(day), false, annuallyRepeating, Event.DEFAULT_COLOUR, Event.DEFAULT_CALENDAR, runtimeID, year != _from), forceLoaded);
                    }
                }
            }
        } catch (Throwable tr) {
            Log.e(TAG, "loadUserEvents()", tr);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // should move to try-with-resources
                }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    // should move to try-with-resources
                }
            }
        }
    }

    private boolean addSimpleEvents(Context context, Locale locale, int[] events, int year) {
        if (PickCountriesPreference.isSelected(context, locale)) {
            addSimpleEvents(context, events, year);
            return true;
        }
        return false;
    }

    private void addBirthdays(Context context, int year) {
        for (int i = 0; i < AnnualEventsListAdapter.birthdays.length; i += 4) {
            Calendar cal = DateUtilities.getCalendar(
                    AnnualEventsListAdapter.birthdays[i + 3],
                    AnnualEventsListAdapter.birthdays[i + 1],
                    AnnualEventsListAdapter.birthdays[i + 2]
            );
            Birthday e = new Birthday(cal,
                    context.getString(AnnualEventsListAdapter.birthdays[i]), null);
            if (e.getYear() != Event.NOT_SPECIFIED) {
                Calendar birthday = DateUtilities.getCalendar(e);
                Calendar temp = DateUtilities.getCalendar(birthday);
                temp.set(Calendar.YEAR, year);
                e.occurrences = DateUtilities.getAge(birthday.getTime(), temp);
            }
            e.setYear(year);
            add(e, false);
        }
    }

    private void addSimpleEvents(Context context, int[] events, int year) {
        for (int i = 0; i < events.length; i += 3) {
            add(new Event(context.getString(events[i]), year, events[i + 1], events[i + 2], true, true), false);
        }
    }

    private void addChristianEvents(Context context, int year) {
        addSimpleEvents(context, christianEvents, year);
        Calendar advent = DateUtilities.getFirstAdvent(year);
        add(new FixedEvent(advent, context.getString(R.string.advent, 1), true), false);
        for (int i = 2; i <= 4; i++) {
            advent.add(Calendar.DAY_OF_YEAR, 7);
            add(new FixedEvent(advent, context.getString(R.string.advent, i), true), false);
        }
    }

    private static class ViewHolder {
        TextView text1, text2, text3, text4;
    }
}
