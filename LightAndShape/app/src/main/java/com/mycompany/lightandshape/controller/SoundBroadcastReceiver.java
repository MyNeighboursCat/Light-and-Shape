/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.mycompany.lightandshape.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import com.mycompany.lightandshape.model.SoundEffect;

/**
 * @author Colin Walters
 * @version 1.0, 24/05/2023
 */
public final class SoundBroadcastReceiver extends BroadcastReceiver {
    private Context context1 = null;
    private SoundEffect soundEffect1 = null;

    // Leave in because manifest complains.
    @SuppressWarnings("unused")
    public SoundBroadcastReceiver() {
        super();
    }

    public SoundBroadcastReceiver(final Context context,
                                  final SoundEffect soundEffect) {
        this.context1 = context;
        this.soundEffect1 = soundEffect;

        this.registerReceiver();
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (this.soundEffect1 == null) {
            return;
        }

        String stringIntent = intent.getAction();
        if (stringIntent != null) {
            if (stringIntent.equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                this.soundEffect1.mediaPlayersTurnDownVolume();
            }
        }
    }

    private void registerReceiver() {
        if (this.context1 != null) {
            final IntentFilter intentFilter = new IntentFilter(
                    AudioManager.ACTION_AUDIO_BECOMING_NOISY);

            this.context1.registerReceiver(this, intentFilter);
        }
    }

    public void unRegisterReceiver() {
        if (this.context1 != null) {
            this.context1.unregisterReceiver(this);
        }

        this.releaseResources();
    }

    private void releaseResources() {
        this.context1 = null;
        this.soundEffect1 = null;
    }
}
