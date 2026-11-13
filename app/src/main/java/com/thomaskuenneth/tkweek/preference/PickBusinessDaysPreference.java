/*
 * PickBusinessDaysPreference.java
 *
 * TKWeek (c) Thomas Künneth 2010 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import androidx.preference.MultiSelectListPreference;
import androidx.preference.PreferenceDataStore;

import com.thomaskuenneth.tkweek.util.Helper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class implements a {@link androidx.preference.DialogPreference} to choose business days
 *
 * @author Thomas Künneth
 */
public class PickBusinessDaysPreference extends MultiSelectListPreference {

    private static final String TAG = PickBusinessDaysPreference.class
            .getSimpleName();

    public PickBusinessDaysPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        List<CharSequence> entries = new ArrayList<>();
        List<CharSequence> weekdays = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        for (int days = 0; days < 7; days++) {
            entries.add(Helper.FORMAT_DAY_OF_WEEK.format(cal.getTime()));
            weekdays.add(Integer.toString(cal.get(Calendar.DAY_OF_WEEK)));
            cal.add(Calendar.DAY_OF_WEEK, 1);
        }
        setEntries(entries.toArray(new CharSequence[0]));
        setEntryValues(weekdays.toArray(new CharSequence[0]));
        setPreferenceDataStore(new BusinessDaysDataStore(context));
    }

    public static Set<Integer> getBusinessDays(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        Set<Integer> businessDays = new HashSet<>();
        for (int weekday = Calendar.SUNDAY; weekday <= Calendar.SATURDAY; weekday++) {
            if (prefs.getBoolean(Integer.toString(weekday), getDefault(weekday))) {
                businessDays.add(weekday);
            }
        }
        return businessDays;
    }

    private static boolean getDefault(int weekday) {
        return !((weekday == Calendar.SATURDAY) || (weekday == Calendar.SUNDAY));
    }

    private static class BusinessDaysDataStore extends PreferenceDataStore {

        private final Context context;

        BusinessDaysDataStore(Context context) {
            this.context = context;
        }

        @Override
        public Set<String> getStringSet(String key, Set<String> defValues) {
            Set<String> selected = new HashSet<>();
            for (int weekday : getBusinessDays(context)) {
                selected.add(Integer.toString(weekday));
            }
            return selected;
        }

        @Override
        public void putStringSet(String key, Set<String> values) {
            SharedPreferences.Editor editor = context
                    .getSharedPreferences(TAG, Context.MODE_PRIVATE).edit();
            for (int weekday = Calendar.SUNDAY; weekday <= Calendar.SATURDAY; weekday++) {
                editor.putBoolean(Integer.toString(weekday),
                        values != null && values.contains(Integer.toString(weekday)));
            }
            editor.apply();
        }
    }
}
