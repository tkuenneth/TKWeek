/*
 * WidgetPreference.java
 *
 * TKWeek (c) Thomas Künneth 2013 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.thomaskuenneth.tkweek.activity.TKWeekActivity;
import com.thomaskuenneth.tkweek.appwidget.EventsListWidget;
import com.thomaskuenneth.tkweek.appwidget.EventsWidget;
import com.thomaskuenneth.tkweek.R;
import com.thomaskuenneth.tkweek.appwidget.DateWidget;
import com.thomaskuenneth.tkweek.appwidget.DayOfYearWidget;
import com.thomaskuenneth.tkweek.appwidget.WeekInfoMediumWidget;
import com.thomaskuenneth.tkweek.appwidget.WeekInfoWidget;

/**
 * Stellt einen Dialog dar, in dem die Deckkraft des Widget-Hintergrunds
 * eingestellt werden kann. Der Wert wird in den SharedPreferences abgelegt.
 *
 * @author Thomas Künneth
 */
public class WidgetPreference extends DialogPreference implements
        OnSeekBarChangeListener {

    private static final String TAG = WidgetPreference.class.getSimpleName();
    private static final String OPACITY = "opacity";

    private TextView seekbarInfo;
    private SeekBar seekbar;

    public WidgetPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.widget_preference);
    }

    public static int getOpacity(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(TAG,
                Context.MODE_PRIVATE);
        return prefs.getInt(OPACITY, 128);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        seekbarInfo = view.findViewById(R.id.widget_opacity_info);
        seekbar = view.findViewById(R.id.widget_opacity);
        seekbar.setOnSeekBarChangeListener(this);
        seekbar.setMax(255);
        seekbar.setProgress(getOpacity(getContext()));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            final Context context = getContext();
            SharedPreferences prefs = context.getSharedPreferences(TAG,
                    Context.MODE_PRIVATE);
            Editor e = prefs.edit();
            e.putInt(OPACITY, seekbar.getProgress());
            e.apply();
            TKWeekActivity.updateWidgets(context, new Class<?>[]{DateWidget.class,
                    DayOfYearWidget.class, EventsWidget.class,
                    EventsListWidget.class, WeekInfoWidget.class,
                    WeekInfoMediumWidget.class});
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        seekbarInfo.setText(getContext().getString(R.string.int_slash_int,
                progress, seekBar.getMax()));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
