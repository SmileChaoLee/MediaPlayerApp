package com.smile.mediaplayerapp;

import android.annotation.SuppressLint;
import android.media.session.PlaybackState;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.smile.mediaplayerapp.utilities.PlaybackStateUtil;

public class MediaControllerCallback extends MediaControllerCompat.Callback {

    private static final String TAG = "MediaControllerCallback";

    @SuppressLint("LongLogTag")
    @Override
    public synchronized void onPlaybackStateChanged(PlaybackStateCompat state) {
        Log.d(TAG, "onPlaybackStateChanged() is called.");
        super.onPlaybackStateChanged(state);
        if( state == null ) {
            return;
        }

        int currentState = state.getState();

        switch (currentState) {
            case PlaybackStateCompat.STATE_PLAYING:
                Log.d(TAG, "Playback state = PlaybackStateCompat.STATE_PLAYING");
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                Log.d(TAG, "Playback state = PlaybackStateCompat.STATE_PAUSED");
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                Log.d(TAG, "Playback state = PlaybackStateCompat.STATE_STOPPED");
                break;
            case PlaybackStateCompat.STATE_NONE:
                // idle
                Log.d(TAG, "Playback state = PlaybackStateCompat.STATE_NONE");
                break;
        }
    }
}
