package com.smile.mediaplayerapp;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
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
    private final MediaSessionCompat mMediaSession;
    private final MediaPlayer mMediaPlayer;

    public MediaSessionCallback(MediaPlaybackService context, MediaSessionCompat mediaSessionCompat, MediaPlayer mediaPlayer) {
        mMediaPlaybackService = context;
        mMediaSession = mediaSessionCompat;
        mMediaPlayer = mediaPlayer;
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
        try {
            AssetFileDescriptor afd = mMediaPlaybackService.getResources().openRawResourceFd(Integer.valueOf(mediaId));
            if (afd == null) {
                return;
            }
            Log.d(TAG, "onPlayFromMediaId()--> afd not null.");

            mMediaPlayer.reset();
            try {
                mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            } catch (IllegalStateException e) {
                Log.d(TAG, "onPlayFromMediaId()--> mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength()) failed.");
                mMediaPlayer.release();
                mMediaPlaybackService.initMediaPlayer();
                mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            }

            afd.close();
            mMediaPlaybackService.initMediaSessionMetadata();

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void onPrepareFromUri(Uri uri, Bundle extras) {
        super.onPrepareFromUri(uri, extras);
        Log.d(TAG, "onPrepareFromUri() is called.");
        PlaybackStateCompat playbackState = mMediaSession.getController().getPlaybackState();
        try {
            switch (playbackState.getState()){
                case PlaybackStateCompat.STATE_PLAYING:
                case PlaybackStateCompat.STATE_PAUSED:
                case PlaybackStateCompat.STATE_NONE:
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(mMediaPlaybackService,uri);
                    mMediaPlayer.prepare();//准备同步
                    playbackState = PlaybackStateUtil.getMediaPlaybackState(PlaybackStateCompat.STATE_CONNECTING);
                    new PlaybackStateCompat.Builder()
                            .setState(PlaybackStateCompat.STATE_CONNECTING,0,1.0f)
                            .build();
                    mMediaSession.setPlaybackState(playbackState);
                    //我们可以保存当前播放音乐的信息，以便客户端刷新UI
                    mMediaSession.setMetadata(new MediaMetadataCompat.Builder()
                            .putString(MediaMetadataCompat.METADATA_KEY_TITLE,extras.getString("title"))    //  ?? unfinished
                            .build()
                    );
                    break;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void onPlay() {
        super.onPlay();
        Log.d(TAG, "onPlay() is called.");
        if( !mMediaPlaybackService.successfullyRetrievedAudioFocus() ) {
            return;
        }

        PlaybackStateCompat playbackState = mMediaSession.getController().getPlaybackState();
        if(playbackState.getState() != PlaybackStateCompat.STATE_PLAYING){
            mMediaPlayer.start();
            playbackState = PlaybackStateUtil.getMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            mMediaSession.setPlaybackState(playbackState);
            mMediaPlaybackService.showPlayingNotification();
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
        PlaybackStateCompat playbackState = mMediaSession.getController().getPlaybackState();
        // if( mMediaPlayer.isPlaying() ) {
        if(playbackState.getState() == PlaybackStateCompat.STATE_PLAYING){
            mMediaPlayer.pause();
            playbackState = PlaybackStateUtil.getMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
            mMediaSession.setPlaybackState(playbackState);
            mMediaPlaybackService.showPausedNotification();
        }
    }

    @Override
    public synchronized void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() is called.");
        PlaybackStateCompat playbackState = mMediaSession.getController().getPlaybackState();
        if(playbackState.getState() != PlaybackStateCompat.STATE_STOPPED){
            mMediaPlayer.reset();
            playbackState = PlaybackStateUtil.getMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
            mMediaSession.setPlaybackState(playbackState);
            mMediaPlaybackService.showStoppedNotification();
        }
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
