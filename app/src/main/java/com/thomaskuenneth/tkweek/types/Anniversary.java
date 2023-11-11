/*
 * Anniversary.java
 *
 * Copyright 2011 - 2020 Thomas Künneth
 *           2021 MATHEMA GmbH
 *           2022 - 2023 Thomas Künneth
 */
package com.thomaskuenneth.tkweek.types;

import java.util.Calendar;

public class Anniversary extends Event implements IContactId {

    private String text;

    private final String contactId;

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
