/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.mycompany.lightandshape.model;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * @author Colin Walters
 * @version 1.0, 24/05/2023
 */
// default package access
final class FoundShape extends Sprite {
    private int destroyCount = 0;
    private Shape.SpriteType type;
    private Shape.SpriteColour colour;

    // default package access
    FoundShape(final ArrayList<Drawable> image, final Canvas canvas,
               final Shape.SpriteType type, final Shape.SpriteColour colour) {
        super(image, canvas, 0.0F, 0.0F);

        this.type = type;
        // the colour here is the colour of the FOUND shape and not colour of
        // FoundShape
        // used for tracking
        this.colour = colour;
    }

    // default package access
    FoundShape(final Bundle bundle1) {
        super(bundle1);

        this.destroyCount = bundle1.getInt(Sprite.SPRITE_BUNDLE + this.getBundleCounter());

        String[] strArray1;
        strArray1 = bundle1.getStringArray(Sprite.SPRITE_BUNDLE
                + this.getBundleCounter());
        if (strArray1 != null) {
            this.type = Shape.SpriteType.valueOf(strArray1[0]);
            this.colour = Shape.SpriteColour.valueOf(strArray1[1]);
        }
    }

    // default package access
    Shape.SpriteType getType() {
        return this.type;
    }

    @Override
        // default package access
    boolean isToDestroy() {
        if (++this.destroyCount >= Shape.MAX_COUNT) {
            this.setToDestroy(true);
        }

        // must call super otherwise this method is called again and causes
        // stack overflow ANR
        return FoundShape.super.isToDestroy();
    }

    @Override
        // default package access
    void releaseResources() {
        super.releaseResources();

        this.type = null;
        this.colour = null;
    }

    @Override
        // default package access
    Bundle writeToBundle(final Bundle bundle1) {
        super.writeToBundle(bundle1);

        final String[] strArray1 = {this.type.toString(),
                this.colour.toString()};

        bundle1.putInt(
                Sprite.SPRITE_BUNDLE
                        + this.getBundleCounter(),
                this.destroyCount);

        bundle1.putStringArray(
                Sprite.SPRITE_BUNDLE
                        + this.getBundleCounter(), strArray1);

        return bundle1;
    }
}
