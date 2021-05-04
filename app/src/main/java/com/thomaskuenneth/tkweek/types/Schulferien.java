/*
 * Schulferien.java
 *
 * TKWeek (c) Thomas Künneth 2014 - 2016
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.types;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.thomaskuenneth.tkweek.R;
import com.thomaskuenneth.tkweek.activity.TKWeekActivity;
import com.thomaskuenneth.tkweek.util.DateUtilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * Diese Klasse ermöglicht den Zugriff auf gespeicherte Schulferien. Die
 * Informationen werden aus der Datei schulferien_Deutschland.txt gelesen. Jede
 * Zeile entspricht einem Bundesland. Die Spalten beginnen immer an der gleichen
 * Position. Land: 0, Herbst: 30, Weihnachten: 50, Winter: 70, Ostern: 90,
 * Pfingsten: 110, Sommer: 140 (früher: 130)
 *
 * @author Thomas
 */
public final class Schulferien {

    private static final String TAG = Schulferien.class.getSimpleName();
    private static final Schulferien INSTANCE = new Schulferien();

    private Hashtable<String, List<Event>> ht;

    private Schulferien() {
        ht = null;
    }

    /**
     * Liefert alle hinterlegten Ferien für ein Bundesland.
     *
     * @param context    Kontext
     * @param bundesland Bundesland
     * @return alle hinterlegten Ferien
     */
    public static List<Event> getSchulferien(Context context, String bundesland) {
        initialize(context);
        return INSTANCE.ht.get(bundesland);
    }

    /**
     * Liefert die Liste der Bundesländer.
     *
     * @param context Kontext
     * @return Liste der Bundesländer
     */
    public static String[] getLaender(Context context) {
        initialize(context);
        Set set = INSTANCE.ht.keySet();
        String[] laender = new String[set.size()];
        set.toArray(laender);
        Arrays.sort(laender);
        return laender;
    }

    private static void initialize(Context context) {
        synchronized (INSTANCE) {
            if (INSTANCE.ht == null) {
                INSTANCE.ht = new Hashtable<>();
                AssetManager am = context.getResources().getAssets();
                InputStream in = null;
                InputStreamReader isr = null;
                BufferedReader br = null;
                try {
                    in = am.open("schulferien_Deutschland.txt");
                    isr = new InputStreamReader(in, StandardCharsets.UTF_8);
                    br = new BufferedReader(isr);
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith("#")) {
                            continue;
                        }
                        if (line.length() != 167) {
                            throw new IllegalArgumentException("wrong length: " + line);
                        }
                        String land = line.substring(0, 30).trim();
                        String herbst = line.substring(30, 50).trim();
                        String weihnachten = line.substring(50, 70).trim();
                        String winter = line.substring(70, 90).trim();
                        String ostern = line.substring(90, 110).trim();
                        String pfingsten = line.substring(110, 150).trim();
                        String sommer = line.substring(150).trim();
                        List<Event> list = INSTANCE.ht.computeIfAbsent(land, k -> new ArrayList<>());
                        fuegeHinzu(context, list, herbst, R.string.herbstferien);
                        fuegeHinzu(context, list, weihnachten,
                                R.string.weihnachtsferien);
                        fuegeHinzu(context, list, winter, R.string.winterferien);
                        fuegeHinzu(context, list, ostern, R.string.osterferien);
                        fuegeHinzu(context, list, pfingsten,
                                R.string.pfingstferien);
                        fuegeHinzu(context, list, sommer, R.string.sommerferien);
                    }
                } catch (IOException e) {
                    Log.e(Schulferien.class.getName(),
                            "error while reading schulferien_Deutschland.txt", e);
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            Log.e(TAG, "close()", e);
                        }
                    }
                    if (isr != null) {
                        try {
                            isr.close();
                        } catch (IOException e) {
                            Log.e(TAG, "close()", e);
                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            Log.e(TAG, "close()", e);
                        }
                    }
                }
            }
        }
    }

    private static void fuegeHinzu(Context context, List<Event> list,
                                   String _data, int resId) {
        String[] segments = _data.split("/");
        for (String data : segments) {
            try {
                switch (data.length()) {
                    case 0:
                        // no date set
                        break;
                    case 8:
                        Date date = TKWeekActivity.FORMAT_DDMMYY.parse(data);
                        Calendar cal = DateUtilities.getCalendar(date);
                        list.add(new FixedEvent(cal, context.getString(resId)));
                        break;
                    case 17:
                        if (data.charAt(8) == '-') {
                            Date from = TKWeekActivity.FORMAT_DDMMYY.parse(data.substring(0, 8));
                            Calendar calFrom = DateUtilities.getCalendar(from);
                            Date to = TKWeekActivity.FORMAT_DDMMYY.parse(data.substring(9, 17));
                            Calendar calTo = DateUtilities.getCalendar(to);
                            list.add(new FixedEvent(calFrom, context.getString(R.string.begin,
                                    context.getString(resId))));
                            list.add(new FixedEvent(calTo, context.getString(R.string.end,
                                    context.getString(resId))));
                        } else {
                            throw new IllegalArgumentException("illegal separator in " + data);
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("wrong length: " + data);
                }
            } catch (ParseException e) {
                Log.e(TAG, "error while parsing " + data, e);
            }
        }
    }
}
