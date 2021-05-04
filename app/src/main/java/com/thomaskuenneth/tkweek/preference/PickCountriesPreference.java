/*
 * PickCountriesPreference.java
 *
 * TKWeek (c) Thomas Künneth 2010 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.thomaskuenneth.tkweek.util.TKWeekUtils;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Objects;

public class PickCountriesPreference extends DialogPreference {

    private static final String TAG = PickCountriesPreference.class
            .getSimpleName();

    private SharedPreferences prefs;

    private final Hashtable<String, CheckBox> ht;

    public PickCountriesPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        ht = new Hashtable<>();
    }

    public static boolean isSelected(Context context, Locale l) {
        SharedPreferences prefs = context.getSharedPreferences(TAG,
                Context.MODE_PRIVATE);
        return prefs.getBoolean(l.getCountry(), true);
    }

    @Override
    protected View onCreateDialogView() {
        Context context = getContext();
        prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        ScrollView v = new ScrollView(context);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        v.addView(layout);
        layout.addView(getCheckbox(context, Locale.GERMANY));
        layout.addView(getCheckbox(context, Locale.US));
        layout.addView(getCheckbox(context, Locale.FRANCE));
        layout.addView(getCheckbox(context, TKWeekUtils.AUSTRIA));
        layout.addView(getCheckbox(context, TKWeekUtils.NORWAY));
        layout.addView(getCheckbox(context, TKWeekUtils.SWITZERLAND));
        layout.addView(getCheckbox(context, TKWeekUtils.SINGAPORE));
        layout.addView(getCheckbox(context, TKWeekUtils.NETHERLANDS));
        layout.addView(getCheckbox(context, TKWeekUtils.RUSSIA));
        layout.addView(getCheckbox(context, TKWeekUtils.SWEDEN));
        layout.addView(getCheckbox(context, TKWeekUtils.IRELAND));
        layout.addView(getCheckbox(context, TKWeekUtils.AUSTRALIA));
        return v;
    }

    private View getCheckbox(Context context, Locale l) {
        CheckBox cb = new CheckBox(context);
        String country = l.getCountry();
        ht.put(country, cb);
        cb.setText(l.getDisplayCountry());
        cb.setChecked(prefs.getBoolean(country, true));
        return cb;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            Editor editor = prefs.edit();
            Enumeration<String> e = ht.keys();
            while (e.hasMoreElements()) {
                String country = e.nextElement();
                editor.putBoolean(country, Objects.requireNonNull(ht.get(country)).isChecked());
            }
            editor.apply();
        }
    }
}
