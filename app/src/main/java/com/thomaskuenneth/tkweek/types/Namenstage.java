/*
 * Namenstage.java
 *
 * TKWeek (c) Thomas Künneth 2009 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.types;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.thomaskuenneth.tkweek.activity.TKWeekActivity;
import com.thomaskuenneth.tkweek.util.TKWeekUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Hashtable;

public final class Namenstage {

    private static final String TAG = Namenstage.class.getSimpleName();
    private static final Namenstage INSTANCE = new Namenstage();

    private Hashtable<String, String> ht = null;

    private Namenstage() {
    }

    public static String getNameDays(Context context, Date date) {
        if (INSTANCE.ht == null) {
            INSTANCE.ht = new Hashtable<>();
            AssetManager am = context.getResources().getAssets();
            InputStream in = null;
            InputStreamReader isr = null;
            BufferedReader br = null;
            try {
                in = am.open("namenstage.txt");
                isr = new InputStreamReader(in, StandardCharsets.UTF_8);
                br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split("=");
                    INSTANCE.ht.put(data[0], data[1]);
                }
            } catch (IOException e) {
                Log.e(Namenstage.class.getName(),
                        "error while reading namenstage.txt", e);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        Log.e(TAG, "close()", e);
                    }
                }
                if (isr != null) {
                    try {
                        isr.close();
                    } catch (IOException e) {
                        Log.e(TAG, "close()", e);
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        Log.e(TAG, "close()", e);
                    }
                }
            }
        }
        return TKWeekUtils.getStringNotNull(INSTANCE.ht.get(TKWeekActivity.FORMAT_YYMM.format(date)));
    }
}
