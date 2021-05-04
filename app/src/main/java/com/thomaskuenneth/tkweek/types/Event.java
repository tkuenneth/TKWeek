/*
 * Event.java
 *
 * TKWeek (c) Thomas Künneth 2009 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.types;

import android.os.Parcel;
import android.os.Parcelable;

import com.thomaskuenneth.tkweek.util.DateUtilities;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;

/**
 * Diese Klasse repräsentiert ein Ereignis. Dieses hat eine Beschreibung, ein
 * Flag, das ein vom Benutzer erstelltes Ereignis kennzeichnet, sowie die Felder
 * Monat, Tag und Jahr. Letzteres kann auf {@code NOT_SPECIFIED} gesetzt
 * werden. In diesem Fall hat das Ereignis keinen Beginn. Sonst vermerkt das
 * Jahr, wann das Ereignis zum ersten Mal eingetreten ist bzw. es eintreten
 * wird. Schließlich kann gekennzeichnet werden, ob das Ereignis einmalig oder
 * jährlich wiederkehrend ist.
 * <p>
 * The runtimeID should be used to group events.
 *
 * @author Thomas Künneth
 */
public class Event implements Parcelable {

    public static final int NOT_SPECIFIED = 9999;
    public static final int DEFAULT_COLOUR = 0;
    public static final String DEFAULT_CALENDAR = "";

    public String descr;
    public boolean builtin;
    public boolean annuallyRepeating;
    public int color;
    public String calendarName;
    public String runtimeID;
    public boolean cloned;
    public int occurrences;

    private int year, month, day;

    public Event() {
        this(Calendar.getInstance());
    }

    public Event(Calendar cal) {
        this(null,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH),
                false,
                false);
    }

    public Event(String descr, Date date, boolean annuallyRepeating) {
        this(DateUtilities.getCalendar(date));
        this.descr = descr;
        this.annuallyRepeating = annuallyRepeating;
    }

    public Event(String descr,
                 int year, int month, int day,
                 boolean builtin,
                 boolean annuallyRepeating) {
        this(descr, year, month, day, builtin, annuallyRepeating, DEFAULT_COLOUR);
    }

    public Event(String descr,
                 int year, int month, int day,
                 boolean builtin,
                 boolean annuallyRepeating,
                 int color) {
        this(descr, year, month, day, builtin, annuallyRepeating, color, DEFAULT_CALENDAR);
    }

    public Event(String descr,
                 int year, int month, int day,
                 boolean builtin,
                 boolean annuallyRepeating,
                 int color,
                 String calendarName) {
        this(descr, year, month, day, builtin, annuallyRepeating, color, calendarName, null);
    }

    public Event(String descr,
                 int year, int month, int day,
                 boolean builtin,
                 boolean annuallyRepeating,
                 int color,
                 String calendarName,
                 String runtimeID) {
        this(descr, year, month, day, builtin, annuallyRepeating, color, calendarName, runtimeID, false);
    }

    public Event(String descr,
                 int year, int month, int day,
                 boolean builtin,
                 boolean annuallyRepeating,
                 int color,
                 String calendarName,
                 String runtimeID,
                 boolean cloned) {
        this(descr, year, month, day, builtin, annuallyRepeating, color, calendarName, runtimeID, cloned, 0);
    }

    public Event(String descr,
                 int year, int month, int day,
                 boolean builtin,
                 boolean annuallyRepeating,
                 int color,
                 String calendarName,
                 String runtimeID,
                 boolean cloned,
                 int occurrences) {
        this.descr = descr;
        this.year = year;
        this.month = month;
        this.day = day;
        this.builtin = builtin;
        this.annuallyRepeating = annuallyRepeating;
        this.color = color;
        this.calendarName = calendarName;
        this.runtimeID = runtimeID;
        this.cloned = cloned;
        this.occurrences = occurrences;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Event createFromParcel(Parcel in) {
            String descr = in.readString();
            int year = in.readInt();
            int month = in.readInt();
            int day = in.readInt();
            boolean builtin = in.readInt() == 1;
            boolean annuallyRepeating = in.readInt() == 1;
            int color = in.readInt();
            String calendarName = in.readString();
            String runtimeID = in.readString();
            boolean cloned = in.readInt() == 1;
            int occurrences = in.readInt();
            return new Event(descr,
                    year,
                    month,
                    day,
                    builtin,
                    annuallyRepeating,
                    color,
                    calendarName,
                    runtimeID,
                    cloned,
                    occurrences);
        }

        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    @NotNull
    @Override
    public String toString() {
        return descr + ", " +
                getYear() + "-" +
                getMonth() + "-" +
                getDay() + "-" +
                builtin + "-" +
                annuallyRepeating + " - " +
                color + " - " +
                calendarName + " - " +
                runtimeID + " - " +
                cloned + " - " +
                occurrences;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(descr);
        dest.writeInt(year);
        dest.writeInt(month);
        dest.writeInt(day);
        dest.writeInt(builtin ? 1 : 0);
        dest.writeInt(annuallyRepeating ? 1 : 0);
        dest.writeInt(color);
        dest.writeString(calendarName);
        dest.writeString(runtimeID);
        dest.writeInt(cloned ? 1 : 0);
        dest.writeInt(occurrences);
    }
}
