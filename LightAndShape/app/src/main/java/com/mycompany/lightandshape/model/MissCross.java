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
final class MissCross extends Sprite {
    private int destroyCount = 0;

    // default package access
    MissCross(final ArrayList<Drawable> image, final Canvas canvas) {
        super(image, canvas, 0.0F, 0.0F);
    }

    // default package access
    MissCross(final Bundle bundle1) {
        super(bundle1);

        this.destroyCount = bundle1.getInt(Sprite.SPRITE_BUNDLE
                + this.getBundleCounter());
    }

    @Override
        // default package access
    boolean isToDestroy() {
        if (++this.destroyCount >= Shape.MAX_COUNT) {
            this.setToDestroy(true);
        }

        // must call super otherwise this method is called again and causes
        // stack overflow ANR
        return MissCross.super.isToDestroy();
    }

    @Override
        // default package access
    Bundle writeToBundle(final Bundle bundle1) {
        super.writeToBundle(bundle1);

        bundle1.putInt(
                Sprite.SPRITE_BUNDLE
                        + this.getBundleCounter(),
                this.destroyCount);

        return bundle1;
    }
}
