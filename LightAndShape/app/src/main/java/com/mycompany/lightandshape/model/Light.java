/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.mycompany.lightandshape.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Colin Walters
 * @version 1.0, 24/05/2023
 */
// default package access
final class Light extends Sprite {
    private static final String LIGHT_BUNDLE = "LIGHT_BUNDLE_";
    private int currentAnglesNumber = 1;
    private Beam beam1;

    // default package access
    Light(final ArrayList<Drawable> image, final Canvas canvas, final int x1,
          final int y1, final int currentAnglesNumber1) {
        super(image, canvas, 0.0F, 0.0F);

        this.currentAnglesNumber = currentAnglesNumber1;

        this.setMinY((3 * this.getCanvasPadding()) + this.getDrawableHeight());
        this.setX(x1 - (this.getDrawableWidth() / 2));
        this.setY(y1 - (this.getDrawableHeight() / 2));
        this.setDirection(Sprite.Direction.NONE);

        this.beam1 = new Beam(this, null, null);
    }

    // default package access
    Light(final Bundle bundle1) {
        super(bundle1);

        // Don't use 'counter' as variable name as it increase with each line
        // for some reason!
        int cnt = 0;

        boolean[] booleanArray1 = bundle1.getBooleanArray(Light.LIGHT_BUNDLE +
                cnt++);

        int[] intArray1 = bundle1.getIntArray(Light.LIGHT_BUNDLE +
                cnt);

        this.beam1 = new Beam(this, booleanArray1, intArray1);
    }

    // default package access
    Beam getBeam1() {
        return this.beam1;
    }

    @Override
        // default package access
    void releaseResources() {
        super.releaseResources();

        this.beam1.releaseResources();
        this.beam1 = null;
    }

    @Override
        // default package access
    Bundle writeToBundle(final Bundle bundle1) {
        super.writeToBundle(bundle1);

        // Don't use 'counter' as variable name as it increase with each line
        // for some reason!
        int cnt = 0;
        final boolean[] booleanArray1 = {this.beam1.directionClockwise,
                this.beam1.rotationDirectionChange};
        final int[] intArray1 = {this.currentAnglesNumber,
                this.beam1.borderPoint1Start, this.beam1.borderPoint1StartX,
                this.beam1.borderPoint1StartY, this.beam1.borderPoint1,
                this.beam1.borderPoint1X, this.beam1.borderPoint1Y};

        bundle1.putBooleanArray(Light.LIGHT_BUNDLE + cnt++,
                booleanArray1);
        bundle1.putIntArray(Light.LIGHT_BUNDLE + cnt,
                intArray1);

        return bundle1;
    }

    // default package access
    final class Beam {
        private static final int LIGHT_BEAM_COLOUR = Color.WHITE;
        private final String TAG = this.getClass().getSimpleName();

        private Light light1;
        private Random random1 = new Random();
        private Paint paintBeam1 = new Paint();

        private final int sourceX;
        private final int sourceY;
        private final int borderBeamWidth;
        private int borderBeamMoveStep;
        private final int borderMinX;
        private final int borderMinY;
        private final int borderMaxX;
        private final int borderMaxY;
        private final int borderWidth;
        private final int borderHeight;

        private int borderPoint2 = 0;
        private int borderPoint2X = 0;
        private int borderPoint2Y = 0;

        private Path path1 = new Path();

        // These must be floats - rounding errors with ints
        private float pointX = 0.0F;
        private float lengthX = 0.0F;
        private float lengthY = 0.0F;
        private float lineY = 0.0F;
        private float shapeX = 0.0F;
        private float shapeY = 0.0F;

        // bundle start
        private boolean directionClockwise = true;
        private boolean rotationDirectionChange = false;
        private final int borderPoint1Start;
        private final int borderPoint1StartX;
        private final int borderPoint1StartY;
        private int borderPoint1;
        private int borderPoint1X;
        private int borderPoint1Y;
        // bundle end

        // no need to put this in the bundle because
        // one constructor is used for new and restore of the light-beam
        private final int pixelsToMove;

