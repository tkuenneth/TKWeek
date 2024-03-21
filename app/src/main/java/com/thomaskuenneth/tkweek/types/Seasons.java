/*
 * Seasons.java
 *
 * Copyright 2014 - 2020 Thomas Künneth
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
package com.thomaskuenneth.tkweek.types;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.thomaskuenneth.tkweek.util.DateUtilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class Seasons {

    private static final String TAG = Seasons.class.getSimpleName();

    public enum SEASON {
        SPRING, SUMMER, AUTUMN, WINTER
    }

    private final List<String> dates;
    private final Date from;
    private final Date to;

    public Seasons(Context context) {
        dates = new ArrayList<>();
        AssetManager am = context.getResources().getAssets();
        Date _from = null;
        Date _to = null;
        try (
                InputStream in = am.open("earthseasons.txt");
                InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr)
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    dates.add(line);
                    Date current = DateUtilities.getDateFromRFC3339String(line);
                    if (current != null) {
                        if ((_from == null) ||
                                current.before(_from)) {
                            _from = current;
                        }
                        if ((_to == null) ||
                                current.after(_to)) {
                            _to = current;
                        }
                    } else {
                        Log.e(TAG, "getDateFromRFC3339String() returned null for " + line);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "error while reading earthseasons.txt", e);
        } finally {
            from = _from;
            to = _to;
        }
    }

    public Calendar getCalendar(SEASON season, int year) {
        int month = switch (season) {
            case SPRING -> 3;
            case SUMMER -> 6;
            case AUTUMN -> 9;
            case WINTER -> 12;
        };
        String needed = String.format(Locale.US, "%04d", year) + "-"
                + String.format(Locale.US, "%02d", month);
        for (String date : dates) {
            if (date.startsWith(needed)) {
                return DateUtilities.getCalendarFromRFC3339String(date);
            }
        }
        return null;
    }

    public Date getFrom() {
        return from;
    }

    public Date getTo() {
        return to;
    }
}
