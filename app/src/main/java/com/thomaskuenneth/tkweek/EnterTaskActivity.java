/*
 * EnterTaskActivity.java
 *
 * Copyright 2013 - 2020 Thomas Künneth
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
package com.thomaskuenneth.tkweek;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.thomaskuenneth.tkweek.util.DateUtilities;
import com.thomaskuenneth.tkweek.util.TKWeekUtils;
import com.thomaskuenneth.tkweek.types.TaskList;
import com.thomaskuenneth.tkweek.util.TasksUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Diese Activity wird für das Erfassen von Aufgaben verwendet.
 *
 * @author Thomas Künneth
 */
public class EnterTaskActivity extends Activity {

    private static final int RQ_GET_ACCOUNTS = 1;

    private Spinner spinnerLists;

    private final Handler handlerGetTaskLists = new GetTaskListsHandler(this);
    private final Handler handlerOK = new OKHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (TKWeekUtils.canGetAccounts(this)) {
            setupUI();
        } else {
            requestPermissionGetAccounts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if ((requestCode == RQ_GET_ACCOUNTS) && (grantResults.length > 0)) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupUI();
            } else {
                setContentView(R.layout.simple);
                TextView textview = findViewById(R.id.textview);
                Button button = findViewById(R.id.button);
                if (TKWeekUtils.shouldShowRequestPermissionRationale(EnterTaskActivity.this, Manifest.permission.GET_ACCOUNTS)) {
                    textview.setText(R.string.needed_right_create_task);
                    button.setText(R.string.grant);
                    button.setOnClickListener(v -> requestPermissionGetAccounts());
                } else {
                    textview.setText(R.string.missing_right_create_task);
                    button.setText(R.string.close);
                    button.setOnClickListener(v -> finish());
                }
            }
        }
    }

    private void setupUI() {
        setContentView(R.layout.enter_task);
        final DatePicker datepickerDueDate = findViewById(R.id.enter_task_duedate);
        spinnerLists = findViewById(R.id.enter_task_list);
        final EditText title = findViewById(R.id.enter_task_title);
        final EditText notes = findViewById(R.id.enter_task_notes);
        final CheckBox checkboxDueDate = findViewById(R.id.enter_task_has_duedate);
        checkboxDueDate.setOnCheckedChangeListener((buttonView, isChecked) -> datepickerDueDate.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        //TKWeekBaseActivity.configureDatePicker(datepickerDueDate);
        final Button ok = findViewById(R.id.enter_task_ok);
        ok.setOnClickListener(v -> {
            if (TKWeekUtils.canGetAccounts(EnterTaskActivity.this)) {
                TasksUtils u = TasksUtils.getInstance(handlerOK, EnterTaskActivity.this);
                if (u != null) {
                    TaskList l = (TaskList) spinnerLists.getSelectedItem();
                    if (l != null) {
                        Date dueDate = null;
                        if (checkboxDueDate.isChecked()) {
                            Calendar cal = DateUtilities
                                    .getCalendar(datepickerDueDate.getYear(),
                                            datepickerDueDate.getMonth(),
                                            datepickerDueDate.getDayOfMonth());
                            DateUtilities.setTimeRelatedFields(cal, 0, 0);
                            DateUtilities.setTimeZoneUTC(cal);
                            dueDate = cal.getTime();
                        }
                        u.addTask(l.id, title.getText().toString(),
                                notes.getText().toString(),
                                dueDate);
                    }
                }
            }
        });
        ok.setEnabled(false);
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                ok.setEnabled(s.length() > 0);
            }
        });
        final Button cancel = findViewById(R.id.enter_task_cancel);
        cancel.setOnClickListener(v -> finish());
        Intent i = getIntent();
        if (i != null) {
            title.setText(i.getStringExtra(android.content.Intent.EXTRA_TEXT));
        }
        readTaskLists();
    }

    private void readTaskLists() {
        TasksUtils u = TasksUtils.getInstance(handlerGetTaskLists, this);
        if (u != null) {
            if (!u.getTaskListsAsync()) {
                // TODO: Fehlerbehandlung, Meldung an Benutzer, ...
                finish();
            }
        }
    }

    private void requestPermissionGetAccounts() {
        TKWeekUtils.requestPermissions(this,
                new String[]{Manifest.permission.GET_ACCOUNTS}, RQ_GET_ACCOUNTS);
    }

    private static class GetTaskListsHandler extends Handler {

        final EnterTaskActivity a;

        GetTaskListsHandler(EnterTaskActivity a) {
            this.a = a;
        }

        @Override
        public void handleMessage(final Message msg) {
            final List<TaskList> lists = (List) msg.obj;
            if ((lists != null) && (lists.size() > 0)) {
                final Runnable r = () -> {
                    ArrayAdapter<TaskList> adapter = new ArrayAdapter<>(
                            a,
                            android.R.layout.simple_spinner_item,
                            lists);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    a.spinnerLists.setAdapter(adapter);
                };
                a.runOnUiThread(r);
            }
        }
    }

    private static class OKHandler extends Handler {

        final EnterTaskActivity a;

        OKHandler(EnterTaskActivity a) {
            this.a = a;
        }

        @Override
        public void handleMessage(final Message msg) {
            final boolean success = TasksUtils.isStatusCode200(msg);
            final Runnable r = new Runnable() {

                @Override
                public void run() {
                    if (success) {
                        Toast.makeText(a,
                                R.string.toast_create_task_success,
                                Toast.LENGTH_SHORT).show();
                        a.setResult(RESULT_OK);
                        a.finish();
                    }
                }

            };
            a.runOnUiThread(r);
        }
    }
}
