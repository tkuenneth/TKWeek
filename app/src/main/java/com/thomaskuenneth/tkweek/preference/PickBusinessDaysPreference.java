/*
 * PickBusinessDaysPreference.java
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

import com.thomaskuenneth.tkweek.util.Helper;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Objects;

/**
 * This class implements a {@link DialogPreference} to choose business days
 *
 * @author Thomas Künneth
 */
public class PickBusinessDaysPreference extends DialogPreference {

    private static final String TAG = PickBusinessDaysPreference.class
            .getSimpleName();

    private SharedPreferences prefs;

    private final Hashtable<String, CheckBox> ht;

    public PickBusinessDaysPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        ht = new Hashtable<>();
    }

    public static String getTag() {
        return TAG;
    }

    public static boolean getDefault(int weekday) {
        return !((weekday == Calendar.SATURDAY) || (weekday == Calendar.SUNDAY));
    }

    @Override
    protected View onCreateDialogView() {
        Context context = getContext();
        prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        ScrollView v = new ScrollView(context);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        v.addView(layout);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        for (int days = 0; days < 7; days++) {
            layout.addView(getCheckbox(context, cal.get(Calendar.DAY_OF_WEEK),
                    Helper.FORMAT_DAY_OF_WEEK.format(cal.getTime())));
            cal.add(Calendar.DAY_OF_WEEK, 1);
        }
        return v;
    }

    private View getCheckbox(Context context, int weekday, String weekdayName) {
        CheckBox cb = new CheckBox(context);
        String key = Integer.toString(weekday);
        ht.put(key, cb);
        cb.setText(weekdayName);
        cb.setChecked(prefs.getBoolean(key, getDefault(weekday)));
        return cb;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            Editor editor = prefs.edit();
            Enumeration<String> e = ht.keys();
            while (e.hasMoreElements()) {
                String key = e.nextElement();
                editor.putBoolean(key, Objects.requireNonNull(ht.get(key)).isChecked());
            }
            editor.apply();
        }
    }
}