        // default package access
        Beam(final Light l1, final boolean[] booleanBundleArray1,
             final int[] intBundleArray1) {
            super();

            this.light1 = l1;

            this.borderMinX = this.light1.getMinX();
            this.borderMinY = this.light1.getMinY();
            this.borderMaxX = this.light1.getMaxX()
                    + this.light1.getDrawableWidth();
            this.borderMaxY = this.light1.getMaxY()
                    + this.light1.getDrawableHeight();
            this.borderWidth = this.borderMaxX - this.borderMinX;
            this.borderHeight = this.borderMaxY - this.borderMinY;

            this.sourceX = this.light1.getX()
                    + (this.light1.getDrawableWidth() / 2);
            this.sourceY = this.light1.getY()
                    + (this.light1.getDrawableHeight() / 2);

            if (intBundleArray1 == null) {
                this.borderPoint1 = this.random1.nextInt(4);
                this.borderPoint1Start = this.borderPoint1;

                switch (this.borderPoint1) {
                    case 0:
                        // top
                        this.borderPoint1X = this.borderMinX
                                + this.random1.nextInt(this.borderWidth);
                        this.borderPoint1Y = this.borderMinY;
                        break;
                    case 1:
                        // right
                        this.borderPoint1X = this.borderMaxX;
                        this.borderPoint1Y = this.borderMinY
                                + this.random1.nextInt(this.borderHeight);
                        break;
                    case 2:
                        // bottom
                        this.borderPoint1X = this.borderMinX
                                + this.random1.nextInt(this.borderWidth);
                        this.borderPoint1Y = this.borderMaxY;
                        break;
                    case 3:
                        // left
                        this.borderPoint1X = this.borderMinX;
                        this.borderPoint1Y = this.borderMinY
                                + this.random1.nextInt(this.borderHeight);
                        break;
                    default:
                        Log.e(this.TAG, "Beam() error 1");
                        throw new RuntimeException();
                }

                this.borderPoint1StartX = this.borderPoint1X;
                this.borderPoint1StartY = this.borderPoint1Y;

                this.setDirection();
            } else {
                this.directionClockwise = booleanBundleArray1[0];
                this.rotationDirectionChange = booleanBundleArray1[1];

                Light.this.currentAnglesNumber = intBundleArray1[0];
                this.borderPoint1Start = intBundleArray1[1];
                this.borderPoint1StartX = intBundleArray1[2];
                this.borderPoint1StartY = intBundleArray1[3];
                this.borderPoint1 = intBundleArray1[4];
                this.borderPoint1X = intBundleArray1[5];
                this.borderPoint1Y = intBundleArray1[6];
            }

            int length = Math.min(this.borderWidth, this.borderHeight);

            this.borderBeamWidth = (int) (length * (Light.this.currentAnglesNumber / 3.0F));

            this.borderBeamMoveStep = length / 100;
            if (this.borderBeamMoveStep < 0) {
                this.borderBeamMoveStep = 5;
            }
            this.pixelsToMove = length;

            this.doPoint2();

            this.paintBeam1.setColor(Beam.LIGHT_BEAM_COLOUR);
        }

