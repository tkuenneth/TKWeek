/*
 * AlarmPreference.java
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
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import com.thomaskuenneth.tkweek.BootCompleteReceiver;
import com.thomaskuenneth.tkweek.R;
import com.thomaskuenneth.tkweek.TimePickerDialogHelper;

/**
 * This class is used to configure notifications.
 *
 * @author Thomas Künneth
 * @see DialogPreference
 */
public class AlarmPreference extends DialogPreference {

    public static final String KEY_PLAY_SOUND = "playSound";
    public static final String KEY_VIBRATE = "vibrate";
    public static final String KEY_NOTIFICATIONSOFFSETFROM = "notificationsOffsetFrom";
    public static final String KEY_NOTIFICATIONSOFFSETTO = "notificationsOffsetTo";
    public static final String KEY_NOTIFICATIONSMINGROUP = "notificationsMinGroup";

    private CheckBox enabled;
    private LinearLayout children;
    private TimePicker picker;
    private CheckBox playSound;
    private CheckBox vibrate;

    public AlarmPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        TimePickerDialogHelper.readFromPreferences(context);
        children = view.findViewById(R.id.children);
        enabled = view.findViewById(R.id.enabled);
        enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setPickerVisible(isChecked);
            }
        });
        enabled.setChecked(TimePickerDialogHelper.enabled);
        picker = view.findViewById(R.id.picker);
        picker.setIs24HourView(DateFormat.is24HourFormat(context));
        picker.setCurrentHour(TimePickerDialogHelper.hour);
        picker.setCurrentMinute(TimePickerDialogHelper.minute);
        playSound = view.findViewById(R.id.play_sound);
        playSound.setChecked(prefs.getBoolean(KEY_PLAY_SOUND, true));
        vibrate = view.findViewById(R.id.vibrate);
        vibrate.setChecked(prefs.getBoolean(KEY_VIBRATE, true));
        setPickerVisible(enabled.isChecked());
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            Context ctx = getContext();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            Editor editor = prefs.edit();
            editor.putBoolean(KEY_PLAY_SOUND, playSound.isChecked());
            editor.putBoolean(KEY_VIBRATE, vibrate.isChecked());
            boolean _enabled = enabled.isChecked();
            int hour = _enabled ? picker.getCurrentHour() : TimePickerDialogHelper.hour;
            int minutes = _enabled ? picker.getCurrentMinute() : TimePickerDialogHelper.minute;
            TimePickerDialogHelper.writeToPreferences(editor, hour, minutes, _enabled);
            editor.apply();
            BootCompleteReceiver.startAlarm(ctx, true);
        }
    }

    private void setPickerVisible(boolean visible) {
        children.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
