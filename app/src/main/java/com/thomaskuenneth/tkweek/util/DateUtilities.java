/*
 * DateUtilities.java
 *
 * TKWeek (c) 2009 - 2020 Thomas Künneth
 *            2021 MATHEMA GmbH
 * All rights reserved.
 */
package com.thomaskuenneth.tkweek.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.DatePicker;

import com.thomaskuenneth.tkweek.types.Event;
import com.thomaskuenneth.tkweek.util.CalendarCondition.CONDITION;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtilities {

    /**
     * All minutes have this many milliseconds except the last minute of the day
     * on a day defined with a leap second.
     */
    public static final long MILLISECS_PER_MINUTE = 60 * 1000;

    /**
     * Number of milliseconds per hour, except when a leap second is inserted.
     */
    public static final long MILLISECS_PER_HOUR = 60 * MILLISECS_PER_MINUTE;

    /**
     * Number of leap seconds per day expect on <BR/>
     * 1. days when a leap second has been inserted, e.g. 1999 JAN 1. <BR/>
     * 2. Daylight-savings "spring forward" or "fall back" days.
     */
    public static final long MILLISECS_PER_DAY = 24 * MILLISECS_PER_HOUR;

    private static final String TAG = DateUtilities.class.getSimpleName();

    private static final Pattern RFC_3339_PATTERN = Pattern
            .compile("(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d)T(\\d\\d):(\\d\\d):(\\d\\d).*");

    public static final String NO_CHURCHES = "0";
    private static final String WESTERN_CHURCHES = "1";
    private static final String ORTHODOX_CHURCHES = "2";
    public static final String OSTERSONNTAG = "ostersonntag";

    /**
     * Klont eine {@code Calendar}-Instanz.
     *
     * @return die geklonte {@code Calendar}-Instanz
     */
    public static Calendar getCalendar(Calendar cal) {
        return (Calendar) cal.clone();
    }

    /**
     * Liefert eine {@code Calendar}-Instanz und setzt die Felder {@code MINUTE}
     * , {@code SECOND} und {@code MILLISECOND} auf 0. {@code HOUR_OF_DAY} wird
     * auf 12 gesetzt.
     *
     * @return die erzeugte {@code Calendar}-Instanz
     */
    public static Calendar getCalendarClearTimeRelatedFields() {
        Calendar cal = Calendar.getInstance();
        DateUtilities.clearTimeRelatedFields(cal);
        return cal;
    }

    /**
     * Create a {@code Calendar} instance
     *
     * @param event event
     * @return a new {@code Calendar} instance
     */
    public static Calendar getCalendar(Event event) {
        int year = event.getYear();
        if (event.annuallyRepeating &&
                (year == Event.NOT_SPECIFIED)) {
            year = Calendar.getInstance().get(Calendar.YEAR);
        }
        return getCalendar(year, event.getMonth(), event.getDay());
    }

    /**
     * Liefert eine {@code Calendar}-Instanz mit dem übergebenem Datum.
     *
     * @param date Datum
     * @return die erzeugte {@code Calendar}-Instanz
     */
    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    /**
     * Liefert eine {@code Calendar}-Instanz mit übergebenen Tag und Monat im
     * aktuellen Jahr.
     *
     * @param month Monat
     * @param day   Tag im Monat
     * @return die erzeugte {@code Calendar}-Instanz
     */
    public static Calendar getCalendar(int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        return cal;
    }

    /**
     * Liefert eine {@code Calendar}-Instanz mit übergebenen Tag, Monat und
     * Jahr.
     *
     * @param year  Jahr
     * @param month Monat
     * @param day   Tag im Monat
     * @return die erzeugte {@code Calendar}-Instanz
     */
    public static Calendar getCalendar(int year, int month, int day) {
        Calendar cal = getCalendar(month, day);
        cal.set(Calendar.YEAR, year);
        return cal;
    }

    /**
     * Prüft auf Schaltjahr.
     *
     * @param jahr das zu prüfende Jahr
     * @return liefert {@code true}, wenn das übergebene Jahr ein Schaltjahr ist
     */
    public static boolean isSchaltjahr(int jahr) {
        boolean schaltjahr = false;
        if ((jahr % 4) == 0) {
            schaltjahr = true;
            if ((jahr % 400) != 0) {
                if ((jahr % 100) == 0) {
                    schaltjahr = false;
                }
            }
        }
        return schaltjahr;
    }

    /**
     * Liefert das nächste Thanksgiving.
     *
     * @param context Kontext
     * @param year    Jahr
     * @return das nächste Thanksgiving
     */
    public static Calendar getThanksgiving(Context context, int year) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        int thanksgiving = Helper.getIntFromSharedPreferences(prefs,
                "thanksgiving", 1);
        Calendar cal = null;
        if (thanksgiving == 1) {
            cal = DateUtilities.getCalendar(year, Calendar.NOVEMBER, 1);
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.THURSDAY) {
                cal.add(Calendar.DAY_OF_WEEK, 1);
            }
            cal.add(Calendar.DAY_OF_WEEK, 21);
        } else if (thanksgiving == 2) {
            cal = DateUtilities.getCalendar(year, Calendar.OCTOBER, 1);
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                cal.add(Calendar.DAY_OF_WEEK, 1);
            }
            cal.add(Calendar.DAY_OF_WEEK, 7);
        }
        return cal;
    }

    /**
     * Calculate the date of Mother's Day for a given year.
     *
     * @param year year
     * @return Mother's Day
     */
    public static Calendar getMothersDay(int year) {
        Calendar mothersDay = DateUtilities.getCalendar(year, Calendar.MAY, 1);
        CalendarCondition cc = CalendarCondition.createCalendarCondition(
                CONDITION.EQUAL, Calendar.DAY_OF_WEEK, Calendar.SUNDAY, true);
        mothersDay = CalendarIterator.iterateUntil(mothersDay, cc, Calendar.DAY_OF_MONTH, 1);
        mothersDay.add(Calendar.WEEK_OF_YEAR, 1);
        Calendar easter = getEasterForYear(year, WESTERN_CHURCHES);
        if (easter != null) {
            if (diffDayPeriods(mothersDay, easter) == 0) {
                mothersDay.add(Calendar.WEEK_OF_YEAR, -1);
            }
        }
        return mothersDay;
    }

    /**
     * Liefert das Datum des ersten Advents.
     *
     * @param year Jahr
     * @return das Datum des ersten Advents
     */
    public static Calendar getFirstAdvent(int year) {
        CalendarCondition cc = CalendarCondition.createCalendarCondition(
                CONDITION.EQUAL, Calendar.DAY_OF_WEEK, Calendar.SUNDAY, true);
        Calendar cal = CalendarIterator.iterateUntil(
                getCalendar(year, Calendar.DECEMBER, 24), cc,
                Calendar.DAY_OF_MONTH, -1);
        cal.add(Calendar.DAY_OF_YEAR, -21);
        return cal;
    }

    public static boolean isWesternChurchesSet(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String churches = prefs.getString(OSTERSONNTAG, WESTERN_CHURCHES);
        return WESTERN_CHURCHES.equals(churches);
    }

    /**
     * Liefert das Datum eines Ostersonntags als {@link Calendar} -Instanz.
     *
     * @param context Kontext
     * @param year    Jahr, für das Ostern ermittelt werden soll
     * @return Datum des nächsten Ostersonntags
     */
    public static Calendar getEasterForYear(Context context, int year) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String churches = prefs.getString(OSTERSONNTAG, WESTERN_CHURCHES);
        return getEasterForYear(year, churches);
    }


    /**
     * Gets the date of eatser sunday as a {@link Calendar} instance.
     *
     * @param year     year
     * @param churches churches
     * @return easter sunday
     */
    private static Calendar getEasterForYear(int year, String churches) {
        Calendar result = null;
        if (!"0".equals(churches)) {
            result = Calendar.getInstance();
            result.set(Calendar.YEAR, year);
            String date;
            if (ORTHODOX_CHURCHES.equals(churches)) {
                date = getEasterSundyInOrthodoxChurches(year);
            } else {
                date = getEasterSundyInWesternChurches(year);
            }
            if (date == null) {
                result = null;
            } else {
                int month = Integer.parseInt(date.substring(4, 6)) - 1;
                int day = Integer.parseInt(date.substring(6, 8));
                result.set(Calendar.MONTH, month);
                result.set(Calendar.DAY_OF_MONTH, day);
            }
        }
        return result;
    }

    private static String getEasterSundyInWesternChurches(int year) {
        switch (year) {
            case 2000:
                return "20000423";
            case 2001:
                return "20010415";
            case 2002:
                return "20020331";
            case 2003:
                return "20030420";
            case 2004:
                return "20040411";
            case 2005:
                return "20050327";
            case 2006:
                return "20060416";
            case 2007:
                return "20070408";
            case 2008:
                return "20080323";
            case 2009:
                return "20090412";
            case 2010:
                return "20100404";
            case 2011:
                return "20110424";
            case 2012:
                return "20120408";
            case 2013:
                return "20130331";
            case 2014:
                return "20140420";
            case 2015:
                return "20150405";
            case 2016:
                return "20160327";
            case 2017:
                return "20170416";
            case 2018:
                return "20180401";
            case 2019:
                return "20190421";
            case 2020:
                return "20200412";
            case 2021:
                return "20210404";
            case 2022:
                return "20220417";
            case 2023:
                return "20230409";
            case 2024:
                return "20240331";
            case 2025:
                return "20250420";
            case 2026:
                return "20260405";
            case 2027:
                return "20270328";
            case 2028:
                return "20280416";
            case 2029:
                return "20290401";
            case 2030:
                return "20300421";
            default:
                return null;
        }
    }

    private static String getEasterSundyInOrthodoxChurches(int year) {
        switch (year) {
            case 2000:
                return "20000430";
            case 2001:
                return "20010415";
            case 2002:
                return "20020505";
            case 2003:
                return "20030427";
            case 2004:
                return "20040411";
            case 2005:
                return "20050501";
            case 2006:
                return "20060423";
            case 2007:
                return "20070408";
            case 2008:
                return "20080427";
            case 2009:
                return "20090419";
            case 2010:
                return "20100404";
            case 2011:
                return "20110424";
            case 2012:
                return "20120415";
            case 2013:
                return "20130505";
            case 2014:
                return "20140420";
            case 2015:
                return "20150412";
            case 2016:
                return "20160501";
            case 2017:
                return "20170416";
            case 2018:
                return "20180408";
            case 2019:
                return "20190428";
            case 2020:
                return "20200419";
            case 2021:
                return "20210502";
            case 2022:
                return "20220424";
            case 2023:
                return "20230416";
            case 2024:
                return "20240505";
            case 2025:
                return "20250420";
            case 2026:
                return "20260405";
            case 2027:
                return "20270328";
            case 2028:
                return "20280416";
            case 2029:
                return "20290401";
            case 2030:
                return "20300421";
            default:
                return null;
        }
    }

    /**
     * Setzt die Felder {@code MINUTE}, {@code SECOND} und {@code MILLISECOND}
     * auf 0. {@code HOUR_OF_DAY} wird auf 12 gesetzt.
     *
     * @param cal {@link Calendar}-Instanz
     * @return modified calendar
     */
    public static Calendar clearTimeRelatedFields(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    /**
     * Setzt die Felder {@code HOUR_OF_DAY} und {@code MINUTE} auf die
     * übergebenen Werte. {@code SECOND} und {@code MILLISECOND} erhalten den
     * Wert 0.
     *
     * @param cal         {@link Calendar}-Instanz
     * @param hour_of_day Stunden
     * @param minute      Minuten
     */
    public static void setTimeRelatedFields(Calendar cal, int hour_of_day,
                                            int minute) {
        cal.set(Calendar.HOUR_OF_DAY, hour_of_day);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Parst den String als Datum im Format 19700829, wobei zwischen Jahr und
     * Monat sowie zwischen Monat und Tag beliebige Zeichen stehen können. Folgt
     * der übergebene String nicht diesem Muster, wird er als xxxxx-12-31
     * interpretiert. Das Jahr wird auf 9999 gesetzt.
     *
     * @param string das zu parsende Datum
     * @return das Datum
     */
    public static Date getDateFromString1(String string) {
        Date result = null;
        if (string != null) {
            Pattern p = Pattern.compile("(\\d\\d\\d\\d).*(\\d\\d).*(\\d\\d)",
                    Pattern.DOTALL);
            Matcher m = p.matcher(string.subSequence(0, string.length()));
            if (m.matches()) {
                String date = m.group(1) + m.group(2) + m.group(3);
                try {
                    result = Helper.FORMAT_YYYYMMDD.parse(date);
                } catch (Throwable tr) {
                    Log.e(TAG, "getDateFromString1()", tr);
                }
            } else {
                p = Pattern.compile(".*-(\\d\\d)-(\\d\\d)$", Pattern.DOTALL);
                m = p.matcher(string.subSequence(0, string.length()));
                if (m.matches()) {
                    Calendar cal = Calendar.getInstance();
                    try {
                        cal.set(Calendar.MONTH,
                                Integer.parseInt(m.group(1)) - 1);
                        cal.set(Calendar.DAY_OF_MONTH,
                                Integer.parseInt(m.group(2)));
                        cal.set(Calendar.YEAR, Event.NOT_SPECIFIED);
                        result = cal.getTime();
                    } catch (Throwable tr) {
                        Log.e(TAG, "getDateFromString1()", tr);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Ermittelt das Alter. Wurde bei einem der beiden Parameter {@code null}
     * übergeben, liefert diese Methode 0.
     *
     * @param birthday Geburtsdatum
     * @param when     Zeitpunkt
     * @return Alter
     */
    public static int getAge(Date birthday, Calendar when) {
        int age = 0;
        if ((birthday != null) && (when != null)) {
            String stringBirthday = Helper.FORMAT_YYYYMMDD.format(birthday);
            int yearBirthday = Integer.parseInt(stringBirthday.substring(0, 4));
            int yearToday = when.get(Calendar.YEAR);
            age = yearToday - yearBirthday;
        }
        return age;
    }

    /**
     * Liefert ein Objekt des Typs {@link Calendar}.
     *
     * @param year  Jahr
     * @param month Monat
     * @param day   Tag
     * @param cc    Bedingung
     * @param field das zu verändernde Feld
     * @param val   Wert, um der das Feld verändert wird
     * @return Objekt des Typs {@link Calendar}
     */
    public static Calendar getCalendarFromCalendarCondition(int year,
                                                            int month, int day, CalendarCondition cc, int field, int val) {
        Calendar cal = DateUtilities.getCalendar(year, month, day);
        cal = CalendarIterator.iterateUntil(cal, cc, field, val);
        return cal;
    }

    /**
     * Prüft, ob die übergebene Calendar-Instanz das heutige Datum
     * repräsentiert.
     *
     * @param cal zu prüfende Calendar-Instanz
     * @return {@code true}, wenn das heutige Datum repräsentiert
     */
    public static boolean isToday(Calendar cal) {
        Date now = new Date();
        Date date = cal.getTime();
        String strNow = Helper.FORMAT_YYYYMMDD.format(now);
        String strDate = Helper.FORMAT_YYYYMMDD.format(date);
        return strNow.equals(strDate);
    }

    /**
     * Setzt die Zeitzone des Kalenders auf UTC.
     *
     * @param cal Calendar-Instanz, deren Zeitzone auf UTC gesetzt werden soll
     */
    public static void setTimeZoneUTC(Calendar cal) {
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Wandelt eine Zahl in das römische Zahlensystem um.
     *
     * @param intNumber Zahl
     * @return die umgewandelte Zahl
     */
    public static String toRoman(int intNumber) {
        StringBuilder sb = new StringBuilder();
        int value = 1000;
        String symbol = "M";
        while (value != 0) {
            while (intNumber >= value) {
                sb.append(symbol);
                intNumber -= value;
            }
            switch (value) {
                case 1000:
                    value = 900;
                    symbol = "CM";
                    break;
                case 900:
                    value = 500;
                    symbol = "D";
                    break;
                case 500:
                    value = 400;
                    symbol = "CD";
                    break;
                case 400:
                    value = 100;
                    symbol = "C";
                    break;
                case 100:
                    value = 90;
                    symbol = "XC";
                    break;
                case 90:
                    value = 50;
                    symbol = "L";
                    break;
                case 50:
                    value = 40;
                    symbol = "XL";
                    break;
                case 40:
                    value = 10;
                    symbol = "X";
                    break;
                case 10:
                    value = 9;
                    symbol = "IX";
                    break;
                case 9:
                    value = 5;
                    symbol = "V";
                    break;
                case 5:
                    value = 4;
                    symbol = "IV";
                    break;
                case 4:
                    value = 1;
                    symbol = "I";
                    break;
                case 1:
                    value = 0;
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * Get a Calendar from a FC3339 String.
     *
     * @param strDate date as a FC3339 String
     * @return a Calendar
     */
    public static Calendar getCalendarFromRFC3339String(String strDate) {
        Calendar cal = null;
        Matcher matcher = RFC_3339_PATTERN.matcher(strDate);
        if (matcher.matches()) {
            cal = Calendar.getInstance();
            setTimeZoneUTC(cal);
            cal.set(Calendar.YEAR, Integer.parseInt(matcher.group(1)));
            cal.set(Calendar.MONTH, Integer.parseInt(matcher.group(2)) - 1);
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(matcher.group(3)));
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(matcher.group(4)));
            cal.set(Calendar.MINUTE, Integer.parseInt(matcher.group(5)));
            cal.set(Calendar.SECOND, Integer.parseInt(matcher.group(6)));
            // TODO: die Millisekunden könnten wir eigentlich auch auslesen
            cal.set(Calendar.MILLISECOND, 0);
            cal.setTime(cal.getTime());
            cal.setTimeZone(TimeZone.getDefault());
            cal.setTime(cal.getTime());
        }
        return cal;
    }

    /**
     * Get a Date from a FC3339 String.
     *
     * @param strDate date as a FC3339 String
     * @return a Date
     */
    public static Date getDateFromRFC3339String(String strDate) {
        Calendar cal = getCalendarFromRFC3339String(strDate);
        if (cal != null) {
            return cal.getTime();
        }
        return null;
    }

    /**
     * Calculates the number of days between two dates. Time is set to 12:00 noon
     * for both dates.
     *
     * @param start start date
     * @param end   end date
     * @return number of days
     */
    public static long diffDayPeriods(Calendar start, Calendar end) {
        clearTimeRelatedFields(start);
        clearTimeRelatedFields(end);
        long endMillis = end.getTimeInMillis();
        long from = endMillis + end.getTimeZone().getOffset(endMillis);
        long startMillis = start.getTimeInMillis();
        long to = startMillis + start.getTimeZone().getOffset(startMillis);
        if (from > to) {
            return -(to - from) / MILLISECS_PER_DAY;
        } else {
            return (from - to) / MILLISECS_PER_DAY;
        }
    }

    public static void setMinDate(DatePicker picker) {
        Calendar temp = Calendar.getInstance();
        temp.clear();
        temp.set(Calendar.YEAR, 0);
        temp.set(Calendar.DAY_OF_YEAR, 1);
        picker.setMinDate(temp.getTimeInMillis());
    }
}
