package com.smile.mediaplayerapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.support.v4.media.MediaBrowserCompat;

import com.smile.mediaplayerapp.utilities.ContentUriAccessUtil;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int SELECT_FILES = 1;

    private MenuItem playMenuItem;
    private MenuItem pauseMenuItem;

    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;
    private MediaControllerCallback mMediaControllerCallback;
    private MediaControllerCompat.TransportControls mTransportControls;
    private MediaMetadataCompat mMetadata;

    // private int mCurrentState;

    private final MediaBrowserCompat.ConnectionCallback mMediaBrowserConnectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    Log.d(TAG,"onConnected() is called.");
                    // Get the token for the MediaSession
                    Log.d(TAG, "mMediaBrowser = " + mMediaBrowser);
                    MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();

                    // Create a MediaControllerCompat
                    try {
                        mMediaController = new MediaControllerCompat(MainActivity.this // Context
                                , token);
                        Log.d(TAG, "mMediaController = " + mMediaController);

                        // Save the controller
                        MediaControllerCompat.setMediaController(MainActivity.this, mMediaController);

                        // Register a Callback to stay in sync
                        mMediaControllerCallback = new MediaControllerCallback(MainActivity.this);
                        Log.d(TAG, "mMediaControllerCallback = " + mMediaControllerCallback);
                        mMediaController.registerCallback(mMediaControllerCallback);

                        mTransportControls = mMediaController.getTransportControls();

                        // Display the initial state
                        mMetadata = mMediaController.getMetadata();
                        Log.d(TAG, "mMetadata = " + mMetadata);

                        // play mp3 file in the raw of resource
                        mTransportControls.prepareFromMediaId(String.valueOf(R.raw.warner_tautz_off_broadway), null);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onConnectionSuspended() {
                    // The Service has crashed. Disable transport controls until it automatically reconnects
                    Log.d(TAG,"onConnectionSuspended() is called.");
                }

                @Override
                public void onConnectionFailed() {
                    // The Service has refused our connection
                    Log.d(TAG,"onConnectionFailed() is called.");
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMediaBrowser = new MediaBrowserCompat(
                this,
                new ComponentName(this, MediaPlaybackService.class), // Bind browser service
                mMediaBrowserConnectionCallbacks,  // Set the connection callback
                getIntent().getExtras()
        );

        // mCurrentState = PlaybackStateCompat.STATE_NONE;

        mMediaBrowser.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        playMenuItem = menu.findItem(R.id.play_menu);
        playMenuItem.setVisible(true);
        playMenuItem.setEnabled(true);
        pauseMenuItem = menu.findItem(R.id.pause_menu);
        pauseMenuItem.setVisible(false);
        pauseMenuItem.setEnabled(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG, "mMediaBrowser.isConnected()= " + mMediaBrowser.isConnected());
        switch (id) {
            case R.id.open_menu:
                selectFileToOpen();
                break;
            case R.id.play_menu:
                /*
                // test code
                if (!mMediaBrowser.isConnected()) {
                    // not connected
                    Log.d(TAG, "Calling mMediaBrowser.connect() ....");
                    mMediaBrowser.connect();
                }
                */
                // mComponentName = new ComponentName(getPackageName(), "MediaPlaybackService");
                if (mMediaBrowser.isConnected()) {
                    playMedia();
                }
                break;
            case R.id.pause_menu:
                pauseMedia();
                break;
            case R.id.stop_menu:
                stopMedia();
                // test code
                // Log.d(TAG, "Calling mMediaBrowser.disconnect() ....");
                // mMediaBrowser.disconnect();
                /*
                if (mComponentName != null) {
                    String packageName = mComponentName.getPackageName();
                    String className = mComponentName.getClassName();
                    Log.d(TAG, "Package name = " + packageName);
                    Toast.makeText(this, "Package name = " + packageName, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Class name = " + className);
                    Toast.makeText(this, "Class name = " + className, Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "mComponentName = null");
                    Toast.makeText(this, "mComponentName = null", Toast.LENGTH_SHORT).show();
                }
                */
                break;
            default:
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() is called.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() is called.");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy() is called.");
        super.onDestroy();
        if (mMediaBrowser != null) {
            mMediaBrowser.disconnect();
            if (mMediaController!=null && mMediaControllerCallback!=null) {
                mMediaController.unregisterCallback(mMediaControllerCallback);
                mMediaController = null;
                mMediaControllerCallback = null;
                mMetadata = null;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data == null) super.onActivityResult(requestCode, resultCode, data);

        // get the selected file list
        ArrayList<Uri> uriArrayList = ContentUriAccessUtil.getUrisList(this, data);
        // ArrayList<Uri> uriArrayList = getUrisList(this, data);
        for (Uri uri : uriArrayList) {
            Log.d(TAG, uri.toString());
        }
    }

    private void selectFileToOpen() {
        // ContentUriAccessUtil.selectFileToOpen(this, SELECT_FILES, false);   // not single file
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // open multiple files
        intent.setType("*/*");
        startActivityForResult(intent, SELECT_FILES);   // might be deprecated
    }

    @SuppressLint("WrongConstant")
    private ArrayList<Uri> getUrisList(Context context, Intent data) {
        ArrayList<Uri> uriArrayList = new ArrayList<>();
        // get persist permission for uris
        Uri contentUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (data.getClipData() != null) {
                // multiple files
                int count = data.getClipData().getItemCount();
                for (int i=0; i<count; i++) {
                    contentUri = data.getClipData().getItemAt(i).getUri();
                    if (contentUri != null && !Uri.EMPTY.equals(contentUri)) {
                        try {
                            int taskFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            context.getContentResolver().takePersistableUriPermission(contentUri, taskFlags);
                            uriArrayList.add(contentUri);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                // single file
                contentUri = data.getData();
                if (contentUri!=null && !Uri.EMPTY.equals(contentUri)) {
                    try {
                        int taskFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        context.getContentResolver().takePersistableUriPermission(contentUri, taskFlags);
                        uriArrayList.add(contentUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            // no permission needed
            if (data.getClipData() != null) {
                // multiple files
                int count = data.getClipData().getItemCount();
                for (int i=0; i<count; i++) {
                    contentUri = data.getClipData().getItemAt(i).getUri();
                    if (contentUri!=null && !Uri.EMPTY.equals(contentUri)) {
                        uriArrayList.add(contentUri);
                    }
                }
            } else {
                // single file
                contentUri = data.getData();
                if (contentUri!=null && !Uri.EMPTY.equals(contentUri)) {
                    uriArrayList.add(contentUri);
                }
            }
        }

        return uriArrayList;
    }

    private void playMedia() {
        Log.d(TAG, "playMedia() is called.");
        if (!mMediaBrowser.isConnected()) {
            Log.d(TAG, "MediaPlaybackService is not connected.");
            return;
        }
        // mCurrentState = mMediaController.getPlaybackState().getState();
        // Log.d(TAG, "mCurrentState = " + mCurrentState);
        // if (mCurrentState != PlaybackStateCompat.STATE_PLAYING) {
            mTransportControls.play();
        // }
    }
    private void pauseMedia() {
        Log.d(TAG, "pauseMedia() is called.");
        if (!mMediaBrowser.isConnected()) {
            Log.d(TAG, "MediaPlaybackService is not connected.");
            return;
        }
        // mCurrentState = mMediaController.getPlaybackState().getState();
        // Log.d(TAG, "mCurrentState = " + mCurrentState);
        // if (mCurrentState != PlaybackStateCompat.STATE_PAUSED){
            mTransportControls.pause();
        // }
    }

    private void stopMedia() {
        Log.d(TAG, "stopMedia() is called.");
        if (!mMediaBrowser.isConnected()) {
            Log.d(TAG, "MediaPlaybackService is not connected.");
            return;
        }
        // mCurrentState = mMediaController.getPlaybackState().getState();
        // Log.d(TAG, "mCurrentState = " + mCurrentState);
        // if (mCurrentState != PlaybackStateCompat.STATE_STOPPED) {
            mTransportControls.stop();
        // }
        // re-create metadata
        // mTransportControls.prepareFromMediaId(String.valueOf(R.raw.warner_tautz_off_broadway), null);
    }

    public void setPlayMenuItem(boolean visible) {
        playMenuItem.setVisible(visible);
        playMenuItem.setEnabled(visible);
    }
    public void setPauseMenuItem(boolean visible) {
        pauseMenuItem.setVisible(visible);
        pauseMenuItem.setEnabled(visible);
    }
}