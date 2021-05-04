/*
 * IContactId.java
 * 
 * TKWeek (c) Thomas Künneth 2011 - 2014
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.types;

/**
 * Erweitert Ereignisse um eine Referenz auf die Kontakt-Id; kann beispielsweise
 * verwendet werden, um mittels Intents einen Kontakt anzuzeiogen.
 * 
 * @author Thomas Künneth
 * 
 */
public interface IContactId {

	public String getContactId();
}
