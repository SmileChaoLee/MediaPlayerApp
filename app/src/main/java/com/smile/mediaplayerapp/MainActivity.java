package com.smile.mediaplayerapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

import com.example.mediaplayerapp.R;
import com.smile.mediaplayerapp.utilities.ContentUriAccessUtil;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int SELECT_FILES = 1;

    private MediaBrowserCompat mMediaBrowser;
    MediaControllerCompat mMediaController;
    MediaControllerCallback mMediaControllerCallback;
    MediaMetadataCompat mMetadata;

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
                        mMediaControllerCallback = new MediaControllerCallback();
                        Log.d(TAG, "mMediaControllerCallback = " + mMediaControllerCallback);
                        mMediaController.registerCallback(mMediaControllerCallback);

                        // Display the initial state
                        mMetadata = mMediaController.getMetadata();
                        Log.d(TAG, "mMetadata = " + mMetadata);

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
        null
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        if (menuInflater != null) {
            menuInflater.inflate(R.menu.menu_main, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.open_menu:
                selectFileToOpen(this);
                break;
            case R.id.play_menu:
                playMedia();
                break;
            case R.id.stop_menu:
                stopMedia();
                break;
            default:
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();
        //Browser sends a connection request
        Log.d(TAG, "onStart() is called.");
        Log.d(TAG, "mMediaBrowser = " + mMediaBrowser);
        if (mMediaBrowser != null) {
            mMediaBrowser.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() is called.");
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
    protected void onDestroy() {
        Log.d(TAG, "onDestroy() is called.");
        super.onDestroy();
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

    private void selectFileToOpen(Context context) {
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

    }
    private void stopMedia() {

    }
}