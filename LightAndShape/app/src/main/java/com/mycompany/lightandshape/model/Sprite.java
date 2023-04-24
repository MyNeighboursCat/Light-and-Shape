/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.mycompany.lightandshape.model;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Colin Walters
 * @version 1.0, 24/05/2023
 */
/*
 * this class always uses the getters and setters to access variables calling a
 * method in this class may call the overridden method in the subclass e.g.
 * this.setX() other classes only use their own getter and setters to access
 * their OWN member variables when necessary
 */
// default package access
abstract class Sprite {
    // default package access
    static final String SPRITE_BUNDLE = "SPRITE_BUNDLE_";
    private final String TAG = this.getClass().getSimpleName();
    // bundle start - data to save to recreate object
    private boolean toDestroy = false;
    private int canvasWidth = 0;
    private int canvasHeight = 0;
    private int canvasPadding = 0;
    // DIFF WITH LIB
    // private volatile int imageDisplayed = 0;
    private int imageDisplayed = 0;
    // DIFF WITH LIB
    // private int drawableWidth = 0;
    private volatile int drawableWidth = 0;
    // DIFF WITH LIB
    // private int drawableHeight = 0;
    private volatile int drawableHeight = 0;
    private int x = 10;
    private int y = 10;
    private int moveStep = 5;
    // DIFF WITH LIB
    // private int minX = 0;
    private volatile int minX = 0;
    // DIFF WITH LIB
    // private int minY = 0;
    private volatile int minY = 0;
    // DIFF WITH LIB
    // private int maxX = 0;
    private volatile int maxX = 0;
    // DIFF WITH LIB
    // private int maxY = 0;
    private volatile int maxY = 0;
    // DIFF WITH LIB
    // private volatile Direction direction = Direction.NONE;
    private Direction direction = Direction.NONE;
    private int bundleCounter = 0;
    private Random rand = new Random();
    private ArrayList<Bitmap> draws1 = new ArrayList<>();
    // bundle end

    // default package access
    Sprite(final ArrayList<Drawable> image, final Canvas canvas,
           @SuppressWarnings("SameParameterValue") float proportion,
           @SuppressWarnings("SameParameterValue") float heightMultiplier) {
        super();

        if (proportion <= 0.0) {
            // DIFF WITH LIB
            // proportion = 8.0F;
            proportion = 9.0F;
        }

        if (heightMultiplier <= 0.0) {
            heightMultiplier = 1.0F;
        }

        final Rect r = canvas.getClipBounds();
        this.setCanvasWidth(r.width());
        this.setCanvasHeight(r.height());
        this.setCanvasPadding(Sprite.calculateCanvasPadding(this
                .getCanvasHeight()));

        this.setDrawableWidth(this.calculateDrawableWidth(
                this.getCanvasWidth(), proportion));
        this.setDrawableHeight(Sprite.calculateDrawableHeight(
                this.getDrawableWidth(), heightMultiplier));

        this.setMinX(this.getCanvasPadding());
        this.setMinY(this.getCanvasPadding());

        this.setMaxX(this.getCanvasWidth() - this.getDrawableWidth()
                - this.getCanvasPadding());
        this.setMaxY(this.getCanvasHeight() - this.getDrawableHeight()
                - this.getCanvasPadding());

        this.convertDrawableToBitmap(image);
    }

    // default package access
    Sprite(final Bundle bundle1) {
        super();

        this.setBundleCounter(0);

        // calling the setters here calls the subclassed methods
        // so these need to be in a specific order,
        // such as setting x and y last,
        // which depend on variables such as the direction being set

        this.setToDestroy(bundle1.getBoolean(Sprite.SPRITE_BUNDLE
                + this.getBundleCounter()));

        this.setDirection(Direction.valueOf(bundle1
                .getString(Sprite.SPRITE_BUNDLE
                        + this.getBundleCounter())));

        int[] intArray1 = bundle1.getIntArray(Sprite.SPRITE_BUNDLE +
                this.getBundleCounter());
        if (intArray1 != null) {
            this.setCanvasWidth(intArray1[0]);
            this.setCanvasHeight(intArray1[1]);
            this.setCanvasPadding(intArray1[2]);
            this.setImageDisplayed(intArray1[3]);
            this.setDrawableWidth(intArray1[4]);
            this.setDrawableHeight(intArray1[5]);
            this.setMoveStep(intArray1[6]);
            this.setMinX(intArray1[7]);
            this.setMinY(intArray1[8]);
            this.setMaxX(intArray1[9]);
            this.setMaxY(intArray1[10]);
            // set x and y after setting min and max x and y because
            // these methods use the ranges in their code
            this.setX(intArray1[11]);
            this.setY(intArray1[12]);
        }
    }

