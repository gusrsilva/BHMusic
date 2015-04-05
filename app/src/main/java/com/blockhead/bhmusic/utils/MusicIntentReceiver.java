package com.blockhead.bhmusic.utils;

import android.content.Context;
import android.content.Intent;

import com.blockhead.bhmusic.activities.MainActivity;
import com.blockhead.bhmusic.activities.MusicService;

/**
 * Created by Gus on 3/21/2015.
 */
public class MusicIntentReceiver extends android.content.BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent.getAction().equals(
                android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            MusicService musicService = MainActivity.getMusicService();
            musicService.pausePlayer();
        }
    }
}
