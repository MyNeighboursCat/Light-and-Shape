/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.mycompany.lightandshape.controller;

import android.graphics.Canvas;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.mycompany.lightandshape.BasicApplication;
import com.mycompany.lightandshape.R;
import com.mycompany.lightandshape.databinding.GameBinding;
import com.mycompany.lightandshape.databinding.GamePausedDialogBinding;
import com.mycompany.lightandshape.model.FrameGenerator;
import com.mycompany.lightandshape.model.Game;
import com.mycompany.lightandshape.model.HighScoreDatabaseHelper;
import com.mycompany.lightandshape.model.SoundEffect;
import com.mycompany.lightandshape.view.MainView;

import java.lang.ref.WeakReference;

/**
 * @author Colin Walters
 * @version 1.0, 24/05/2023
 */
@SuppressWarnings("Convert2Lambda")
public final class GameActivity extends AppCompatActivity {
    // don't call this GAME_BUNDLE to distinguish from Game class bundle name
    private static final String GAME = "GAME";
    private static final String GAME_ACTIVITY_BUNDLE = "GAME_ACTIVITY_BUNDLE_";
    private final String TAG = this.getClass().getSimpleName();
    private boolean allowEvents = false;
    private GameBinding binding = null;
    private LinearLayout gameLinearLayout = null;
    private LinearLayout statusLinearLayout = null;
    private AlertDialog alertDialog = null;
    private Button resumeButton = null;
    private Button abandonButton = null;
    private FrameGenerator mainFrameGenerator = null;
    private MainView mainView = null;
    private Game game1 = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            Bundle bundle = savedInstanceState.getBundle(GameActivity.GAME);
            if (bundle != null) {
                this.game1 = new Game(bundle);
            }

            // don't use 'counter' as variable name as it increase with each
            // line for some reason!
            int cnt = 0;

