/*
 * TKWeekUtils.java
 *
 * TKWeek (c) Thomas Künneth 2015 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.thomaskuenneth.tkweek.R;
import com.thomaskuenneth.tkweek.activity.TKWeekActivity;
import com.thomaskuenneth.tkweek.activity.TKWeekPrefsActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

/**
 * Diese Klasse enthält Hilfsmethoden, die anwendungsweit aufgerufen werden.
 *
 * @author Thomas Kuenneth
 */
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
    /**
     * Wird verwendet, um ein Datum zu transportieren. Der Inhalt wird aus dem
     * Ausdruck <code>DateFormat.getDateInstance(DateFormat.FULL)</code>
     * erzeugt.
     */
    public static final String TK_WEEK_FORMAT_FULL = "TKWeek.FORMAT_FULL";

    private static final String TAG = TKWeekUtils.class.getSimpleName();

    public static final int RQ_TKWEEK_PREFS = 0x2908;

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
                    Intent i = new Intent(activity, TKWeekPrefsActivity.class);
                    activity.startActivityForResult(i, RQ_TKWEEK_PREFS);
                }
            }, pos, pos + settings.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            message.setMovementMethod(LinkMovementMethod.getInstance());
        }
        message.setText(spannable);
    }

    /**
     * Speichert einen String.
     *
     * @param context Kontext
     * @param name    Dateiname
     * @param s       String
     * @return true, wenn die Datei erfolgreich gespeichert wurde
     */
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

    /**
     * Lädt eine Datei. Zurückgeliefert wird der Inhalt der Datei als String, oder im Fehlerfall ein Leerstring.
     *
     * @param context Kontext
     * @param name    Name
     * @return Dateiinhalt
     */
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

    /**
     * Liefert den API-Level.
     *
     * @return API-Level
     */
    public static int getAPILevel() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * Fordert Berechtigungen an.
     *
     * @param activity    Activity
     * @param permissions Berechtigungen
     * @param requestCode request code
     */
    @TargetApi(23)
    public static void requestPermissions(Activity activity,
                                          String[] permissions, int requestCode) {
        if (getAPILevel() >= 23) {
            activity.requestPermissions(permissions, requestCode);
        }
    }

    /**
     * Prüft, ob der Nutzer über eine Berechtigung informiert werden soll.
     *
     * @param activity   Activity
     * @param permission Berechtigung
     * @return true, wenn der Nutzer über eine Berechtigung informiert werden soll
     */
    public static boolean shouldShowRequestPermissionRationale(Activity activity,
                                                               String permission) {
        return activity.shouldShowRequestPermissionRationale(permission);
    }

    /**
     * Liefert true, wenn lesend auf das Call Log zugegriffen werden kann
     *
     * @param context Kontext
     * @return true, wenn lesender Zugriff auf das Call Log erlaubt ist
     */
    @TargetApi(23)
    public static boolean canReadCallLog(Context context) {
        return canDoXYZ(context, Manifest.permission.READ_CALL_LOG);
    }

    /**
     * Liefert true, wenn lesend auf Kontakte zugegriffen werden kann
     *
     * @param context Kontext
     * @return true, wenn lesender Zugriff auf Kontakte erlaubt ist
     */
    public static boolean canReadContacts(Context context) {
        return canDoXYZ(context, Manifest.permission.READ_CONTACTS);
    }

    /**
     * Liefert true, wenn lesend auf Kalenderdaten zugegriffen werden kann
     *
     * @param context Kontext
     * @return true, wenn lesender Zugriff auf Kalenderdaten erlaubt ist
     */
    public static boolean canReadCalendar(Context context) {
        return canDoXYZ(context, Manifest.permission.READ_CALENDAR);
    }

    /**
     * Liefert true, wenn schreibend auf externe Medien zugegriffen werden kann
     *
     * @param context Kontext
     * @return true, wenn schreibender Zugriff auf externe Medien erlaubt ist
     */
    public static boolean canWriteExternalStorage(Context context) {
        return canDoXYZ(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    /**
     * Liefert true, wenn die Liste der Konten ermittelt werden darf.
     *
     * @param context Kontext
     * @return true, wenn die Liste der Konten ermittelt werden darf
     */
    public static boolean canGetAccounts(Context context) {
        return canDoXYZ(context, Manifest.permission.GET_ACCOUNTS);
    }

    @TargetApi(23)
    private static boolean canDoXYZ(Context context, String permission) {
        boolean ok = true;
        if (getAPILevel() >= 23) {
            ok = context.checkSelfPermission(permission)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return ok;
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

    /**
     * Erzeugt ein Intent, das ein Datum transportiert. Das Datum wird als
     * String gespeichert. Es wird über die Konstante
     * {@link #TK_WEEK_FORMAT_FULL} angesprochen.
     *
     * @param packageContext Kontext
     * @param cls            Empfänger
     * @param date           das Datum
     * @return das erzeugte Intent
     */
    public static Intent getSendDateIntent(Context packageContext,
                                           Class<?> cls, Date date) {
        Intent intent = new Intent(packageContext, cls);
        intent.putExtra(TK_WEEK_FORMAT_FULL, TKWeekActivity.FORMAT_FULL.format(date));
        return intent;
    }
}
