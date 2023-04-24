/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.mycompany.lightandshape.model;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.mycompany.lightandshape.BasicApplication;
import com.mycompany.lightandshape.R;
import com.mycompany.lightandshape.controller.GameActivity;
import com.mycompany.lightandshape.controller.SettingsActivity;
import com.mycompany.lightandshape.model.Shape.ShapeType;
import com.mycompany.lightandshape.view.MainView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Random;

/**
 * @author Colin Walters
 * @version 1.0, 24/05/2023
 */
@SuppressWarnings({"unchecked", "Convert2Lambda"})
public final class Game {
    private static final String GAME_BUNDLE = "GAME_BUNDLE_";
    private static final String LIGHTS_BUNDLE = "LIGHTS_BUNDLE_";
    private static final String TARGET_SHAPES_BUNDLE = "TARGET_SHAPES_BUNDLE_";
    private static final String FIND_SHAPES_BUNDLE = "FIND_SHAPES_BUNDLE_";
    private static final String TOUCH_POINTS_BUNDLE = "TOUCH_POINTS_BUNDLE_";
    private static final String FOUND_SHAPES_BUNDLE = "FOUND_SHAPES_BUNDLE_";
    private static final String MISS_CROSSES_BUNDLE = "MISS_CROSSES_BUNDLE_";
    private static final float TOUCH_POINT_TOLERANCE_PROPORTION = 0.5F;
    // 1000 milliseconds in a second
    // the timing may not be precise because of rounding errors:
    // ints instead of floats are used for the sprite positioning
    private static final int PIXELS_TO_MOVE_TIME_SHAPE = 7000;
    private static final int PIXELS_TO_MOVE_TIME_LIGHT_BEAM = 1500;
    private static final int MOVE_STEP_MAXIMUM_PROPORTION = 20;
    private final String TAG = this.getClass().getSimpleName();
    // bundle start - data to save to recreate object
    // Threads: keyword is used to make sure different threads have
    // the same value of a variable.
    // The synchronized keyword used in the method declaration is an alternative
    private volatile boolean paused = false;
    private boolean moveShapes = false;
    private int lightMoveCount = 0;
    private ArrayList<Integer> validNewTargetShapes = new ArrayList<>();
    private ArrayList<Integer> validNewFindShapes = new ArrayList<>();
    private volatile int missesLeft = 0;
    private volatile long timeMeterBase = 0L;
    private volatile long pausedTime = 0L;
    private long longScore = -1L;
    private String stringScore = null;
    private ArrayList<Light> lights = new ArrayList<>();
    private volatile ArrayList<Shape> targetShapes = new ArrayList<>();
    private ArrayList<Shape> findShapes = new ArrayList<>();
    // bundle end
    private volatile ArrayList<TouchPoint> touchPoints = new ArrayList<>();
    private ArrayList<FoundShape> foundShapes = new ArrayList<>();
    private ArrayList<MissCross> missCrosses = new ArrayList<>();
    private int currentFindShapesNumber = 0;
    private int currentSpeedNumber = 0;
    private int canvasPadding = 0;
    private int canvasWidth = 0;
    private int canvasHeight = 0;
    private volatile GameActivity gameActivity1;
    private MainView mainView1 = null;
    private Canvas canvas1;
    private Bitmap bitmap1 = null;
    private ArrayList<SoundEffect.SoundType> mediaPlayerTypes = null;
    private volatile SoundEffect soundEffect1 = null;
    private Resources res = null;
    private ArrayList<Drawable> myImage1 = null;
    private ArrayList<Drawable> myImage2 = null;
    private ArrayList<Drawable> myImage3 = null;
    private ArrayList<Drawable> myImage4 = null;
    private ArrayList<Drawable> myImage5 = null;
    private ArrayList<Drawable> myImage6 = null;
    private ArrayList<Drawable> myImage7 = null;
    private ArrayList<Drawable> myImage8 = null;
    private ArrayList<Shape> targetShapes2 = null;
    private ArrayList<Shape> findShapes2 = null;
    private ArrayList<TouchPoint> touchPoints2 = null;
    private ArrayList<FoundShape> foundShapes2 = null;
    private ArrayList<MissCross> missCrosses2 = null;
    private volatile Chronometer timeMeter = null;
    private Paint paintBitmap = null;
    private Paint paintBackground = null;
    private Paint paintBorder = null;
    private Paint paintSprite = null;
    private volatile long systemClockElapsedRealtime = 0L;
    private volatile long frameRate = 0L;

    public Game(final Bundle bundle1) {
        super();

        // Don't use 'counter' as variable name as it increase with each line
        // for some reason!
        int cnt = 0;
        Bundle bundle2;
        this.gameActivity1 = null;
        this.canvas1 = null;

        boolean[] booleanArray1 = bundle1.getBooleanArray(Game.GAME_BUNDLE + cnt++);
        if (booleanArray1 != null) {
            this.paused = booleanArray1[0];
            this.moveShapes = booleanArray1[1];
        }

        int[] intArray1 = bundle1.getIntArray(Game.GAME_BUNDLE + cnt++);
        if (intArray1 != null) {
            this.lightMoveCount = intArray1[0];
            this.missesLeft = intArray1[1];
        }

        long[] longArray1 = bundle1.getLongArray(Game.GAME_BUNDLE + cnt++);
        if (longArray1 != null) {
            this.timeMeterBase = longArray1[0];
            this.pausedTime = longArray1[1];
            this.longScore = longArray1[2];
        }

        this.stringScore = bundle1.getString(Game.GAME_BUNDLE + cnt++);

        this.validNewTargetShapes = bundle1
                .getIntegerArrayList(Game.GAME_BUNDLE + cnt++);

        this.validNewFindShapes = bundle1.getIntegerArrayList(Game.GAME_BUNDLE
                + cnt);

        for (cnt = 0; cnt < SettingsActivity.MAX_LIGHTS; cnt++) {
            bundle2 = bundle1.getBundle(Game.LIGHTS_BUNDLE
                    + cnt);
            if (bundle2 != null) {
                this.lights.add(new Light(bundle2));
            }
        }

        for (cnt = 0; cnt < SettingsActivity.MAX_TARGET_SHAPES; cnt++) {
            bundle2 = bundle1.getBundle(Game.TARGET_SHAPES_BUNDLE
                    + cnt);
            if (bundle2 != null) {
                this.targetShapes.add(new Shape(bundle2));
            }
        }

        // Can't use for loop with SettingsActivity.MAX_FIND_SHAPES because
        // findShapes contains missed shapes as well
        cnt = 0;
        do {
            bundle2 = bundle1.getBundle(Game.FIND_SHAPES_BUNDLE
                    + cnt);
            if (bundle2 != null) {
                this.findShapes.add(new Shape(bundle2));
            }

            cnt++;
        } while (bundle2 != null);

        for (cnt = 0; cnt < SettingsActivity.MAX_TOUCH_POINTS; cnt++) {
            bundle2 = bundle1.getBundle(Game.TOUCH_POINTS_BUNDLE
                    + cnt);
            if (bundle2 != null) {
                this.touchPoints.add(new TouchPoint(bundle2));
            }
        }

        for (cnt = 0; cnt < SettingsActivity.MAX_TARGET_SHAPES; cnt++) {
            bundle2 = bundle1.getBundle(Game.FOUND_SHAPES_BUNDLE
                    + cnt);
            if (bundle2 != null) {
                this.foundShapes.add(new FoundShape(bundle2));
            }
        }

        for (cnt = 0; cnt < SettingsActivity.MAX_TARGET_SHAPES; cnt++) {
            bundle2 = bundle1.getBundle(Game.MISS_CROSSES_BUNDLE
                    + cnt);
            if (bundle2 != null) {
                this.missCrosses.add(new MissCross(bundle2));
            }
        }
    }

