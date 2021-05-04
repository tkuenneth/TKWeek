/*
 * Anniversary.java
 * 
 * TKWeek (c) Thomas Künneth 2011 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.types;

import java.util.Calendar;

/**
 * Diese Klasse repräsentiert Jahrestage.
 *
 * @author Thomas Künneth
 * @see Event
 */
public class Anniversary extends Event implements IContactId {

    private String text;

    private final String contactId;

    /**
     * Erzeugt einen Jahrestag.
     *
     * @param cal   Datum
     * @param descr Beschreibung
     */
    public Anniversary(Calendar cal, String descr, String contactId) {
        super(descr,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH),
                true,
                true);
        text = null;
        this.contactId = contactId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getContactId() {
        return contactId;
    }
}