            this.allowEvents = savedInstanceState.getBoolean(
                    GameActivity.GAME_ACTIVITY_BUNDLE + cnt);
        }

        // make sure only the music stream volume is adjusted
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // could use the theme setting in manifest.xml at app or activity level
        // to use the full-screen and hide the title bar
        // but halo dark theme is only available for API 11+
        // hide phone status bar
        Window window = this.getWindow();
        if (window != null) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        // hide the title bar
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        this.binding = GameBinding.inflate(getLayoutInflater());
        this.setContentView(binding.getRoot());
        this.statusLinearLayout = this.binding.statusLinearLayout;
        this.gameLinearLayout = this.binding.gameLinearLayout;

        this.binding.pauseImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                GameActivity.this.doPausedDialog();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                    new OnBackInvokedCallback() {
                        @Override
                        public void onBackInvoked() {
                            GameActivity.this.doPausedDialog();
                        }
                    });
        } else {
            this.getOnBackPressedDispatcher().addCallback(this,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            GameActivity.this.doPausedDialog();
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // game has been restored in onCreate so don't set
        // game1 = null
        this.mainFrameGenerator = null;
        this.mainView = null;

        // cannot add MainView to XML code (through custom tab in XML editor)
        // because this uses the context parameter in the constructor
        // whereas MainView needs to receive a GameActivity parameter
        this.gameLinearLayout.removeAllViews();
        this.mainView = new MainView(this);
        this.gameLinearLayout.addView(this.mainView);

        if (this.game1 != null) {
            if (this.game1.isPaused() && (this.game1.getMissesLeft() > 0)) {
                this.doAlertDialog();
            }
        }
    }

    @Override
    protected void onPause() {
        if (this.game1 != null) {
            if (!this.game1.isPaused()) {
                this.pauseGame();
            }

            SoundEffect soundEffect1 = this.game1.getSoundEffect1();
            if (soundEffect1 != null) {
                soundEffect1.mediaPlayersRelease();
            }
        }

        this.doUnregisterSensors();

        if (this.mainFrameGenerator != null) {
            boolean retry = true;

            this.mainFrameGenerator.done();
            while (retry) {
                try {
                    this.mainFrameGenerator.join();
                    retry = false;
                } catch (final InterruptedException e) {
                    // do nothing
                }
            }

            this.mainFrameGenerator = null;
        }

        this.mainView = null;

        if (this.alertDialog != null) {
            this.alertDialog.dismiss();
            this.alertDialog = null;
        }

        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        if (this.game1 != null) {
            if (!this.game1.isPaused()) {
                this.pauseGame();
            }

            outState.putBundle(GameActivity.GAME, this.game1.writeToBundle());
        }

        // don't use 'counter' as variable name as it increase with each line
        // for some reason!
        int cnt = 0;
        outState.putBoolean(GameActivity.GAME_ACTIVITY_BUNDLE + cnt,
                this.allowEvents);

        // do this last like book\online code
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        this.releaseResources();
        super.onDestroy();
    }

    public void setMainFrameGenerator(final FrameGenerator mainFrameGenerator) {
        this.mainFrameGenerator = mainFrameGenerator;
    }

    public FrameGenerator getMainFrameGenerator() {
        return this.mainFrameGenerator;
    }

    public void startMainFrameGenerator(final Canvas canvas) {
        // Background thread.
        BasicApplication.APP_EXECUTORS.getBackgroundThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final int[] settings = new int[HighScoreDatabaseHelper.NUMBER_OF_SETTINGS];
                final WeakReference<GameActivity> gameActivityWeakReference =
                        new WeakReference<>(GameActivity.this);

                try {
                    // Get a reference to the activity if it is still there.
                    final GameActivity gameActivity = gameActivityWeakReference.get();
                    if (gameActivity == null || gameActivity.isFinishing()) {
                        return;
                    }

                    // Get UI values from database
                    HighScoreDatabaseHelper db = new HighScoreDatabaseHelper(
                            gameActivity, "", null, 0);

                    HighScoreDatabaseHelper.SettingsCursor dbSettingsCursor = db.getSettingsRecords(
                            null, true,
                            HighScoreDatabaseHelper.SETTINGS_NO_HIGH_SCORE_VALUE);
                    if (dbSettingsCursor.getCount() == HighScoreDatabaseHelper.NUMBER_OF_SETTINGS) {
                        String columnName;
                        for (dbSettingsCursor.moveToFirst(); !dbSettingsCursor
                                .isAfterLast(); dbSettingsCursor.moveToNext()) {
                            columnName = dbSettingsCursor.getColumnSettingsName();

                            // NOTE: can't use string == string
                            if (columnName.compareTo(HighScoreDatabaseHelper.LIGHT_NO) == 0) {
                                settings[0] = dbSettingsCursor.getColumnSettingsValue();
                            } else if (columnName
                                    .compareTo(HighScoreDatabaseHelper.ANGLE) == 0) {
                                settings[1] = dbSettingsCursor.getColumnSettingsValue();
                            } else if (columnName
                                    .compareTo(HighScoreDatabaseHelper.TARGET_SHAPES_NO) == 0) {
                                settings[2] = dbSettingsCursor.getColumnSettingsValue();
                            } else if (columnName
                                    .compareTo(HighScoreDatabaseHelper.FIND_SHAPES_NO) == 0) {
                                settings[3] = dbSettingsCursor.getColumnSettingsValue();
                            } else if (columnName
                                    .compareTo(HighScoreDatabaseHelper.MISSES_NO) == 0) {
                                settings[4] = dbSettingsCursor.getColumnSettingsValue();
                            } else if (columnName
                                    .compareTo(HighScoreDatabaseHelper.SPEED) == 0) {
                                settings[5] = dbSettingsCursor.getColumnSettingsValue();
                            } else if (!(columnName
                                    .compareTo(HighScoreDatabaseHelper.SOUND) == 0)) {
                                Log.e(gameActivity.TAG,
                                        "startMainFrameGenerator() error 1");
                                throw new RuntimeException();
                            }
                        }
                    } else {
                        Log.e(gameActivity.TAG, "startMainFrameGenerator() error 2");
                        throw new RuntimeException();
                    }

                    // close cursor
                    dbSettingsCursor.close();
                    // close database
                    db.close();
                } finally {
                    // Main thread.
                    BasicApplication.APP_EXECUTORS.getMainThreadExecutor().execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    // Get a reference to the activity if it is still there.
                                    final GameActivity gameActivity =
                                            gameActivityWeakReference.get();
                                    if (gameActivity == null || gameActivity.isFinishing()) {
                                        return;
                                    }

                                    // if activity hasn't been restored
                                    if (gameActivity.game1 == null) {
                                        gameActivity.game1 = new Game(gameActivity, canvas,
                                                settings);
                                    } else {
                                        gameActivity.game1.resetGame(gameActivity, canvas,
                                                settings);
                                    }

                                    gameActivity.mainFrameGenerator = new FrameGenerator(
                                            gameActivity, gameActivity.game1);
                                    gameActivity.mainFrameGenerator.start();

                                    if (gameActivity.game1 != null) {
                                        if (!gameActivity.game1.isPaused() &&
                                                gameActivity.game1.getMissesLeft() > 0) {
                                            gameActivity.doRegisterSensors();
                                        }
                                    }
                                }
                            });
                }
            }
        });
    }

    public MainView getMainView() {
        return this.mainView;
    }

    public GameBinding getBinding() {
        return binding;
    }

    private void doPausedDialog() {
        if ((this.game1 == null) || !this.allowEvents) {
            return;
        }

        if (!this.game1.isPaused()) {
            this.pauseGame();
        }

        this.doAlertDialog();
    }

    private void pauseGame() {
        if ((this.mainFrameGenerator == null) || (this.game1 == null)) {
            return;
        }

        this.game1.setPaused();
    }

    private void doAlertDialog() {
        this.doUnregisterSensors();

        // clear previous dialog
        // e.g. power saving mode on, then internet connection off
        if (this.alertDialog != null) {
            this.alertDialog.dismiss();
            this.alertDialog = null;
        }

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        final GamePausedDialogBinding gamePausedDialogBinding =
                GamePausedDialogBinding.inflate(getLayoutInflater());
        final View layout = gamePausedDialogBinding.getRoot();
        alertDialogBuilder.setView(layout);

        this.resumeButton = gamePausedDialogBinding.resumeButton;
        this.abandonButton = gamePausedDialogBinding.abandonButton;

        this.resumeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (GameActivity.this.alertDialog != null) {
                    GameActivity.this.alertDialog.dismiss();
                    GameActivity.this.alertDialog = null;
                }

                GameActivity.this.doRegisterSensors();
                GameActivity.this.game1.setPaused();
            }
        });

        this.abandonButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (GameActivity.this.alertDialog != null) {
                    GameActivity.this.alertDialog.dismiss();
                    GameActivity.this.alertDialog = null;
                }

                GameActivity.this.finish();
            }
        });

        alertDialogBuilder.setTitle(this.getString(R.string.game_paused));

        // back button is disabled
        alertDialogBuilder.setCancelable(false);

        this.alertDialog = alertDialogBuilder.create();
        this.alertDialog.setOwnerActivity(this);
        this.alertDialog.show();
    }

    private void doRegisterSensors() {
        GameActivity.this.mainView
                .setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(final View v, final MotionEvent event) {
                        if (event.getAction() != MotionEvent.ACTION_DOWN) {
                            return false;
                        }

                        if (!GameActivity.this.game1.isPaused()) {
                            GameActivity.this.game1.addTouchPoint((int) event.getX(),
                                    (int) event.getY());
                        }

                        v.performClick();

                        return true;
                    }
                });

        this.setEnableButtons(true);
    }

    private void doUnregisterSensors() {
        GameActivity.this.mainView.setOnTouchListener(null);
        this.setEnableButtons(false);
    }

    private void setEnableButtons(final boolean enableButtons) {
        int child_maximum;

        if (!enableButtons) {
            this.allowEvents = false;
        }

        // status layout
        child_maximum = this.statusLinearLayout.getChildCount();
        for (int i = 0; i < child_maximum; i++) {
            this.statusLinearLayout.getChildAt(i).setEnabled(enableButtons);
        }

        this.gameLinearLayout.setEnabled(enableButtons);

        if (enableButtons) {
            this.allowEvents = true;
        }
    }

    private void releaseResources() {
        if (this.mainFrameGenerator != null) {
            boolean retry = true;

            this.mainFrameGenerator.done();
            while (retry) {
                try {
                    this.mainFrameGenerator.join();
                    retry = false;
                } catch (final InterruptedException e) {
                    // do nothing
                }
            }

            this.mainFrameGenerator = null;
        }

        if (this.game1 != null) {
            this.game1.releaseResources();
            this.game1 = null;
        }

        if (this.mainView != null) {
            // let MainView.surfaceDestroyed() call MainView.releaseResources()
            // called when this.gameLinearLayout.removeAllViews() called
            this.mainView = null;
        }

        if (this.gameLinearLayout != null) {
            this.gameLinearLayout.removeAllViews();
            this.gameLinearLayout = null;
        }

        if (this.alertDialog != null) {
            this.alertDialog.dismiss();
            this.alertDialog = null;
        }

        this.binding = null;
        this.statusLinearLayout = null;
        this.resumeButton = null;
        this.abandonButton = null;
    }
}
