/*
 * Mondphasen.java
 *
 * Copyright 2014 - 2020 Thomas Künneth
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
package com.thomaskuenneth.tkweek.types;

import android.content.Context;

import com.thomaskuenneth.tkweek.R;

import net.time4j.Moment;
import net.time4j.PlainDate;
import net.time4j.PlainTime;
import net.time4j.PlainTimestamp;
import net.time4j.calendar.astro.MoonPhase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class Mondphasen {

    private static final String TAG = Mondphasen.class.getSimpleName();

    private Mondphasen() {
    }

    public static List<Event> getMoonPhases(Context context, int year) {
        List<Event> moonPhases = new ArrayList<>();
        Moment start = PlainTimestamp.of(
                        PlainDate.of(year, 1, 1),
                        PlainTime.midnightAtStartOfDay())
                .atUTC();
        Moment end = PlainTimestamp.of(
                        PlainDate.of(year + 1, 1, 1),
                        PlainTime.midnightAtStartOfDay())
                .atUTC();
        Moment[] moments = new Moment[]{start, start};
        Calendar cal = Calendar.getInstance();
        do {
            moments[1] = moments[0];
            Arrays.stream(MoonPhase.values()).iterator().forEachRemaining(moonPhase -> {
                Moment moment = moonPhase.atOrAfter(moments[0]);
                if (moment.isBefore(end)) {
                    cal.setTimeInMillis(1000L * moment.getPosixTime());
                    int id = switch (moonPhase) {
                        case NEW_MOON -> R.string.new_moon;
                        case FIRST_QUARTER -> R.string.first_quarter;
                        case FULL_MOON -> R.string.full_moon;
                        case LAST_QUARTER -> R.string.last_quarter;
                    };
                    moonPhases.add(new FixedEvent(cal, context.getString(id), true));
                    if (moment.isAfter(moments[1])) {
                        moments[1] = moment;
                    }
                }
            });
            moments[0] = moments[1].plus(1, TimeUnit.DAYS);
        } while (moments[0].isBefore(end));
        return moonPhases;
    }
}
