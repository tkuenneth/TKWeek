/*
 * Sign.java
 *
 * TKWeek (c) Thomas Künneth 2009 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.types;

import java.util.Hashtable;

/**
 * Diese Klasse wird verwendet, um ein Tierkreiszeichen zu speichern. In einer
 * {@link Hashtable} könnte ein Monat als Schlüssel auf eine Instanz verweisen;
 * jeder Monat gehört zu zwei Tierkreiszeichen; der erste Tag des zweiten
 * Zeichens wird hier gespeichert.
 *
 * @author Thomas Künneth
 */
public class Sign {

    private final int firstDayOfSecondSign, firstSign, secondSign;

    public Sign(int firstDayOfSecondSign, int firstSign, int secondSign) {
        super();
        this.firstDayOfSecondSign = firstDayOfSecondSign;
        this.firstSign = firstSign;
        this.secondSign = secondSign;
    }

    public int getFirstDayOfSecondSign() {
        return firstDayOfSecondSign;
    }

    public int getFirstSign() {
        return firstSign;
    }

    public int getSecondSign() {
        return secondSign;
    }
}
