/*
 * TKWeekFragmentListAdapter.java
 *
 * TKWeek (c) Thomas Künneth 2009 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.thomaskuenneth.tkweek.ActivityDescription;
import com.thomaskuenneth.tkweek.R;
import com.thomaskuenneth.tkweek.fragment.AboutFragment;
import com.thomaskuenneth.tkweek.fragment.AboutYearFragment;
import com.thomaskuenneth.tkweek.fragment.AnnualEventsFragment;
import com.thomaskuenneth.tkweek.fragment.CalendarFragment;
import com.thomaskuenneth.tkweek.fragment.DateCalculatorFragment;
import com.thomaskuenneth.tkweek.fragment.DaysBetweenDatesFragment;
import com.thomaskuenneth.tkweek.fragment.MyDayFragment;
import com.thomaskuenneth.tkweek.fragment.WeekFragment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TKWeekFragmentListAdapter extends BaseAdapter {

    private static List<ActivityDescription> items = null;

    private final LayoutInflater mInflater;

    public TKWeekFragmentListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        if (items == null) {
            items = createList(context);
        }
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(
                    R.layout.two_line_item, null);
            holder = new ViewHolder();
            holder.text1 = convertView
                    .findViewById(R.id.text1);
            holder.text2 = convertView
                    .findViewById(R.id.text2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ActivityDescription item = (ActivityDescription) getItem(position);
        holder.text1.setText(item.getText1());
        holder.text2.setText(item.getText2());
        return convertView;
    }

    @Nullable
    public static ActivityDescription find(@NotNull Class<?> clazz) {
        for (int i = 0; i < items.size(); i++) {
            ActivityDescription current = items.get(i);
            if (current.getFragment().equals(clazz)) {
                return current;
            }
        }
        return null;
    }

    public static int getPosition(@NotNull Class<?> clazz) {
        for (int i = 0; i < items.size(); i++) {
            ActivityDescription current = items.get(i);
            if (current.getFragment().equals(clazz)) {
                return i;
            }
        }
        return -1;
    }

    private List<ActivityDescription> createList(Context context) {
        List<ActivityDescription> items = new ArrayList<>();
        items.add(new ActivityDescription(context
                .getString(R.string.week_activity_text1), context
                .getString(R.string.week_activity_text2), WeekFragment.class));
        items.add(new ActivityDescription(context
                .getString(R.string.myday_activity_text1), context
                .getString(R.string.myday_activity_text2), MyDayFragment.class));
        items.add(new ActivityDescription(context
                .getString(R.string.days_between_dates_activity_text1), context
                .getString(R.string.days_between_dates_activity_text2),
                DaysBetweenDatesFragment.class));
        items.add(new ActivityDescription(context
                .getString(R.string.date_calculator_activity_text1), context
                .getString(R.string.date_calculator_activity_text2),
                DateCalculatorFragment.class));
        items.add(new ActivityDescription(context
                .getString(R.string.annual_events_activity_text1), context
                .getString(R.string.annual_events_activity_text2),
                AnnualEventsFragment.class));
        items.add(new ActivityDescription(context
                .getString(R.string.about_a_year_activity_text1), context
                .getString(R.string.about_a_year_activity_text2),
                AboutYearFragment.class));
        items.add(new ActivityDescription(context
                .getString(R.string.calendar_activity_text1), context
                .getString(R.string.calendar_activity_text2),
                CalendarFragment.class));
        items.add(new ActivityDescription(context
                .getString(R.string.about_activity_text1), context
                .getString(R.string.about_activity_text2), AboutFragment.class));
        return items;
    }

    private static class ViewHolder {
        TextView text1, text2;
    }
}
