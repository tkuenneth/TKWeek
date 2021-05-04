/*
 * EnterNoteActivity.java
 *
 * TKWeek (c) Thomas Künneth 2015
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Mit dieser Activity können Notizen eingegeben werden.
 *
 * @author Thomas Kuenneth
 */
public class EnterNoteActivity extends Activity {

    public static final String EXTRA_NOTES = "notes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_notes);
        final EditText notes = (EditText) findViewById(R.id.notes);
        final Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        final Button ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(EXTRA_NOTES, notes.getText().toString());
                setResult(RESULT_OK, data);
                finish();
            }
        });
        Intent intent = getIntent();
        if (intent != null) {
            String note = intent.getStringExtra(EXTRA_NOTES);
            if (note != null) {
                notes.setText(note);
            }
        }
    }
}
