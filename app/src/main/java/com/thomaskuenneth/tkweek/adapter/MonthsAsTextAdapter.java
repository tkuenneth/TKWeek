/*
 * MonthsAsTextAdapter.java
 *
 * TKWeek (c) Thomas Künneth 2012 - 2021
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

import com.thomaskuenneth.tkweek.R;
import com.thomaskuenneth.tkweek.R.layout;
import com.thomaskuenneth.tkweek.activity.TKWeekActivity;

import java.util.Calendar;

/**
 * Diese Klasse liefert die zwölf Kalendermonate.
 *
 * @author Thomas Künneth
 */
public class MonthsAsTextAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
    private final String[] months;

    public MonthsAsTextAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        months = new String[12];
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        for (int m = 0; m < 12; m++) {
            cal.set(Calendar.MONTH, m);
            months[m] = TKWeekActivity.FORMAT_MONTH_SHORT.format(cal.getTime());
        }
    }

    public int getCount() {
        return months.length;
    }

    public Object getItem(int position) {
        return months[position];
    }

    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(layout.gallery_item, null);
            holder = new ViewHolder();
            holder.text1 = convertView
                    .findViewById(R.id.gallery_item);
            convertView.setTag(holder);
            holder.text1.setTextColor(holder.text1.getTextColors()
                    .getDefaultColor());
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String month = (String) getItem(position);
        holder.text1.setText(month);
        return convertView;
    }

    private static class ViewHolder {
        TextView text1;
    }
}
