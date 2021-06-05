/*
 * ContactsUtils.java
 *
 * Copyright 2011 - 2020 Thomas Künneth
 * Copyright 2021 MATHEMA GmbH
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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.thomaskuenneth.tkweek.R;
import com.thomaskuenneth.tkweek.types.Anniversary;
import com.thomaskuenneth.tkweek.types.Birthday;
import com.thomaskuenneth.tkweek.types.Event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class reads birthdays and anniversaries from Google Contacts.
 *
 * @author Thomas Künneth
 */
public class ContactsUtils {

    public static List<Event> queryContacts(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        List<Event> result = new ArrayList<>();
        // get IDs and names of all contacts with a display name
        String[] mainQueryProjection = {ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME};
        Cursor mainQueryCursor = null;
        if (TKWeekUtils.canReadContacts(context)) {
            mainQueryCursor = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI, mainQueryProjection,
                    null, null, null);
        }
        if (mainQueryCursor != null) {
            while (mainQueryCursor.moveToNext()) {
                String contactId = mainQueryCursor.getString(mainQueryCursor
                        .getColumnIndex(ContactsContract.Contacts._ID));
                String displayName = mainQueryCursor.getString(mainQueryCursor
                        .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String[] dataQueryProjection = new String[]{
                        ContactsContract.CommonDataKinds.Event.TYPE,
                        ContactsContract.CommonDataKinds.Event.START_DATE,
                        ContactsContract.CommonDataKinds.Event.LABEL};
                String dataQuerySelection = ContactsContract.Data.CONTACT_ID
                        + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                String[] dataQuerySelectionArgs = new String[]{contactId,
                        ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE};
                Cursor dataQueryCursor = contentResolver.query(
                        ContactsContract.Data.CONTENT_URI, dataQueryProjection,
                        dataQuerySelection, dataQuerySelectionArgs, null);
                if (dataQueryCursor != null) {
                    while (dataQueryCursor.moveToNext()) {
                        int type = dataQueryCursor.getInt(0);
                        String label = dataQueryCursor.getString(2);
                        String strDate = dataQueryCursor.getString(1);
                        if (ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY == type) {
                            Date birthday = DateUtilities.getDateFromString1(strDate);
                            if (birthday != null) {
                                result.add(new Birthday(DateUtilities.getCalendar(birthday),
                                        displayName, contactId));
                            }
                        } else {
                            Date anniversary = DateUtilities
                                    .getDateFromString1(strDate);
                            if (anniversary != null) {
                                Anniversary item = new Anniversary(
                                        DateUtilities.getCalendar(anniversary),
                                        displayName, contactId);
                                if ((label == null) || (label.length() < 1)) {
                                    if (ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY == type) {
                                        label = context.getString(R.string.anniversary);
                                    } else if (ContactsContract.CommonDataKinds.Event.TYPE_CUSTOM == type) {
                                        label = context.getString(R.string.custom);
                                    } else {
                                        label = context.getString(R.string.other);
                                    }
                                }
                                item.calendarName = context.getString(R.string.contacts);
                                item.setText(label);
                                result.add(item);
                            }
                        }
                    }
                    dataQueryCursor.close();
                }
            }
            mainQueryCursor.close();
        }
        return result;
    }
}
