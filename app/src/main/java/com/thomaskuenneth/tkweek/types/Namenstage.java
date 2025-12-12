/*
 * Namenstage.java
 *
 * Copyright 2009 - 2020 Thomas Künneth
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

import com.thomaskuenneth.tkweek.util.Helper;
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
                    BufferedReader br = new BufferedReader(isr)
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
        return TKWeekUtils.getStringNotNull(INSTANCE.ht.get(Helper.FORMAT_YYMM.format(date)));
    }
}
