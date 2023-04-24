/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.mycompany.lightandshape.model;

import android.content.Intent;

import com.mycompany.lightandshape.R;
import com.mycompany.lightandshape.controller.GameActivity;
import com.mycompany.lightandshape.controller.HighScoreActivity;

/**
 * @author Colin Walters
 * @version 1.0, 24/05/2023
 */
// use extends Thread and not implements Runnable because
// MainView.surfaceDestroyed
// needs to use Thread.join()
public final class FrameGenerator extends Thread {
    // volatile variable on two threads
    // ensures threads have access to the same value of the variable
    private volatile boolean done = false;
    private volatile GameActivity gameActivity1;
    private volatile Game game1;

    public FrameGenerator(final GameActivity gameActivity, final Game game) {
        this.gameActivity1 = gameActivity;
        this.game1 = game;
    }

    public void done() {
        this.done = true;
    }

    private void releaseResources() {
        this.gameActivity1 = null;
        this.game1 = null;
    }

    // runs on a different thread
    @Override
    public void run() {
        boolean notStoppedByBack = false;
        int missesLeft;
        long longScore = 0L;
        String stringScore = "";

        while (!this.done) {
            this.game1.doDraw();

            // Game over
            missesLeft = this.game1.getMissesLeft();
            longScore = this.game1.getLongScore();
            stringScore = this.game1.getStringScore();

            if ((missesLeft <= 0) && (longScore >= 0L)
                    && (stringScore.length() > 0)) {
                this.done();
                notStoppedByBack = true;
            }
        }

        // Pressing the back key means onDestroy() of the gameActivity is called
        // setting
        // done = true and the following code would still execute if using the
        // done if the if
        // statement
        // if (done) {
        if (notStoppedByBack) {
            final Intent intent = new Intent(this.gameActivity1,
                    HighScoreActivity.class);
            intent.putExtra(this.gameActivity1
                    .getString(R.string.game_over_intent_parameter), true);
            intent.putExtra(this.gameActivity1
                    .getString(R.string.long_score_intent_parameter), longScore);
            intent.putExtra(this.gameActivity1
                            .getString(R.string.string_score_intent_parameter),
                    stringScore);
            this.gameActivity1.startActivity(intent);
            this.gameActivity1.finish();
        }

        this.releaseResources();
    }
}