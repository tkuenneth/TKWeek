/*
 * TimePickerDialogHelper.java
 *
 * Copyright 2016 - 2020 Thomas Künneth
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
package com.thomaskuenneth.tkweek;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class TimePickerDialogHelper {

    private static final String NOTIFICATION_TIME_HOUR = "notificationTimeHour";
    private static final String NOTIFICATION_TIME_MINUTE = "notificationTimeMinute";
    private static final String NOTIFICATIONS_ENABLED = "notificationsEnabled";

    public static int hour;
    public static int minute;
    public static boolean enabled;

    public static void readFromPreferences(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        hour = prefs.getInt(NOTIFICATION_TIME_HOUR, 12);
        minute = prefs.getInt(NOTIFICATION_TIME_MINUTE, 0);
        enabled = prefs.getBoolean(NOTIFICATIONS_ENABLED, true);
    }
}
