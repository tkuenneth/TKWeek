/*
 * MonthsAsTextAdapter.java
 *
 * Copyright 2012 - 2020 Thomas Künneth
 *           2021 MATHEMA GmbH
 *           2022 - 2023 Thomas Künneth
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
import com.thomaskuenneth.tkweek.util.Helper;

import java.util.Calendar;

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
            months[m] = Helper.FORMAT_MONTH_SHORT.format(cal.getTime());
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