        private boolean checkPointIsInsideBeam(final int realX, final int realY) {
            boolean insideBeam1 = false;
            boolean insideBeam2 = false;

            this.lineY = 0.0F;

            final int shapePointSector = this.findPointSector(realX, realY);
            final int point1Sector = this.findPointSector(this.borderPoint1X,
                    this.borderPoint1Y);
            final int point2Sector = this.findPointSector(this.borderPoint2X,
                    this.borderPoint2Y);

            boolean pointSectorInBeam = false;
            int nextSector = point2Sector + 1;
            if (nextSector > 4) {
                nextSector = 1;
            }

            // can't use for loop here: ++i > 4 condition
            int i = point1Sector;
            while (i != nextSector) {
                if (i == shapePointSector) {
                    pointSectorInBeam = true;
                }

                if (++i > 4) {
                    i = 1;
                }
            }

            // point is outside of the two end beam sectors
            if (!pointSectorInBeam) {
                return false;
            }

            // point is not in either of the two end beam sectors so must be in
            // between
            // don't use > and < here but != : sector might be 4 in between 3
            // and 1
            if ((shapePointSector != point1Sector)
                    && (shapePointSector != point2Sector)) {
                return true;
            }

            // shape must be in one or the other beam end sectors or both by
            // this stage
            // point 1
            if (shapePointSector == point1Sector) {
                this.doPointConversions(realX, realY, this.borderPoint1,
                        this.borderPoint1X, this.borderPoint1Y);
                if (this.pointX < 0.0F) {
                    if ((this.shapeX <= 0.0F) && (this.shapeY >= 0.0F)) {
                        this.lineY = -(this.lengthY / this.lengthX)
                                * this.shapeX;
                        if (this.shapeY >= this.lineY) {
                            insideBeam1 = true;
                        }
                    }
                } else if (this.pointX > 0.0F) {
                    if ((this.shapeX >= 0.0F) && (this.shapeY >= 0.0F)) {
                        this.lineY = (this.lengthY / this.lengthX)
                                * this.shapeX;
                        if (this.shapeY <= this.lineY) {
                            insideBeam1 = true;
                        }
                    }
                } else {
                    if ((this.shapeX >= 0.0F) && (this.shapeY >= 0.0F)) {
                        insideBeam1 = true;
                    }
                }
            }

            // point 2
            if (shapePointSector == point2Sector) {
                this.doPointConversions(realX, realY, this.borderPoint2,
                        this.borderPoint2X, this.borderPoint2Y);
                if (this.pointX < 0.0F) {
                    if ((this.shapeX <= 0.0F) && (this.shapeY >= 0.0F)) {
                        this.lineY = -(this.lengthY / this.lengthX)
                                * this.shapeX;
                        if (this.shapeY <= this.lineY) {
                            insideBeam2 = true;
                        }
                    }
                } else if (this.pointX > 0.0F) {
                    if ((this.shapeX >= 0.0F) && (this.shapeY >= 0.0F)) {
                        this.lineY = (this.lengthY / this.lengthX)
                                * this.shapeX;
                        if (this.shapeY >= this.lineY) {
                            insideBeam2 = true;
                        }
                    }
                } else {
                    if ((this.shapeX <= 0.0F) && (this.shapeY >= 0.0F)) {
                        insideBeam2 = true;
                    }
                }
            }

            if (shapePointSector == point1Sector) {
                if (shapePointSector == point2Sector) {
                    return insideBeam1 && insideBeam2;
                } else {
                    return insideBeam1;
                }
            } else {
                //noinspection ConstantConditions
                if (shapePointSector == point2Sector) {
                    return insideBeam2;
                } else {
                    Log.e(this.TAG, "checkPointIsInsideBeam() error 1");
                    throw new RuntimeException();
                }
            }
        }

        // default package access
        void doBeam(final Canvas canvas1, final boolean moveLightBeam) {
            if (moveLightBeam) {
                this.moveBeam();
            }
            this.drawBeam(canvas1);
            this.setRotateDirection();
        }

