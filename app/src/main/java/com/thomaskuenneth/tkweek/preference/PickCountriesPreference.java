/*
 * PickCountriesPreference.java
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
package com.thomaskuenneth.tkweek.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import androidx.collection.ArraySet;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.PreferenceManager;

import com.thomaskuenneth.tkweek.util.TKWeekUtils;

import java.util.ArrayList;
import java.util.Locale;

public class PickCountriesPreference extends MultiSelectListPreference {

    private static final ArrayList<CharSequence> entries = new ArrayList<>();
    private static final ArrayList<CharSequence> values = new ArrayList<>();
    private static final ArraySet<String> defaults = new ArraySet<>();

    static {
        addCountry(Locale.GERMANY);
        addCountry(Locale.US);
        addCountry(Locale.FRANCE);
        addCountry(TKWeekUtils.AUSTRIA);
        addCountry(TKWeekUtils.NORWAY);
        addCountry(TKWeekUtils.SWITZERLAND);
        addCountry(TKWeekUtils.SINGAPORE);
        addCountry(TKWeekUtils.NETHERLANDS);
        addCountry(TKWeekUtils.RUSSIA);
        addCountry(TKWeekUtils.SWEDEN);
        addCountry(TKWeekUtils.IRELAND);
        addCountry(TKWeekUtils.AUSTRALIA);
    }

    public PickCountriesPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEntries(entries.toArray(new CharSequence[0]));
        setEntryValues(values.toArray(new CharSequence[0]));
        setValues(defaults);
    }

    public static boolean isSelected(Context context, Locale l) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getStringSet("countries", defaults).contains(l.getCountry());
    }

    private static void addCountry(Locale l) {
        String country = l.getCountry();
        defaults.add(country);
        values.add(country);
        entries.add(l.getDisplayCountry());
    }
}