    public Game(final GameActivity activity, final Canvas canvas,
                final int[] settings) {
        super();

        this.gameActivity1 = activity;
        this.canvas1 = canvas;

        this.canvasWidth = this.canvas1.getWidth();
        this.canvasHeight = this.canvas1.getHeight();

        this.doConstructorInitialize(true, settings);
    }

    private void addNewImageFrame(final ArrayList<Drawable> image1,
                                  final String type) {
        if (type.compareTo("CIRCLE") == 0) {
            image1.add(this.myImage8.get(0));
        } else if (type.compareTo("SQUARE") == 0) {
            image1.add(this.myImage8.get(1));
        } else if (type.compareTo("TRIANGLE") == 0) {
            image1.add(this.myImage8.get(2));
        } else {
            Log.e(this.TAG, "addNewImageFrame() error 1");
            throw new RuntimeException();
        }

        // Add cross image
        image1.add(this.myImage7.get(0));
    }

    public void addTouchPoint(final int touchX, final int touchY) {
        Shape shape1;
        try {
            shape1 = this.targetShapes.get(0);
        } catch (final Exception e) {
            Log.e(this.TAG, "addTouchPoint() error 1");
            throw new RuntimeException();
        }

        if ((touchX >= shape1.getMinX())
                && (touchX <= ((shape1.getMaxX() + shape1.getDrawableWidth()) - 1))
                && (touchY >= shape1.getMinY())
                && (touchY <= ((shape1.getMaxY() + shape1.getDrawableHeight()) - 1))) {
            this.touchPoints.add(new TouchPoint(touchX, touchY));
        }
    }

    private void createShape(final int i, final int j,
                             final ArrayList<Shape> targetShapes1,
                             final ArrayList<Shape> findShapes1) {
        boolean isCorrectlySelectedShape = false;

        // code in this method must use targetShapes1 and findShapes1 and
        // NOT targetShapes and findShapes

        // shapes are unique in both find and target shapes
        // this includes missed shapes
        // correctly selected shapes are deleted and replace straight away
        // missed shapes are deleted when isToDestroy() is true
        // valid target shapes is updated when the shape is deleted, so
        // when a new shape is next created it could be the previous missed
        // shape (now disappeared) again and it could become a find shape
        // as well
        // the number of misses is restricted to MAX_FIND_SHAPES:
        // otherwise there is an unlimited number of misses at one time so
        // no shapes would be left to create new ones since all shapes in
        // the find and target shapes are unique even if they are misses

        if (i >= 0) {
            // Missed shapes use isToDestroy()
            final Shape shape1 = targetShapes1.get(i);
            if (!shape1.isMissedShape()) {
                // correctly selected shape
                final int typeColour = (shape1.getColour().ordinal() * 3)
                        + shape1.getType().ordinal();
                this.validNewTargetShapes.add(typeColour);

                targetShapes1.remove(i);
                findShapes1.remove(j);

                isCorrectlySelectedShape = true;
            }
        }

        if (this.validNewTargetShapes.size() == 0) {
            Log.e(this.TAG, "createShape() error 1");
            throw new RuntimeException();
        }

        // Target shapes
        final Random rand = new Random();
        final int randomNumber = rand.nextInt(this.validNewTargetShapes.size());
        final int typeColour = this.validNewTargetShapes.get(randomNumber);
        this.validNewTargetShapes.remove(randomNumber);

        this.myImage3.clear();
        this.myImage3.add(this.myImage2.get(typeColour));
        String[] stringArray1 = this.setTypeAndColour(typeColour);
        String type = stringArray1[0];
        String colour = stringArray1[1];
        this.addNewImageFrame(this.myImage3, type);

        final Shape shape1 = new Shape(this.myImage3, this.canvas1, type,
                colour, ShapeType.TARGET);
        shape1.setTargetShapeDetails();

        targetShapes1.add(shape1);

        if (targetShapes1.size() == 0) {
            Log.e(this.TAG, "createShape() error 2");
            throw new RuntimeException();
        }

        // Find shapes
        this.validNewFindShapes.clear();
        for (final Shape sprite1 : targetShapes1) {
            if (!sprite1.isMissedShape()) {
                final int typeColour1 = (sprite1.getColour().ordinal() * 3)
                        + sprite1.getType().ordinal();

                if (findShapes1.size() == 0) {
                    this.validNewFindShapes.add(typeColour1);
                } else {
                    boolean found = false;

                    for (final Shape sprite2 : findShapes1) {
                        if (!sprite2.isMissedShape()) {
                            final int typeColour2 = (sprite2.getColour()
                                    .ordinal() * 3)
                                    + sprite2.getType().ordinal();

                            if (typeColour1 == typeColour2) {
                                found = true;
                            }
                        }
                    }

                    if (!found) {
                        this.validNewFindShapes.add(typeColour1);
                    }
                }
            }
        }

        if (this.validNewFindShapes.size() == 0) {
            Log.e(this.TAG, "createShape() error 3");
            throw new RuntimeException();
        }

        if (i >= 0 || (j < this.currentFindShapesNumber)) {
            final int typeColour1 = this.validNewFindShapes.get(rand
                    .nextInt(this.validNewFindShapes.size()));

            for (final Shape sprite1 : targetShapes1) {
                final int typeColour2 = (sprite1.getColour().ordinal() * 3)
                        + sprite1.getType().ordinal();
                if ((typeColour1 == typeColour2) && !sprite1.isMissedShape()) {
                    sprite1.setNewShape(true);
                }
            }

            this.myImage3.clear();
            this.myImage3.add(this.myImage2.get(typeColour1));
            stringArray1 = this.setTypeAndColour(typeColour1);
            type = stringArray1[0];
            colour = stringArray1[1];
            this.addNewImageFrame(this.myImage3, type);

            final Shape shape3 = new Shape(this.myImage3, this.canvas1, type,
                    colour, ShapeType.FIND);
            shape3.setNewShape(true);

            if (isCorrectlySelectedShape) {
                findShapes1.add(j, shape3);
            } else {
                findShapes1.add(shape3);
            }
        }

        if (findShapes1.size() == 0) {
            Log.e(this.TAG, "createShape() error 4");
            throw new RuntimeException();
        }
    }

