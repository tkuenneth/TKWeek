/*
 * TKWeekFragmentListAdapter.java
 *
 * Copyright 2009 - 2020 Thomas Künneth
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
package com.thomaskuenneth.tkweek.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.thomaskuenneth.tkweek.R;
import com.thomaskuenneth.tkweek.TKWeekModule;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TKWeekFragmentListAdapter extends BaseAdapter {

    private static final TKWeekModule[] items = TKWeekModule.values();

    private final LayoutInflater mInflater;

    public TKWeekFragmentListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
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
        TKWeekModule item = (TKWeekModule) getItem(position);
        holder.text1.setText(item.getTitleRes());
        holder.text2.setText(item.getDescriptionRes());
        return convertView;
    }

    @Nullable
    public static TKWeekModule find(@NotNull Class<?> clazz) {
        for (TKWeekModule current : items) {
            if (current.getClazz().equals(clazz)) {
                return current;
            }
        }
        return null;
    }

    public static TKWeekModule get(int index) {
        return items[index];
    }

    public static int getPosition(@NotNull Class<?> clazz) {
        for (int i = 0; i < items.length; i++) {
            TKWeekModule current = items[i];
            if (current.getClazz().equals(clazz)) {
                return i;
            }
        }
        return -1;
    }

    private static class ViewHolder {
        TextView text1, text2;
    }
}
