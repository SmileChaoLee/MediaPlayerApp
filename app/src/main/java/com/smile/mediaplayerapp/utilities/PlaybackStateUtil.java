package com.smile.mediaplayerapp.utilities;

import android.support.v4.media.session.PlaybackStateCompat;

public class PlaybackStateUtil {

    private PlaybackStateUtil() {};

    public static PlaybackStateCompat getMediaPlaybackState(int state) {
        PlaybackStateCompat.Builder playbackStateBuilder = new PlaybackStateCompat.Builder();

        if( state == PlaybackStateCompat.STATE_PLAYING ) {
            playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE);
        } else {
            playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY);
        }
        playbackStateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);

        PlaybackStateCompat playbackStateCompat = playbackStateBuilder.build();

        return playbackStateCompat;
    }
}
