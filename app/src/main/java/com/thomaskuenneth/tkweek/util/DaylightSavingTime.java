/*
 * DaylightSavingTime.java
 *
 * TKWeek (c) Thomas Künneth 2010 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Mit Hilfe dieser Klasse kann der Beginn und das Ende der Sommerzeit ermittelt
 * werden.
 *
 * @author Thomas Künneth
 */
public final class DaylightSavingTime {

    private Date begin;
    private Date end;

    public DaylightSavingTime(int year) {
        Calendar cal = Calendar.getInstance();
        if (year != -1) {
            cal.set(Calendar.YEAR, year);
        }
        TimeZone timeZone = cal.getTimeZone();
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        Date begin = null;
        Date end = null;
        for (int i = cal.getActualMinimum(Calendar.DAY_OF_YEAR); i <= cal
                .getActualMaximum(Calendar.DAY_OF_YEAR); i++) {
            // Tag innerhalb des jahres
            cal.set(Calendar.DAY_OF_YEAR, i);
            // Datum und Startzeit
            cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
            cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
            Date date1 = cal.getTime();
            boolean b1 = timeZone.inDaylightTime(date1);
            // Datum und Endezeit
            cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MINUTE));
            cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
            Date date2 = cal.getTime();
            boolean b2 = timeZone.inDaylightTime(date2);
            if (b1 != b2) {
                if (b2) {
                    begin = date1;
                } else {
                    end = date2;
                }
            }
            this.begin = begin;
            this.end = end;
        }
    }

    public Date getBegin() {
        return begin;
    }

    public Date getEnd() {
        return end;
    }
}