        private void doPoint2() {
            this.borderPoint2 = this.borderPoint1;
            this.borderPoint2X = 0;
            this.borderPoint2Y = 0;
            int borderLengthRemains;

            this.path1.reset();
            this.path1.moveTo(this.sourceX, this.sourceY);
            this.path1.lineTo(this.borderPoint1X, this.borderPoint1Y);

            switch (this.borderPoint2) {
                case 0:
                    // top
                    borderLengthRemains = (this.borderPoint1X - this.borderMinX)
                            + this.borderBeamWidth;
                    break;
                case 1:
                    // right
                    borderLengthRemains = (this.borderPoint1Y - this.borderMinY)
                            + this.borderBeamWidth;
                    break;
                case 2:
                    // bottom
                    borderLengthRemains = (this.borderMaxX - this.borderPoint1X)
                            + this.borderBeamWidth;
                    break;
                case 3:
                    // left
                    borderLengthRemains = (this.borderMaxY - this.borderPoint1Y)
                            + this.borderBeamWidth;
                    break;
                default:
                    Log.e(this.TAG, "doPoint2() error 1");
                    throw new RuntimeException();
            }

            do {
                switch (this.borderPoint2) {
                    case 0:
                        // top
                        borderLengthRemains -= this.borderWidth;
                        if (borderLengthRemains > 0) {
                            this.borderPoint2 = 1;
                            this.path1.lineTo(this.borderMaxX, this.borderMinY);
                        }
                        break;
                    case 1:
                        // right
                        borderLengthRemains -= this.borderHeight;
                        if (borderLengthRemains > 0) {
                            this.borderPoint2 = 2;
                            this.path1.lineTo(this.borderMaxX, this.borderMaxY);
                        }
                        break;
                    case 2:
                        // bottom
                        borderLengthRemains -= this.borderWidth;
                        if (borderLengthRemains > 0) {
                            this.borderPoint2 = 3;
                            this.path1.lineTo(this.borderMinX, this.borderMaxY);
                        }
                        break;
                    case 3:
                        // left
                        borderLengthRemains -= this.borderHeight;
                        if (borderLengthRemains > 0) {
                            this.borderPoint2 = 0;
                            this.path1.lineTo(this.borderMinX, this.borderMinY);
                        }
                        break;
                    default:
                        Log.e(this.TAG, "doPoint2() error 2");
                        throw new RuntimeException();
                }
            } while (borderLengthRemains > 0);

            switch (this.borderPoint2) {
                case 0:
                    // top
                    this.borderPoint2X = this.borderMinX
                            + (this.borderWidth + borderLengthRemains);
                    this.borderPoint2Y = this.borderMinY;
                    break;
                case 1:
                    // right
                    this.borderPoint2X = this.borderMaxX;
                    this.borderPoint2Y = this.borderMinY
                            + (this.borderHeight + borderLengthRemains);
                    break;
                case 2:
                    // bottom
                    this.borderPoint2X = this.borderMaxX
                            - (this.borderWidth + borderLengthRemains);
                    this.borderPoint2Y = this.borderMaxY;
                    break;
                case 3:
                    // left
                    this.borderPoint2X = this.borderMinX;
                    this.borderPoint2Y = this.borderMaxY
                            - (this.borderHeight + borderLengthRemains);
                    break;
                default:
                    Log.e(this.TAG, "doPoint2() error 3");
                    throw new RuntimeException();
            }

            this.path1.lineTo(this.borderPoint2X, this.borderPoint2Y);
            this.path1.lineTo(this.sourceX, this.sourceY);
        }

        // make parameters floats not ints to avoid rounding errors
        private void doPointConversions(final float realX, final float realY,
                                        final int borderPoint, final float borderPointX,
                                        final float borderPointY) {

            this.pointX = 0.0F;
            this.lengthX = 0.0F;
            this.lengthY = 0.0F;
            this.shapeX = 0.0F;
            this.shapeY = 0.0F;

            // point 1
            switch (borderPoint) {
                case 0:
                    // top
                    if (borderPointX < this.sourceX) {
                        this.lengthX = this.sourceX - borderPointX;
                        this.pointX = -this.lengthX;
                    } else if (borderPointX > this.sourceX) {
                        this.lengthX = borderPointX - this.sourceX;
                        this.pointX = this.lengthX;
                    } else {
                        this.lengthX = 0;
                        this.pointX = this.lengthX;
                    }
                    this.lengthY = this.sourceY - borderPointY;
                    this.shapeX = realX - this.sourceX;
                    this.shapeY = this.sourceY - realY;
                    break;
                case 1:
                    // right
                    if (borderPointY < this.sourceY) {
                        this.lengthX = this.sourceY - borderPointY;
                        this.pointX = -this.lengthX;
                    } else if (borderPointY > this.sourceY) {
                        this.lengthX = borderPointY - this.sourceY;
                        this.pointX = this.lengthX;
                    } else {
                        this.lengthX = 0;
                        this.pointX = this.lengthX;
                    }
                    this.lengthY = borderPointX - this.sourceX;
                    this.shapeX = realY - this.sourceY;
                    this.shapeY = realX - this.sourceX;
                    break;
                case 2:
                    // bottom
                    if (borderPointX > this.sourceX) {
                        this.lengthX = borderPointX - this.sourceX;
                        this.pointX = -this.lengthX;
                    } else if (borderPointX < this.sourceX) {
                        this.lengthX = this.sourceX - borderPointX;
                        this.pointX = this.lengthX;
                    } else {
                        this.lengthX = 0;
                        this.pointX = this.lengthX;
                    }
                    this.lengthY = borderPointY - this.sourceY;
                    this.shapeX = this.sourceX - realX;
                    this.shapeY = realY - this.sourceY;
                    break;
                case 3:
                    // left
                    if (borderPointY > this.sourceY) {
                        this.lengthX = borderPointY - this.sourceY;
                        this.pointX = -this.lengthX;
                    } else if (borderPointY < this.sourceY) {
                        this.lengthX = this.sourceY - borderPointY;
                        this.pointX = this.lengthX;
                    } else {
                        this.lengthX = 0;
                        this.pointX = this.lengthX;
                    }
                    this.lengthY = this.sourceX - borderPointX;
                    this.shapeX = this.sourceY - realY;
                    this.shapeY = this.sourceX - realX;
                    break;
                default:
                    Log.e(this.TAG, "doPointConversions() error 1");
                    throw new RuntimeException();
            }
        }

