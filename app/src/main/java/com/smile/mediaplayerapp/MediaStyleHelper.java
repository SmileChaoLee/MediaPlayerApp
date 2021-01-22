package com.smile.mediaplayerapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

/**
 * Helper APIs for constructing MediaStyle notifications
 */
public class MediaStyleHelper {
    public static final String CHANNEL_ID = "MediaStyleHelper";
    /**
     * Build a notification using the information from the given media session. Makes heavy use
     * of {@link MediaMetadataCompat#getDescription()} to extract the appropriate information.
     * @param context Context used to construct the notification.
     * @param mediaSession Media session to get information.
     * @return A pre-built notification with information from the given media session.
     */
    public static NotificationCompat.Builder from(
            Context context, MediaSessionCompat mediaSession) {
        MediaControllerCompat controller = mediaSession.getController();
        MediaMetadataCompat mediaMetadata = controller.getMetadata();
        MediaDescriptionCompat description = mediaMetadata.getDescription();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setContentTitle(description.getTitle())
               .setContentText(description.getSubtitle())
               .setSubText(description.getDescription())
               .setLargeIcon(description.getIconBitmap())
               .setContentIntent(controller.getSessionActivity())
               .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))
               .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        // create notification channel for  Android 8.0 or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "MediaSessionCompat";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        //
        return builder;
    }
}
