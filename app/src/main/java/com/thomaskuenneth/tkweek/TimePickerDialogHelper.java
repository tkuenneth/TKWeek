/*
 * TimePickerDialogHelper.java
 *
 * TKWeek (c) Thomas Künneth 2016 - 2017
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * Helper class for reading and writing the alarm time for notifications.
 *
 * @author Thomas Künneth
 */
public class TimePickerDialogHelper {

    private static final String NOTIFICATION_TIME_HOUR = "notificationTimeHour";
    private static final String NOTIFICATION_TIME_MINUTE = "notificationTimeMinute";
    private static final String NOTIFICATIONS_ENABLED = "notificationsEnabled";

    public static int hour;
    public static int minute;
    public static boolean enabled;

    /**
     * Reads the current alarm time and sets the corresponding variables.
     *
     * @param context context
     */
    public static void readFromPreferences(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        hour = prefs.getInt(NOTIFICATION_TIME_HOUR, 12);
        minute = prefs.getInt(NOTIFICATION_TIME_MINUTE, 0);
        enabled = prefs.getBoolean(NOTIFICATIONS_ENABLED, true);
    }

    /**
     * Writes the new alarm time to shared preferences.
     * Attention: <code>commit()</code> must be called after this
     * method has returned
     *
     * @param editor  where to write the data
     * @param hour    hour
     * @param minute  minute
     * @param enabled Should the alarm go off?
     */
    public static void writeToPreferences(Editor editor,
                                          int hour,
                                          int minute,
                                          boolean enabled) {
        TimePickerDialogHelper.hour = hour;
        TimePickerDialogHelper.minute = minute;
        editor.putInt(NOTIFICATION_TIME_HOUR, hour);
        editor.putInt(NOTIFICATION_TIME_MINUTE, minute);
        editor.putBoolean(NOTIFICATIONS_ENABLED, enabled);
    }
}
