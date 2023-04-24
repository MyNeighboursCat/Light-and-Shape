/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.mycompany.lightandshape.controller;

import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.mycompany.lightandshape.BasicApplication;
import com.mycompany.lightandshape.R;
import com.mycompany.lightandshape.databinding.ListDisplayBinding;
import com.mycompany.lightandshape.databinding.SettingsDialogBinding;
import com.mycompany.lightandshape.model.HighScoreDatabaseHelper;
import com.mycompany.lightandshape.model.HighScoreDatabaseHelper.HighScoreCursor;
import com.mycompany.lightandshape.model.HighScoreDatabaseHelper.SettingsCursor;

import java.lang.ref.WeakReference;

/**
 * @author Colin Walters
 * @version 1.0, 24/05/2023
 */
@SuppressWarnings("Convert2Lambda")
public final class SettingsActivity extends AppCompatActivity {
    public static final int MAX_LIGHTS = 4;
    public static final int DEFAULT_LIGHTS = 2;
    public static final int DEFAULT_ANGLES = 2;
    public static final int MAX_TARGET_SHAPES = 20;
    public static final int DEFAULT_TARGET_SHAPES = 10;
    public static final int DEFAULT_FIND_SHAPES = 2;
    public static final int DEFAULT_MISSES = 5;
    public static final int MAX_SPEED = 6;
    public static final int DEFAULT_SPEED = 6;
    public static final int DEFAULT_SOUND = 5;
    public static final int MAX_TOUCH_POINTS = 5;
    private static final int MIN_LIGHTS = 1;
    private static final int MIN_ANGLES = 1;
    private static final int MAX_ANGLES = 4;
    // need to leave at least MAX_FIND_SHAPES for valid target shapes
    // there is a maximum of 27 valid target shapes in res
    // so 27 - 5 = 22 maximum target shapes but use 20
    // when a target shape is selected correctly, the old shape is deleted and
    // a new target shape is added straight away so 22 is the maximum potential
    // value of MAX_TARGET_SHAPES
    // the number of misses is restricted to MAX_FIND_SHAPES to ensure that
    // the above is true: otherwise there is an unlimited number of misses at
    // one time so no shapes would be left to create new ones since all shapes
    // in the find and target shapes are unique even if they are misses
    private static final int MIN_TARGET_SHAPES = 5;
    public static final int MAX_FIND_SHAPES = SettingsActivity.MIN_TARGET_SHAPES;
    private static final int MIN_FIND_SHAPES = 1;
    private static final int MIN_MISSES = 1;
    private static final int MAX_MISSES = 10;
    private static final int MIN_SPEED = 1;
    private static final int MIN_SOUND = 0;
    private static final int MAX_SOUND = 10;
    private static final int DIALOG_LIGHTS_ID = 0;
    private static final int DIALOG_ANGLES_ID = 1;
    private static final int DIALOG_TARGET_SHAPES_ID = 2;
    private static final int DIALOG_FIND_SHAPES_ID = 3;
    private static final int DIALOG_MISSES_ID = 4;
    private static final int DIALOG_SPEED_ID = 5;
    private static final int DIALOG_SOUND_ID = 6;
    private volatile int highScoreIndex = -1;
    private volatile int currentLightsNumber = SettingsActivity.DEFAULT_LIGHTS;
    private volatile int currentAnglesNumber = SettingsActivity.DEFAULT_ANGLES;
    private volatile int currentTargetShapesNumber = SettingsActivity.DEFAULT_TARGET_SHAPES;
    private volatile int currentFindShapesNumber = SettingsActivity.DEFAULT_FIND_SHAPES;
    private volatile int currentMissesNumber = SettingsActivity.DEFAULT_MISSES;
    private volatile int currentSpeedNumber = SettingsActivity.DEFAULT_SPEED;
    private volatile int currentSoundNumber = SettingsActivity.DEFAULT_SOUND;
    private static final String SETTINGS_BUNDLE = "SETTINGS_BUNDLE_";
    private final String TAG = this.getClass().getSimpleName();
    private int savedItem = -1;
    private int savedProgress = -1;
    private int savedValue = -1;
    private String[] settingsNameArray = null;
    private ListView mList = null;
    private AlertDialog alertDialog = null;
    private SeekBar settingsSeekBar = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // find extras - values passed in
        final Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            this.highScoreIndex = extras.getInt(this.getString(R.string.index_intent_parameter));
        }

        // get saved bundle values
        if (savedInstanceState != null) {
            // restoring - cannot do in onRestoreInstanceState() because that is
            // called after onCreate()
            // so dialog would not appear

            // don't use 'counter' as variable name as it increase with each
            // line for some reason!
            int cnt = 0;
            this.savedItem = savedInstanceState
                    .getInt(SettingsActivity.SETTINGS_BUNDLE
                            + cnt++);
            this.savedProgress = savedInstanceState
                    .getInt(SettingsActivity.SETTINGS_BUNDLE
                            + cnt++);
            this.savedValue = savedInstanceState
                    .getInt(SettingsActivity.SETTINGS_BUNDLE
                            + cnt);
        }

        this.settingsNameArray = new String[]{
                HighScoreDatabaseHelper.LIGHT_NO + ": ",
                HighScoreDatabaseHelper.ANGLE + ": ",
                HighScoreDatabaseHelper.TARGET_SHAPES_NO + ": ",
                HighScoreDatabaseHelper.FIND_SHAPES_NO + ": ",
                HighScoreDatabaseHelper.MISSES_NO + ": ",
                HighScoreDatabaseHelper.SPEED + ": ",
                HighScoreDatabaseHelper.SOUND + ": "};

        // Make sure only the music stream volume is adjusted
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

        this.mList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent,
                                    final View arg1, final int position, final long id) {
                // position = The position of the view in the adapter
                // id = The row id of the item that was clicked
                // These are effectively the same
                // Can't use switch() on long so use position
                // Game over and not settings
                if (SettingsActivity.this.highScoreIndex < 0) {
                    SettingsActivity.this.savedItem = position;
                    SettingsActivity.this
                            .doAlertDialog(SettingsActivity.this.savedItem);
                }
            }
        });

        // Game over and not settings
        if (this.highScoreIndex >= 0) {
            // If disabled, scrolling is disabled as well
            // Can't find a way of disabling without switching off scrolling
            // Put a condition in getListView().setOnItemClickListener above
            // getListView().setEnabled(false);

            this.doDisplay(false);
        } else {
            boolean displayDialog = false;

            if (savedInstanceState != null) {
                displayDialog = true;
            }

            this.doDisplay(displayDialog);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.doDisplay(true);
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
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        // don't use 'counter' as variable name as it increase with each line
        // for some reason!
        int cnt = 0;

        if ((this.alertDialog != null) && (this.settingsSeekBar != null)
                && (this.savedItem > -1)) {
            outState.putInt(
                    SettingsActivity.SETTINGS_BUNDLE + cnt++,
                    this.savedItem);
            // NOTE: need to set saved variable for onRestart()
            this.savedProgress = this.settingsSeekBar.getProgress();
            outState.putInt(
                    SettingsActivity.SETTINGS_BUNDLE + cnt++,
                    this.savedProgress);

            this.savedValue = this.settingsSeekBar.getProgress();
            switch (this.savedItem) {
                case DIALOG_LIGHTS_ID:
                    this.savedValue += SettingsActivity.MIN_LIGHTS;
                    break;
                case DIALOG_ANGLES_ID:
                    this.savedValue += SettingsActivity.MIN_ANGLES;
                    break;
                case DIALOG_TARGET_SHAPES_ID:
                    this.savedValue += SettingsActivity.MIN_TARGET_SHAPES;
                    break;
                case DIALOG_FIND_SHAPES_ID:
                    this.savedValue += SettingsActivity.MIN_FIND_SHAPES;
                    break;
                case DIALOG_MISSES_ID:
                    this.savedValue += SettingsActivity.MIN_MISSES;
                    break;
                case DIALOG_SPEED_ID:
                    this.savedValue += SettingsActivity.MIN_SPEED;
                    break;
                case DIALOG_SOUND_ID:
                    //noinspection ConstantConditions
                    this.savedValue += SettingsActivity.MIN_SOUND;
                    break;
                default:
                    Log.e(this.TAG, "onSaveInstanceState() error 1");
                    throw new RuntimeException();
            }
            outState.putInt(
                    SettingsActivity.SETTINGS_BUNDLE + cnt,
                    this.savedValue);

        } else {
            outState.putInt(
                    SettingsActivity.SETTINGS_BUNDLE + cnt++,
                    -1);
            outState.putInt(
                    SettingsActivity.SETTINGS_BUNDLE + cnt++,
                    -1);
            outState.putInt(
                    SettingsActivity.SETTINGS_BUNDLE + cnt,
                    -1);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        if (this.alertDialog != null) {
            this.alertDialog.dismiss();
            this.alertDialog = null;
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        this.releaseResources();
        super.onDestroy();
    }

    private void doDisplay(final boolean displayDialog) {
        BasicApplication.APP_EXECUTORS.getBackgroundThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final WeakReference<SettingsActivity> settingsActivityWeakReference =
                        new WeakReference<>(SettingsActivity.this);

                try {
                    // Get a reference to the activity if it is still there.
                    final SettingsActivity settingsActivity = settingsActivityWeakReference.get();
                    if (settingsActivity == null || settingsActivity.isFinishing()) {
                        return;
                    }

                    long highScoreID;
                    HighScoreDatabaseHelper db = settingsActivity.openDatabase();

                    if (settingsActivity.highScoreIndex >= 0) {
                        // Game over
                        HighScoreCursor dbHighScoreCursor = db.findScores();
                        dbHighScoreCursor
                                .moveToPosition(settingsActivity.highScoreIndex);
                        highScoreID = dbHighScoreCursor.getColumnHighScoreId();
                        settingsActivity.getCursorSettings(db, false, highScoreID);

                        dbHighScoreCursor.close();
                    } else {
                        // Settings
                        highScoreID = HighScoreDatabaseHelper.SETTINGS_NO_HIGH_SCORE_VALUE;
                        settingsActivity.getCursorSettings(db, true, highScoreID);
                    }

                    db.close();
                } finally {
                    // Main thread.
                    BasicApplication.APP_EXECUTORS.getMainThreadExecutor().execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    // Get a reference to the activity if it is still there.
                                    final SettingsActivity settingsActivity =
                                            settingsActivityWeakReference.get();
                                    if (settingsActivity == null ||
                                            settingsActivity.isFinishing()) {
                                        return;
                                    }

                                    final String[] stringArray1 = new String[]{
                                            settingsActivity.settingsNameArray[0]
                                                    + settingsActivity.currentLightsNumber,
                                            settingsActivity.settingsNameArray[1]
                                                    + settingsActivity.currentAnglesNumber,
                                            settingsActivity.settingsNameArray[2]
                                                    + settingsActivity.currentTargetShapesNumber,
                                            settingsActivity.settingsNameArray[3]
                                                    + settingsActivity.currentFindShapesNumber,
                                            settingsActivity.settingsNameArray[4]
                                                    + settingsActivity.currentMissesNumber,
                                            settingsActivity.settingsNameArray[5]
                                                    + settingsActivity.currentSpeedNumber,
                                            settingsActivity.settingsNameArray[6]
                                                    + settingsActivity.currentSoundNumber};

                                    final ArrayAdapter<String> adapter1 = new ArrayAdapter<>(
                                            settingsActivity, android.R.layout.simple_list_item_1,
                                            stringArray1);
                                    settingsActivity.mList.setAdapter(adapter1);

                                    if (displayDialog) {
                                        settingsActivity.doDialog();
                                    }
                                }
                            });
                }
            }
        });
    }

    private void getCursorSettings(final HighScoreDatabaseHelper db,
                                   final boolean settingsRecords, final long highScoreID) {
        SettingsCursor dbSettingsCursor = db.getSettingsRecords(null,
                settingsRecords, highScoreID);

        if (dbSettingsCursor.getCount() == HighScoreDatabaseHelper.NUMBER_OF_SETTINGS) {
            String columnName;

            for (dbSettingsCursor.moveToFirst(); !dbSettingsCursor
                    .isAfterLast(); dbSettingsCursor.moveToNext()) {
                columnName = dbSettingsCursor.getColumnSettingsName();

                // NOTE: can't use string == string
                if (columnName.compareTo(HighScoreDatabaseHelper.LIGHT_NO) == 0) {
                    this.currentLightsNumber = dbSettingsCursor
                            .getColumnSettingsValue();
                } else if (columnName.compareTo(HighScoreDatabaseHelper.ANGLE) == 0) {
                    this.currentAnglesNumber = dbSettingsCursor
                            .getColumnSettingsValue();
                } else if (columnName
                        .compareTo(HighScoreDatabaseHelper.TARGET_SHAPES_NO) == 0) {
                    this.currentTargetShapesNumber = dbSettingsCursor
                            .getColumnSettingsValue();
                } else if (columnName
                        .compareTo(HighScoreDatabaseHelper.FIND_SHAPES_NO) == 0) {
                    this.currentFindShapesNumber = dbSettingsCursor
                            .getColumnSettingsValue();
                } else if (columnName
                        .compareTo(HighScoreDatabaseHelper.MISSES_NO) == 0) {
                    this.currentMissesNumber = dbSettingsCursor
                            .getColumnSettingsValue();
                } else if (columnName.compareTo(HighScoreDatabaseHelper.SPEED) == 0) {
                    this.currentSpeedNumber = dbSettingsCursor
                            .getColumnSettingsValue();
                } else if (columnName.compareTo(HighScoreDatabaseHelper.SOUND) == 0) {
                    this.currentSoundNumber = dbSettingsCursor
                            .getColumnSettingsValue();
                } else {
                    Log.e(this.TAG, "getCursorSettings() error 1");
                    throw new RuntimeException();
                }
            }
        } else {
            Log.e(this.TAG, "getCursorSettings() error 2");
            throw new RuntimeException();
        }

        dbSettingsCursor.close();
    }

    private void doDialog() {
        if ((this.savedItem > -1) && (this.savedProgress > -1)
                && (this.savedValue > -1)) {
            this.doAlertDialog(this.savedItem);
        } else {
            this.clearDialogVariables();
        }
    }

    private void doAlertDialog(final int position) {
        int maximum, progress;
        String title;

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                SettingsActivity.this);

        final SettingsDialogBinding settingsDialogBinding =
                SettingsDialogBinding.inflate(getLayoutInflater());
        final View layout = settingsDialogBinding.getRoot();
        alertDialogBuilder.setView(layout);

        switch (position) {
            case DIALOG_LIGHTS_ID:
                title = this.settingsNameArray[0];
                if ((this.savedProgress > -1) && (this.savedValue > -1)) {
                    title += Integer.toString(this.savedValue);
                    progress = this.savedProgress;
                } else {
                    title += Integer.toString(this.currentLightsNumber);
                    progress = this.currentLightsNumber
                            - SettingsActivity.MIN_LIGHTS;
                }
                maximum = SettingsActivity.MAX_LIGHTS - SettingsActivity.MIN_LIGHTS;
                break;
            case DIALOG_ANGLES_ID:
                title = this.settingsNameArray[1];
                if ((this.savedProgress > -1) && (this.savedValue > -1)) {
                    title += Integer.toString(this.savedValue);
                    progress = this.savedProgress;
                } else {
                    title += Integer.toString(this.currentAnglesNumber);
                    progress = this.currentAnglesNumber
                            - SettingsActivity.MIN_ANGLES;
                }
                maximum = SettingsActivity.MAX_ANGLES - SettingsActivity.MIN_ANGLES;
                break;
            case DIALOG_TARGET_SHAPES_ID:
                title = this.settingsNameArray[2];
                if ((this.savedProgress > -1) && (this.savedValue > -1)) {
                    title += Integer.toString(this.savedValue);
                    progress = this.savedProgress;
                } else {
                    title += Integer.toString(this.currentTargetShapesNumber);
                    progress = this.currentTargetShapesNumber
                            - SettingsActivity.MIN_TARGET_SHAPES;
                }
                maximum = SettingsActivity.MAX_TARGET_SHAPES
                        - SettingsActivity.MIN_TARGET_SHAPES;
                break;
            case DIALOG_FIND_SHAPES_ID:
                title = this.settingsNameArray[3];
                if ((this.savedProgress > -1) && (this.savedValue > -1)) {
                    title += Integer.toString(this.savedValue);
                    progress = this.savedProgress;
                } else {
                    title += Integer.toString(this.currentFindShapesNumber);
                    progress = this.currentFindShapesNumber
                            - SettingsActivity.MIN_FIND_SHAPES;
                }
                maximum = SettingsActivity.MAX_FIND_SHAPES
                        - SettingsActivity.MIN_FIND_SHAPES;
                break;
            case DIALOG_MISSES_ID:
                title = this.settingsNameArray[4];
                if ((this.savedProgress > -1) && (this.savedValue > -1)) {
                    title += Integer.toString(this.savedValue);
                    progress = this.savedProgress;
                } else {
                    title += Integer.toString(this.currentMissesNumber);
                    progress = this.currentMissesNumber
                            - SettingsActivity.MIN_MISSES;
                }
                maximum = SettingsActivity.MAX_MISSES - SettingsActivity.MIN_MISSES;
                break;
            case DIALOG_SPEED_ID:
                title = this.settingsNameArray[5];
                if ((this.savedProgress > -1) && (this.savedValue > -1)) {
                    title += Integer.toString(this.savedValue);
                    progress = this.savedProgress;
                } else {
                    title += Integer.toString(this.currentSpeedNumber);
                    progress = this.currentSpeedNumber - SettingsActivity.MIN_SPEED;
                }
                maximum = SettingsActivity.MAX_SPEED - SettingsActivity.MIN_SPEED;
                break;
            case DIALOG_SOUND_ID:
                title = this.settingsNameArray[6];
                if ((this.savedProgress > -1) && (this.savedValue > -1)) {
                    title += Integer.toString(this.savedValue);
                    progress = this.savedProgress;
                } else {
                    title += Integer.toString(this.currentSoundNumber);
                    progress = this.currentSoundNumber - SettingsActivity.MIN_SOUND;
                }
                maximum = SettingsActivity.MAX_SOUND - SettingsActivity.MIN_SOUND;
                break;
            default:
                Log.e(this.TAG, "doAlertDialog() error 1");
                throw new RuntimeException();
        }

        this.settingsSeekBar = settingsDialogBinding.settingsSeekBar;
        this.settingsSeekBar.setMax(maximum);
        this.settingsSeekBar.setProgress(progress);
        this.settingsSeekBar
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(final SeekBar arg0,
                                                  final int arg1, final boolean arg2) {
                        String title;

                        switch (position) {
                            case DIALOG_LIGHTS_ID:
                                title = SettingsActivity.this.settingsNameArray[0]
                                        + (arg0.getProgress()
                                        + SettingsActivity.MIN_LIGHTS);
                                break;
                            case DIALOG_ANGLES_ID:
                                title = SettingsActivity.this.settingsNameArray[1]
                                        + (arg0.getProgress()
                                        + SettingsActivity.MIN_ANGLES);
                                break;
                            case DIALOG_TARGET_SHAPES_ID:
                                title = SettingsActivity.this.settingsNameArray[2]
                                        + (arg0.getProgress()
                                        + SettingsActivity.MIN_TARGET_SHAPES);
                                break;
                            case DIALOG_FIND_SHAPES_ID:
                                title = SettingsActivity.this.settingsNameArray[3]
                                        + (arg0.getProgress()
                                        + SettingsActivity.MIN_FIND_SHAPES);
                                break;
                            case DIALOG_MISSES_ID:
                                title = SettingsActivity.this.settingsNameArray[4]
                                        + (arg0.getProgress()
                                        + SettingsActivity.MIN_MISSES);
                                break;
                            case DIALOG_SPEED_ID:
                                title = SettingsActivity.this.settingsNameArray[5]
                                        + (arg0.getProgress()
                                        + SettingsActivity.MIN_SPEED);
                                break;
                            case DIALOG_SOUND_ID:
                                title = SettingsActivity.this.settingsNameArray[6]
                                        + (arg0.getProgress()
                                        + SettingsActivity.MIN_SOUND);
                                break;
                            default:
                                Log.e(SettingsActivity.this.TAG,
                                        "doAlertDialog() error 2");
                                throw new RuntimeException();
                        }

                        if (SettingsActivity.this.alertDialog != null) {
                            SettingsActivity.this.alertDialog.setTitle(title);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(final SeekBar arg0) {
                    }

                    @Override
                    public void onStopTrackingTouch(final SeekBar arg0) {
                    }
                });

        alertDialogBuilder
                // back button is disabled
                .setCancelable(false)
                .setTitle(title)
                .setPositiveButton(this.getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int id) {
                                if (SettingsActivity.this.settingsSeekBar == null) {
                                    return;
                                }

                                if (dialog != null) {
                                    dialog.dismiss();
                                    // Cannot clear variable as it needs to be final dialog = null;
                                }

                                String columnName;
                                int value;

                                switch (position) {
                                    case DIALOG_LIGHTS_ID:
                                        SettingsActivity.this.currentLightsNumber =
                                                SettingsActivity.this.settingsSeekBar
                                                        .getProgress()
                                                        + SettingsActivity.MIN_LIGHTS;
                                        columnName = HighScoreDatabaseHelper.LIGHT_NO;
                                        value = SettingsActivity.this.currentLightsNumber;
                                        break;
                                    case DIALOG_ANGLES_ID:
                                        SettingsActivity.this.currentAnglesNumber =
                                                SettingsActivity.this.settingsSeekBar
                                                        .getProgress()
                                                        + SettingsActivity.MIN_ANGLES;
                                        columnName = HighScoreDatabaseHelper.ANGLE;
                                        value = SettingsActivity.this.currentAnglesNumber;
                                        break;
                                    case DIALOG_TARGET_SHAPES_ID:
                                        SettingsActivity.this.currentTargetShapesNumber =
                                                SettingsActivity.this.settingsSeekBar
                                                        .getProgress()
                                                        + SettingsActivity.MIN_TARGET_SHAPES;
                                        columnName = HighScoreDatabaseHelper.TARGET_SHAPES_NO;
                                        value = SettingsActivity.this.currentTargetShapesNumber;
                                        break;
                                    case DIALOG_FIND_SHAPES_ID:
                                        SettingsActivity.this.currentFindShapesNumber =
                                                SettingsActivity.this.settingsSeekBar
                                                        .getProgress()
                                                        + SettingsActivity.MIN_FIND_SHAPES;
                                        columnName = HighScoreDatabaseHelper.FIND_SHAPES_NO;
                                        value = SettingsActivity.this.currentFindShapesNumber;
                                        break;
                                    case DIALOG_MISSES_ID:
                                        SettingsActivity.this.currentMissesNumber =
                                                SettingsActivity.this.settingsSeekBar
                                                        .getProgress()
                                                        + SettingsActivity.MIN_MISSES;
                                        columnName = HighScoreDatabaseHelper.MISSES_NO;
                                        value = SettingsActivity.this.currentMissesNumber;
                                        break;
                                    case DIALOG_SPEED_ID:
                                        SettingsActivity.this.currentSpeedNumber =
                                                SettingsActivity.this.settingsSeekBar
                                                        .getProgress()
                                                        + SettingsActivity.MIN_SPEED;
                                        columnName = HighScoreDatabaseHelper.SPEED;
                                        value = SettingsActivity.this.currentSpeedNumber;
                                        break;
                                    case DIALOG_SOUND_ID:
                                        SettingsActivity.this.currentSoundNumber =
                                                SettingsActivity.this.settingsSeekBar
                                                        .getProgress()
                                                        + SettingsActivity.MIN_SOUND;
                                        columnName = HighScoreDatabaseHelper.SOUND;
                                        value = SettingsActivity.this.currentSoundNumber;
                                        break;
                                    default:
                                        Log.e(SettingsActivity.this.TAG,
                                                "doAlertDialog() error 3");
                                        throw new RuntimeException();
                                }

                                SettingsActivity.this.asyncSettingsDatabase(
                                        new String[] {columnName, Integer.toString(value)});
                            }
                        })
                .setNegativeButton(this.getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                final int id) {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                                SettingsActivity.this.clearDialogVariables();

                                final Toast toast = Toast.makeText(
                                        SettingsActivity.this
                                                .getApplicationContext(),
                                        R.string.settings_dialog_negative_message,
                                        Toast.LENGTH_SHORT);

                                toast.show();
                            }
                        });

        this.alertDialog = alertDialogBuilder.create();
        this.alertDialog.setOwnerActivity(SettingsActivity.this);
        // Don't use this because the system calls it if the dialog is displayed
        // and
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
        this.settingsSeekBar = null;
        this.savedItem = -1;
        this.savedProgress = -1;
        this.savedValue = -1;
    }

    private void asyncSettingsDatabase (final String[] string1) {
        BasicApplication.APP_EXECUTORS.getBackgroundThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final WeakReference<SettingsActivity> settingsActivityWeakReference =
                        new WeakReference<>(SettingsActivity.this);

                try {
                    // Get a reference to the activity if it is still there.
                    final SettingsActivity settingsActivity = settingsActivityWeakReference.get();
                    if (settingsActivity == null || settingsActivity.isFinishing()) {
                        return;
                    }

                    HighScoreDatabaseHelper db = settingsActivity.openDatabase();
                    db.editSettings(string1[0], Integer.parseInt(string1[1]));

                    db.close();
                } finally {
                    // Main thread.
                    BasicApplication.APP_EXECUTORS.getMainThreadExecutor().execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    // Get a reference to the activity if it is still there.
                                    final SettingsActivity settingsActivity =
                                            settingsActivityWeakReference.get();
                                    if (settingsActivity == null ||
                                            settingsActivity.isFinishing()) {
                                        return;
                                    }

                                    settingsActivity.clearDialogVariables();

                                    settingsActivity.doDisplay(false);

                                    final Toast toast = Toast.makeText(
                                            settingsActivity.getApplicationContext(),
                                            R.string.settings_dialog_positive_message,
                                            Toast.LENGTH_SHORT);
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

    private void releaseResources() {
        this.alertDialog = null;
        this.settingsNameArray = null;
        this.settingsSeekBar = null;
        this.mList = null;
    }
}
