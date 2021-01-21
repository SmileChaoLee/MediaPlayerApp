package com.smile.mediaplayerapp;

import android.annotation.SuppressLint;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

public class MediaControllerCallback extends MediaControllerCompat.Callback {

    private static final String TAG = "MediaControllerCallback";

    public MediaControllerCallback() {
    }

    @SuppressLint("LongLogTag")
    @Override
    public synchronized void onPlaybackStateChanged(PlaybackStateCompat state) {
        Log.d(TAG, "onPlaybackStateChanged() is called.");
        super.onPlaybackStateChanged(state);
        if( state == null ) {
            return;
        }
    }
}
