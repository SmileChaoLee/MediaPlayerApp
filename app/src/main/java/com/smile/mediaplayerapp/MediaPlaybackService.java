package com.smile.mediaplayerapp;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import com.smile.mediaplayerapp.utilities.PlaybackStateUtil;

import java.io.IOException;
import java.util.List;

public class MediaPlaybackService extends MediaBrowserServiceCompat implements AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = "MediaPlaybackService";
    private static final String LOG_TAG = "MediaSessionCompatTag";
    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";

    private MediaSessionCompat mMediaSession;
    private MediaPlayer mMediaPlayer;

    public static final String MEDIA_ID_ROOT = "MediaBrowserServiceCompat";

    private MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            Log.d(TAG, "onPrepared() is called.");
            mMediaPlayer.start();
            PlaybackStateCompat playbackState = PlaybackStateUtil.getMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            mMediaSession.setPlaybackState(playbackState);
        }
    };
    private MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            Log.d(TAG, "onCompletion() is called.");
            PlaybackStateCompat playbackState = PlaybackStateUtil.getMediaPlaybackState(PlaybackStateCompat.STATE_NONE);
            mMediaSession.setPlaybackState(playbackState);
            mMediaPlayer.reset();
        }
    };

    // check for if this is really needed for a MediaBrowserServiceCompat
    private BroadcastReceiver  mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive() is called.");
            if( mMediaPlayer != null && mMediaPlayer.isPlaying() ) {
                mMediaPlayer.pause();
            }
        }
    };

    // check for if this is really needed for a MediaBrowserServiceCompat
    /*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() is called.");
        MediaButtonReceiver.handleIntent(mMediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }
    */

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() is called.");

        initMediaPlayer();
        initMediaSession();
        initNoisyReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() is called.");

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(this);

        unregisterReceiver(mNoisyReceiver);

        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mMediaSession != null) {
            mMediaSession.release();
            mMediaSession = null;
        }

        NotificationManagerCompat.from(this).cancel(1);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        Log.d(TAG, "onGetRoot() is called.");
        return new BrowserRoot(MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d(TAG, "onLoadChildren() is called.");
        result.sendResult(null);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.d(TAG, "onAudioFocusChange() is called.");
        switch( focusChange ) {
            case AudioManager.AUDIOFOCUS_LOSS: {
                if( mMediaPlayer.isPlaying() ) {
                    mMediaPlayer.stop();
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                mMediaPlayer.pause();
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                if( mMediaPlayer != null ) {
                    mMediaPlayer.setVolume(0.3f, 0.3f);
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_GAIN: {
                if( mMediaPlayer != null ) {
                    if( !mMediaPlayer.isPlaying() ) {
                        mMediaPlayer.start();
                    }
                    mMediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;
            }
        }
    }

    private void initMediaSession() {
        Log.d(TAG, "initMediaSession() is called.");

        ComponentName mMediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        // Create a MediaSessionCompat
        mMediaSession = new MediaSessionCompat(this, LOG_TAG, mMediaButtonReceiver, null);
        // or if no BroadcastReceiver for media button and Notification
        // mMediaSession = new MediaSessionCompat(this, LOG_TAG);

        // MySessionCallback() has methods that handle callbacks from a media controller
        mMediaSession.setCallback(new MediaSessionCallback(this));

        // Enable callbacks from MediaButtons and TransportControls
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        PlaybackStateCompat playbackState = PlaybackStateUtil.getMediaPlaybackState(PlaybackStateCompat.STATE_NONE);
        mMediaSession.setPlaybackState(playbackState);

        mMediaSession.setActive(true);

        // media button receiving
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mMediaSession.setMediaButtonReceiver(pendingIntent);
        //

        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mMediaSession.getSessionToken());
    }

    private void initNoisyReceiver() {
        Log.d(TAG, "initNoisyReceiver() is called.");
        //Handles headphones coming unplugged. cannot be done through a manifest receiver
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver( mNoisyReceiver, filter);
    }

    public void initMediaPlayer() {
        Log.d(TAG, "initMediaPlayer() is called.");
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setVolume(1.0f, 1.0f);
        // mMediaPlayer.setOnPreparedListener(preparedListener);
        mMediaPlayer.setOnCompletionListener(completionListener);
    }

    public void prepareFromMediaId(String mediaId, Bundle extras) {
        Log.d(TAG, "prepareFromMediaId() is called.");
        try {
            AssetFileDescriptor afd = getResources().openRawResourceFd(Integer.valueOf(mediaId));
            if (afd == null) {
                return;
            }
            Log.d(TAG, "prepareFromMediaId()--> afd not null.");

            mMediaPlayer.reset();
            try {
                mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            } catch (IllegalStateException e) {
                Log.d(TAG, "prepareFromMediaId()--> mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength()) failed.");
                mMediaPlayer.release();
                initMediaPlayer();
                mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            }

            afd.close();
            initMediaSessionMetadata();

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

    public void prepareMediaFromUri(Uri uri, Bundle extras) {
        Log.d(TAG, "prepareMediaFromUri() is called.");
        PlaybackStateCompat playbackState = mMediaSession.getController().getPlaybackState();
        try {
            switch (playbackState.getState()){
                case PlaybackStateCompat.STATE_PLAYING:
                case PlaybackStateCompat.STATE_PAUSED:
                case PlaybackStateCompat.STATE_NONE:
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(this, uri);
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

    public void playMedia() {
        Log.d(TAG, "playMedia() is called.");
        if( !successfullyRetrievedAudioFocus() ) {
            return;
        }

        PlaybackStateCompat playbackState = mMediaSession.getController().getPlaybackState();
        int state = playbackState.getState();
        if(state == PlaybackStateCompat.STATE_STOPPED){
            Log.d(TAG, "playMedia() --> playbackState is PlaybackStateCompat.STATE_STOPPED");
            // mMediaPlayer.reset();
            // mMediaPlayer.seekTo(0);
            mMediaPlayer.start();
        } else if (state != PlaybackState.STATE_PLAYING) {
            mMediaPlayer.start();
        }
        playbackState = PlaybackStateUtil.getMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
        mMediaSession.setPlaybackState(playbackState);
        showPlayingNotification();
    }

    public void pauseMedia() {
        Log.d(TAG, "pauseMedia() is called.");
        PlaybackStateCompat playbackState = mMediaSession.getController().getPlaybackState();
        // if( mMediaPlayer.isPlaying() ) {
        if(playbackState.getState() == PlaybackStateCompat.STATE_PLAYING){
            mMediaPlayer.pause();
            playbackState = PlaybackStateUtil.getMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
            mMediaSession.setPlaybackState(playbackState);
            showPausedNotification();
        }
    }

    public void stopMedia() {
        Log.d(TAG, "stopMedia() is called.");
        PlaybackStateCompat playbackState = mMediaSession.getController().getPlaybackState();
        if(playbackState.getState() != PlaybackStateCompat.STATE_STOPPED){
            mMediaPlayer.pause();
            mMediaPlayer.seekTo(0);
            playbackState = PlaybackStateUtil.getMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
            mMediaSession.setPlaybackState(playbackState);
            showStoppedNotification();
        }
    }

    public void initMediaSessionMetadata() {
        Log.d(TAG, "initMediaSessionMetadata() is called.");
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
        //Notification icon in card
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        //lock screen icon for pre lollipop
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "Beo Dat May Troi");
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "Singer: Anh Tho");
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, 1);
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, 1);

        mMediaSession.setMetadata(metadataBuilder.build());
    }

    public synchronized void showPlayingNotification() {
        Log.d(TAG, "showPlayingNotification() is called.");
        NotificationCompat.Builder builder = MediaStyleHelper.from(this, mMediaSession);
        if( builder == null ) {
            return;
        }

        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0).setMediaSession(mMediaSession.getSessionToken()));
        builder.setSmallIcon(R.mipmap.notification_icon);
        NotificationManagerCompat.from(this).notify(1, builder.build());
    }

    public synchronized void showPausedNotification() {
        Log.d(TAG, "showPausedNotification() is called.");
        NotificationCompat.Builder builder = MediaStyleHelper.from(this, mMediaSession);
        if( builder == null ) {
            return;
        }

        builder.addAction(new androidx.core.app.NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mMediaSession.getSessionToken()));
        builder.setSmallIcon(R.mipmap.notification_icon);
        NotificationManagerCompat.from(this).notify(1, builder.build());
    }

    public synchronized void showStoppedNotification() {
        Log.d(TAG, "showStoppedNotification() is called.");
        NotificationCompat.Builder builder = MediaStyleHelper.from(this, mMediaSession);
        if( builder == null ) {
            return;
        }

        builder.addAction(new androidx.core.app.NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mMediaSession.getSessionToken()));
        builder.setSmallIcon(R.mipmap.notification_icon);
        NotificationManagerCompat.from(this).notify(1, builder.build());
    }

    public synchronized boolean successfullyRetrievedAudioFocus() {
        Log.d(TAG, "successfullyRetrievedAudioFocus() is called.");
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        Log.d(TAG, "result = " + result);
        if (result == AudioManager.AUDIOFOCUS_GAIN) {
            Log.d(TAG, "AudioFocus is AudioManager.AUDIOFOCUS_GAIN");
        } else {
            Log.d(TAG, "AudioFocus is not AudioManager.AUDIOFOCUS_GAIN");
        }

        return result == AudioManager.AUDIOFOCUS_GAIN;
    }
}
