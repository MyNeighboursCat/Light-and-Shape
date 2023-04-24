/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.mycompany.lightandshape.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.mycompany.lightandshape.controller.GameActivity;
import com.mycompany.lightandshape.model.FrameGenerator;

/**
 * @author Colin Walters
 * @version 1.0, 24/05/2023
 */
public final class MainView extends SurfaceView implements SurfaceHolder.Callback {
    private GameActivity gameActivity1 = null;
    private volatile SurfaceHolder surfaceHolder1 = null;
    private volatile Bitmap cache = null;
    private volatile Paint paint1 = new Paint();

    public MainView(final Context context) {
        super(context);
    }

    public MainView(final GameActivity context) {
        super(context);

        this.gameActivity1 = context;

        // Minimum dimensions
        this.setMinimumWidth(100);
        this.setMinimumHeight(100);

        this.surfaceHolder1 = this.getHolder();
        this.surfaceHolder1.addCallback(this);
    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, final int format,
                               final int width, final int height) {
        // No need to code for surface dimension change because the screen has fixed orientation.
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        if (this.cache == null) {
            this.cache = Bitmap.createBitmap(this.getMeasuredWidth(), this.getMeasuredHeight(),
                    Bitmap.Config.ARGB_8888);
        }

        this.gameActivity1.startMainFrameGenerator(new Canvas(this.cache));
    }

    @Override
    public void surfaceDestroyed(final SurfaceHolder holder) {
        // Activity already destroyed.
        // Thread stopped by GameActivity.onDestroy().
        if (this.gameActivity1 == null) {
            return;
        }

        // Stop the background thread and wait for it to finish so it is not possible to draw to
        // surface after it has been destroyed.
        final FrameGenerator mainFrameGenerator = this.gameActivity1.getMainFrameGenerator();
        if (mainFrameGenerator != null) {
            boolean retry = true;

            mainFrameGenerator.done();
            while (retry) {
                try {
                    mainFrameGenerator.join();
                    retry = false;
                } catch (final InterruptedException e) {
                    // Do nothing.
                }
            }

            this.gameActivity1.setMainFrameGenerator(null);
        }

        this.releaseResources();
    }

    // Called from Game.doDraw() which is on background thread so volatile SurfaceHolder
    // surfaceHolder1.
    public void drawOnSurfaceHolderCanvas() {
        if ((this.cache != null) && (this.surfaceHolder1 != null)) {
            final Canvas canvas = this.surfaceHolder1.lockCanvas();
            if ((canvas != null) && (this.paint1 != null)) {
                canvas.drawBitmap(this.cache, 0.0F, 0.0F, this.paint1);
                // post to main UI thread
                this.surfaceHolder1.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void releaseResources() {
        this.gameActivity1 = null;
        this.surfaceHolder1 = null;
        this.cache = null;
        this.paint1 = null;
    }
}