    // DIFF WITH LIB
    // private static int calculateCanvasPadding(final int canvasHeight1) {
    // default package access
    static int calculateCanvasPadding(final int canvasHeight1) {
        // DIFF WITH LIB
        // return canvasHeight1 / 40;
        return canvasHeight1 / 80;
    }

    private static int calculateDrawableHeight(final int width,
                                               final float multiplier) {
        return (int) (width * multiplier);
    }

    private int calculateDrawableWidth(final int canvasSmallestSide,
                                       final float proportion) {
        float number = canvasSmallestSide;
        number -= this.getCanvasPadding() * 2.0F;
        number /= proportion;

        return (int) number;
    }

    // default package access
    void convertDrawableToBitmap(final ArrayList<Drawable> image) {
        if (this.getDraws1() != null) {
            this.getDraws1().clear();
        }

        for (final Drawable draw1 : image) {
            final Bitmap bitmap = Bitmap.createBitmap(this.getDrawableWidth(),
                    this.getDrawableHeight(), Bitmap.Config.ARGB_8888);
            final Canvas c = new Canvas(bitmap);
            draw1.setBounds(0, 0, this.getDrawableWidth(),
                    this.getDrawableHeight());
            draw1.draw(c);
            this.getDraws1().add(bitmap);
        }
    }

    // default package access
    Bitmap getASprite() {
        return this.getDraws1().get(this.getImageDisplayed());
    }

    // default package access
    int getBundleCounter() {
        // increase the variable AFTER returning the current value
        return this.bundleCounter++;
    }

    // default package access
    @SuppressWarnings("SameParameterValue")
    private void setBundleCounter(final int bundleCounter) {
        this.bundleCounter = bundleCounter;
    }

    // default package access
    private int getCanvasHeight() {
        return this.canvasHeight;
    }

    // default package access
    private void setCanvasHeight(final int canvasHeight) {
        this.canvasHeight = canvasHeight;
    }

    // default package access
    int getCanvasPadding() {
        return this.canvasPadding;
    }

    // default package access
    private void setCanvasPadding(final int canvasPadding) {
        this.canvasPadding = canvasPadding;
    }

    // default package access
    private int getCanvasWidth() {
        return this.canvasWidth;
    }

    // default package access
    private void setCanvasWidth(final int canvasWidth) {
        this.canvasWidth = canvasWidth;
    }

    // default package access
    private Direction getDirection() {
        return this.direction;
    }

    // default package access
    void setDirection(final Direction direction) {
        this.direction = direction;
    }

    // default package access
    int getDrawableHeight() {
        return this.drawableHeight;
    }

    // default package access
    private void setDrawableHeight(final int drawableHeight) {
        this.drawableHeight = drawableHeight;
    }

    // default package access
    int getDrawableWidth() {
        return this.drawableWidth;
    }

    // default package access
    private void setDrawableWidth(final int drawableWidth) {
        this.drawableWidth = drawableWidth;
    }

    // default package access
    ArrayList<Bitmap> getDraws1() {
        return this.draws1;
    }

    // default package access
    @SuppressWarnings("SameParameterValue")
    private void setDraws1(final ArrayList<Bitmap> draws1) {
        this.draws1 = draws1;
    }

    // default package access
    int getImageDisplayed() {
        return this.imageDisplayed;
    }

    // default package access
    void setImageDisplayed(final int imageDisplayed) {
        this.imageDisplayed = imageDisplayed;
    }

    // default package access
    int getMaxX() {
        return this.maxX;
    }

    // default package access
    private void setMaxX(final int maxX) {
        this.maxX = maxX;
    }

    // default package access
    int getMaxY() {
        return this.maxY;
    }

    // default package access
    private void setMaxY(final int maxY) {
        this.maxY = maxY;
    }

