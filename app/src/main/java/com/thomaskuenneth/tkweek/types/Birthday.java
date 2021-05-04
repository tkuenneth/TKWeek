/*
 * Birthday.java
 * 
 * TKWeek (c) Thomas Künneth 2011 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.types;

import java.util.Calendar;

/**
 * Diese Klasse repräsentiert Geburtstage.
 *
 * @author Thomas Künneth
 * @see Event
 */
public class Birthday extends Event implements IContactId {

    private final String contactId;

    /**
     * Erzeugt einen Geburtstag.
     *
     * @param cal   Datum
     * @param descr Beschreibung
     */
    public Birthday(Calendar cal, String descr, String contactId) {
        super(descr,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH),
                true,
                true);
        this.contactId = contactId;
    }

    public String getContactId() {
        return contactId;
    }
}
