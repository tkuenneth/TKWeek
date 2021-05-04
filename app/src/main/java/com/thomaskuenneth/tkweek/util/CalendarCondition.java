/*
 * CalendarCondition.java
 *
 * TKWeek (c) Thomas Künneth 2010 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.util;

import java.util.Calendar;

/**
 * Diese Klasse beschreibt eine Bedingung, die eine {@link CalendarIterator}-Instanz
 * erfüllen muss.
 *
 * @author Thomas Künneth
 * @see RequiredCalendarConditionException
 */
public class CalendarCondition {

    public enum CONDITION {
        SMALLER, EQUAL, BIGGER, NOT_EQUAL
    }

    private final CONDITION c;
    private final int field;
    private final int value;
    private final boolean required;

    private CalendarCondition(CONDITION c, int field, int value,
                              boolean required) {
        this.c = c;
        this.field = field;
        this.value = value;
        this.required = required;
    }

    /**
     * Factorymethode: erzeugt eine {@link CalendarCondition}-Instanz.
     *
     * @param c        die Bedingung
     * @param field    Konstante aus {@link Calendar}
     * @param value    der erwartete Wert
     * @param required wird auf {@code true} gesetzt, wenn eine Bedingung erfüllt
     *                 sein muss
     * @return die erzeugte Instanz
     */
    public static CalendarCondition createCalendarCondition(CONDITION c,
                                                            int field, int value, boolean required) {
        return new CalendarCondition(c, field, value, required);
    }

    /**
     * Prüft, ob das übergebene Objekt die gespeicherte Bedingung erfüllt.
     * Kann RequiredCalendarConditionException werfen.
     *
     * @param cal das zu prüfende Objekt
     * @return liefert {@code true}, wenn das übergebene Objekt die gespeicherte
     * Bedingung erfüllt
     */
    public boolean matches(Calendar cal)
            throws RequiredCalendarConditionException {
        boolean result = false;
        int current = cal.get(field);
        switch (c) {
            case SMALLER:
                result = current < value;
                break;
            case EQUAL:
                result = current == value;
                break;
            case BIGGER:
                result = current > value;
                break;
            case NOT_EQUAL:
                result = current != value;
                break;
        }
        if (required && !result) {
            throw new RequiredCalendarConditionException();
        }
        return result;
    }
}
