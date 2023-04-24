/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.mycompany.lightandshape.controller;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.mycompany.lightandshape.BasicApplication;
import com.mycompany.lightandshape.R;
import com.mycompany.lightandshape.databinding.HighScoreDialogBinding;
import com.mycompany.lightandshape.databinding.ListDisplayBinding;
import com.mycompany.lightandshape.model.HighScoreDatabaseHelper;
import com.mycompany.lightandshape.model.HighScoreDatabaseHelper.HighScoreCursor;
import com.mycompany.lightandshape.model.SoundEffect;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author Colin Walters
 * @version 1.0, 24/05/2023
 */
@SuppressWarnings("Convert2Lambda")
public final class HighScoreActivity extends AppCompatActivity {
    private static final String HIGH_SCORE_BUNDLE = "HIGH_SCORE_BUNDLE_";
    private volatile boolean gameOver = false;
    private volatile long longScore = 0L;
    private volatile String stringScore = "";
    private String stringInput = "";
    private ListView mList = null;
    private AlertDialog alertDialog = null;
    private volatile EditText inputEditText = null;
    private Button okButton = null;
    private Button cancelButton = null;
    private SoundEffect soundEffect1 = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Find extras - values passed in
        final Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            // Only set gameOver value if not recreating this activity.  This prevents dialog box
            // being shown a second time again after entering initials.
            if (savedInstanceState == null) {
                this.gameOver = extras.getBoolean(
                        this.getString(R.string.game_over_intent_parameter));
            }
            this.longScore = extras.getLong(this.getString(R.string.long_score_intent_parameter));
            this.stringScore = extras.getString(
                    this.getString(R.string.string_score_intent_parameter));
        }

        // Get saved bundle values
        if (savedInstanceState != null) {
            // Don't use 'counter' as variable name as it increases with each
            // line for some reason!
            int cnt = 0;

            this.gameOver = savedInstanceState.getBoolean(HighScoreActivity.HIGH_SCORE_BUNDLE +
                    cnt++);

            if (savedInstanceState.getString(HighScoreActivity.HIGH_SCORE_BUNDLE + cnt)
                    != null) {
                this.stringInput = savedInstanceState.getString(
                        HighScoreActivity.HIGH_SCORE_BUNDLE + cnt);
            }
        }

        // Make sure only the music stream volume is adjusted.
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        final ListDisplayBinding listDisplayBinding =
                ListDisplayBinding.inflate(getLayoutInflater());
        this.setContentView(listDisplayBinding.getRoot());
        this.setSupportActionBar(listDisplayBinding.toolbar);

        final ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        this.mList = listDisplayBinding.list;

        this.doDisplay(this.gameOver, savedInstanceState == null);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.doDisplay(this.gameOver, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Respond to the action bar's Up/Home button.
        if (item.getItemId() == android.R.id.home) {
            // Return to existing instance of the calling activity rather than create a new one.
            // This keeps the existing state of the calling activity.
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        // don't use 'counter' as variable name as it increase with each line
        // for some reason!
        int cnt = 0;

        outState.putBoolean(HighScoreActivity.HIGH_SCORE_BUNDLE + cnt++,
                this.gameOver);

        if ((this.alertDialog != null) && (this.inputEditText != null)) {
            this.stringInput = this.inputEditText.getText().toString();
            outState.putString(
                    HighScoreActivity.HIGH_SCORE_BUNDLE
                            + cnt, this.stringInput);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        if (this.alertDialog != null) {
            this.alertDialog.dismiss();
            this.alertDialog = null;
        }

        if (this.soundEffect1 != null) {
            this.soundEffect1.mediaPlayersRelease();
            this.soundEffect1 = null;
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        this.releaseResources();
        super.onDestroy();
    }

    private void doDisplay(final boolean displayDialog, final boolean doSound) {
        BasicApplication.APP_EXECUTORS.getBackgroundThreadExecutor().execute(new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                final ArrayList<String> arrayList1 = new ArrayList<>();
                final WeakReference<HighScoreActivity> highScoreActivityWeakReference =
                        new WeakReference<>(HighScoreActivity.this);

                try {
                    // Get a reference to the activity if it is still there.
                    final HighScoreActivity highScoreActivity =
                            highScoreActivityWeakReference.get();
                    if (highScoreActivity == null || highScoreActivity.isFinishing()) {
                        return;
                    }

                    HighScoreDatabaseHelper db = highScoreActivity.openDatabase();

                    String str1;
                    final StringBuilder str2 = new StringBuilder();
                    final StringBuilder str3 = new StringBuilder();

                    // header
                    str1 = String.format("%s  %s          %s",
                            highScoreActivity.getString(R.string.rank),
                            highScoreActivity.getString(R.string.time),
                            highScoreActivity.getString(R.string.initials));
                    arrayList1.add(str1);

                    HighScoreCursor dbHighScoreCursor = db.findScores();
                    int row = 1;
                    for (dbHighScoreCursor.moveToFirst(); !dbHighScoreCursor
                            .isAfterLast(); dbHighScoreCursor.moveToNext()) {

                        str1 = String.format("%02d.", row++);
                        str1 += "     ";

                        str2.setLength(0);
                        str2.append(dbHighScoreCursor.getColumnTextScore());

                        str3.setLength(0);
                        final int maximumCount = 9 - str2.length();
                        for (int i = 0; i < maximumCount; i++) {
                            if (i == 3) {
                                str3.append(':');
                            } else {
                                str3.append('0');
                            }
                        }
                        str1 += str3.toString();
                        str1 += str2.toString();
                        str1 += "  ";

                        str1 += dbHighScoreCursor.getColumnInitials();

                        arrayList1.add(str1);
                    }

                    dbHighScoreCursor.close();

                    db.close();
                } finally {
                    // Main thread.
                    BasicApplication.APP_EXECUTORS.getMainThreadExecutor().execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    // Get a reference to the activity if it is still there.
                                    final HighScoreActivity highScoreActivity =
                                            highScoreActivityWeakReference.get();
                                    if (highScoreActivity == null ||
                                            highScoreActivity.isFinishing()) {
                                        return;
                                    }

                                    final ArrayAdapter<String> adapter1 = new ArrayAdapter<>(
                                            highScoreActivity,
                                            android.R.layout.simple_list_item_1, arrayList1);
                                    highScoreActivity.mList.setAdapter(adapter1);

                                    if (displayDialog) {
                                        highScoreActivity.doDialog(doSound);
                                    } else {
                                        highScoreActivity.mList.setOnItemClickListener(
                                                new OnItemClickListener() {
                                                    @Override
                                                    public void onItemClick(
                                                            final AdapterView<?> parent,
                                                            final View arg1, final int position,
                                                            final long id) {
                                                        // position = The position of the view in
                                                        // the adapter
                                                        // id = The row id of the item that was
                                                        // clicked
                                                        // These are effectively the same
                                                        // Can't use switch() on long so use
                                                        // position

                                                        // Do nothing if title pressed
                                                        if (position > 0) {
                                                            // Need to subtract one for title item
                                                            highScoreActivity
                                                                    .showHighScoreRecordSetting(
                                                                            position - 1);
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                }
            }
        });
    }

    private void doDialog(final boolean doSound) {
        BasicApplication.APP_EXECUTORS.getBackgroundThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                boolean isHighScore = false;
                final WeakReference<HighScoreActivity> highScoreActivityWeakReference =
                        new WeakReference<>(HighScoreActivity.this);

                try {
                    // Get a reference to the activity if it is still there.
                    final HighScoreActivity highScoreActivity =
                            highScoreActivityWeakReference.get();
                    if (highScoreActivity == null || highScoreActivity.isFinishing()) {
                        return;
                    }

                    HighScoreDatabaseHelper db = highScoreActivity.openDatabase();
                    isHighScore = db.isInHighScores(highScoreActivity.longScore);

                    db.close();
                } finally {
                    // Main thread.
                    final boolean finalIsHighScore = isHighScore;
                    BasicApplication.APP_EXECUTORS.getMainThreadExecutor().execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    // Get a reference to the activity if it is still there.
                                    final HighScoreActivity highScoreActivity =
                                            highScoreActivityWeakReference.get();
                                    if (highScoreActivity == null ||
                                            highScoreActivity.isFinishing()) {
                                        return;
                                    }

                                    highScoreActivity.doAlertDialog(finalIsHighScore);
                                    if (doSound) {
                                        final ArrayList<SoundEffect.SoundType> mediaPlayerTypes =
                                                new ArrayList<>();
                                        mediaPlayerTypes.add(SoundEffect.SoundType.GAME_OVER);
                                        highScoreActivity.soundEffect1 = new SoundEffect(
                                                highScoreActivity.getApplicationContext(),
                                                mediaPlayerTypes, SoundEffect.SoundType.GAME_OVER);
                                    }
                                }
                            });
                }
            }
        });
    }

    private void doAlertDialog(final boolean isHighScore) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                HighScoreActivity.this);

        if (isHighScore) {
            final HighScoreDialogBinding highScoreDialogBinding =
                    HighScoreDialogBinding.inflate(getLayoutInflater());
            final View layout = highScoreDialogBinding.getRoot();
            alertDialogBuilder.setView(layout);

            this.inputEditText = highScoreDialogBinding.hsidEditText;
            this.okButton = highScoreDialogBinding.okButton;
            this.cancelButton = highScoreDialogBinding.cancelButton;

            alertDialogBuilder
                    .setMessage(this.getString(R.string.your_score_is)
                            + " "
                            + this.stringScore
                            + "\n"
                            + this.getString(R.string
                            .high_score_input_dialog_positive_message));

            // need one filter for the length max and one for alphas only
            // need length max as adding a filter seems to override max
            // length xml property
            final InputFilter filter1 = new InputFilter.LengthFilter(3);
            final InputFilter filter2 = new InputFilter() {
                @Override
                public CharSequence filter(final CharSequence source,
                                           final int start, final int end, final Spanned
                                                   dest,
                                           final int dstart, final int dend) {
                    for (int i = start; i < end; i++) {
                        if (!Character.isLetter(source.charAt(i))) {
                            return "";
                        }
                    }

                    return null;
                }
            };
            this.inputEditText.setFilters(new InputFilter[]{filter1,
                    filter2});

            if (this.stringInput.length() > 0) {
                this.inputEditText.setText(this.stringInput);
            }

            this.inputEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(final Editable s) {
                }

                @Override
                public void beforeTextChanged(final CharSequence s,
                                              final int start, final int count, final int
                                                      after) {
                }

                @Override
                public void onTextChanged(final CharSequence s,
                                          final int start, final int before, final int
                                                  count) {
                    HighScoreActivity.this.okButton.setEnabled(s.length() == 3);
                }
            });

            this.okButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    HighScoreActivity.this.asyncHighScoreDatabase();
                }
            });

            if (this.stringInput.length() != 3) {
                this.okButton.setEnabled(false);
            }

            this.cancelButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final Toast toast = Toast.makeText(
                            HighScoreActivity.this.getApplicationContext(),
                            R.string.high_score_not_updated,
                            Toast.LENGTH_SHORT);
                    toast.show();

                    HighScoreActivity.this.finish();
                }
            });

            // Can't find a way of disabling OK button if initials are less
            // than three in length
            // so use OK and cancel buttons in XML layout
        } else {
            alertDialogBuilder
                    .setMessage(
                            this.getString(R.string.your_score_is)
                                    + " "
                                    + this.stringScore
                                    + "\n"
                                    + this.getString(R.string
                                    .high_score_input_dialog_negative_message))
                    .setPositiveButton(this.getString(android.R.string.ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(
                                        final DialogInterface dialog,
                                        final int id) {
                                    HighScoreActivity.this.finish();
                                }
                            });
        }

        alertDialogBuilder.setTitle(this.getString(R.string.game_over));
        alertDialogBuilder.setCancelable(false); // back button is disabled

        this.alertDialog = alertDialogBuilder.create();
        this.alertDialog.setOwnerActivity(HighScoreActivity.this);
        // Don't use this because the system calls it if the dialog is
        // displayed and
        // the menu button is pressed.
        // This happens before onSaveInstanceState().
        /*
         * alertDialog.setOnDismissListener(new
         * DialogInterface.OnDismissListener() { public void
         * onDismiss(DialogInterface dialog) { clearDialogVariables(); } });
         */
        this.alertDialog.show();
    }

    private void clearDialogVariables() {
        this.alertDialog = null;
        this.inputEditText = null;
        this.stringInput = "";
    }

    private void asyncHighScoreDatabase() {
        BasicApplication.APP_EXECUTORS.getBackgroundThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final WeakReference<HighScoreActivity> highScoreActivityWeakReference =
                        new WeakReference<>(HighScoreActivity.this);

                try {
                    // Get a reference to the activity if it is still there.
                    final HighScoreActivity highScoreActivity =
                            highScoreActivityWeakReference.get();
                    if (highScoreActivity == null || highScoreActivity.isFinishing()) {
                        return;
                    }

                    HighScoreDatabaseHelper db = highScoreActivity.openDatabase();
                    db.addScore(highScoreActivity.longScore, new String[] {
                            HighScoreActivity.this.stringScore,
                            HighScoreActivity.this.inputEditText.getText().toString()
                                    .toUpperCase()});

                    db.close();
                } finally {
                    // Main thread.
                    BasicApplication.APP_EXECUTORS.getMainThreadExecutor().execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    // Get a reference to the activity if it is still there.
                                    final HighScoreActivity highScoreActivity =
                                            highScoreActivityWeakReference.get();
                                    if (highScoreActivity == null ||
                                            highScoreActivity.isFinishing()) {
                                        return;
                                    }

                                    if (highScoreActivity.alertDialog != null) {
                                        highScoreActivity.alertDialog.dismiss();
                                        highScoreActivity.alertDialog = null;
                                    }
                                    highScoreActivity.clearDialogVariables();

                                    // need to stop dialog reappearing after looking
                                    // at settings
                                    // in high score table, which triggers
                                    // onRestart()
                                    highScoreActivity.gameOver = false;

                                    highScoreActivity.doDisplay(false, false);

                                    final Toast toast = Toast.makeText(highScoreActivity
                                                    .getApplicationContext(),
                                            R.string.high_score_updated, Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            });
                }
            }
        });
    }

    private HighScoreDatabaseHelper openDatabase() {
        return new HighScoreDatabaseHelper(this, "", null, 0);
    }

    private void showHighScoreRecordSetting(final int position) {
        final Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(this.getString(R.string.index_intent_parameter),
                position);
        this.startActivity(intent);
    }

    private void releaseResources() {
        this.alertDialog = null;
        this.soundEffect1 = null;

        this.inputEditText = null;
        this.okButton = null;
        this.cancelButton = null;

        this.stringScore = null;
        this.stringInput = null;

        this.mList = null;
    }
}
