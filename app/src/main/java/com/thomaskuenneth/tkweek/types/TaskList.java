/*
 * TaskList.java
 * 
 * TKWeek (c) Thomas Künneth 2013
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.types;

/**
 * Diese Klasse repräsentiert eine Google Tasks-Aufgabenliste. Aus
 * Effizienzgründen folgt sie bewusst nicht dem üblichen
 * Getter-/Setter-Paradigma.
 * 
 * @author Thomas Künneth
 */
public class TaskList {

	public String title;
	public String id;

	public TaskList(String title, String id) {
		this.title = title;
		this.id = id;
	}

	@Override
	public String toString() {
		return title;
	}
}
