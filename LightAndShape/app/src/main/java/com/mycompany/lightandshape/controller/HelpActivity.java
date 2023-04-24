/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.mycompany.lightandshape.controller;

import android.media.AudioManager;
import android.os.Bundle;
import android.view.MenuItem;

import com.mycompany.lightandshape.R;
import com.mycompany.lightandshape.databinding.TextDisplayBinding;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author Colin Walters
 * @version 1.0, 24/05/2023
 */
public final class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure only the music stream volume is adjusted.
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        final TextDisplayBinding binding = TextDisplayBinding.inflate(getLayoutInflater());
        this.setContentView(binding.getRoot());
        this.setSupportActionBar(binding.toolbar);

        final ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        binding.textDisplayTextView.setText(R.string.help_text);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Respond to the action bar's Up/Home button.
        if (item.getItemId() == android.R.id.home) {
            // Return to existing instance of the calling activity rather than create a new one.
            // This keeps the existing state of the calling activity.
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
