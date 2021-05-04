/*
 * Task.java
 * 
 * TKWeek (c) Thomas Künneth 2013 - 2015
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.types;

import java.util.Date;

/**
 * Diese Klasse repräsentiert Google Tasks-Aufgaben. Aus Effizienzgründen folgt
 * sie bewusst nicht dem üblichen Getter-/Setter-Paradigma.
 *
 * @author Thomas Künneth
 */
public class Task {

    public String title;
    public String notes;
    public Date due;
    public String listTitle;
    public String selfLink;
    public boolean completed;

    public Task(String title, String notes, Date due, String listTitle, String selfLink, boolean completed) {
        this.title = title;
        this.notes = notes;
        this.due = due;
        this.listTitle = listTitle;
        this.selfLink = selfLink;
        this.completed = completed;
    }
}
