/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.mycompany.lightandshape.controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.mycompany.lightandshape.BasicApplication;
import com.mycompany.lightandshape.R;
import com.mycompany.lightandshape.databinding.MainBinding;
import com.mycompany.lightandshape.model.HighScoreDatabaseHelper;
import com.mycompany.lightandshape.model.SoundEffect;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * @author Colin Walters
 * @version 1.0, 24/05/2023
 */
@SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"})
public final class MainActivity extends AppCompatActivity {
    private static final String MAIN_BUNDLE = "MAIN_BUNDLE_";
    private final String TAG = this.getClass().getSimpleName();
    private boolean showResetDataDialog = false;
    private boolean allowEvents = true;
    private boolean startingGame = false;
    private MainBinding binding = null;
    private TableLayout tableLayout1 = null;
    private AlertDialog resetDataAlertDialog = null;
    private SoundEffect soundEffect1 = null;

    final ActivityResultLauncher<Intent> mStartForResult = this.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    /*if (result.getResultCode() == Activity.RESULT_OK) {
                        final Intent intent = result.getData();
                        if (intent != null) {
                            final Bundle extras = intent.getExtras();
                            if (extras != null) {

                            }
                        }
                    }*/
                    MainActivity.this.startingGame = false;
                }
            });

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get saved bundle values
        if (savedInstanceState != null) {
            // restoring - cannot do in onRestoreInstanceState() because that is
            // called after onCreate()
            // so dialog would not appear

            int cnt = 0;
            final boolean[] booleanArray1 = savedInstanceState
                    .getBooleanArray(MainActivity.MAIN_BUNDLE + cnt);

            if (booleanArray1 != null) {
                this.showResetDataDialog = booleanArray1[0];
                this.allowEvents = booleanArray1[1];
                this.startingGame = booleanArray1[2];
            }
        }

        // Make sure only the music stream volume is adjusted
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        this.binding = MainBinding.inflate(getLayoutInflater());
        this.setContentView(binding.getRoot());
        this.setSupportActionBar(binding.toolbar);

        this.tableLayout1 = this.binding.tableLayout1;

        this.doButtonOnClickListeners();

        final ArrayList<SoundEffect.SoundType> mediaPlayerTypes = new ArrayList<>();
        mediaPlayerTypes.add(SoundEffect.SoundType.START);
        this.soundEffect1 = new SoundEffect(this.getApplicationContext(),
                mediaPlayerTypes, SoundEffect.SoundType.START);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (MainActivity.this.showResetDataDialog) {
            MainActivity.this.doResetDataAlertDialog();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!this.startingGame) {
            // to stop double tap
            this.setButtonsStatus(true);
        }
    }

    @Override
    protected void onStop() {
        this.showResetDataDialog = false;
        if (this.resetDataAlertDialog != null) {
            this.resetDataAlertDialog.dismiss();
            this.resetDataAlertDialog = null;

            this.showResetDataDialog = true;
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

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        final boolean[] booleanArray1 = {this.showResetDataDialog, this.allowEvents,
                this.startingGame};

        int cnt = 0;
        outState.putBooleanArray(MainActivity.MAIN_BUNDLE + cnt, booleanArray1);

        super.onSaveInstanceState(outState);
    }

    private void doButtonOnClickListeners() {
        this.binding.playImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                MainActivity.this.onClickButtonHandler(v);
            }
        });
        this.binding.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                MainActivity.this.onClickButtonHandler(v);
            }
        });

        this.binding.highScoreImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                MainActivity.this.onClickButtonHandler(v);
            }
        });
        this.binding.highScoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                MainActivity.this.onClickButtonHandler(v);
            }
        });

        this.binding.settingsImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                MainActivity.this.onClickButtonHandler(v);
            }
        });
        this.binding.settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                MainActivity.this.onClickButtonHandler(v);
            }
        });

        // Disable this.
        /*this.binding.rateImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                MainActivity.this.onClickButtonHandler(v);
            }
        });
        this.binding.rateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                MainActivity.this.onClickButtonHandler(v);
            }
        });*/

        this.binding.helpImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                MainActivity.this.onClickButtonHandler(v);
            }
        });
        this.binding.helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                MainActivity.this.onClickButtonHandler(v);
            }
        });

        this.binding.aboutImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                MainActivity.this.onClickButtonHandler(v);
            }
        });
        this.binding.aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                MainActivity.this.onClickButtonHandler(v);
            }
        });

        this.binding.resetDataImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                MainActivity.this.onClickButtonHandler(v);
            }
        });
        this.binding.resetDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                MainActivity.this.onClickButtonHandler(v);
            }
        });
    }

    private void onClickButtonHandler(final View v) {
        // allowEvents is needed because disabling buttons takes time
        // it is therefore possible to trigger the same event more than once
        // in a short space of time without this variable check
        if (!this.allowEvents) {
            return;
        }

        this.allowEvents = false;

        Intent intent;

        // set buttons to disabled when first click happens - here and in onClick()
        // the back button doesn't appear to be a problem but do in MainActivity
        // because code is needed for other buttons
        // if another activity is called, onResume() sets buttons to enabled
        // if an activity is not called, set the buttons to enabled after the
        // appropriate actions are done
        // dialogs: button clicks appear to be OK if a check for dialog != null
        // is done
        this.setButtonsStatus(false);

        if (v == binding.playImageButton || v == binding.playButton) {
            this.startingGame = true;
            this.doGame();
        } else if (v == binding.highScoreImageButton || v == binding.highScoreButton) {
            intent = new Intent(this, HighScoreActivity.class);
            // game over off
            intent.putExtra(
                    this.getString(R.string.game_over_intent_parameter), false);
            // no score
            intent.putExtra(
                    this.getString(R.string.long_score_intent_parameter),
                    (long) 0);
            intent.putExtra(
                    this.getString(R.string.string_score_intent_parameter), "");
            this.startActivity(intent);
        } else if (v == binding.settingsImageButton || v == binding.settingsButton) {
            intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(this.getString(R.string.index_intent_parameter), -1);
            this.startActivity(intent);
        /*} else if (v == binding.rateImageButton || v == binding.rateButton) {
            // Disable this.
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri
                    .parse("market://details?id=com.mycompany.lightandshape"));
            this.startActivity(intent);*/
        } else if (v == binding.helpImageButton || v == binding.helpButton) {
            intent = new Intent(this, HelpActivity.class);
            this.startActivity(intent);
        } else if (v == binding.aboutImageButton || v == binding.aboutButton) {
            intent = new Intent(this, AboutActivity.class);
            this.startActivity(intent);
        } else if (v == binding.resetDataImageButton || v == binding.resetDataButton) {
            this.doResetDataAlertDialog();
        } else {
            Log.e(this.TAG, "onClickButtonHandler error 1");
            throw new RuntimeException();
        }
    }

    private void doGame() {
        Intent intent = new Intent(this, GameActivity.class);
        mStartForResult.launch(intent);
    }

    private void doResetDataAlertDialog() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        alertDialogBuilder.setTitle(this.getString(R.string.reset_data));
        alertDialogBuilder.setMessage(this
                .getString(R.string.reset_data_warning));

        alertDialogBuilder.setCancelable(false); // back button is disabled

        alertDialogBuilder.setPositiveButton(
                this.getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int id) {
                        // Background thread.
                        BasicApplication.APP_EXECUTORS.getBackgroundThreadExecutor().execute(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        final WeakReference<MainActivity> mainActivityWeakReference
                                                = new WeakReference<>(MainActivity.this);

                                        try {
                                            // Get a reference to the activity if it is still there.
                                            final MainActivity mainActivity =
                                                    mainActivityWeakReference.get();
                                            if (mainActivity == null ||
                                                    mainActivity.isFinishing()) {
                                                return;
                                            }

                                            HighScoreDatabaseHelper db1 =
                                                    new HighScoreDatabaseHelper(mainActivity,
                                                            "", null, 0);
                                            SQLiteDatabase db2 = db1.getReadableDatabase();
                                            HighScoreDatabaseHelper.removeData(db2);

                                            db1.close();
                                            db2.close();
                                        } finally {
                                            // Main thread.
                                            BasicApplication.APP_EXECUTORS
                                                    .getMainThreadExecutor().execute(
                                                    new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            // Get a reference to the activity if it
                                                            // is still there.
                                                            final MainActivity mainActivity =
                                                                    mainActivityWeakReference.get();
                                                            if (mainActivity == null ||
                                                                    mainActivity.isFinishing()) {
                                                                return;
                                                            }

                                                            final Toast toast =
                                                                    Toast.makeText(mainActivity
                                                                                    .getApplicationContext(),
                                                                    R.string.data_has_been_reset,
                                                                            Toast.LENGTH_SHORT);
                                                            toast.show();

                                                            if (MainActivity.this
                                                                    .resetDataAlertDialog != null) {
                                                                MainActivity.this
                                                                        .resetDataAlertDialog
                                                                        .dismiss();
                                                                MainActivity.this
                                                                        .resetDataAlertDialog =
                                                                        null;
                                                            }

                                                            mainActivity.setButtonsStatus(true);
                                                        }
                                                    });
                                        }
                                    }
                                });
                    }
                }).setNegativeButton(this.getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        final Toast toast = Toast.makeText(
                                MainActivity.this.getApplicationContext(),
                                R.string.data_has_not_been_reset,
                                Toast.LENGTH_SHORT);
                        toast.show();

                        if (MainActivity.this.resetDataAlertDialog != null) {
                            MainActivity.this.resetDataAlertDialog.dismiss();
                            MainActivity.this.resetDataAlertDialog = null;
                        }

                        // to stop double tap
                        MainActivity.this.setButtonsStatus(true);
                    }
                });

        this.resetDataAlertDialog = alertDialogBuilder.create();
        this.resetDataAlertDialog.setOwnerActivity(MainActivity.this);
        this.resetDataAlertDialog.show();
    }

    private void setButtonsStatus(final boolean enableButtons) {
        if (this.startingGame && enableButtons) {
            return;
        }

        if (!enableButtons) {
            this.allowEvents = false;
        }

        int child_maximum;

        if (this.tableLayout1 == null) {
            return;
        }

        // image buttons and buttons in main layout
        child_maximum = this.tableLayout1.getChildCount();
        for (int i = 0; i < child_maximum; i++) {
            final TableRow tableRow1 = (TableRow) this.tableLayout1
                    .getChildAt(i);
            final int child_maximum2 = tableRow1.getChildCount();
            for (int j = 0; j < child_maximum2; j++) {
                tableRow1.getChildAt(j).setEnabled(enableButtons);
            }
        }

        if (enableButtons) {
            this.allowEvents = true;
        }
    }

    private void releaseResources() {
        this.binding = null;
        this.tableLayout1 = null;
        this.resetDataAlertDialog = null;
        this.soundEffect1 = null;
    }
}