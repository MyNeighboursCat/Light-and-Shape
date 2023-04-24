/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.mycompany.lightandshape.model;

import android.os.Bundle;

/**
 * @author Colin Walters
 * @version 1.0, 24/05/2023
 */
// default package access
final class TouchPoint {
    private static final String TOUCH_POINT_BUNDLE = "TOUCH_POINT_BUNDLE_";
    private volatile int touchX;
    private volatile int touchY;

    // default package access
    TouchPoint(final Bundle bundle1) {
        int bundleCounter = 0;

        int[] intArray1 = bundle1.getIntArray(TouchPoint.TOUCH_POINT_BUNDLE
                + bundleCounter);
        if (intArray1 != null) {
            this.touchX = intArray1[0];
            this.touchY = intArray1[1];
        }
    }

    // default package access
    TouchPoint(final int x1, final int y1) {
        super();

        this.touchX = x1;
        this.touchY = y1;
    }

    // default package access
    int getTouchX() {
        return this.touchX;
    }

    // default package access
    int getTouchY() {
        return this.touchY;
    }

    // default package access
    Bundle writeToBundle(final Bundle bundle1) {
        int bundleCounter = 0;

        final int[] intArray1 = {this.touchX, this.touchY};

        bundle1.putIntArray(
                TouchPoint.TOUCH_POINT_BUNDLE
                        + bundleCounter, intArray1);

        return bundle1;
    }
}