        private void drawBeam(final Canvas canvas1) {
            canvas1.save();
            canvas1.clipPath(this.path1);
            canvas1.drawPaint(this.paintBeam1);
            canvas1.restore();
        }

        private int findPointSector(final int pointX, final int pointY) {
            int pointSector;

            if (pointX <= this.sourceX) {
                if (pointY <= this.sourceY) {
                    pointSector = 1;
                } else {
                    pointSector = 4;
                }
            } else {
                if (pointY <= this.sourceY) {
                    pointSector = 2;
                } else {
                    pointSector = 3;
                }
            }

            return pointSector;
        }

        // default package access
        int getPixelsToMove() {
            return this.pixelsToMove;
        }

        // default package access
        void insideLightBeam(final Shape shape1) {
            boolean point1InsideBeam = false;
            boolean point2InsideBeam = false;
            boolean point3InsideBeam = false;
            boolean point4InsideBeam = false;

            if (this.checkPointIsInsideBeam(shape1.getX(), shape1.getY())) {
                point1InsideBeam = true;
            }
            if (this.checkPointIsInsideBeam(
                    (shape1.getX() + shape1.getDrawableWidth()) - 1,
                    shape1.getY())) {
                point2InsideBeam = true;
            }
            if (this.checkPointIsInsideBeam(shape1.getX(),
                    (shape1.getY() + shape1.getDrawableHeight()) - 1)) {
                point3InsideBeam = true;
            }
            if (this.checkPointIsInsideBeam(
                    (shape1.getX() + shape1.getDrawableWidth()) - 1,
                    (shape1.getY() + shape1.getDrawableHeight()) - 1)) {
                point4InsideBeam = true;
            }

            shape1.doCorners(point1InsideBeam, point2InsideBeam,
                    point3InsideBeam, point4InsideBeam);
        }

