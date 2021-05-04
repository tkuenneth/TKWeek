/*
 * Mondphasen.java
 *
 * TKWeek (c) Thomas Künneth 2014 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
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

/**
 * Mit dieser Klasse können Mondphasen abgefragt werden.
 *
 * @author Thomas Künneth
 */
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
                        if (line.length() > 0) {
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
