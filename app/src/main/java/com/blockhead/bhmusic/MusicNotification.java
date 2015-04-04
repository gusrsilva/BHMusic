package com.blockhead.bhmusic;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Gus on 3/22/2015.
 */
public class MusicNotification extends BroadcastReceiver {

    private MusicService mService;
    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;

    public static final String ACTION_PAUSE = "com.blockhead.bhmusic.pause";
    public static final String ACTION_PLAY = "com.blockhead.bhmusic.play";
    public static final String ACTION_PREV = "com.blockhead.bhmusic.prev";
    public static final String ACTION_NEXT = "com.blockhead.bhmusic.next";
    private PendingIntent mPauseIntent;
    private PendingIntent mPlayIntent;
    private PendingIntent mPreviousIntent;
    private PendingIntent mNextIntent;

    public MusicNotification(MusicService musicService)
    {
        mService = musicService;

        //Initialize Pending Intents
        String pkg = mService.getPackageName();
        mPauseIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPlayIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPreviousIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mNextIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public void startNotification()
    {

    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
