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
import android.content.res.AssetManager;
import android.util.Log;

import com.thomaskuenneth.tkweek.R;
import com.thomaskuenneth.tkweek.util.DateUtilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class Mondphasen {

    private static final String TAG = Mondphasen.class.getSimpleName();

    private static final Mondphasen INSTANCE = new Mondphasen();

    private HashMap<String, String> map = null;

    private Mondphasen() {
    }

    public static List<Event> getMoonPhases(Context context, int year) {
        List<Event> result = new ArrayList<>();
        getData(context);
        Set<String> keys = INSTANCE.map.keySet();
        Iterator<String> iter = keys.iterator();
        String strYear = Integer.toString(year);
        while (iter.hasNext()) {
            String key = iter.next();
            if (!key.startsWith(strYear)) {
                continue;
            }
            String value = INSTANCE.map.get(key);
            Calendar cal = DateUtilities.getCalendarFromRFC3339String(key);
            int id = -1;
            if ("0".equals(value)) {
                id = R.string.new_moon;
            } else if ("1".equals(value)) {
                id = R.string.first_quarter;
            } else if ("2".equals(value)) {
                id = R.string.full_moon;
            } else if ("3".equals(value)) {
                id = R.string.last_quarter;
            }
            result.add(new FixedEvent(cal, context.getString(id), true));
        }
        return result;
    }

    private static void getData(Context context) {
        synchronized (INSTANCE) {
            if (INSTANCE.map == null) {
                INSTANCE.map = new HashMap<>();
                AssetManager am = context.getResources().getAssets();
                InputStream in = null;
                InputStreamReader isr = null;
                BufferedReader br = null;
                try {
                    in = am.open("moonphases.txt");
                    isr = new InputStreamReader(in, StandardCharsets.UTF_8);
                    br = new BufferedReader(isr);
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty()) {
                            int pos = line.indexOf(',');
                            String value = line.substring(0, pos);
                            String key = line.substring(pos + 1);
                            INSTANCE.map.put(key, value);
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "error while reading moonphases.txt", e);
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            Log.e(TAG, "error while reading moonphases.txt", e);
                        }
                    }
                    if (isr != null) {
                        try {
                            isr.close();
                        } catch (IOException e) {
                            Log.e(TAG, "error while reading moonphases.txt", e);
                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            Log.e(TAG, "error while reading moonphases.txt", e);
                        }
                    }
                }
            }
        }
    }
}