        private void moveBeam() {
            switch (this.borderPoint1) {
                case 0:
                    // top
                    if (this.directionClockwise) {
                        this.borderPoint1X += this.borderBeamMoveStep;
                        if (this.borderPoint1X > this.borderMaxX) {
                            this.borderPoint1X = this.borderMaxX;
                            this.borderPoint1++;
                            this.rotationDirectionChange = true;
                            this.borderPoint1Y = this.borderMinY + 1;
                        }
                    } else {
                        this.borderPoint1X -= this.borderBeamMoveStep;
                        if (this.borderPoint1X < this.borderMinX) {
                            this.borderPoint1X = this.borderMinX;
                            this.borderPoint1--;
                            this.rotationDirectionChange = true;
                            this.borderPoint1Y = this.borderMinY + 1;
                        }
                    }
                    break;
                case 1:
                    // right
                    if (this.directionClockwise) {
                        this.borderPoint1Y += this.borderBeamMoveStep;
                        if (this.borderPoint1Y > this.borderMaxY) {
                            this.borderPoint1Y = this.borderMaxY;
                            this.borderPoint1++;
                            this.rotationDirectionChange = true;
                            this.borderPoint1X = this.borderMaxX - 1;
                        }
                    } else {
                        this.borderPoint1Y -= this.borderBeamMoveStep;
                        if (this.borderPoint1Y < this.borderMinY) {
                            this.borderPoint1Y = this.borderMinY;
                            this.borderPoint1--;
                            this.rotationDirectionChange = true;
                            this.borderPoint1X = this.borderMaxX - 1;
                        }
                    }
                    break;
                case 2:
                    // bottom
                    if (this.directionClockwise) {
                        this.borderPoint1X -= this.borderBeamMoveStep;
                        if (this.borderPoint1X < this.borderMinX) {
                            this.borderPoint1X = this.borderMinX;
                            this.borderPoint1++;
                            this.rotationDirectionChange = true;
                            this.borderPoint1Y = this.borderMaxY - 1;
                        }
                    } else {
                        this.borderPoint1X += this.borderBeamMoveStep;
                        if (this.borderPoint1X > this.borderMaxX) {
                            this.borderPoint1X = this.borderMaxX;
                            this.borderPoint1--;
                            this.rotationDirectionChange = true;
                            this.borderPoint1Y = this.borderMaxY - 1;
                        }
                    }
                    break;
                case 3:
                    // left
                    if (this.directionClockwise) {
                        this.borderPoint1Y -= this.borderBeamMoveStep;
                        if (this.borderPoint1Y < this.borderMinY) {
                            this.borderPoint1Y = this.borderMinY;
                            this.borderPoint1++;
                            this.rotationDirectionChange = true;
                            this.borderPoint1X = this.borderMinX + 1;
                        }
                    } else {
                        this.borderPoint1Y += this.borderBeamMoveStep;
                        if (this.borderPoint1Y > this.borderMaxY) {
                            this.borderPoint1Y = this.borderMaxY;
                            this.borderPoint1--;
                            this.rotationDirectionChange = true;
                            this.borderPoint1X = this.borderMinX + 1;
                        }
                    }
                    break;
                default:
                    Log.e(this.TAG, "moveBeam() error 1");
                    throw new RuntimeException();
            }

            if (this.borderPoint1 < 0) {
                this.borderPoint1 = 3;
            } else if (this.borderPoint1 > 3) {
                this.borderPoint1 = 0;
            }

            this.doPoint2();
        }

        private void releaseResources() {
            this.light1 = null;
            this.random1 = null;
            this.paintBeam1 = null;
            this.path1 = null;
        }

        // default package access
        void setBorderBeamMoveStep(final int borderBeamMoveStep) {
            this.borderBeamMoveStep = borderBeamMoveStep;
        }

        private void setDirection() {
            switch (this.random1.nextInt(2)) {
                case 0:
                    this.directionClockwise = true;
                    break;
                case 1:
                    this.directionClockwise = false;
                    break;
                default:
                    Log.e(this.TAG, "setDirection() error 1");
                    throw new RuntimeException();
            }

            this.rotationDirectionChange = false;
        }

        private void setRotateDirection() {
            if (this.borderPoint1 == this.borderPoint1Start) {
                if (this.rotationDirectionChange) {
                    if (this.directionClockwise) {
                        switch (this.borderPoint1Start) {
                            case 0:
                                // top
                                if (this.borderPoint1X > this.borderPoint1StartX) {
                                    this.setDirection();
                                }
                                break;
                            case 1:
                                // right
                                if (this.borderPoint1Y > this.borderPoint1StartY) {
                                    this.setDirection();
                                }
                                break;
                            case 2:
                                // bottom
                                if (this.borderPoint1X < this.borderPoint1StartX) {
                                    this.setDirection();
                                }
                                break;
                            case 3:
                                // left
                                if (this.borderPoint1Y < this.borderPoint1StartY) {
                                    this.setDirection();
                                }
                                break;
                            default:
                                Log.e(this.TAG, "setRotateDirection() error 1");
                                throw new RuntimeException();
                        }
                    } else {
                        // anti-clockwise
                        switch (this.borderPoint1Start) {
                            case 0:
                                // top
                                if (this.borderPoint1X < this.borderPoint1StartX) {
                                    this.setDirection();
                                }
                                break;
                            case 1:
                                // right
                                if (this.borderPoint1Y < this.borderPoint1StartY) {
                                    this.setDirection();
                                }
                                break;
                            case 2:
                                // bottom
                                if (this.borderPoint1X > this.borderPoint1StartX) {
                                    this.setDirection();
                                }
                                break;
                            case 3:
                                // left
                                if (this.borderPoint1Y > this.borderPoint1StartY) {
                                    this.setDirection();
                                }
                                break;
                            default:
                                Log.e(this.TAG, "setRotateDirection() error 2");
                                throw new RuntimeException();
                        }
                    }
                }
            }
        }
    }
}
