/*
 * MoonPhasesPreference.java
 *
 * TKWeek (c) Thomas Künneth 2016 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.thomaskuenneth.tkweek.R;

public class MoonPhasesPreference extends DialogPreference {

    public static final String HIDE_MOONPHASES = "hide_moon_phases";
    public static final String SHOW_NEW_MOON = "show_new_moon";
    public static final String SHOW_FULL_MOON = "show_full_moon";
    public static final String SHOW_FIRST_QUARTER = "show_first_quarter";
    public static final String SHOW_LAST_QUARTER = "show_last_quarter";

    private SharedPreferences prefs;
    private LinearLayout checkboxes;
    private CheckBox showMoonPhases;
    private CheckBox newMoon;
    private CheckBox fullMoon;
    private CheckBox firstQuarter;
    private CheckBox lastQuarter;

    public MoonPhasesPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateDialogView() {
        Context context = getContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        checkboxes = new LinearLayout(context);
        checkboxes.setOrientation(LinearLayout.VERTICAL);
        showMoonPhases = new CheckBox(context);
        showMoonPhases.setText(context.getString(R.string.show_moon_phases));
        showMoonPhases.setOnCheckedChangeListener((buttonView, isChecked) -> setVisibility(isChecked));
        showMoonPhases.setChecked(!prefs.getBoolean(HIDE_MOONPHASES, false));
        layout.addView(showMoonPhases);
        newMoon = createAndAddMoonPhaseCheckbox(R.string.new_moon, SHOW_NEW_MOON);
        firstQuarter = createAndAddMoonPhaseCheckbox(R.string.first_quarter, SHOW_FIRST_QUARTER);
        fullMoon = createAndAddMoonPhaseCheckbox(R.string.full_moon, SHOW_FULL_MOON);
        lastQuarter = createAndAddMoonPhaseCheckbox(R.string.last_quarter, SHOW_LAST_QUARTER);
        layout.addView(checkboxes);
        setVisibility(showMoonPhases.isChecked());
        return layout;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            Editor editor = prefs.edit();
            editor.putBoolean(HIDE_MOONPHASES, !showMoonPhases.isChecked());
            editor.putBoolean(SHOW_NEW_MOON, newMoon.isChecked());
            editor.putBoolean(SHOW_FIRST_QUARTER, firstQuarter.isChecked());
            editor.putBoolean(SHOW_FULL_MOON, fullMoon.isChecked());
            editor.putBoolean(SHOW_LAST_QUARTER, lastQuarter.isChecked());
            editor.apply();
        }
    }

    private CheckBox createAndAddMoonPhaseCheckbox(int resId, String key) {
        Context context = getContext();
        CheckBox cb = new CheckBox(context);
        cb.setText(context.getString(resId));
        cb.setChecked(prefs.getBoolean(key, true));
        checkboxes.addView(cb);
        return cb;
    }

    private void setVisibility(boolean visible) {
        checkboxes.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
