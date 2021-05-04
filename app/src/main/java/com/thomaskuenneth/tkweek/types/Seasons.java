/*
 * Seasons.java
 *
 * TKWeek (c) Thomas Künneth 2014 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
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

/**
 * Use this class to get seasons.
 *
 * @author Thomas Künneth
 */
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
        InputStream in = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        Date _from = null;
        Date _to = null;
        try {
            in = am.open("earthseasons.txt");
            isr = new InputStreamReader(in, StandardCharsets.UTF_8);
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
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
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    Log.e(TAG, "error while reading earthseasons.txt",
                            e);
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    Log.e(TAG, "error while reading earthseasons.txt",
                            e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "error while reading earthseasons.txt",
                            e);
                }
            }
        }
    }

    public Calendar getCalendar(SEASON season, int year) {
        int month = -1;
        switch (season) {
            case SPRING:
                month = 3;
                break;
            case SUMMER:
                month = 6;
                break;
            case AUTUMN:
                month = 9;
                break;
            case WINTER:
                month = 12;
                break;
        }
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
