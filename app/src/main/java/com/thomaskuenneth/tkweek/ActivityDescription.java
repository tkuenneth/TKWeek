/*
 * ActivityDescription.java
 *
 * TKWeek (c) Thomas Künneth 2009 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek;

import androidx.fragment.app.Fragment;

public class ActivityDescription {

    private final String text1;
    private final String text2;
    private final Class<? extends Fragment> fragment;

    public ActivityDescription(String text1, String text2,
                               Class<? extends Fragment> fragment) {
        this.text1 = text1;
        this.text2 = text2;
        this.fragment = fragment;
    }

    public String getText1() {
        return text1;
    }

    public String getText2() {
        return text2;
    }

    public Class<? extends Fragment> getFragment() {
        return fragment;
    }
}
