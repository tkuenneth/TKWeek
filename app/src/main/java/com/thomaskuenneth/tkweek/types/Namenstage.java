/*
 * Namenstage.java
 *
 * Copyright 2009 - 2020 Thomas Künneth
 *           2021 MATHEMA GmbH
 *           2022 - 2023 Thomas Künneth
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

    private static final String TAG = Namenstage.class.getName();
    private static final Namenstage INSTANCE = new Namenstage();

    private Hashtable<String, String> ht = null;

    private Namenstage() {
    }

    public static String getNameDays(Context context, Date date) {
        if (INSTANCE.ht == null) {
            INSTANCE.ht = new Hashtable<>();
            AssetManager am = context.getResources().getAssets();
            try (
                    InputStream in = am.open("namenstage.txt");
                    InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
                    BufferedReader br = new BufferedReader(isr);
            ) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split("=");
                    INSTANCE.ht.put(data[0], data[1]);
                }
            } catch (IOException e) {
                Log.e(TAG, "error while reading namenstage.txt", e);
            }
        }
        return TKWeekUtils.getStringNotNull(INSTANCE.ht.get(TKWeekActivity.FORMAT_YYMM.format(date)));
    }
}