    // default package access
    int getMinX() {
        return this.minX;
    }

    // default package access
    void setMinX(final int minX) {
        this.minX = minX;
    }

    // default package access
    int getMinY() {
        return this.minY;
    }

    // default package access
    void setMinY(final int minY) {
        this.minY = minY;
    }

    // default package access
    private int getMoveStep() {
        return this.moveStep;
    }

    // default package access
    void setMoveStep(final int moveStep) {
        this.moveStep = moveStep;
    }

    // default package access
    Random getRand() {
        return this.rand;
    }

    // default package access
    @SuppressWarnings("SameParameterValue")
    private void setRand(final Random rand) {
        this.rand = rand;
    }

    // default package access
    int getX() {
        return this.x;
    }

    // default package access
    void setX(final int x) {
        if (x < this.getMinX()) {
            this.x = this.getMinX();
        } else this.x = Math.min(x, this.getMaxX());
    }

    // default package access
    int getY() {
        return this.y;
    }

    // default package access
    void setY(final int y) {
        if (y < this.getMinY()) {
            this.y = this.getMinY();
        } else this.y = Math.min(y, this.getMaxY());
    }

    // default package access
    boolean isToDestroy() {
        return this.toDestroy;
    }

    // default package access
    void setToDestroy(final boolean toDestroy) {
        this.toDestroy = toDestroy;
    }

    // NOTE: although setX() and SetY() are called internally, causing a
    // performance hit,
    // this is valid because it ensures that x and y are not set out of bounds
    // default package access
    void move() {
        switch (this.getDirection()) {
            case NONE:
                return;
            case UP:
                this.setY(this.getY() - this.getMoveStep());
                break;
            case UP_RIGHT:
                this.setY(this.getY() - this.getMoveStep());
                this.setX(this.getX() + this.getMoveStep());
                break;
            case RIGHT:
                this.setX(this.getX() + this.getMoveStep());
                break;
            case DOWN_RIGHT:
                this.setY(this.getY() + this.getMoveStep());
                this.setX(this.getX() + this.getMoveStep());
                break;
            case DOWN:
                this.setY(this.getY() + this.getMoveStep());
                break;
            case DOWN_LEFT:
                this.setY(this.getY() + this.getMoveStep());
                this.setX(this.getX() - this.getMoveStep());
                break;
            case LEFT:
                this.setX(this.getX() - this.getMoveStep());
                break;
            case UP_LEFT:
                this.setY(this.getY() - this.getMoveStep());
                this.setX(this.getX() - this.getMoveStep());
                break;
            default:
                Log.e(this.TAG, "move() error 1");
                throw new RuntimeException();
        }

        // DIFF WITH LIB
        /*
         * this.setImageDisplayed(this.getImageDisplayed() + 1); if
         * (this.getImageDisplayed() >= this.getDraws1().size()) {
         * this.setImageDisplayed(0); }
         */
    }

    // default package access
    void releaseResources() {
        if (this.getDraws1() != null) {
            this.getDraws1().clear();
        }
        this.setDraws1(null);
        this.setRand(null);
    }

    // default package access
    Bundle writeToBundle(final Bundle bundle1) {
        this.setBundleCounter(0);

        final int[] intArray1 = {this.getCanvasWidth(),
                this.getCanvasHeight(), this.getCanvasPadding(),
                this.getImageDisplayed(), this.getDrawableWidth(),
                this.getDrawableHeight(), this.getMoveStep(), this.getMinX(),
                this.getMinY(), this.getMaxX(), this.getMaxY(), this.getX(),
                this.getY()};

        bundle1.putBoolean(
                Sprite.SPRITE_BUNDLE
                        + this.getBundleCounter(),
                this.isToDestroy());
        bundle1.putString(
                Sprite.SPRITE_BUNDLE
                        + this.getBundleCounter(), this
                        .getDirection().toString());
        bundle1.putIntArray(
                Sprite.SPRITE_BUNDLE
                        + this.getBundleCounter(), intArray1);

        return bundle1;
    }

    // default package access
    enum Direction {
        NONE, UP, UP_RIGHT, RIGHT, DOWN_RIGHT, DOWN, DOWN_LEFT, LEFT, UP_LEFT
    }
}
