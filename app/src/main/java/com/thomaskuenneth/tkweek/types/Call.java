/*
 * Call.java
 * 
 * TKWeek (c) Thomas Künneth 2013
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.types;

/**
 * Diese Klasse repräsentiert einen Anruf. Aus Effizienzgründen folgt sie
 * bewusst nicht dem üblichen Getter-/Setter-Paradigma.
 * 
 * @author Thomas Künneth
 * 
 */
public class Call {

	public String number;
	public long date;
	public String name;
	public String label;
	public int _id;

	public Call(String number, long date, String name, String label, int _id) {
		this.number = number;
		this.date = date;
		this.name = name;
		this.label = label;
		this._id = _id;
	}
}
