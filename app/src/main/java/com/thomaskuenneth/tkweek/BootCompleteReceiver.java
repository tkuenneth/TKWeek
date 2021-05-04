/*
 * BootCompleteReceiver.java
 *
 * TKWeek (c) Thomas Künneth 2016 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.Calendar;

/**
 * This class sets an alarm after booting has completed.
 *
 * @author Thomas Künneth
 * @see BroadcastReceiver
 */
public class BootCompleteReceiver extends BroadcastReceiver {

    private static final String TAG = BootCompleteReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        startAlarm(context, false);
    }

    /**
     * Sets an alarm.
     *
     * @param context context
     * @param nextDay Should the alarm be postponed?
     */
    public static void startAlarm(Context context, boolean nextDay) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent
                .getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        Calendar cal = Calendar.getInstance();
        int minCurrent = (cal.get(Calendar.HOUR_OF_DAY) * 60)
                + cal.get(Calendar.MINUTE);
        TimePickerDialogHelper.readFromPreferences(context);
        cal.set(Calendar.HOUR_OF_DAY, TimePickerDialogHelper.hour);
        cal.set(Calendar.MINUTE, TimePickerDialogHelper.minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        int minAlarm = (cal.get(Calendar.HOUR_OF_DAY) * 60)
                + cal.get(Calendar.MINUTE);
        if (nextDay) {
            if (minCurrent >= minAlarm) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
        }
        AlarmManager am = (AlarmManager) context
                .getSystemService(Service.ALARM_SERVICE);
        if (TimePickerDialogHelper.enabled) {
            am.setRepeating(AlarmManager.RTC, cal.getTimeInMillis(),
                    DateUtils.DAY_IN_MILLIS, sender);
            Log.d(TAG, "next alarm is scheduled for " +
                    cal.getTime().toString());
        } else {
            am.cancel(sender);
            Log.d(TAG, "alarm cancelled");
        }
    }
}