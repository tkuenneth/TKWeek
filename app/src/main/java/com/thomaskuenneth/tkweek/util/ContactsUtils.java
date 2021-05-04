/*
 * ContactsUtils.java
 * 
 * TKWeek (c) Thomas Künneth 2011 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
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
        // get IDs and names of all visible contacts
        String[] mainQueryProjection = {ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.IN_VISIBLE_GROUP};
        String mainQuerySelection = ContactsContract.Contacts.IN_VISIBLE_GROUP
                + "!= ?";
        String[] mainQuerySelectionArgs = new String[]{"0"};
        Cursor mainQueryCursor = null;
        if (TKWeekUtils.canReadContacts(context)) {
            mainQueryCursor = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI, mainQueryProjection,
                    mainQuerySelection, mainQuerySelectionArgs, null);
        }
        if (mainQueryCursor != null) {
            while (mainQueryCursor.moveToNext()) {
                String contactId = mainQueryCursor.getString(mainQueryCursor
                        .getColumnIndex(ContactsContract.Contacts._ID));
                String displayName = mainQueryCursor.getString(mainQueryCursor
                        .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                int inVisibleGroup = mainQueryCursor
                        .getInt(mainQueryCursor
                                .getColumnIndex(ContactsContract.Contacts.IN_VISIBLE_GROUP));
                // workaround for a bug in Honeycomb
                if (inVisibleGroup == 0) {
                    continue;
                }
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
