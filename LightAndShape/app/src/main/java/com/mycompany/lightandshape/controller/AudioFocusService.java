/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.mycompany.lightandshape.controller;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.IBinder;

import com.mycompany.lightandshape.model.SoundEffect;

/**
 * @author Colin Walters
 * @version 1.0, 24/05/2023
 */
public final class AudioFocusService extends Service implements OnAudioFocusChangeListener {
    private Context context1 = null;
    private SoundEffect soundEffect1 = null;
    private AudioManager mAudioManager = null;

    // Leave in because manifest complains.
    @SuppressWarnings("unused")
    public AudioFocusService() {
        super();
    }

    public AudioFocusService(final Context context, final SoundEffect soundEffect) {
        this.context1 = context;
        this.soundEffect1 = soundEffect;

        if (this.context1 != null) {
            this.mAudioManager = (AudioManager) this.context1
                    .getSystemService(Context.AUDIO_SERVICE);
        }
    }

    @Override
    public void onAudioFocusChange(final int focusChange) {
        if (this.soundEffect1 == null) {
            return;
        }

        switch (focusChange) {
            // Not sure whether AudioManager.AUDIOFOCUS_GAIN triggers onAudioFocusChange().
            case AudioManager.AUDIOFOCUS_GAIN:
                if (this.soundEffect1.getMediaPlayers1() != null) {
                    if (this.soundEffect1.getMediaPlayers1().size() == 0) {
                        // Must have been AUDIOFOCUS_LOSS.
                        this.soundEffect1.mediaPlayersCreate();
                    } else {
                        this.soundEffect1.mediaPlayersTurnUpVolume();
                    }
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Do not release the media players here.
                // More that one game created by Game class constructor so games lose audio focus
                // and sound is lost.  Leave to GameActivity onStop().  Audio focus gain above
                // appears not to be called.
                //this.soundEffect1.mediaPlayersRelease();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                this.soundEffect1.mediaPlayersPause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                this.soundEffect1.mediaPlayersTurnDownVolume();
                break;
            default:
                break;
        }
    }

    @Override
    public IBinder onBind(final Intent arg0) {
        return null;
    }

    public boolean requestFocus() {
        if (this.mAudioManager == null) {
            return false;
        }

        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == this.mAudioManager
                .requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    public void abandonFocus() {
        if (this.mAudioManager != null) {
            this.mAudioManager.abandonAudioFocus(this);
        }

        this.releaseResources();
    }

    private void releaseResources() {
        this.context1 = null;
        this.soundEffect1 = null;
        this.mAudioManager = null;
    }
}
