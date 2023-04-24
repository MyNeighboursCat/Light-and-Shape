/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.mycompany.lightandshape.model;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * @author Colin Walters
 * @version 1.0, 24/05/2023
 */
// default package access
final class Shape extends Sprite {
    // default package access
    static final int MAX_COUNT = 200;
    private boolean drawShape = false;
    private boolean topLeftCornerShown = false;
    private boolean topRightCornerShown = false;
    private boolean bottomLeftCornerShown = false;
    private boolean bottomRightCornerShown = false;
    private boolean allCornersShown = false;
    private boolean allCornersNotShown = false;
    private boolean shownThenHide = false;
    private boolean newShape = false;
    private boolean missedShape = false;
    private int newShapeCounter = 0;
    private int destroyCount = 0;
    private SpriteType type;
    private SpriteColour colour;
    private ShapeType findTargetType;
    private int pixelsToMove;

    // default package access
    Shape(final ArrayList<Drawable> image, final Canvas canvas,
          final String type, final String colour,
          final Shape.ShapeType shapeType1) {
        super(image, canvas, 0.0F, 0.0F);

        this.type = SpriteType.valueOf(type);
        this.colour = SpriteColour.valueOf(colour);
        this.findTargetType = shapeType1;

        this.setDirection();
        this.setMoveStep(2);

        this.pixelsToMove = this.getMaxY() - this.getMinY();
    }

    // default package access
    Shape(final Bundle bundle1) {
        super(bundle1);

        boolean[] booleanArray1 = bundle1.getBooleanArray(Sprite.SPRITE_BUNDLE +
                this.getBundleCounter());

        if (booleanArray1 != null) {
            this.drawShape = booleanArray1[0];
            this.topLeftCornerShown = booleanArray1[1];
            this.topRightCornerShown = booleanArray1[2];
            this.bottomLeftCornerShown = booleanArray1[3];
            this.bottomRightCornerShown = booleanArray1[4];
            this.allCornersShown = booleanArray1[5];
            this.allCornersNotShown = booleanArray1[6];
            this.shownThenHide = booleanArray1[7];
            this.newShape = booleanArray1[8];
            this.missedShape = booleanArray1[9];
        }

        int[] intArray1 = bundle1.getIntArray(Sprite.SPRITE_BUNDLE +
                this.getBundleCounter());
        if (intArray1 != null) {
            this.newShapeCounter = intArray1[0];
            this.destroyCount = intArray1[1];
            this.pixelsToMove = intArray1[2];
        }

        String[] strArray1 = bundle1.getStringArray(Sprite.SPRITE_BUNDLE +
                this.getBundleCounter());
        if (strArray1 != null) {
            this.type = SpriteType.valueOf(strArray1[0]);
            this.colour = SpriteColour.valueOf(strArray1[1]);
            this.findTargetType = ShapeType.valueOf(strArray1[2]);
        }
    }

    // default package access
    void doCorners(final boolean topLeftCornerShown1,
                   final boolean topRightCornerShown1,
                   final boolean bottomLeftCornerShown1,
                   final boolean bottomRightCornerShown1) {

        if (!this.topLeftCornerShown) {
            this.topLeftCornerShown = topLeftCornerShown1;
        }

        if (!this.topRightCornerShown) {
            this.topRightCornerShown = topRightCornerShown1;
        }

        if (!this.bottomLeftCornerShown) {
            this.bottomLeftCornerShown = bottomLeftCornerShown1;
        }

        if (!this.bottomRightCornerShown) {
            this.bottomRightCornerShown = bottomRightCornerShown1;
        }

    }

    // default package access
    void doNewShape() {
        if (!this.newShape) {
            return;
        }

        this.newShapeCounter++;

        if (this.newShapeCounter > Shape.MAX_COUNT) {
            this.newShapeCounter = 0;
            this.newShape = false;
            // show shape colour
            this.setImageDisplayed(0);
        } else if ((this.findTargetType == ShapeType.TARGET)
                && ((this.newShapeCounter % 20) == 0)) {
            this.setImageDisplayed(this.getImageDisplayed() + 1);
            // minus one for cross image
            if (this.getImageDisplayed() >= (this.getDraws1().size() - 1)) {
                this.setImageDisplayed(0);
            }
        }
    }

    // default package access
    void drawShape(final Canvas canvas1, final Paint paintSprite) {
        if (this.isMissedShape()) {
            this.setImageDisplayed(0);
            canvas1.drawBitmap(this.getASprite(), this.getX(), this.getY(),
                    paintSprite);
            // miss out transparent image
            this.setImageDisplayed(this.getImageDisplayed() + 2);
            // draw cross over shape
            canvas1.drawBitmap(this.getASprite(), this.getX(), this.getY(),
                    paintSprite);
            this.setImageDisplayed(0);
        } else {
            canvas1.drawBitmap(this.getASprite(), this.getX(), this.getY(),
                    paintSprite);
        }
    }

    // default package access
    SpriteColour getColour() {
        return this.colour;
    }

    // default package access
    int getPixelsToMove() {
        return this.pixelsToMove;
    }

    // default package access
    SpriteType getType() {
        return this.type;
    }

    // default package access
    boolean isDrawShape() {
        return this.drawShape;
    }

    // default package access
    boolean isMissedShape() {
        return this.missedShape;
    }

