/*
 * DaylightSavingTime.java
 *
 * Copyright 2010 - 2020 Thomas Künneth
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
package com.thomaskuenneth.tkweek.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
            cal.set(Calendar.DAY_OF_YEAR, i);
            // date and start time
            cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
            cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
            Date date1 = cal.getTime();
            boolean b1 = timeZone.inDaylightTime(date1);
            // date and end time
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
