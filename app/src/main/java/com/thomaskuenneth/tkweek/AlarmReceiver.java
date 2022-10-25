/*
 * AlarmReceiver.java
 *
 * Copyright 2016 - 2020 Thomas Künneth
 *           2021 MATHEMA GmbH
 *           2022 Thomas Künneth
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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;

import androidx.core.app.NotificationCompat;

import com.thomaskuenneth.tkweek.activity.TKWeekActivity;
import com.thomaskuenneth.tkweek.adapter.AnnualEventsListAdapter;
import com.thomaskuenneth.tkweek.types.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = AlarmReceiver.class.getSimpleName();
    private static final int RQ_LAUNCH_EVENTS = 1;
    private static final String CHANNEL_ID_EVENTS = "TKWeek_events";

    private static int id = 0;

    public static final String KEY_CANCEL_NOTIFICATION = "cancelNotification";

    private static final String KEY_NOTIFICATIONS_OFFSET_FROM = "notificationsOffsetFrom";
    private static final String KEY_NOTIFICATIONS_OFFSET_TO = "notificationsOffsetTo";
    private static final String KEY_NOTIFICATIONS_MIN_GROUP = "notificationsMinGroup";
    private static final String KEY_PLAY_SOUND = "playSound";
    private static final String KEY_VIBRATE = "vibrate";

    @Override
    public void onReceive(final Context context, Intent intent) {
        BootCompleteReceiver.startAlarm(context, true);
        PowerManager pm = context
                .getSystemService(PowerManager.class);
        final PowerManager.WakeLock wl = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, TAG);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        wl.acquire(10 * 60 * 1000L /*10 minutes*/);
        Runnable r = () -> {
            Calendar calFrom = Calendar.getInstance();
            int offsetFrom = prefs.getInt(KEY_NOTIFICATIONS_OFFSET_FROM, 0);
            calFrom.add(Calendar.DAY_OF_YEAR, offsetFrom);
            Calendar calTo = Calendar.getInstance();
            int offsetTo = prefs.getInt(KEY_NOTIFICATIONS_OFFSET_TO, 1);
            calTo.add(Calendar.DAY_OF_YEAR, offsetTo);
            AnnualEventsListAdapter listAdapter = new AnnualEventsListAdapter(context,
                    calFrom, calTo, false, null);
            List<NotificationCompat.Builder> builders = new ArrayList<>();
            long when = System.currentTimeMillis();
            int numEvents = listAdapter.getCount();
            int minGroup = prefs.getInt(KEY_NOTIFICATIONS_MIN_GROUP, 0);
            boolean groupNotifications = numEvents > minGroup;
            for (int i = 0; i < numEvents; i++) {
                NotificationCompat.Builder b = createBuilder(context,
                        when--,
                        R.drawable.ic_tkweek_one_sheet_no_gradients);
                if (groupNotifications) {
                    b.setGroup("tkweek");
                }
                Event event = (Event) listAdapter.getItem(i);
                b.setContentTitle(listAdapter.getDescription(event, context))
                        .setSubText(listAdapter.getDaysAsString(event))
                        .setContentText(listAdapter.getDateAsString(event, context));
                builders.add(b);
            }
            if (groupNotifications) {
                NotificationCompat.Builder summary = createBuilder(context,
                        when,
                        R.drawable.ic_tkweek_one_sheet_no_gradients);
                String mContentTitle = context.getString(R.string.number_of_events, numEvents);
                summary.setGroup("tkweek");
                summary.setContentTitle(mContentTitle);
                summary.setGroupSummary(true);
                summary.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
                NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
                for (int i = builders.size() - 1; i >= 0; i--) {
                    // NotificationCompat.Builder builder = builders.get(i);
                    String s = String.format("%s   %s",
                            mContentTitle,
                            "SUBTEXT");
                    Spannable sb = new SpannableString(s);
                    sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, s.indexOf("   "),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    style.addLine(sb);
                }
                style.setBigContentTitle(mContentTitle);
                style.setSummaryText(context.getString(R.string.string1_dash_string2,
                        TKWeekActivity.FORMAT_DATE_SHORT.format(calFrom.getTime()),
                        TKWeekActivity.FORMAT_DATE_SHORT.format(calTo.getTime())));
                summary.setStyle(style);
                builders.add(summary);
            }
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID_EVENTS,
                        context.getString(R.string.channel_events),
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.enableLights(true);
                channel.enableVibration(prefs.getBoolean(KEY_VIBRATE, true));
                nm.createNotificationChannel(channel);
            }

            int size = builders.size();
            for (int i = 0; i < size; i++) {
                int defaults = 0;
                if ((i + 1) == size) {
                    defaults |= NotificationCompat.DEFAULT_LIGHTS;
                    if (prefs.getBoolean(KEY_PLAY_SOUND, true)) {
                        defaults |= NotificationCompat.DEFAULT_SOUND;
                    }
                    if (prefs.getBoolean(KEY_VIBRATE, true)) {
                        defaults |= NotificationCompat.DEFAULT_VIBRATE;
                    }
                }
                NotificationCompat.Builder builder = builders.get(i);
                builder.setDefaults(defaults);
                int _id = builder.getExtras().getInt(KEY_CANCEL_NOTIFICATION);
                nm.notify(_id, builder.build());
            }
            wl.release();
        };
        Thread t = new Thread(r);
        t.start();
    }

    private static NotificationCompat.Builder createBuilder(Context context, long when, int smallIcon) {
        int _id = getNextId();
        Intent intent = new Intent(context, TKWeekActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(KEY_CANCEL_NOTIFICATION, _id);
        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID_EVENTS);
        b.getExtras().putInt(KEY_CANCEL_NOTIFICATION, _id);
        b.setSmallIcon(smallIcon)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setWhen(when)
                .setSortKey(Long.toHexString(when))
                .setShowWhen(false)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setContentIntent(PendingIntent.getActivity(context,
                        RQ_LAUNCH_EVENTS, intent, PendingIntent.FLAG_IMMUTABLE));
        return b;
    }

    private static synchronized int getNextId() {
        return id++;
    }
}
