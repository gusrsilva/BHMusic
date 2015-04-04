package com.blockhead.bhmusic;



import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.util.Log;
import android.widget.Toast;

import com.blockhead.bhmusic.MusicService;

/**
 * Keeps track of a notification and updates it automatically for a given
 * MediaSession. Maintaining a visible notification (usually) guarantees that the music service
 * won't be killed during playback.
 */
public class MediaNotificationManager extends BroadcastReceiver {
    private static final String TAG = "TAG";

    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;

    public static final String ACTION_PAUSE = "com.example.android.uamp.pause";
    public static final String ACTION_PLAY = "com.example.android.uamp.play";
    public static final String ACTION_PREV = "com.example.android.uamp.prev";
    public static final String ACTION_NEXT = "com.example.android.uamp.next";
    public static final String ACTION_STOP_CASTING = "com.exmaple.android.uamp.stop_cast";

    private final MusicService mService;

    private NotificationManager mNotificationManager;

    private PendingIntent mPauseIntent;
    private PendingIntent mPlayIntent;
    private PendingIntent mPreviousIntent;
    private PendingIntent mNextIntent;

    private PendingIntent mStopCastIntent;

    private int mNotificationColor;

    private boolean mStarted = false;

    public MediaNotificationManager(MusicService service) {
        mService = service;

        mNotificationColor = mService.getResources().getColor(R.color.notification_bg);

        mNotificationManager = (NotificationManager) mService
                .getSystemService(Context.NOTIFICATION_SERVICE);

        String pkg = mService.getPackageName();
        mPauseIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPlayIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPreviousIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mNextIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mStopCastIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_STOP_CASTING).setPackage(pkg),
                PendingIntent.FLAG_CANCEL_CURRENT);

        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        mNotificationManager.cancelAll();
    }

    /**
     * Posts the notification and starts tracking the session to keep it
     * updated. The notification will automatically be removed if the session is
     * destroyed before {@link #stopNotification} is called.
     */
    public void startNotification() {
            // The notification must be updated after setting started to true
            Notification notification = createNotification();

            if (notification != null) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_NEXT);
                filter.addAction(ACTION_PAUSE);
                filter.addAction(ACTION_PLAY);
                filter.addAction(ACTION_PREV);
                filter.addAction(ACTION_STOP_CASTING);
                mService.registerReceiver(this, filter);

                mService.startForeground(NOTIFICATION_ID, notification);
            }
    }

    /**
     * Removes the notification and stops tracking the session. If the session
     * was destroyed this has no effect.
     */
    public void stopNotification() {
        if (mStarted) {
            mStarted = false;
            try {
                mNotificationManager.cancel(NOTIFICATION_ID);
                mService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
            mService.stopForeground(true);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "Received intent with action " + action);
        switch (action) {
            case ACTION_PAUSE:
                mService.pausePlayer();
                startNotification();
                break;
            case ACTION_PLAY:
               mService.resumePlayer();
                startNotification();
                break;
            case ACTION_NEXT:
                mService.playNext();

                break;
            case ACTION_PREV:
                mService.playPrev();
                break;
            case ACTION_STOP_CASTING:
                Intent i = new Intent(context, MusicService.class);
                //i.setAction(MusicService.ACTION_CMD);
                //i.putExtra(MusicService.CMD_NAME, MusicService.CMD_STOP_CASTING);
                mService.startService(i);
                break;
            default:
                Log.d(TAG, "Unknown intent ignored. Action=" + action);
        }
    }


    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(mService, NowPlaying.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(mService, REQUEST_CODE, openUI,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }


    private Notification createNotification() {

        Notification.Builder notificationBuilder = new Notification.Builder(mService);
        int playPauseButtonPosition = 0;

        // If skip to previous action is enabled
            Log.d("BHCA", "2");
            notificationBuilder.addAction(R.drawable.ic_skip_previous_black_48dp,
                    mService.getString(R.string.label_previous), mPreviousIntent);

            // If there is a "skip to previous" button, the play/pause button will
            // be the second one. We need to keep track of it, because the MediaStyle notification
            // requires to specify the index of the buttons (actions) that should be visible
            // when in compact view.
            playPauseButtonPosition = 1;

        addPlayPauseAction(notificationBuilder);

        // If skip to next action is enabled

            Log.d("BHCA", "3");
            notificationBuilder.addAction(R.drawable.ic_skip_next_black_48dp,
                    mService.getString(R.string.label_next), mNextIntent);


        Bitmap art = mService.getSongCover();

        Log.d("BHCA", "4");

        notificationBuilder
                .setStyle(new Notification.MediaStyle()
                        .setShowActionsInCompactView(
                                new int[]{playPauseButtonPosition}))
                .setColor(mNotificationColor)
                .setSmallIcon(R.drawable.status_icon)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(createContentIntent())
                .setContentTitle(mService.getSongTitle())
                .setContentText(mService.getSongArtist())
                .setShowWhen(false)
                .setLargeIcon(art);

        Log.d("BHCA", "5");
        setNotificationPlaybackState(notificationBuilder);
        Log.d("BHCA", "6");
        return notificationBuilder.build();
    }

    private void addPlayPauseAction(Notification.Builder builder) {
        String label;
        int icon;
        PendingIntent intent;
        if (mService.isPng()) {
            label = mService.getString(R.string.label_pause);
            icon = R.drawable.ic_pause_black_48dp;
            intent = mPauseIntent;
        } else {
            label = mService.getString(R.string.label_play);
            icon = R.drawable.ic_play_black_48dp;
            intent = mPlayIntent;
        }
        builder.addAction(new Notification.Action(icon, label, intent));
    }

    private void setNotificationPlaybackState(Notification.Builder builder) {
        if (!mService.isPng())
        {
            mService.stopForeground(true);
        }

        // Make sure that the notification can be dismissed by the user when we are not playing:
        builder.setOngoing(mService.isPng());
    }

}
