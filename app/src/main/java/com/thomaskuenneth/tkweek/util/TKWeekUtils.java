/*
 * TKWeekUtils.java
 *
 * Copyright 2015 - 2020 Thomas Künneth
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
package com.thomaskuenneth.tkweek.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.thomaskuenneth.tkweek.R;
import com.thomaskuenneth.tkweek.TKWeekModule;
import com.thomaskuenneth.tkweek.adapter.TKWeekFragmentListAdapter;
import com.thomaskuenneth.tkweek.viewmodel.TKWeekViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

public class TKWeekUtils {

    public static final String EMPTY_STRING = "";
    public static final Locale AUSTRIA = new Locale("de", "AT");
    public static final Locale SWITZERLAND = new Locale("de", "CH");
    public static final Locale SINGAPORE = new Locale("en", "SG");
    public static final Locale NORWAY = new Locale("nb", "NO");
    public static final Locale NETHERLANDS = new Locale("nl", "NL");
    public static final Locale RUSSIA = new Locale("ru", "RU");
    public static final Locale SWEDEN = new Locale("sv", "SE");
    public static final Locale IRELAND = new Locale("en", "IE");
    public static final Locale AUSTRALIA = new Locale("en", "AU");

    private static final String TAG = TKWeekUtils.class.getSimpleName();

    public static void linkToSettings(ViewGroup layout, Activity activity, int resId) {
        TextView message = layout.findViewById(R.id.message);
        String s = activity.getString(resId);
        String settings = activity.getString(R.string.settings);
        Spannable spannable = new SpannableString(s);
        int pos = s.indexOf(settings);
        if (pos >= 0) {
            spannable.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    if (activity instanceof FragmentActivity) {
                        TKWeekViewModel viewModel = new ViewModelProvider((FragmentActivity) activity).get(TKWeekViewModel.class);
                        TKWeekModule module = TKWeekFragmentListAdapter.find(TKWeekModule.Prefs.getClazz());
                        if (module != null) {
                            viewModel.selectModuleWithArguments(module, new Bundle(), false);
                        }
                    }
                }
            }, pos, pos + settings.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            message.setMovementMethod(LinkMovementMethod.getInstance());
        }
        message.setText(spannable);
    }

    public static boolean save(Context context, String name, String s) {
        FileOutputStream fos = null;
        boolean ok = false;
        try {
            fos = context.openFileOutput(name, Context.MODE_PRIVATE);
            fos.write(s.getBytes());
            ok = true;
        } catch (IOException e) {
            Log.e(TAG, "save()", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG, "save()", e);
                }
            }
        }
        return ok;
    }

    public static String load(Context context, String name) {
        StringBuilder sb = new StringBuilder();
        FileReader fr = null;
        try {
            File f = new File(context.getFilesDir(), name);
            if (f.exists()) {
                fr = new FileReader(f);
                int ch;
                while ((ch = fr.read()) != -1) {
                    sb.append((char) ch);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "load()", e);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    Log.e(TAG, "load()", e);
                }
            }
        }
        return sb.toString();
    }

    public static boolean canPostNotifications(Context context) {
        if (Build.VERSION.SDK_INT >= 33)
            return canDoXYZ(context, Manifest.permission.POST_NOTIFICATIONS);
        else
            return true;
    }

    public static boolean shouldShowRequestPermissionRationale(Activity activity,
                                                               String permission) {
        return activity.shouldShowRequestPermissionRationale(permission);
    }

    public static boolean canReadContacts(Context context) {
        return canDoXYZ(context, Manifest.permission.READ_CONTACTS);
    }

    public static boolean canReadCalendar(Context context) {
        return canDoXYZ(context, Manifest.permission.READ_CALENDAR);
    }

    private static boolean canDoXYZ(Context context, String permission) {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static String getStringNotNull(String string) {
        return (string == null) ? EMPTY_STRING : string;
    }

    public static int length(String string) {
        return getStringNotNull(string).length();
    }

    public static String integerToString(int i) {
        return String.format(Locale.US, "%d", i);
    }
}
