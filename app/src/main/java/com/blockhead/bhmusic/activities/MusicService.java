package com.blockhead.bhmusic.activities;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.blockhead.bhmusic.R;
import com.blockhead.bhmusic.objects.Album;
import com.blockhead.bhmusic.objects.Playlist;
import com.blockhead.bhmusic.objects.Song;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    public static final String ACTION_PAUSE_PLAY = "com.blockhead.bhmusic.pauseplay";
    public static final String ACTION_PREV = "com.blockhead.bhmusic.prev";
    public static final String ACTION_NEXT = "com.blockhead.bhmusic.next";
    public static final String ACTION_CLOSE = "com.blockhead.bhmusic.close";
    public static final int REPEAT_NONE = 0;
    public static final int REPEAT_ONE = 1;
    public static final int REPEAT_ALL = 2;
    private static final int NOTIFICATION_ID = 27;
    private static final int REQUEST_CODE = 127;
    private static NotificationListener notificationListener;
    private final IBinder musicBind = new MusicBinder();
    public boolean shuffle = false;
    public int repeat = 0;
    //media player
    private MediaPlayer player;
    //song list
    private ArrayList<Song> songs, albumSongs, playlistSongs;
    private Stack<Integer> shuffleStack;
    private int songPosn, albumPosn, playlistPosn;
    private String songTitle = "";
    private String songArtist = "";
    private String songAlbum = "";
    private String coverURI;
    private Bitmap superBlurredCover;
    public boolean isPngAlbum = false, isPngPlaylist = false;
    private Random rand;
    private RenderScript rs;
    private int cTransparent;
    private Album currAlbum;
    private Playlist currPlaylist;
    private Song albumSong, playSong, playlistSong;
    private AudioManager audioManager;
    private boolean wasPlaying = false;
    private Notification.Builder mBuilder;
    private Notification notification;
    private NotificationManager notificationManager;
    private RemoteViews notificationView, smallNotificationView;
    private Toast mToast;
    private ImageLoader imageLoader;
    public boolean isFinished = false;
    private int currentApi;


    @Override
    public void onCreate() {
        super.onCreate();
        currentApi = android.os.Build.VERSION.SDK_INT;
        imageLoader = ImageLoader.getInstance(); // Get singleton instance
        songPosn = -1;
        player = new MediaPlayer();
        initMusicPlayer();
        rand = new Random();
        cTransparent = getResources().getColor(R.color.transparent);
        //mNotMan = new MediaNotificationManager(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        getAudioFocus();

        //Set Notification Listener
        notificationListener = new NotificationListener();
        registerReceiver(notificationListener, new IntentFilter(ACTION_PAUSE_PLAY));
        registerReceiver(notificationListener, new IntentFilter(ACTION_PREV));
        registerReceiver(notificationListener, new IntentFilter(ACTION_NEXT));
        registerReceiver(notificationListener, new IntentFilter(ACTION_CLOSE));

        mToast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        //Unregister Notification Listener
        if (notificationListener != null) {
            unregisterReceiver(notificationListener);
            notificationListener = null;
        }
    }

    private boolean getAudioFocus() {
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        return (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }

    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                //Resume playback
                if (player == null) initMusicPlayer();
                else if (!player.isPlaying()) {
                    if (wasPlaying)
                        resumePlayer();
                }
                player.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (player.isPlaying()) {
                    stopPlayer();
                    wasPlaying = true;
                } else {
                    wasPlaying = false;
                }
                //player.release();
                //player = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (player.isPlaying()) {
                    pausePlayer();
                    wasPlaying = true;
                } else {
                    wasPlaying = false;
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (player.isPlaying()) {
                    player.setVolume(0.1f, 0.1f);
                    wasPlaying = true;
                } else
                    wasPlaying = false;
                break;
        }
    }


    public void initMusicPlayer() {
        //set player properties
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSongs) {
        songs = theSongs;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        player = null;
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (player.getCurrentPosition() > 0 && !isLastSong())
        {
            mp.reset();
            playNext();
        }
        else if (shuffle || repeat == REPEAT_ALL || repeat == REPEAT_ONE)
        {
            mp.reset();
            playNext();
        }

        else
        {
            stopPlayer();
            isFinished = true;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //blur and set cover
        if (coverURI != null && MainActivity.artworkHeader) {
            MainActivity.fauxAB.setBackgroundColor(cTransparent);
            MainActivity.pagerTitleStrip.setBackgroundColor(cTransparent);
            MainActivity.coverArt.setImageBitmap(superBlurredCover);
        } else {
            MainActivity.fauxAB.setBackgroundColor(MainActivity.primaryColor);
            MainActivity.pagerTitleStrip.setBackgroundColor(MainActivity.primaryColor);
        }


        //start playback
        mp.start();

        wasPlaying = true;

        //mNotMan.startNotification();
        createNotification();
    }

    private boolean isLastSong()
    {
        try
        {
            if(isPngAlbum && ((albumPosn+1)>=albumSongs.size()))
                return true;
            else if(isPngPlaylist && ((playlistPosn+1)>=playlistSongs.size()))
                return true;
            else if((songPosn+1)>=songs.size())
                return true;
            else
                return false;
        }
        catch(NullPointerException e)
        {
            return true;
        }
    }

    public void playSong() {
        isPngAlbum = false;
        isPngPlaylist = false;
        player.reset();
        playSong = songs.get(songPosn);
        songTitle = playSong.getTitle();
        songArtist = playSong.getArtist();
        songAlbum = playSong.getAlbumTitle();
        //songCover = playSong.getCover();
        coverURI = playSong.getCoverURI();
        superBlurredCover = blurBitmapStrong(coverURI);

        long currSong = playSong.getID();
        Uri trackUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);

        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("BHCA", "Error setting data source", e);
        }

        //call onPrepared()
        player.prepareAsync();
    }

    public void setSong(int songIndex) {
        songPosn = songIndex;
    }

    public int getPosn() {
        return player.getCurrentPosition();
    }

    public int getDur() {
        return player.getDuration();
    }

    public boolean isPng() {
        if (player != null)
            return player.isPlaying();
        else
            return false;
    }

    public void pausePlayer() {
        try {
            player.pause();
            notificationView.setImageViewResource(R.id.notificationPlayPause, R.drawable.ic_play_black_48dp);
            smallNotificationView.setImageViewResource(R.id.small_notificationPlayPause, R.drawable.ic_play_black_36dp);
            notification = mBuilder.setOngoing(false).build();
            notification.bigContentView = notificationView;
            notificationManager.notify(NOTIFICATION_ID, notification);
        } catch (Exception e) {
            Log.d("BHCA", "Error: " + e.getMessage());
        }

        wasPlaying = false;
    }

    public void stopPlayer() {
        pausePlayer();
        player.stop();
    }

    public void resumePlayer() {
        try {
            player.start();
            notification = mBuilder.setOngoing(true).build();
            notification.bigContentView = notificationView;
            notificationView.setImageViewResource(R.id.notificationPlayPause, R.drawable.ic_pause_black_48dp);
            smallNotificationView.setImageViewResource(R.id.small_notificationPlayPause, R.drawable.ic_pause_black_36dp);
            notificationManager.notify(NOTIFICATION_ID, notification);
        } catch (Exception e) {
            Log.d("BHCA", "Error: " + e.getMessage());
        }

    }

    public void seek(int posn) {
        player.seekTo(posn);
    }

    public void go() {
        resumePlayer();
    }

    public void playPrev() {
        if(songs.size() == 0)
        {
            Toast.makeText(getApplicationContext(), "No songs found on device.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isPngAlbum)
        {
            if(getPosn() > 20000)
                seek(0);
            else if(shuffle && !shuffleStack.isEmpty())
            {
                playAlbum(currAlbum, shuffleStack.pop());
            }
            else
            {
                if ((albumPosn - 1) < 0)
                    albumPosn = albumSongs.size() - 1;
                playAlbum(currAlbum, albumPosn - 1);
            }
        }
        else if (isPngPlaylist)
        {
            if(getPosn() > 20000)
                seek(0);
            else if(shuffle && shuffleStack != null && !shuffleStack.isEmpty())
            {
                playPlaylist(currPlaylist, shuffleStack.pop());
            }
            else
            {
                if ((playlistPosn - 1) < 0)
                    playlistPosn = playlistSongs.size() - 1;
                playPlaylist(currPlaylist, playlistPosn - 1);
            }
        }
        else
        {
            if(getPosn() > 20000)       //First restart current song
                seek(0);
            else if(shuffle && shuffleStack != null && !shuffleStack.empty())
            {
                //Play last song added to shuffle stack
                songPosn = shuffleStack.pop();
                playSong();
            }
            else
            {
                songPosn--;
                if (songPosn < 0) songPosn = songs.size() - 1;
                playSong();
            }
        }
    }

    public void playNext() {
        if(songs.size() == 0)
        {
            Toast.makeText(getApplicationContext(), "No songs found on device.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isPngAlbum) //If On Album
        {
            if (repeat == REPEAT_ONE)
            {
                albumPosn--;    //Stay on same song
            }
            else if (shuffle)
            {
                int newSong = albumPosn;

                if(shuffleStack == null)
                    shuffleStack = new Stack<>();
                shuffleStack.push(newSong);

                while (newSong == albumPosn)
                {
                    newSong = rand.nextInt(albumSongs.size());
                }
                albumPosn = newSong;
            }
            else
            {   //If End of List
                if (isLastSong())
                {
                    if (repeat == REPEAT_ALL)
                    {
                        if(albumSongs.size() == 1)
                            albumPosn--;
                        else
                        {
                            playAlbum(currAlbum, 0);    //Restart album
                            return;
                        }
                    }
                    else {
                        albumPosn=0;
                        playAlbum(currAlbum, albumPosn);
                        pausePlayer();
                        return;
                    }
                }
            }
            playAlbum(currAlbum, albumPosn+1);
        }
        else if (isPngPlaylist) //If On Playlist
        {
            if (repeat == REPEAT_ONE)
            {
                playlistPosn--; //Stay on same song
            }
            else if (shuffle)
            {
                int newSong = playlistPosn;

                if(shuffleStack == null)
                    shuffleStack = new Stack<>();

                shuffleStack.push(newSong);

                while (newSong == playlistPosn)
                {
                    newSong = rand.nextInt(playlistSongs.size());
                }
                playlistPosn = newSong;
            }
            else
            {
                if (isLastSong())
                {
                    if (repeat == REPEAT_ALL)
                    {
                        if(playlistSongs.size() == 1)
                            playlistPosn--;
                        else
                        {
                            playPlaylist(currPlaylist, 0);    //Restart album
                            return;
                        }
                    }
                    else
                    {
                        playlistPosn=0;
                        playPlaylist(currPlaylist, playlistPosn);
                        pausePlayer();
                        return;
                    }
                }
            }
            playPlaylist(currPlaylist, playlistPosn+1);
        }
        else //If Not On Album or Playlist
        {
            if (repeat == REPEAT_ONE)
            {
                //stay on same song
            }
            else if (shuffle)
            {
                int newSong = songPosn;
                if(shuffleStack == null)
                    shuffleStack = new Stack<>();
                shuffleStack.push(newSong);    //Add previous song position

                while (newSong == songPosn)
                    newSong = rand.nextInt(songs.size());

                songPosn = newSong;
            }
            else
            {
                songPosn++;
                if (isLastSong()) {
                    if (repeat == REPEAT_ALL)
                        songPosn = 0;
                    else {
                        stopPlayer();
                        return;
                    }
                }
            }
            playSong();
        }

    }

    public void setShuffle() {
        if (shuffle) {
            shuffle = false;
            mToast.setText("Shuffle Off");
            mToast.show();
        } else {
            shuffle = true;
            shuffleStack = new Stack<>();
            mToast.setText("Shuffle On");
            mToast.show();
        }
    }

    public void setRepeat() {
        if (repeat == REPEAT_NONE) {
            repeat = REPEAT_ALL;
            mToast.setText("Repeat All");
            mToast.show();
        } else if (repeat == REPEAT_ALL) {
            repeat = REPEAT_ONE;
            mToast.setText("Repeat Current");
            mToast.show();
        } else if (repeat == REPEAT_ONE) {
            repeat = REPEAT_NONE;
            mToast.setText("Repeat Off");
            mToast.show();
        }
    }

    public int getRepeat() {
        return repeat;
    }

    //BEGIN GET FUNCTIONS
    public String getSongTitle() {
        return songTitle;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public String getSongAlbum() {
        return songAlbum;
    }

    /*
    public Bitmap getSongCover() {
        return songCover;
    }
    */

    public String getCoverURI(){ return  coverURI; }

    public Song getCurrSong()
    {
        if(songPosn == -1)
            return null;
        else if(isPngAlbum)
            return albumSong;
        else if(isPngPlaylist)
            return playlistSong;
        else
            return songs.get(songPosn);
    }

    public Playlist getCurrPlaylist(){ return currPlaylist; }

    public Bitmap getSuperBlurredCover() {
        if(superBlurredCover == null && coverURI != null)
            superBlurredCover = blurBitmapStrong(coverURI);
        return superBlurredCover;
    }

    //Begin Blur Functions
    public Bitmap blurBitmap(Bitmap orig) {
        Bitmap blur;
        if (orig != null) {
            //Blur & Set coverArt
            blur = orig.copy(orig.getConfig(), true);
            //this will blur the bitmapOriginal with a radius of 8 and save it in bitmapOriginal
            rs = RenderScript.create(getApplicationContext());
            final Allocation input = Allocation.createFromBitmap(rs, blur); //use this constructor for best performance, because it uses USAGE_SHARED mode which reuses memory
            final Allocation output = Allocation.createTyped(rs, input.getType());
            final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            script.setRadius(4.f);
            script.setInput(input);
            script.forEach(output);
            output.copyTo(blur);

            return blur;
        } else
            return null;
    }

    public Bitmap blurBitmapStrong(String Uri) {
        Bitmap bitmap = blurBitmap(imageLoader.loadImageSync(Uri));
        if (bitmap != null) {
            //Blur & Set coverArt
            bitmap = bitmap.copy(bitmap.getConfig(), true);
            //this will blur the bitmapOriginal with a radius of 8 and save it in bitmapOriginal
            rs = RenderScript.create(getApplicationContext());
            final Allocation input = Allocation.createFromBitmap(rs, bitmap); //use this constructor for best performance, because it uses USAGE_SHARED mode which reuses memory
            final Allocation output = Allocation.createTyped(rs, input.getType());
            final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            script.setRadius(16.f);
            script.setInput(input);
            script.forEach(output);
            output.copyTo(bitmap);

            return bitmap;
        } else
            return null;
    }

    //Begin Album Functions
    public void playAlbum(Album alb, int pos) {

        if(currAlbum == null || currAlbum != alb)
            shuffleStack = new Stack<>();

        if (currAlbum != alb || pos != albumPosn)
        {
            currAlbum = alb;
            albumPosn = pos;
            albumSongs = alb.tracks;

            player.reset();
            isPngAlbum = true;

            albumSong = albumSongs.get(albumPosn);

            songTitle = albumSong.getTitle();
            songArtist = albumSong.getArtist();
            songAlbum = albumSong.getAlbumTitle();
            //songCover = albumSong.getCover();
            coverURI = albumSong.getCoverURI();
            superBlurredCover = blurBitmapStrong(coverURI);

            long currSong = albumSong.getID();
            Uri trackUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);

            try {
                player.setDataSource(getApplicationContext(), trackUri);
            } catch (Exception e) {
                Log.e("BHCA", "Error setting data source", e);
            }

            //call onPrepared()
            player.prepareAsync();
        }

    }

    public void playPlaylist(Playlist playList, int pos) {

        if(currPlaylist == null || currPlaylist != playList)
            shuffleStack = new Stack<>();

        if (currPlaylist != playList || pos != playlistPosn)
        {   //If song is different than the one currently playing
            currPlaylist = playList;
            playlistSongs = playList.getMembers();
            playlistPosn = pos % playlistSongs.size();

            player.reset();
            isPngPlaylist = true;

            playlistSong = playlistSongs.get(playlistPosn);


            songTitle = playlistSong.getTitle();
            songArtist = playlistSong.getArtist();
            songAlbum = playlistSong.getAlbumTitle();
            //songCover = albumSong.getCover();
            coverURI = playlistSong.getCoverURI();
            superBlurredCover = blurBitmapStrong(coverURI);

            long currSong = playlistSong.getID();
            Uri trackUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);

            try {
                player.setDataSource(getApplicationContext(), trackUri);
            } catch (Exception e) {
                Log.e("BHCA", "Error setting data source", e);
            }

            //call onPrepared()
            player.prepareAsync();
        }

    }

    //Notification Functions
    @TargetApi(21)
    private void createNotification() {

        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent playPausePendingIntent, prevPendingIntent, nextPendingIntent, closePendingIntent;


        //Set Large Notification View
        notificationView = new RemoteViews(getPackageName(), R.layout.notification_large);
        notificationView.setTextViewText(R.id.notificationTitle, getSongTitle());
        notificationView.setTextViewText(R.id.notificationText, getSongArtist());
        //Set Cover

        Bitmap cov = imageLoader.loadImageSync(coverURI);
        if (cov == null)
            notificationView.setImageViewResource(R.id.notificationImage, R.drawable.default_cover_xlarge);
        else
            notificationView.setImageViewBitmap(R.id.notificationImage, cov);

        //Set Small Notification View
        smallNotificationView = new RemoteViews(getPackageName(), R.layout.notification_small);
        smallNotificationView.setTextViewText(R.id.small_notificationTitle, getSongTitle());
        smallNotificationView.setTextViewText(R.id.small_notificationText, getSongArtist());
        //Set Cover;
        if (cov == null)
            smallNotificationView.setImageViewResource(R.id.small_notificationImage, R.drawable.default_cover_xlarge);
        else
            smallNotificationView.setImageViewBitmap(R.id.small_notificationImage, cov);


        //the intent that is started when the notification is clicked (works)
        Intent contentIntent = new Intent(this, NowPlayingActivity.class);
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingContentIntent = PendingIntent.getActivity(this, 0,
                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Play/Pause Intent
        Intent pausePlayIntent = new Intent(ACTION_PAUSE_PLAY);
        playPausePendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, pausePlayIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        //Skip Prev Intent
        Intent skipPrevIntent = new Intent(ACTION_PREV);
        prevPendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, skipPrevIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        //Skip Next Intent
        Intent skipNextIntent = new Intent(ACTION_NEXT);
        nextPendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, skipNextIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        //Close Intent
        Intent closeIntent = new Intent(ACTION_CLOSE);
        closePendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, closeIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        mBuilder = new Notification.Builder(this);


        if(currentApi >= Build.VERSION_CODES.LOLLIPOP)
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);

        notification = mBuilder
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentTitle(getSongTitle())
                .setContentText(getSongArtist())
                .setContent(smallNotificationView)
                .setContentIntent(pendingContentIntent)
                .setSmallIcon(R.drawable.ic_music_circle_white_24dp)
                .setLargeIcon(imageLoader.loadImageSync(coverURI))
                .setDeleteIntent(closePendingIntent)
                .setOngoing(true)
                .setShowWhen(false)
                .build();
        notification.bigContentView = notificationView;

        //startForeground(NOTIFICATION_ID, notification);

        //Attach Listeners to Large Notification Buttons
        notificationView.setOnClickPendingIntent(R.id.notificationPlayPause, playPausePendingIntent);
        notificationView.setOnClickPendingIntent(R.id.notificationPrev, prevPendingIntent);
        notificationView.setOnClickPendingIntent(R.id.notificationNext, nextPendingIntent);

        //Attach Listeners to Small Notification Buttons
        smallNotificationView.setOnClickPendingIntent(R.id.small_notificationPlayPause, playPausePendingIntent);


        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void removeNotification(Context context){
        mBuilder = new Notification.Builder(context);
        notification = mBuilder.setOngoing(false)
                .build();
        if(notificationManager != null)
            notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public class NotificationListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_PAUSE_PLAY:
                    if (isPng()) {
                        pausePlayer();
                    } else {
                        resumePlayer();
                    }
                    break;
                case ACTION_NEXT:
                    playNext();
                    break;
                case ACTION_PREV:
                    playPrev();
                    break;
                case ACTION_CLOSE:
                    stopPlayer();
                    removeNotification(context);
                    break;
            }
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }


}