    private void doConstructorInitialize(final boolean firstInstance,
                                         final int[] settings) {
        this.canvasPadding = Sprite.calculateCanvasPadding(this.canvasHeight);

        final int currentLightsNumber = settings[0];
        final int currentAnglesNumber = settings[1];
        final int currentTargetShapesNumber = settings[2];
        this.currentFindShapesNumber = settings[3];
        final int currentMissesNumber = settings[4];
        this.currentSpeedNumber = settings[5];

        if (firstInstance) {
            this.missesLeft = currentMissesNumber;
        }
        // Need to reverse speed setting
        if (this.currentSpeedNumber > 3) {
            this.moveShapes = true;
            this.currentSpeedNumber -= 3;
        }
        this.currentSpeedNumber = ((SettingsActivity.MAX_SPEED / 2) + 1)
                - this.currentSpeedNumber;

        this.mainView1 = this.gameActivity1.getMainView();
        this.res = this.gameActivity1.getResources();

        // Sound effects
        this.setSoundEffect1();

        // Lights
        this.myImage1 = new ArrayList<>();
        this.myImage1.add(ResourcesCompat.getDrawable(this.res, R.drawable.light, null));

        // Target and find shapes
        this.myImage2 = new ArrayList<>();
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.bluecircle, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.bluesquare, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.bluetriangle, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.browncircle, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.brownsquare, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.browntriangle, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.cyancircle, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.cyansquare, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.cyantriangle, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.greycircle, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.greysquare, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.greytriangle, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.magentacircle, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.magentasquare, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.magentatriangle, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.orangecircle, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.orangesquare, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.orangetriangle, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.pinkcircle, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.pinksquare, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.pinktriangle, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.redcircle, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.redsquare, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.redtriangle, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.yellowcircle, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.yellowsquare, null));
        this.myImage2.add(ResourcesCompat.getDrawable(this.res, R.drawable.yellowtriangle, null));

        this.myImage3 = new ArrayList<>();

        this.myImage4 = new ArrayList<>();
        this.myImage4.add(ResourcesCompat.getDrawable(this.res, R.drawable.greencircle, null));

        this.myImage5 = new ArrayList<>();
        this.myImage5.add(ResourcesCompat.getDrawable(this.res, R.drawable.greensquare, null));

        this.myImage6 = new ArrayList<>();
        this.myImage6.add(ResourcesCompat.getDrawable(this.res, R.drawable.greentriangle, null));

        this.myImage7 = new ArrayList<>();
        this.myImage7.add(ResourcesCompat.getDrawable(this.res, R.drawable.misscross, null));

        this.myImage8 = new ArrayList<>();
        this.myImage8.add(ResourcesCompat.getDrawable(this.res, R.drawable.whitecircle, null));
        this.myImage8.add(ResourcesCompat.getDrawable(this.res, R.drawable.whitesquare, null));
        this.myImage8.add(ResourcesCompat.getDrawable(this.res, R.drawable.whitetriangle, null));

        // Create lights and shapes
        if (firstInstance) {
            for (int i = 0; i < this.myImage2.size(); i++) {
                this.validNewTargetShapes.add(i);
            }

            for (int i = 0; i < currentTargetShapesNumber; i++) {
                this.createShape(-1, i, this.targetShapes, this.findShapes);
            }

            // Create lights
            Shape shape1;
            try {
                shape1 = this.targetShapes.get(0);
            } catch (final Exception e) {
                Log.e(this.TAG, "doConstructorInitialize() error 1");
                throw new RuntimeException();
            }
            final int minX1 = shape1.getMinX();
            final int maxX1 = shape1.getMaxX() + shape1.getDrawableWidth();
            final int minY1 = shape1.getMinY();
            final int maxY1 = shape1.getMaxY() + shape1.getDrawableHeight();
            final int width1 = maxX1 - minX1;
            final int height1 = maxY1 - minY1;

            switch (currentLightsNumber) {
                case 1:
                    this.lights.add(new Light(this.myImage1, this.canvas1, minX1
                            + (width1 / 2), minY1 + (height1 / 2),
                            currentAnglesNumber));
                    break;
                case 2:
                    this.lights.add(new Light(this.myImage1, this.canvas1, minX1
                            + (width1 / 2), minY1 + (height1 / 4),
                            currentAnglesNumber));
                    this.lights.add(new Light(this.myImage1, this.canvas1, minX1
                            + (width1 / 2), minY1 + ((height1 / 4) * 3),
                            currentAnglesNumber));
                    break;
                case 3:
                    this.lights.add(new Light(this.myImage1, this.canvas1, minX1
                            + (width1 / 2), minY1 + (height1 / 4),
                            currentAnglesNumber));
                    this.lights.add(new Light(this.myImage1, this.canvas1, minX1
                            + (width1 / 4), minY1 + ((height1 / 4) * 3),
                            currentAnglesNumber));
                    this.lights.add(new Light(this.myImage1, this.canvas1, minX1
                            + ((width1 / 4) * 3), minY1 + ((height1 / 4) * 3),
                            currentAnglesNumber));
                    break;
                case 4:
                    this.lights.add(new Light(this.myImage1, this.canvas1, minX1
                            + (width1 / 4), minY1 + (height1 / 4),
                            currentAnglesNumber));
                    this.lights.add(new Light(this.myImage1, this.canvas1, minX1
                            + ((width1 / 4) * 3), minY1 + (height1 / 4),
                            currentAnglesNumber));
                    this.lights.add(new Light(this.myImage1, this.canvas1, minX1
                            + (width1 / 4), minY1 + ((height1 / 4) * 3),
                            currentAnglesNumber));
                    this.lights.add(new Light(this.myImage1, this.canvas1, minX1
                            + ((width1 / 4) * 3), minY1 + ((height1 / 4) * 3),
                            currentAnglesNumber));
                    break;
                default:
                    Log.e(this.TAG, "doConstructorInitialize() error 2");
                    throw new RuntimeException();
            }
        } else {
            if (!this.lights.isEmpty()) {
                // do the details that were missed out when using bundle
                // constructor
                for (final Light sprite1 : this.lights) {
                    sprite1.convertDrawableToBitmap(this.myImage1);
                }

                for (final Shape sprite1 : this.targetShapes) {
                    final int typeColour = (sprite1.getColour().ordinal() * 3)
                            + sprite1.getType().ordinal();
                    this.myImage3.clear();
                    this.myImage3.add(this.myImage2.get(typeColour));
                    this.myImage3.add(this.myImage8.get(sprite1.getType()
                            .ordinal()));
                    // Add cross image
                    this.myImage3.add(this.myImage7.get(0));

                    sprite1.convertDrawableToBitmap(this.myImage3);
                }

                for (final Shape sprite1 : this.findShapes) {
                    final int typeColour = (sprite1.getColour().ordinal() * 3)
                            + sprite1.getType().ordinal();
                    this.myImage3.clear();
                    this.myImage3.add(this.myImage2.get(typeColour));
                    this.myImage3.add(this.myImage8.get(sprite1.getType()
                            .ordinal()));
                    // Add cross image
                    this.myImage3.add(this.myImage7.get(0));

                    sprite1.convertDrawableToBitmap(this.myImage3);
                }

                for (final FoundShape sprite1 : this.foundShapes) {
                    if (sprite1.getType() == Shape.SpriteType.CIRCLE) {
                        sprite1.convertDrawableToBitmap(this.myImage4);
                    } else if (sprite1.getType() == Shape.SpriteType.SQUARE) {
                        sprite1.convertDrawableToBitmap(this.myImage5);
                    } else if (sprite1.getType() == Shape.SpriteType.TRIANGLE) {
                        sprite1.convertDrawableToBitmap(this.myImage6);
                    }
                }

                for (final MissCross sprite1 : this.missCrosses) {
                    sprite1.convertDrawableToBitmap(this.myImage7);
                }
            }
        }

        // Timer
        this.timeMeter = this.gameActivity1.getBinding().timeMeter;
        if (firstInstance) {
            // base time defaults to SystemClock.elapsedRealtime()
            this.timeMeterBase = this.timeMeter.getBase();
        } else {
            if (this.paused) {
                this.setChronometer();
                // this will show the current paused time in the chronometer
                this.pausedTime = SystemClock.elapsedRealtime();
            } else {
                this.timeMeter.setBase(this.timeMeterBase);
            }
        }
        if (!this.paused) {
            this.timeMeter.start();
        }

        // Paints
        this.paintBackground = new Paint();
        this.paintBackground.setColor(Color.BLACK);

        this.paintBorder = new Paint();
        this.paintBorder.setColor(Color.WHITE);
        // The stroke width is not the same measurement as pixels
        this.paintBorder.setStrokeWidth(3);
        this.paintBorder.setStyle(Paint.Style.STROKE);

        this.paintSprite = new Paint();
    }

    // default package access
    void doDraw() {
        if ((this.gameActivity1 == null) || (this.canvas1 == null)
                || (this.mainView1 == null)) {
            return;
        }

        if (this.systemClockElapsedRealtime != 0L) {
            this.frameRate = SystemClock.elapsedRealtime()
                    - this.systemClockElapsedRealtime;
        }
        this.systemClockElapsedRealtime = SystemClock.elapsedRealtime();

        if (!this.paused) {
            this.doInitialize();
            this.doFindShapes();
            this.doTargetShapes();
            this.doFoundShapes();
            this.doMissCrosses();
            this.doTouch();
            this.moveTargetShapes();
            this.doFindShapesPositions();
        }

        this.drawInitialize();
        this.drawSprites();
        this.drawBorder();
        this.gameOver();

        if (!this.paused) {
            if (this.lightMoveCount >= this.currentSpeedNumber) {
                this.lightMoveCount = 0;
            }
        }

        this.mainView1.drawOnSurfaceHolderCanvas();
    }

    private void doFindShapes() {
        // clone() creates a shallow copy of the ArrayList
        // this means that only the object references and NOT the objects
        // themselves are copied
        // so changing sprite1 values - e.g. sprite1.doNewShape() - in
        // findShapes
        // is the same as using findShapes2
        this.findShapes2 = (ArrayList<Shape>) this.findShapes.clone();
        for (final Shape sprite1 : this.findShapes) {
            if (sprite1.isToDestroy()) {
                final int i = this.findShapes2.indexOf(sprite1);
                if (i > -1) {
                    this.findShapes2.remove(i);
                }
            } else {
                if (sprite1.isNewShape()) {
                    sprite1.doNewShape();
                }
            }
        }
        this.findShapes = this.findShapes2;
    }

    private void doFindShapesPositions() {
        int i = 0;
        for (final Shape sprite1 : this.findShapes) {
            // if x is > maximum x then x is reset to x
            sprite1.setX(this.canvasPadding + (i * sprite1.getDrawableWidth())
                    + (i * this.canvasPadding));
            sprite1.setY(this.canvasPadding);
            i++;
        }
    }

    private void doFoundShapes() {
        this.foundShapes2 = (ArrayList<FoundShape>) this.foundShapes.clone();
        for (final FoundShape sprite1 : this.foundShapes) {
            int i;

            if (sprite1.isToDestroy()) {
                i = this.foundShapes2.indexOf(sprite1);
                if (i > -1) {
                    this.foundShapes2.remove(i);
                }
            }
        }
        this.foundShapes = this.foundShapes2;
    }

    private void doInitialize() {
        this.lightMoveCount++;
    }

    private void doMissCrosses() {
        this.missCrosses2 = (ArrayList<MissCross>) this.missCrosses.clone();
        for (final MissCross sprite1 : this.missCrosses) {
            int i;

            if (sprite1.isToDestroy()) {
                i = this.missCrosses2.indexOf(sprite1);
                if (i > -1) {
                    this.missCrosses2.remove(i);
                }
            }
        }
        this.missCrosses = this.missCrosses2;
    }

    @SuppressWarnings("SameParameterValue")
    private void doMoveStep(final Sprite sprite1, final int pixelsToMove,
                            final int pixelsToMoveTime) {
        if (this.frameRate != 0L) {
            final int maximumMoveStep = pixelsToMove
                    / Game.MOVE_STEP_MAXIMUM_PROPORTION;
            @SuppressWarnings("UnnecessaryLocalVariable") final int screenTime = pixelsToMoveTime;
            final float frameCycles = (((float) screenTime) / ((float) this.frameRate));
            int spriteMoveStep = Math.round(pixelsToMove / frameCycles);
            if (spriteMoveStep < 0) {
                spriteMoveStep = 1;
            } else if (spriteMoveStep > maximumMoveStep) {
                spriteMoveStep = maximumMoveStep;
            }
            sprite1.setMoveStep(spriteMoveStep);
        }

        sprite1.move();
    }

    private void doTargetShapes() {
        this.targetShapes2 = (ArrayList<Shape>) this.targetShapes.clone();
        for (final Shape sprite1 : this.targetShapes) {
            if (sprite1.isToDestroy()) {
                final int i = this.targetShapes2.indexOf(sprite1);
                if (i > -1) {
                    final int typeColour = (sprite1.getColour().ordinal() * 3)
                            + sprite1.getType().ordinal();
                    this.validNewTargetShapes.add(typeColour);

                    this.targetShapes2.remove(i);
                }
            } else {
                if (sprite1.isNewShape()) {
                    sprite1.doNewShape();
                }

                sprite1.resetShapeValues();
                for (final Light sprite2 : this.lights) {
                    sprite2.getBeam1().insideLightBeam(sprite1);
                }
                sprite1.setShownThenHide();
            }
        }
        this.targetShapes = this.targetShapes2;

        // Shape misses
        this.targetShapes2 = (ArrayList<Shape>) this.targetShapes.clone();
        this.findShapes2 = (ArrayList<Shape>) this.findShapes.clone();
        for (final Shape sprite1 : this.targetShapes) {
            boolean missed = false;

            if (!sprite1.isNewShape() && !sprite1.isDrawShape()
                    && !sprite1.isMissedShape()) {
                // Need a separate if (isShownThenHide() sets shownThenHide =
                // false)
                if (sprite1.isShownThenHide()) {
                    for (final Shape sprite2 : this.findShapes) {
                        // drawShape is only set for target shapes in
                        // setShownThenHide()
                        if (!sprite2.isNewShape() && !sprite2.isMissedShape()
                                && (sprite1.getType() == sprite2.getType())
                                && (sprite1.getColour() == sprite2.getColour())) {
                            final int i = this.targetShapes2.indexOf(sprite1);
                            final int j = this.findShapes2.indexOf(sprite2);

                            if ((i > -1) && (j > -1)) {
                                // the number of misses is restricted to
                                // MAX_FIND_SHAPES:
                                // otherwise there is an unlimited number of
                                // misses at one time so
                                // no shapes would be left to create new ones
                                // since all shapes in
                                // the find and target shapes are unique even if
                                // they are misses
                                // shouldn't really happen because new shapes
                                // flash - cannot be
                                // missed - but include just in case
                                int missedShapesCount = 0;
                                for (final Shape sprite3 : this.findShapes2) {
                                    if (sprite3.isMissedShape()) {
                                        missedShapesCount++;
                                    }
                                }

                                if (missedShapesCount < SettingsActivity.MAX_FIND_SHAPES) {
                                    final Shape shape1 = this.targetShapes2
                                            .get(i);
                                    final Shape shape2 = this.findShapes2
                                            .get(j);

                                    shape1.setMissedShape(true);
                                    shape1.setDirection(Sprite.Direction.NONE);
                                    shape2.setMissedShape(true);
                                    shape2.setDirection(Sprite.Direction.NONE);

                                    this.createShape(i, j, this.targetShapes2,
                                            this.findShapes2);
                                    missed = true;
                                    // Must use synchronized because using existing value of
                                    // variable (read) to add one and update (write).  If just
                                    // setting to a number e.g. 10 then synchronized would not be
                                    // needed.
                                    synchronized (this) {
                                        this.missesLeft--;
                                    }
                                    this.soundEffect1
                                            .doSound(SoundEffect.SoundType.MISS);
                                }
                            }
                        }

                        if (missed) {
                            break;
                        }
                    }
                }
            }
        }
        this.targetShapes = this.targetShapes2;
        this.findShapes = this.findShapes2;
    }

    private void doTouch() {
        Shape shape1;
        try {
            shape1 = this.targetShapes.get(0);
        } catch (final Exception e) {
            Log.e(this.TAG, "doTouch() error 1");
            throw new RuntimeException();
        }

        this.targetShapes2 = (ArrayList<Shape>) this.targetShapes.clone();
        this.findShapes2 = (ArrayList<Shape>) this.findShapes.clone();
        this.touchPoints2 = (ArrayList<TouchPoint>) this.touchPoints.clone();

        for (final TouchPoint point1 : this.touchPoints) {
            boolean hit = false;
            final int touchPointX = point1.getTouchX();
            final int touchPointY = point1.getTouchY();

            for (final Shape sprite1 : this.targetShapes) {
                if (sprite1.isDrawShape() && !sprite1.isMissedShape()) {
                    int targetShapeMinX = sprite1.getX();
                    int targetShapeMinY = sprite1.getY();
                    int targetShapeMaxX = (targetShapeMinX + sprite1
                            .getDrawableWidth()) - 1;
                    int targetShapeMaxY = (targetShapeMinY + sprite1
                            .getDrawableHeight()) - 1;

                    final int touchPointToleranceX = (int) ((targetShapeMaxX - targetShapeMinX) *
                            Game.TOUCH_POINT_TOLERANCE_PROPORTION);
                    final int touchPointToleranceY = (int) ((targetShapeMaxY - targetShapeMinY) *
                            Game.TOUCH_POINT_TOLERANCE_PROPORTION);
                    targetShapeMinX -= touchPointToleranceX;
                    targetShapeMaxX += touchPointToleranceX;
                    targetShapeMinY -= touchPointToleranceY;
                    targetShapeMaxY += touchPointToleranceY;

                    if ((touchPointX >= targetShapeMinX)
                            && (touchPointX <= targetShapeMaxX)
                            && (touchPointY >= targetShapeMinY)
                            && (touchPointY <= targetShapeMaxY)) {
                        for (final Shape sprite2 : this.findShapes) {
                            // drawShape is only set for target shapes in
                            // setShownThenHide()
                            if (!sprite2.isMissedShape()
                                    && (sprite1.getType() == sprite2.getType())
                                    && (sprite1.getColour() == sprite2
                                    .getColour())) {
                                final int i = this.targetShapes2
                                        .indexOf(sprite1);
                                final int j = this.findShapes2.indexOf(sprite2);

                                if ((i > -1) && (j > -1)) {
                                    final Shape.SpriteType targetShapeType1 = sprite1
                                            .getType();
                                    final Shape.SpriteColour targetShapeColour1 = sprite1
                                            .getColour();
                                    this.createShape(i, j, this.targetShapes2,
                                            this.findShapes2);

                                    FoundShape foundShape1;
                                    if (targetShapeType1 == Shape.SpriteType.CIRCLE) {
                                        foundShape1 = new FoundShape(
                                                this.myImage4, this.canvas1,
                                                targetShapeType1,
                                                targetShapeColour1);
                                    } else if (targetShapeType1 == Shape.SpriteType.SQUARE) {
                                        foundShape1 = new FoundShape(
                                                this.myImage5, this.canvas1,
                                                targetShapeType1,
                                                targetShapeColour1);
                                    } else if (targetShapeType1 == Shape.SpriteType.TRIANGLE) {
                                        foundShape1 = new FoundShape(
                                                this.myImage6, this.canvas1,
                                                targetShapeType1,
                                                targetShapeColour1);
                                    } else {
                                        Log.e(this.TAG, "doTouch() error 2");
                                        throw new RuntimeException();
                                    }

                                    foundShape1.setMinX(shape1.getMinX());
                                    foundShape1.setMinY(shape1.getMinY());
                                    foundShape1.setX(targetShapeMinX
                                            + touchPointToleranceX);
                                    foundShape1.setY(targetShapeMinY
                                            + touchPointToleranceY);
                                    this.foundShapes.add(foundShape1);

                                    this.soundEffect1
                                            .doSound(SoundEffect.SoundType.HIT);

                                    hit = true;
                                }
                            }

                            if (hit) {
                                break;
                            }
                        }
                    }
                }
            }

            if (!hit) {
                // Must use synchronized because using existing value of variable (read) to add one
                // and update (write).  If just setting to a number e.g. 10 then synchronized would
                // not be needed.
                synchronized (this) {
                    this.missesLeft--;
                }

                final MissCross missCross = new MissCross(this.myImage7,
                        this.canvas1);
                missCross.setMinX(shape1.getMinX());
                missCross.setMinY(shape1.getMinY());
                missCross
                        .setX(touchPointX - (missCross.getDrawableWidth() / 2));
                missCross.setY(touchPointY
                        - (missCross.getDrawableHeight() / 2));
                this.missCrosses.add(missCross);

                this.soundEffect1.doSound(SoundEffect.SoundType.MISS);
            }

            // Remove touch point
            final int i = this.touchPoints2.indexOf(point1);
            if (i > -1) {
                this.touchPoints2.remove(i);
            }
        }

        this.targetShapes = this.targetShapes2;
        this.findShapes = this.findShapes2;
        this.touchPoints = this.touchPoints2;
    }

    private void drawBorder() {
        final Rect r = this.canvas1.getClipBounds();
        r.set(0, 0, r.width(), r.height());
        this.canvas1.drawRect(r, this.paintBorder);

        // Line under find shapes
        Shape shape1;
        try {
            shape1 = this.targetShapes.get(0);
        } catch (final Exception e) {
            Log.e(this.TAG, "drawBorder() error 1");
            throw new RuntimeException();
        }

        r.set(0, 0, r.width(), shape1.getMinY() - this.canvasPadding);
        this.canvas1.drawRect(r, this.paintBorder);
    }

    private void drawInitialize() {
        synchronized (this) {
            // Main thread.
            BasicApplication.APP_EXECUTORS.getMainThreadExecutor().execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            final WeakReference<GameActivity> gameActivityWeakReference =
                                    new WeakReference<>(Game.this.gameActivity1);
                            // Get a reference to the activity if it is still there.
                            final GameActivity gameActivity = gameActivityWeakReference.get();
                            if (gameActivity == null || gameActivity.isFinishing()) {
                                return;
                            }

                            final StringBuilder stringBuilder = new StringBuilder(
                                    gameActivity.getString(R.string.misses));
                            stringBuilder.append(" : ");
                            stringBuilder.append(Game.this.missesLeft);
                            TextView textView = Game.this.gameActivity1.getBinding().missesTextView;
                            textView.setText(stringBuilder);
                        }
                    }
            );
        }

        // Background
        if (this.bitmap1 == null) {
            this.bitmap1 = Bitmap.createBitmap(this.canvasWidth,
                    this.canvasHeight, Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(this.bitmap1);

            // Background
            canvas.drawRect(0.0F, 0.0F, this.canvasWidth, this.canvasHeight,
                    this.paintBackground);
        }
        this.canvas1.drawBitmap(this.bitmap1, 0.0F, 0.0F, this.paintBitmap);
    }

    private void drawSprites() {
        for (final Shape sprite1 : this.findShapes) {
            sprite1.drawShape(this.canvas1, this.paintSprite);
        }

        for (final Light sprite1 : this.lights) {
            this.canvas1.drawBitmap(sprite1.getASprite(), sprite1.getX(),
                    sprite1.getY(), this.paintSprite);
        }

        boolean moveLightBeam = false;
        if ((this.lightMoveCount >= this.currentSpeedNumber)
                && !this.isPaused()) {
            moveLightBeam = true;
        }
        for (final Light sprite1 : this.lights) {
            if (this.frameRate != 0L) {
                final int lightBeamPixelsToMove = sprite1.getBeam1()
                        .getPixelsToMove();
                final int lightBeamMaximumMoveStep = lightBeamPixelsToMove
                        / Game.MOVE_STEP_MAXIMUM_PROPORTION;
                final float lightBeamFrameCycles = (((float) Game.PIXELS_TO_MOVE_TIME_LIGHT_BEAM)
                        / ((float) this.frameRate));
                int lightBeamMoveStep = Math.round(lightBeamPixelsToMove
                        / lightBeamFrameCycles);
                if (lightBeamMoveStep < 0) {
                    lightBeamMoveStep = 1;
                } else if (lightBeamMoveStep > lightBeamMaximumMoveStep) {
                    lightBeamMoveStep = lightBeamMaximumMoveStep;
                }
                sprite1.getBeam1().setBorderBeamMoveStep(lightBeamMoveStep);
            }
            sprite1.getBeam1().doBeam(this.canvas1, moveLightBeam);
        }

        for (final FoundShape sprite1 : this.foundShapes) {
            this.canvas1.drawBitmap(sprite1.getASprite(), sprite1.getX(),
                    sprite1.getY(), this.paintSprite);
        }

        for (final Shape sprite1 : this.targetShapes) {
            if (sprite1.isDrawShape() || sprite1.isMissedShape()) {
                sprite1.drawShape(this.canvas1, this.paintSprite);
            }
        }

        for (final MissCross sprite1 : this.missCrosses) {
            this.canvas1.drawBitmap(sprite1.getASprite(), sprite1.getX(),
                    sprite1.getY(), this.paintSprite);
        }
    }

    private void gameOver() {
        if (this.missesLeft <= 0) {
            this.timeMeter.stop();

            this.stringScore = this.timeMeter.getText().toString();
            // Remove all non-digit characters.
            final String str1 = this.stringScore.replaceAll("\\D","");
            this.longScore = Long.parseLong(str1);
        }
    }

    // default package access
    long getLongScore() {
        return this.longScore;
    }

    public int getMissesLeft() {
        return this.missesLeft;
    }

    public SoundEffect getSoundEffect1() {
        return this.soundEffect1;
    }

    // default package access
    String getStringScore() {
        return this.stringScore;
    }

    public boolean isPaused() {
        return this.paused;
    }

    private void moveTargetShapes() {
        for (final Shape sprite1 : this.targetShapes) {
            if (this.moveShapes
                    && (this.lightMoveCount >= this.currentSpeedNumber)) {
                this.doMoveStep(sprite1, sprite1.getPixelsToMove(),
                        Game.PIXELS_TO_MOVE_TIME_SHAPE);
            }
        }
    }

    public void releaseResources() {
        if (this.validNewTargetShapes != null) {
            this.validNewTargetShapes.clear();
            this.validNewTargetShapes = null;
        }
        if (this.validNewFindShapes != null) {
            this.validNewFindShapes.clear();
            this.validNewFindShapes = null;
        }

        if (this.lights != null) {
            for (final Light sprite1 : this.lights) {
                sprite1.releaseResources();
            }
            this.lights.clear();
            this.lights = null;
        }
        if (this.targetShapes != null) {
            for (final Shape sprite1 : this.targetShapes) {
                sprite1.releaseResources();
            }
            this.targetShapes.clear();
            this.targetShapes = null;
        }
        if (this.findShapes != null) {
            for (final Shape sprite1 : this.findShapes) {
                sprite1.releaseResources();
            }
            this.findShapes.clear();
            this.findShapes = null;
        }
        if (this.touchPoints != null) {
            this.touchPoints.clear();
            this.touchPoints = null;
        }
        if (this.foundShapes != null) {
            for (final FoundShape sprite1 : this.foundShapes) {
                sprite1.releaseResources();
            }
            this.foundShapes.clear();
            this.foundShapes = null;
        }
        if (this.missCrosses != null) {
            for (final MissCross sprite1 : this.missCrosses) {
                sprite1.releaseResources();
            }
            this.missCrosses.clear();
            this.missCrosses = null;
        }

        if (this.targetShapes2 != null) {
            for (final Shape sprite1 : this.targetShapes2) {
                sprite1.releaseResources();
            }
            this.targetShapes2.clear();
            this.targetShapes2 = null;
        }
        if (this.findShapes2 != null) {
            for (final Shape sprite1 : this.findShapes2) {
                sprite1.releaseResources();
            }
            this.findShapes2.clear();
            this.findShapes2 = null;
        }
        if (this.touchPoints2 != null) {
            this.touchPoints2.clear();
            this.touchPoints2 = null;
        }
        if (this.foundShapes2 != null) {
            for (final FoundShape sprite1 : this.foundShapes2) {
                sprite1.releaseResources();
            }
            this.foundShapes2.clear();
            this.foundShapes2 = null;
        }
        if (this.missCrosses2 != null) {
            for (final MissCross sprite1 : this.missCrosses2) {
                sprite1.releaseResources();
            }
            this.missCrosses2.clear();
            this.missCrosses2 = null;
        }

        // do not set strings to "" but just clear reference
        // setting to "" may effect save bundles because it
        // has a reference to the same string
        this.stringScore = null;

        if (this.mediaPlayerTypes != null) {
            this.mediaPlayerTypes.clear();
            this.mediaPlayerTypes = null;
        }

        if (this.myImage1 != null) {
            this.myImage1.clear();
            this.myImage1 = null;
        }
        if (this.myImage2 != null) {
            this.myImage2.clear();
            this.myImage2 = null;
        }
        if (this.myImage3 != null) {
            this.myImage3.clear();
            this.myImage3 = null;
        }
        if (this.myImage4 != null) {
            this.myImage4.clear();
            this.myImage4 = null;
        }
        if (this.myImage5 != null) {
            this.myImage5.clear();
            this.myImage5 = null;
        }
        if (this.myImage6 != null) {
            this.myImage6.clear();
            this.myImage6 = null;
        }
        if (this.myImage7 != null) {
            this.myImage7.clear();
            this.myImage7 = null;
        }
        if (this.myImage8 != null) {
            this.myImage8.clear();
            this.myImage8 = null;
        }

        this.gameActivity1 = null;
        this.mainView1 = null;
        this.canvas1 = null;
        this.bitmap1 = null;

        this.soundEffect1 = null;
        this.res = null;
        this.timeMeter = null;
        this.paintBitmap = null;
        this.paintBackground = null;
        this.paintBorder = null;
        this.paintSprite = null;
    }

    public void resetGame(final GameActivity activity, final Canvas canvas,
                          final int[] settings) {
        this.gameActivity1 = activity;
        this.canvas1 = canvas;

        this.canvasWidth = this.canvas1.getWidth();
        this.canvasHeight = this.canvas1.getHeight();

        this.doConstructorInitialize(false, settings);
    }

    private void setChronometer() {
        // set base time back from the current time
        // (SystemClock.elapsedRealtime()) by
        // the difference between the last paused time (pausedTime) and the last
        // chronometer time base (timeMeterBase)
        synchronized (this) {
            this.timeMeterBase = SystemClock.elapsedRealtime() -
                    (this.pausedTime - this.timeMeterBase);
        }
        this.timeMeter.setBase(this.timeMeterBase);
    }

    public void setPaused() {
        synchronized (this) {
            this.paused = !this.paused;
        }

        this.systemClockElapsedRealtime = 0L;
        this.frameRate = 0L;

        if (this.paused) {
            // save paused time - time since phone switched on:
            // SystemClock.elapsedRealtime()
            this.pausedTime = SystemClock.elapsedRealtime();
            this.timeMeter.stop();
        } else {
            this.setChronometer();
            this.timeMeter.start();
        }
    }

    private void setSoundEffect1() {
        // must reset mediaPlayerTypes because arrays are passed by reference
        // so SoundEffect.mediaPlayersRelease() is called by
        // GameActivity.onStop()
        // which clears mediaPlayerTypes1
        // this method is called by GameActivity.onRestart()

        this.mediaPlayerTypes = new ArrayList<>();
        this.mediaPlayerTypes.add(SoundEffect.SoundType.HIT);
        this.mediaPlayerTypes.add(SoundEffect.SoundType.MISS);

        this.soundEffect1 = new SoundEffect(
                this.gameActivity1.getApplicationContext(),
                this.mediaPlayerTypes, null);
    }

    private String[] setTypeAndColour(final int typeColour) {
        String type;
        String colour;

        switch (typeColour) {
            case 0:
                type = "CIRCLE";
                colour = "BLUE";
                break;
            case 1:
                type = "SQUARE";
                colour = "BLUE";
                break;
            case 2:
                type = "TRIANGLE";
                colour = "BLUE";
                break;
            case 3:
                type = "CIRCLE";
                colour = "BROWN";
                break;
            case 4:
                type = "SQUARE";
                colour = "BROWN";
                break;
            case 5:
                type = "TRIANGLE";
                colour = "BROWN";
                break;
            case 6:
                type = "CIRCLE";
                colour = "CYAN";
                break;
            case 7:
                type = "SQUARE";
                colour = "CYAN";
                break;
            case 8:
                type = "TRIANGLE";
                colour = "CYAN";
                break;
            case 9:
                type = "CIRCLE";
                colour = "GREY";
                break;
            case 10:
                type = "SQUARE";
                colour = "GREY";
                break;
            case 11:
                type = "TRIANGLE";
                colour = "GREY";
                break;
            case 12:
                type = "CIRCLE";
                colour = "MAGENTA";
                break;
            case 13:
                type = "SQUARE";
                colour = "MAGENTA";
                break;
            case 14:
                type = "TRIANGLE";
                colour = "MAGENTA";
                break;
            case 15:
                type = "CIRCLE";
                colour = "ORANGE";
                break;
            case 16:
                type = "SQUARE";
                colour = "ORANGE";
                break;
            case 17:
                type = "TRIANGLE";
                colour = "ORANGE";
                break;
            case 18:
                type = "CIRCLE";
                colour = "PINK";
                break;
            case 19:
                type = "SQUARE";
                colour = "PINK";
                break;
            case 20:
                type = "TRIANGLE";
                colour = "PINK";
                break;
            case 21:
                type = "CIRCLE";
                colour = "RED";
                break;
            case 22:
                type = "SQUARE";
                colour = "RED";
                break;
            case 23:
                type = "TRIANGLE";
                colour = "RED";
                break;
            case 24:
                type = "CIRCLE";
                colour = "YELLOW";
                break;
            case 25:
                type = "SQUARE";
                colour = "YELLOW";
                break;
            case 26:
                type = "TRIANGLE";
                colour = "YELLOW";
                break;
            default:
                Log.e(this.TAG, "setTypeAndColour() error 1");
                throw new RuntimeException();
        }

        return new String[]{type, colour};
    }

    public Bundle writeToBundle() {
        // Don't use 'counter' as variable name as it increase with each line
        // for some reason!
        int cnt = 0;
        final boolean[] booleanArray1 = {this.paused, this.moveShapes};
        final int[] intArray1 = {this.lightMoveCount, this.missesLeft};
        final long[] longArray1 = {this.timeMeterBase, this.pausedTime,
                this.longScore};
        final Bundle bundle1 = new Bundle();

        bundle1.putBooleanArray(Game.GAME_BUNDLE + cnt++,
                booleanArray1);
        bundle1.putIntArray(Game.GAME_BUNDLE + cnt++,
                intArray1);
        bundle1.putLongArray(Game.GAME_BUNDLE + cnt++,
                longArray1);
        bundle1.putString(Game.GAME_BUNDLE + cnt++,
                this.stringScore);

        // Arrays are passed by reference so the code in releaseResources()
        // would
        // clear the arrays in the bundle if clone() wasn't used
        bundle1.putIntegerArrayList(Game.GAME_BUNDLE + cnt++,
                (ArrayList<Integer>) this.validNewTargetShapes.clone());
        bundle1.putIntegerArrayList(Game.GAME_BUNDLE + cnt,
                (ArrayList<Integer>) this.validNewFindShapes.clone());

        cnt = 0;
        for (final Light sprite1 : this.lights) {
            bundle1.putBundle(Game.LIGHTS_BUNDLE + cnt++,
                    sprite1.writeToBundle(new Bundle()));
        }

        cnt = 0;
        for (final Shape sprite1 : this.targetShapes) {
            bundle1.putBundle(
                    Game.TARGET_SHAPES_BUNDLE + cnt++,
                    sprite1.writeToBundle(new Bundle()));
        }

        cnt = 0;
        for (final Shape sprite1 : this.findShapes) {
            bundle1.putBundle(
                    Game.FIND_SHAPES_BUNDLE + cnt++,
                    sprite1.writeToBundle(new Bundle()));
        }

        cnt = 0;
        for (final TouchPoint sprite1 : this.touchPoints) {
            bundle1.putBundle(
                    Game.TOUCH_POINTS_BUNDLE + cnt++,
                    sprite1.writeToBundle(new Bundle()));
        }

        cnt = 0;
        for (final FoundShape sprite1 : this.foundShapes) {
            bundle1.putBundle(
                    Game.FOUND_SHAPES_BUNDLE + cnt++,
                    sprite1.writeToBundle(new Bundle()));
        }

        cnt = 0;
        for (final MissCross sprite1 : this.missCrosses) {
            bundle1.putBundle(
                    Game.MISS_CROSSES_BUNDLE + cnt++,
                    sprite1.writeToBundle(new Bundle()));
        }

        return bundle1;
    }
}
