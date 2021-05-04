/*
 * RequiredCalendarConditionException.java
 *
 * TKWeek (c) Thomas Künneth 2010 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.util;

/**
 * Dieses {@link Throwable} signalisiert, dass eine {@link CalendarCondition}
 * nicht erfüllt ist, obwohl sie als <em>required</em> gekennzeichnet wurde.
 *
 * @author Thomas Künneth
 * @see CalendarCondition
 */
public class RequiredCalendarConditionException extends Throwable {

    private static final long serialVersionUID = -8068585161022723848L;

}