    // default package access
    @SuppressWarnings("SameParameterValue")
    void setMissedShape(final boolean missedShape) {
        this.missedShape = missedShape;
    }

    // default package access
    boolean isNewShape() {
        return this.newShape;
    }

    // default package access
    @SuppressWarnings("SameParameterValue")
    void setNewShape(final boolean newShape) {
        this.newShape = newShape;
    }

    // default package access
    boolean isShownThenHide() {
        if (this.shownThenHide) {
            // clear shownThenHide in case not a find shape
            this.shownThenHide = false;
            return true;
        } else {
            return false;
        }
    }

    @Override
        // default package access
    boolean isToDestroy() {
        if (!this.missedShape) {
            return false;
        }

        if (++this.destroyCount >= Shape.MAX_COUNT) {
            this.setToDestroy(true);
        }

        // must call super otherwise this method is called again and causes
        // stack overflow ANR
        return Shape.super.isToDestroy();
    }

    @Override
        // default package access
    void releaseResources() {
        super.releaseResources();

        this.type = null;
        this.colour = null;
        this.findTargetType = null;
    }

    // default package access
    void resetShapeValues() {
        this.drawShape = false;
        this.topLeftCornerShown = false;
        this.topRightCornerShown = false;
        this.bottomLeftCornerShown = false;
        this.bottomRightCornerShown = false;
        this.shownThenHide = false;
    }

    private void setDirection() {
        // This needs to be 8 to include 7
        final int i = this.getRand().nextInt(8);
        switch (i) {
            case 0:
                this.setDirection(Direction.UP);
                break;
            case 1:
                this.setDirection(Direction.UP_RIGHT);
                break;
            case 2:
                this.setDirection(Direction.RIGHT);
                break;
            case 3:
                this.setDirection(Direction.DOWN_RIGHT);
                break;
            case 4:
                this.setDirection(Direction.DOWN);
                break;
            case 5:
                this.setDirection(Direction.DOWN_LEFT);
                break;
            case 6:
                this.setDirection(Direction.LEFT);
                break;
            case 7:
                this.setDirection(Direction.UP_LEFT);
                break;
            default:
                this.setDirection(Direction.UP);
                break;
        }
    }

    // default package access
    void setShownThenHide() {
        if (this.topLeftCornerShown && this.topRightCornerShown
                && this.bottomLeftCornerShown && this.bottomRightCornerShown) {
            this.allCornersShown = true;
        }

        if (!this.topLeftCornerShown && !this.topRightCornerShown
                && !this.bottomLeftCornerShown && !this.bottomRightCornerShown) {
            this.allCornersNotShown = true;
        }

        if (this.topLeftCornerShown || this.topRightCornerShown
                || this.bottomLeftCornerShown || this.bottomRightCornerShown) {
            this.drawShape = true;
        }

        if (this.allCornersShown && this.allCornersNotShown) {
            this.shownThenHide = true;
            this.allCornersShown = false;
            this.allCornersNotShown = false;
        }
    }

    // default package access
    void setTargetShapeDetails() {
        this.setMinY((3 * this.getCanvasPadding()) + this.getDrawableHeight());
        this.setX(this.getRand().nextInt(this.getMaxX() - this.getMinX())
                + this.getMinX());
        this.setY(this.getRand().nextInt(this.getMaxY() - this.getMinY())
                + this.getMinY());
    }

    @Override
        // default package access
    void setX(final int x1) {
        super.setX(x1);

        if ((this.getX() == this.getMinX()) || (this.getX() == this.getMaxX())) {
            this.setDirection();
        }

    }

    @Override
        // default package access
    void setY(final int y1) {
        super.setY(y1);

        if ((this.getY() == this.getMinY()) || (this.getY() == this.getMaxY())) {
            this.setDirection();
        }
    }

    @Override
        // default package access
    Bundle writeToBundle(final Bundle bundle1) {
        super.writeToBundle(bundle1);

        final boolean[] booleanArray1 = {this.drawShape,
                this.topLeftCornerShown, this.topRightCornerShown,
                this.bottomLeftCornerShown, this.bottomRightCornerShown,
                this.allCornersShown, this.allCornersNotShown,
                this.shownThenHide, this.newShape, this.missedShape};
        final int[] intArray1 = {this.newShapeCounter, this.destroyCount,
                this.pixelsToMove};
        final String[] strArray1 = {this.type.toString(),
                this.colour.toString(), this.findTargetType.toString()};

        bundle1.putBooleanArray(
                Sprite.SPRITE_BUNDLE
                        + this.getBundleCounter(),
                booleanArray1);
        bundle1.putIntArray(
                Sprite.SPRITE_BUNDLE
                        + this.getBundleCounter(), intArray1);
        bundle1.putStringArray(
                Sprite.SPRITE_BUNDLE
                        + this.getBundleCounter(), strArray1);

        return bundle1;
    }

    // default package access
    enum ShapeType {
        FIND, TARGET
    }

    // default package access
    @SuppressWarnings("unused")
    enum SpriteColour {
        BLUE, BROWN, CYAN, GREY, MAGENTA, ORANGE, PINK, RED, YELLOW
    }

    // default package access
    enum SpriteType {
        CIRCLE, SQUARE, TRIANGLE
    }
}