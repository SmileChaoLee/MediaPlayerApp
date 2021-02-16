package com.smile.mediaplayerapp;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.smile.mediaplayerapp.utilities.PlaybackStateUtil;

import java.io.IOException;

public class MediaSessionCallback extends MediaSessionCompat.Callback {

    private static final String TAG = "MediaSessionCallback";

    private final MediaPlaybackService mMediaPlaybackService;

    public MediaSessionCallback(MediaPlaybackService context) {
        mMediaPlaybackService = context;
    }

    @Override
    public synchronized void onCommand(String command, Bundle extras, ResultReceiver cb) {
        super.onCommand(command, extras, cb);
        Log.d(TAG, "onCommand() is called.");
    }

    @Override
    public synchronized void onPrepare() {
        super.onPrepare();
        Log.d(TAG, "onPrepare() is called.");
    }

    @Override
    public synchronized void onPrepareFromMediaId(String mediaId, Bundle extras) {
        super.onPrepareFromMediaId(mediaId, extras);
        Log.d(TAG, "onPrepareFromMediaId() is called.");
        mMediaPlaybackService.prepareFromMediaId(mediaId, extras);
    }

    @Override
    public synchronized void onPrepareFromUri(Uri uri, Bundle extras) {
        super.onPrepareFromUri(uri, extras);
        Log.d(TAG, "onPrepareFromUri() is called.");
        mMediaPlaybackService.prepareMediaFromUri(uri, extras);
    }

    @Override
    public synchronized void onPlay() {
        super.onPlay();
        Log.d(TAG, "onPlay() is called.");
        mMediaPlaybackService.playMedia();
    }

    @Override
    public synchronized void onPlayFromMediaId(String mediaId, Bundle extras) {
        super.onPlayFromMediaId(mediaId, extras);
        Log.d(TAG, "onPlayFromMediaId() is called.");
    }

    @Override
    public synchronized void onPlayFromUri(Uri uri, Bundle extras) {
        super.onPlayFromUri(uri, extras);
        Log.d(TAG, "onPlayFromUri() is called.");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() is called.");
        mMediaPlaybackService.pauseMedia();
    }

    @Override
    public synchronized void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() is called.");
        mMediaPlaybackService.stopMedia();
    }

    @Override
    public synchronized void onFastForward() {
        super.onFastForward();
        Log.d(TAG, "onFastForward() is called.");
    }

    @Override
    public synchronized void onRewind() {
        super.onRewind();
        Log.d(TAG, "onRewind() is called.");
    }
}
