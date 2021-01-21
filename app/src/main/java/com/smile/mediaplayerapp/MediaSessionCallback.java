package com.smile.mediaplayerapp;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

public class MediaSessionCallback extends MediaSessionCompat.Callback {

    private static final String TAG = "MediaSessionCallback";

    private final Context mContext;
    private final MediaSessionCompat mMediaSession;
    private final PlaybackStateCompat.Builder mPlaybackStateBuilder;
    private final MediaPlayer mMediaPlayer;

    public MediaSessionCallback(Context context, MediaSessionCompat mediaSessionCompat, PlaybackStateCompat.Builder playbackStateBuilder, MediaPlayer mediaPlayer) {
        mContext = context;
        mMediaSession = mediaSessionCompat;
        mPlaybackStateBuilder = playbackStateBuilder;
        mMediaPlayer = mediaPlayer;
    }

    @Override
    public synchronized void onCommand(String command, Bundle extras, ResultReceiver cb) {
        super.onCommand(command, extras, cb);
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
    }

    @Override
    public synchronized void onPrepareFromUri(Uri uri, Bundle extras) {
        Log.d(TAG, "onPrepareFromUri() is called.");
        super.onPrepareFromUri(uri, extras);
    }

    @Override
    public synchronized void onPlay() {
        super.onPlay();
        Log.d(TAG, "onPlay() is called.");
        PlaybackStateCompat playbackState = mPlaybackStateBuilder.build();
        if(playbackState.getState() == PlaybackStateCompat.STATE_PAUSED){
            mMediaPlayer.start();
            playbackState = mPlaybackStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,0,1.0f).build();
            mMediaSession.setPlaybackState(playbackState);
        }

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
    }

    @Override
    public synchronized void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() is called.");
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
