/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.mycompany.lightandshape.model;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.mycompany.lightandshape.BasicApplication;
import com.mycompany.lightandshape.R;
import com.mycompany.lightandshape.controller.AudioFocusService;
import com.mycompany.lightandshape.controller.SoundBroadcastReceiver;
import com.mycompany.lightandshape.model.HighScoreDatabaseHelper.SettingsCursor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Colin Walters
 * @version 1.0, 24/05/2023
 */
@SuppressWarnings("Convert2Lambda")
public final class SoundEffect {
    private final String TAG = this.getClass().getSimpleName();
    private volatile int currentSoundNumber1 = 0;
    private volatile Context context1;
    private volatile ArrayList<SoundType> mediaPlayerTypes1;
    private volatile HashMap<SoundType, MediaPlayer> mediaPlayers1 = new HashMap<>();
    private volatile boolean soundPlayable = false;
    private volatile AudioFocusService audioFocusService1 = null;
    private volatile SoundBroadcastReceiver soundBroadcastReceiver1 = null;

    public SoundEffect(final Context context,
                       final ArrayList<SoundType> mediaPlayerTypes,
                       final SoundType soundTypePlay) {
        super();

        this.context1 = context;
        this.mediaPlayerTypes1 = mediaPlayerTypes;

        this.doDatabase(soundTypePlay);
    }

