package com.smile.mediaplayerapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.session.PlaybackState;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.smile.mediaplayerapp.utilities.PlaybackStateUtil;

public class MediaControllerCallback extends MediaControllerCompat.Callback {

    private static final String TAG = "MediaControllerCallback";

    private final MainActivity mMainActivity;

    public MediaControllerCallback(Activity activity) {
        mMainActivity = (MainActivity)activity;
    }

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
                mMainActivity.setPlayMenuItem(false);
                mMainActivity.setPauseMenuItem(true);
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                Log.d(TAG, "Playback state = PlaybackStateCompat.STATE_PAUSED");
                mMainActivity.setPlayMenuItem(true);
                mMainActivity.setPauseMenuItem(false);
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                Log.d(TAG, "Playback state = PlaybackStateCompat.STATE_STOPPED");
                mMainActivity.setPlayMenuItem(true);
                mMainActivity.setPauseMenuItem(false);
                break;
            case PlaybackStateCompat.STATE_NONE:
                // idle
                Log.d(TAG, "Playback state = PlaybackStateCompat.STATE_NONE");
                mMainActivity.setPlayMenuItem(true);
                mMainActivity.setPauseMenuItem(false);
                break;
            default:
                Log.d(TAG, "Playback state = " + currentState);
                break;
        }
    }
}
