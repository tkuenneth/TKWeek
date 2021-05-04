/*
 * FixedEvent.java
 *
 * TKWeek (c) Thomas Künneth 2009 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.types;

import java.util.Calendar;

/**
 * Diese Klasse repräsentiert einmalige, also nicht wiederkehrende Ereignisse.
 * Sie wurde eingeführt, um kennzuzeichnen, wenn keine Arithmetik auf dem Jahr
 * ausgeführt werden soll.
 *
 * @author Thomas Künneth
 * @see Event
 */
public class FixedEvent extends Event {

    /**
     * Erzeugt ein einmaliges Ereignis. Dessen Eigenschaft {@code builtin} wird
     * auf {@code true} gesetzt, damit beim Exportieren der Ereignisliste dieses
     * Ereignis NICHT gespeichert wird.
     *
     * @param cal   Datum
     * @param descr Beschreibung
     */
    public FixedEvent(Calendar cal, String descr) {
        this(cal, descr, true);
    }

    /**
     * Erzeugt ein einmaliges Ereignis.
     *
     * @param cal     Datum
     * @param descr   Beschreibung
     * @param builtin kann auf {@code true} gesetzt werden, wenn das Ereignis beim
     *                Exportieren der Ereignisliste NICHT gespeichert werden soll
     */
    public FixedEvent(Calendar cal, String descr, boolean builtin) {
        super(descr, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
                .get(Calendar.DAY_OF_MONTH), builtin, false);
    }
}
