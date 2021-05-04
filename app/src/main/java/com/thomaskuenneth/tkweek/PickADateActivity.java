/*
 * PickADateActivity.java
 *
 * TKWeek (c) Thomas Künneth 2010 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TextView;

import com.thomaskuenneth.tkweek.types.Event;

/**
 * Diese Activity stellt einen Dialog dar, mit dem ein Datum sowie eine
 * Beschreibung eingegeben werden kann.
 *
 * @author Thomas Künneth
 */
public class PickADateActivity extends Activity implements
        OnDateChangedListener {

    private static final String NEW_EVENT_EVENT = "newEventEvent";
    private static final int DIALOG_NEW_EVENT = 1;

    private DatePicker datePicker;
    private TextView newEventDescr;
    private CheckBox newEventAnnuallyRepeating;

    private Event newEventEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newEventEvent = null;
        if (savedInstanceState != null) {
            newEventEvent = savedInstanceState.getParcelable(NEW_EVENT_EVENT);
        }
        if (newEventEvent == null) {
            newEventEvent = new Event();
        }
        showDialog(DIALOG_NEW_EVENT);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (newEventEvent != null) {
            outState.putParcelable(NEW_EVENT_EVENT, newEventEvent);
        }
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        if (id == DIALOG_NEW_EVENT) {
            updateViewsFromEvent();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_NEW_EVENT) {
            View view = LayoutInflater.from(this).inflate(R.layout.new_event,
                    null);
            datePicker = view.findViewById(R.id.new_event_date);
            //OldTKWeekBaseActivity.configureDatePicker(datePicker);

            newEventDescr = view.findViewById(R.id.new_event_descr);
            newEventAnnuallyRepeating = view
                    .findViewById(R.id.new_event_annually_repeating);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.new_event)
                    .setPositiveButton(android.R.string.ok,
                            (dialog1, whichButton) -> {
                                newEventEvent.setYear(datePicker.getYear());
                                newEventEvent.setMonth(datePicker
                                        .getMonth());
                                newEventEvent.setDay(datePicker
                                        .getDayOfMonth());
                                newEventEvent.descr = newEventDescr
                                        .getText().toString();
                                newEventEvent.annuallyRepeating = newEventAnnuallyRepeating
                                        .isChecked();
                                Intent i = new Intent();
                                i.putExtra(Event.class.getName(),
                                        newEventEvent);
                                setResult(RESULT_OK, i);
                                finish();
                            })
                    .setNegativeButton(android.R.string.cancel,
                            (dialog12, whichButton) -> {
                                setResult(RESULT_CANCELED);
                                finish();
                            }).setView(view)
                    .setOnCancelListener(dialog13 -> {
                        setResult(RESULT_CANCELED);
                        finish();
                    }).create();
            updateViewsFromEvent();
            return dialog;
        }
        return super.onCreateDialog(id);
    }

    @Override
    public void onDateChanged(DatePicker picker, int year, int month, int day) {
        if (newEventEvent != null) {
            newEventEvent.setYear(year);
            newEventEvent.setMonth(month);
            newEventEvent.setDay(day);
        }
    }

    private void updateViewsFromEvent() {
        if (newEventEvent != null) {
            newEventDescr.setText(newEventEvent.descr);
            newEventAnnuallyRepeating.setChecked(newEventEvent
                    .annuallyRepeating);
            datePicker.init(newEventEvent.getYear(), newEventEvent.getMonth(),
                    newEventEvent.getDay(), this);
        }
    }
}
