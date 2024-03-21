/*
 * CalendarIterator.java
 *
 * Copyright 2010 - 2020 Thomas Künneth
 *           2021 MATHEMA GmbH
 *           2022 - 2024 Thomas Künneth
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.thomaskuenneth.tkweek.util;

import java.util.Calendar;

public class CalendarIterator {

    public static Calendar iterateUntil(Calendar cal,
                                        CalendarCondition untilCondition, int field, int value) {
        while (true) {
            try {
                if (untilCondition.matches(cal)) {
                    break;
                }
            } catch (RequiredCalendarConditionException ignored) {
            }
            cal.add(field, value);
        }
        return cal;
    }
}
