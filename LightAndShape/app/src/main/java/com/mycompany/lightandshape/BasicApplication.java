/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */

package com.mycompany.lightandshape;

import android.app.Application;

/**
 * Android Application class. Used for accessing singletons.
 *
 * @author Colin Walters
 * @version 1.0, 24/05/2023
 */
public final class BasicApplication extends Application {
    /**
     * Global executor pools for the whole application.
     */
    public static final AppExecutors APP_EXECUTORS = new AppExecutors();
}
