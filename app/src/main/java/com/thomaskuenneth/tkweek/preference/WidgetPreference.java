/*
 * WidgetPreference.java
 *
 * Copyright 2013 - 2020 Thomas Künneth
 * Copyright 2021 MATHEMA GmbH
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

import com.thomaskuenneth.tkweek.R;
import com.thomaskuenneth.tkweek.activity.TKWeekActivity;
import com.thomaskuenneth.tkweek.appwidget.DateWidget;
import com.thomaskuenneth.tkweek.appwidget.DayOfYearWidget;
import com.thomaskuenneth.tkweek.appwidget.EventsListWidget;
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
                    DayOfYearWidget.class, EventsListWidget.class, WeekInfoWidget.class});
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