    private void createMediaPlayer(final SoundType soundType,
                                   final int soundResource1) {
        try {
            final MediaPlayer mediaPlayer1 = MediaPlayer.create(this.context1,
                    soundResource1);
            this.mediaPlayerSetVolume(mediaPlayer1);
            this.mediaPlayers1.put(soundType, mediaPlayer1);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void doDatabase(final SoundType soundTypePlay) {
        // Background thread.
        BasicApplication.APP_EXECUTORS.getBackgroundThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final WeakReference<SoundEffect> soundEffectWeakReference =
                        new WeakReference<>(SoundEffect.this);

                try {
                    // Get a reference to the SoundEffect if it is still there.
                    final SoundEffect soundEffect = soundEffectWeakReference.get();
                    if (soundEffect == null) {
                        return;
                    }

                    // abstract method so no super call
                    HighScoreDatabaseHelper db = new HighScoreDatabaseHelper(
                            soundEffect.context1, "", null, 0);
                    SettingsCursor dbSettingsCursor = db.getSettingsRecords(
                            HighScoreDatabaseHelper.SOUND, true,
                            HighScoreDatabaseHelper.SETTINGS_NO_HIGH_SCORE_VALUE);
                    if (dbSettingsCursor.getCount() == 1) {
                        dbSettingsCursor.moveToFirst();
                        soundEffect.currentSoundNumber1 = dbSettingsCursor.getColumnSettingsValue();
                    } else {
                        Log.e(soundEffect.TAG, "doDatabase() error 1");
                        throw new RuntimeException();
                    }

                    dbSettingsCursor.close();
                    db.close();

                    soundEffect.mediaPlayersCreate();
                } finally {
                    // Main thread.
                    BasicApplication.APP_EXECUTORS.getMainThreadExecutor().execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    // Get a reference to the SoundEffect if it is still there.
                                    final SoundEffect soundEffect = soundEffectWeakReference.get();
                                    if (soundEffect == null) {
                                        return;
                                    }

                                    if (soundTypePlay != null) {
                                        soundEffect.doSound(soundTypePlay);
                                    }
                                }
                            });
                }
            }
        });
    }

    // default package access
    void doSound(final SoundType soundType) {
        if (!this.soundPlayable) {
            return;
        }

        try {
            // HashMap reference comment
            // Note: the implementation of HashMap is not synchronized.
            // If one thread of several threads accessing an instance modifies
            // the map structurally,
            // access to the map needs to be synchronized.
            // A structural modification is an operation that adds or removes an
            // entry.
            // Changes in the value of an entry are not structural changes.
            final MediaPlayer mediaPlayer1 = this.mediaPlayers1.get(soundType);
            if (mediaPlayer1 != null) {
                mediaPlayer1.seekTo(0);
                mediaPlayer1.start();
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<SoundType, MediaPlayer> getMediaPlayers1() {
        if (!this.soundPlayable) {
            return null;
        }

        return this.mediaPlayers1;
    }

    public void mediaPlayersCreate() {
        if (this.currentSoundNumber1 == 0) {
            return;
        }

        for (final SoundType soundType1 : this.mediaPlayerTypes1) {
            int resource1;

            switch (soundType1) {
                case START:
                    resource1 = R.raw.start;
                    break;
                case HIT:
                    resource1 = R.raw.hit;
                    break;
                case MISS:
                    resource1 = R.raw.miss;
                    break;
                case GAME_OVER:
                    resource1 = R.raw.gameover;
                    break;
                default:
                    Log.e(this.TAG, "mediaPlayersCreate() error 1");
                    throw new RuntimeException();
            }

            this.createMediaPlayer(soundType1, resource1);
        }

        boolean audioSetUpOK;
        this.audioFocusService1 = new AudioFocusService(this.context1, this);
        audioSetUpOK = this.audioFocusService1.requestFocus();

        if (audioSetUpOK) {
            this.soundBroadcastReceiver1 = new SoundBroadcastReceiver(
                    this.context1, this);
            this.soundPlayable = true;
        }
    }

    private void mediaPlayerSetVolume(final MediaPlayer mediaPlayer) {
        mediaPlayer.setVolume(this.currentSoundNumber1 / 10.0F,
                this.currentSoundNumber1 / 10.0F);
    }

    public void mediaPlayersPause() {
        if (!this.soundPlayable) {
            return;
        }

        MediaPlayer mediaPlayer1;

        for (final SoundType soundType1 : this.mediaPlayerTypes1) {
            try {
                mediaPlayer1 = this.mediaPlayers1.get(soundType1);
                if (mediaPlayer1 != null && mediaPlayer1.isPlaying()) {
                    mediaPlayer1.pause();
                    mediaPlayer1.seekTo(0);
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void mediaPlayersRelease() {
        if ((this.mediaPlayers1 != null) && (this.mediaPlayerTypes1 != null)) {
            if (this.mediaPlayers1.size() > 0) {
                for (final SoundType soundType1 : this.mediaPlayerTypes1) {
                    this.releaseMediaPlayer(soundType1);
                }
            }
        }

        this.soundPlayable = false;

        if (this.mediaPlayerTypes1 != null) {
            this.mediaPlayerTypes1.clear();
            this.mediaPlayerTypes1 = null;
        }

        if (this.mediaPlayers1 != null) {
            this.mediaPlayers1.clear();
            this.mediaPlayers1 = null;
        }

        if (this.audioFocusService1 != null) {
            this.audioFocusService1.abandonFocus();
            this.audioFocusService1 = null;
        }

        if (this.soundBroadcastReceiver1 != null) {
            this.soundBroadcastReceiver1.unRegisterReceiver();
            this.soundBroadcastReceiver1 = null;
        }

        this.context1 = null;
    }

    public void mediaPlayersTurnDownVolume() {
        if (!this.soundPlayable) {
            return;
        }

        MediaPlayer mediaPlayer1;

        for (final SoundType soundType1 : this.mediaPlayerTypes1) {
            try {
                mediaPlayer1 = this.mediaPlayers1.get(soundType1);
                if (mediaPlayer1 != null) {
                    mediaPlayer1.setVolume(0.0F, 0.0F);
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void mediaPlayersTurnUpVolume() {
        if (!this.soundPlayable) {
            return;
        }

        MediaPlayer mediaPlayer1;

        for (final SoundType soundType1 : this.mediaPlayerTypes1) {
            try {
                mediaPlayer1 = this.mediaPlayers1.get(soundType1);
                if (mediaPlayer1 != null) {
                    this.mediaPlayerSetVolume(mediaPlayer1);
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void releaseMediaPlayer(final SoundType soundType) {
        try {
            final MediaPlayer mediaPlayer1 = this.mediaPlayers1.get(soundType);
            if (mediaPlayer1 != null) {
                mediaPlayer1.release();
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public enum SoundType {
        START, HIT, MISS, GAME_OVER
    }
}
