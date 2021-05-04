/*
 * Appointment.java
 * 
 * TKWeek (c) Thomas Künneth 2012 - 2013
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.types;

/**
 * Diese Klasse repräsentiert einen Termin. Aus Effizienzgründen folgt sie
 * bewusst nicht dem üblichen Getter-/Setter-Paradigma.
 * 
 * @author Thomas Künneth
 * 
 */
public class Appointment {

	public String title;
	public String description;
	public long dtstart;
	public long dtend;
	public long id;
	public int color;

	public Appointment(String title, String description, long dtstart,
			long dtend, long id, int color) {
		this.title = title;
		this.description = description;
		this.dtstart = dtstart;
		this.dtend = dtend;
		this.id = id;
		this.color = color;
	}
}
