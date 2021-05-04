/*
 * CalendarIterator.java
 *
 * TKWeek (c) Thomas Künneth 2010 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.util;

import java.util.Calendar;

/**
 * Diese Klasse stellt Methoden bereit, die über {@code CalendarCondition}-Instanzen
 * iterieren.
 *
 * @author Thomas Künneth
 * @see CalendarCondition
 * @see RequiredCalendarConditionException
 */
public class CalendarIterator {

    /**
     * Iteriert über eine {@code Calendar}-Instanz, bis eine Bedingung erfüllt
     * ist.
     *
     * @param cal            Kalender, über den iteriert wird
     * @param untilCondition Zielbedingung
     * @param field          Feld, das verändert werden soll, bis die Bedingung erfüllt ist
     * @param value          Wert, um den das Feld verändert wird
     * @return Kalender nach Erreichen der Bedingung
     */
    public static Calendar iterateUntil(Calendar cal,
                                        CalendarCondition untilCondition, int field, int value) {
        while (true) {
            try {
                if (untilCondition.matches(cal)) {
                    // Bedingung erfüllt - raus aus der Schleife
                    break;
                }
            } catch (RequiredCalendarConditionException e) {
                // Bedingung nicht erfüllt - weiter machen
            }
            cal.add(field, value);
        }
        return cal;
    }
}
