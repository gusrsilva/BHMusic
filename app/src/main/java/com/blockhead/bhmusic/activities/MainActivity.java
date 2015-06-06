package com.blockhead.bhmusic.activities;


import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blockhead.bhmusic.R;
import com.blockhead.bhmusic.adapters.AlbumAdapter;
import com.blockhead.bhmusic.adapters.ArtistAdapter;
import com.blockhead.bhmusic.adapters.SongAdapter;
import com.blockhead.bhmusic.objects.Album;
import com.blockhead.bhmusic.objects.Artist;
import com.blockhead.bhmusic.objects.Song;
import com.blockhead.bhmusic.utils.DiskLruImageCache;
import com.blockhead.bhmusic.utils.IndexableListView;
import com.blockhead.bhmusic.utils.OnSwipeTouchListener;
import com.blockhead.bhmusic.utils.XMLParser;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity implements MediaPlayerControl {

    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";
    public static ArrayList<Artist> artistList;
    public static ArrayList<Album> albumList;
    public static TextView nowPlayingArtist, nowPlayingTitle;
    public static ImageView coverArt;
    public static ImageButton fab, shuffleButton, repeatButton;
    public static RelativeLayout fauxAB;
    public static android.support.v4.view.PagerTitleStrip pagerTitleStrip;
    public static Album currAlbum;
    public static Artist currArtist;
    public static ActionBar mActionBar;
    public static boolean artworkHeader = true;
    public static int primaryColor, accentColor;
    private static ListView songView;
    private static GridView albumView, artistView;
    private static MusicService musicSrv = new MusicService();
    private static boolean playbackPaused = false;
    private static SeekBar seekBar;
    private static IndexableListView mListView;
    private static SongAdapter songAdt;
    private static AlbumAdapter albumAdt;
    private static ArtistAdapter artistAdt;
    private static String abTitle;
    private static SharedPreferences sharedPref;
    private static ArtistArtUtil runner;
    private final Object mDiskCacheLock = new Object();
    Handler monitorHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            mediaPlayerMonitor();
        }

    };
    private ArrayList<Song> songList;
    private Intent playIntent;
    private boolean musicBound = false;
    private boolean paused = false;
    private boolean shuffle = false;
    private TextView timePos, timeDur;
    private ServiceConnection musicConnection;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private Animation repeatRotationAnimation, shuffleAnimation;
    private Drawable playDrawable, pauseDrawable;
    private static Drawable fabDrawable;
    private DiskLruImageCache mDiskLruCache;

    //DEFINE COLORS FOR USERS TO CHOOSE
    public final static int MATERIAL_RED = 0,MATERIAL_PINK=1,MATERIAL_PURPLE=2,MATERIAL_DEEPPURPLE=3,
    MATERIAL_INDIGO=4,MATERIAL_BLUE=5,MATERIAL_LIGHTBLUE=6,MATERIAL_CYAN=7,MATERIAL_TEAL=8,
    MATERIAL_GREEN=9,MATERIAL_LIGHTGREEN=10,MATERIAL_NEONGREEN=11,MATERIAL_LIME=12,MATERIAL_YELLOW=13,MATERIAL_AMBER=14,
    MATERIAL_ORANGE=15,MATERIAL_DEEPORANGE=16,MATERIAL_GREY=17,MATERIAL_BLUEGREY=18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        mActionBar = getActionBar();

        // Create global configuration and initialize ImageLoader with this config
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
        .build();
        ImageLoader.getInstance().init(config);

        //Read Preferences
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        artworkHeader = sharedPref.getBoolean("artwork_header", true);
        abTitle = sharedPref.getString("main_title", "MUSIC");
        int primaryColorKey, accentColorKey;
        try {
            primaryColorKey = Integer.parseInt(sharedPref.getString("primary_color_key", "4"));
            accentColorKey = Integer.parseInt(sharedPref.getString("accent_color_key", "1"));
        }
        catch(Exception e )
        {
            Log.d("BHCA", "CRASH: " + e.getMessage());
            primaryColorKey = 4;
            accentColorKey = 1;
        }

        //Toast.makeText(getApplicationContext(), "Primary:" + primaryColorKey + " Accent: " + accentColorKey, Toast.LENGTH_LONG).show();
        //Set Colors
        primaryColor = getResources().getColor(getColor(primaryColorKey));
        accentColor = getResources().getColor(getColor(accentColorKey));

        //Set ActionBar Title
        if (mActionBar != null)
            mActionBar.setTitle(abTitle);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        songList = new ArrayList<Song>();
        albumList = new ArrayList<Album>();
        artistList = new ArrayList<Artist>();

        getAlbumList();
        getSongList();
        getArtistList();

        Collections.sort(songList, new Comparator<Song>() {
            @Override
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
        Collections.sort(albumList, new Comparator<Album>() {
            @Override
            public int compare(Album a, Album b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
        Collections.sort(artistList, new Comparator<Artist>() {
            @Override
            public int compare(Artist a, Artist b) {
                return a.getName().compareTo(b.getName());
            }
        });


        runner = new ArtistArtUtil();
        runner.execute();

        /////////////////
        fauxAB = (RelativeLayout) findViewById(R.id.fauxAB);
        fauxAB.setBackgroundColor(primaryColor);
        fauxAB.setOnTouchListener(new OnSwipeTouchListener(this) {
            public void onSwipeBottom() {
                nowPlayingButtonPressed(null);
            }

            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        ////////////////

        songAdt = new SongAdapter(this, songList);
        albumAdt = new AlbumAdapter(this, albumList);
        artistAdt = new ArtistAdapter(this, artistList);


        //connect to the service
        musicConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
                //get service
                musicSrv = binder.getService();
                //pass list
                musicSrv.setList(songList);
                musicBound = true;

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                musicBound = false;
            }
        };

        //Define Views
        shuffleButton = (ImageButton) findViewById(R.id.shuffleButton);
        repeatButton = (ImageButton) findViewById(R.id.repeatButton);
        pagerTitleStrip = (android.support.v4.view.PagerTitleStrip) findViewById(R.id.pager_title_strip);
        seekBar = (SeekBar) findViewById(R.id.progressBar);
        fab = (ImageButton) findViewById(R.id.playButton);
        nowPlayingTitle = (TextView) findViewById(R.id.trackTitle);
        nowPlayingArtist = (TextView) findViewById(R.id.trackArtist);
        coverArt = (ImageView) findViewById(R.id.coverArt);

        //Set Colors
        fabDrawable = fab.getBackground();
        fabDrawable.setColorFilter(accentColor, PorterDuff.Mode.SRC_ATOP);
        pagerTitleStrip.setBackgroundColor(primaryColor);

        //Set Animations
        repeatRotationAnimation = AnimationUtils.loadAnimation(this, R.anim.repeat_rotate_animation);
        shuffleAnimation = AnimationUtils.loadAnimation(this, R.anim.shuffle_rotate_animation);

        //Define Drawables
        pauseDrawable = getResources().getDrawable(R.drawable.pause);
        playDrawable = getResources().getDrawable(R.drawable.play);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;


            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                if (fromUser)
                    progress = progressValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekTo(progress);
            }
        });

        ScheduledExecutorService myScheduledExecutorService = Executors.newScheduledThreadPool(1);
        timePos = (TextView) findViewById(R.id.currTime);
        timeDur = (TextView) findViewById(R.id.totalTime);

        myScheduledExecutorService.scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        monitorHandler.sendMessage(monitorHandler.obtainMessage());
                    }
                },
                0, //initialDelay
                200, //delay
                TimeUnit.MILLISECONDS);

    }

    private static void playNext() {
        musicSrv.playNext();
        if (playbackPaused) {
            playbackPaused = false;
        }
    }

    private static void playPrev() {
        musicSrv.playPrev();
        if (playbackPaused) {
            playbackPaused = false;
        }
    }

    public static String prettyTime(int n) {
        String mins, secs;
        n = n / 1000;
        mins = (n / 60) + "";
        secs = (n % 60) + "";
        if (n % 60 < 10)
            secs = "0" + secs;

        return (mins + ":" + secs);
    }

    public static MusicService getMusicService() {
        return musicSrv;
    }

    public static ImageButton getFAB() {
        return fab;
    }

    public static int randomColor() {
        Random rand = new Random();
        int color = rand.nextInt(18);
        return getColor(color);
    }

    public static int getColor(int color)
    {
        switch(color) {
            case MATERIAL_RED:
                return (R.color.material_red);
            case MATERIAL_PINK:
                return (R.color.material_pink);
            case MATERIAL_PURPLE:
                return (R.color.material_purple);
            case MATERIAL_DEEPPURPLE:
                return (R.color.material_deeppurple);
            case MATERIAL_INDIGO:
                return (R.color.material_indigo);
            case MATERIAL_BLUE:
                return (R.color.material_blue);
            case MATERIAL_LIGHTBLUE:
                return (R.color.material_lightblue);
            case MATERIAL_CYAN:
                return (R.color.material_cyan);
            case MATERIAL_TEAL:
                return (R.color.material_teal);
            case MATERIAL_GREEN:
                return (R.color.material_green);
            case MATERIAL_LIGHTGREEN:
                return (R.color.material_lightgreen);
            case MATERIAL_LIME:
                return (R.color.material_lime);
            case MATERIAL_YELLOW:
                return (R.color.material_yellow);
            case MATERIAL_AMBER:
                return (R.color.material_amber);
            case MATERIAL_ORANGE:
                return (R.color.material_orange);
            case MATERIAL_DEEPORANGE:
                return (R.color.material_deeporange);
            case MATERIAL_GREY:
                return (R.color.material_grey);
            case MATERIAL_BLUEGREY:
                return (R.color.material_bluegrey);
            case MATERIAL_NEONGREEN:
                return (R.color.material_neongreen);
            default:
                return (R.color.material_indigo);

        }
    }

    public static void fabPressed(View v) {
        if(musicSrv == null){
            musicSrv = new MusicService();
            musicSrv.initMusicPlayer();
        }

            if (playbackPaused) {
                musicSrv.resumePlayer();
                //seekBar.setMax(musicSrv.getDur());
                playbackPaused = false;
            } else {
                musicSrv.pausePlayer();
                playbackPaused = true;
            }
    }

    public static void prevPressed(View v) {
        playPrev();
    }

    public static void nextPressed(View v) {
        playNext();
    }

    public static void updatePrefs() {
        artworkHeader = sharedPref.getBoolean("artwork_header", true);
        abTitle = sharedPref.getString("main_title", "MUSIC");

        //Set ActionBar Title
        if (mActionBar != null)
            mActionBar.setTitle(abTitle);
        if(currAlbum != null) {
            if ((currAlbum.getCoverURI() != null) && artworkHeader == false){
                fauxAB.setBackgroundColor(primaryColor);
                pagerTitleStrip.setBackgroundColor(primaryColor);
            }
        }

    }

    public static void updateColors(Resources res)
    {

        primaryColor = res.getColor(getColor(Integer.parseInt(sharedPref.getString("primary_color_key", "4"))));
        accentColor = res.getColor(getColor(Integer.parseInt(sharedPref.getString("accent_color_key", "1"))));

        fabDrawable.setColorFilter(accentColor, PorterDuff.Mode.SRC_ATOP);
        if(currAlbum != null ) {
            if (currAlbum.getCoverURI() == null) {
                fauxAB.setBackgroundColor(primaryColor);
                pagerTitleStrip.setBackgroundColor(primaryColor);
            }
        } else {
            fauxAB.setBackgroundColor(primaryColor);
            pagerTitleStrip.setBackgroundColor(primaryColor);
        }
    }

    private void mediaPlayerMonitor() {
        //set FAB icon
        if (musicSrv != null) {
            if (musicSrv.isPng()) {
                if (fab.getDrawable() != pauseDrawable) {
                    fab.setImageDrawable(pauseDrawable);
                }

                //Set Seekbar
                seekBar.setVisibility(View.VISIBLE);

                int mediaDuration = musicSrv.getDur();
                int mediaPosition = musicSrv.getPosn();
                seekBar.setMax(mediaDuration);
                seekBar.setProgress(mediaPosition);
                timePos.setText(prettyTime(mediaPosition));
                timeDur.setText(prettyTime(mediaDuration));

                if (!musicSrv.getSongTitle().equalsIgnoreCase(nowPlayingTitle.getText().toString())) {
                    setNowPlayingInfo();
                }
            } else {
                if (fab.getDrawable() != playDrawable) {
                    fab.setImageDrawable(playDrawable);
                }
            }
        }

        //PUT SET NOW PLAYING INFO BACK RIGHT HERE
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            paused = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {// Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_exit) {
            finish();
            return true;
        }
        if (id == R.id.action_shuffle_all) {
            shufflePressed(null);
        }
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_now_playing) {
            nowPlayingButtonPressed(null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    public void getSongList() {
        //retreive song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        //


        //
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ALBUM);
            int trackNumberColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);


            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                int thisTrack = musicCursor.getInt(trackNumberColumn);
                int duration = musicCursor.getInt(durationColumn);
                String thisDuration = prettyTime(duration);

                Song temp = new Song(thisId, thisTitle, thisArtist, thisAlbum, thisTrack, thisDuration);

                songList.add(temp);
            }
            while (musicCursor.moveToNext());
        }
    }

    public void getAlbumList() {
        ContentResolver musicResolver = getContentResolver();
        Uri artUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        Cursor coverCursor = musicResolver.query(artUri, null, null, null, null);


        if (coverCursor != null && coverCursor.moveToFirst()) {
            int coverColumn = coverCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
            int albumColumn = coverCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
            int artistColumn = coverCursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST);

            do {
                String thisCover = coverCursor.getString(coverColumn);
                String thisAlbum = coverCursor.getString(albumColumn);
                String thisArtist = coverCursor.getString(artistColumn);

                albumList.add(new Album(thisAlbum, thisCover, thisArtist));
            }
            while (coverCursor.moveToNext());
        }

    }

    private void getArtistList() {
        String current;
        int matchResult;
        for (int i = 0; i < albumList.size(); i++) {
            current = albumList.get(i).getArtist();
            matchResult = artistMatch(current);
            if (matchResult == -1) {   //If its a new artist
                Artist temp = new Artist(current);
                temp.addDummyAlbum(current); //Add placeholder to new artists
                temp.addAlbum(albumList.get(i));
                albumList.get(i).setArtistObj(temp);
                artistList.add(temp);
            } else {   //If its an existing artist add album to repo
                artistList.get(matchResult).addAlbum(albumList.get(i));
            }
        }
    }

    private int artistMatch(String current) {
        //check for match
        for (int j = 0; j < artistList.size(); j++) {
            if (artistList.get(j).getName().equalsIgnoreCase(current))
                return j;
        }

        return -1;
    }

    public void songPicked(View view) {
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        //seekBar.setMax(musicSrv.getDur());
        musicSrv.playSong();
        if (playbackPaused) {
            playbackPaused = false;
        }
    }

    public void albumPicked(View view) {
        //musicSrv.setSong(Integer.parseInt(view.getTag().toString()));

        int pos = Integer.parseInt(view.getTag().toString());
        currAlbum = albumList.get(pos);

        Intent intent = new Intent(this, ViewAlbumActivity.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create((View) fab, "fab"));

        startActivity(intent, options.toBundle());
    }

    public void artistPicked(View view) {

        int pos = Integer.parseInt(view.getTag().toString());
        currArtist = artistList.get(pos);
        View artistImage = findViewById(R.id.artistImage);

        Intent intent = new Intent(this, ViewArtistActivity.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create((View) fab, "fab")
                //,Pair.create(artistImage, "artistImage") //Glitchy will fix later
        );

        startActivity(intent, options.toBundle());
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicSrv != null && musicBound && musicSrv.isPng())
            return musicSrv.getPosn();
        else
            return 0;
    }

    @Override
    public int getDuration() {
        if (musicSrv != null && musicBound && musicSrv.isPng())
            return musicSrv.getDur();
        else
            return 0;
    }

    @Override
    public boolean isPlaying() {
        if (musicSrv != null && musicBound)
            return musicSrv.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicSrv.pausePlayer();
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public void start() {
        musicSrv.go();
    }

    // Creates a unique subdirectory of the designated app cache directory. Tries to use external
    // but if not mounted, falls back on internal storage.

    public void closePressed(View v) {
        stopService(playIntent);
        musicSrv = null;
        System.exit(0);
        finish();
    }


    ////////////////////////// BEGIN
    ////////////////////////////////// BUTTON
    //////////////////////////////////////////// PRESSED
    ////////////////////////////////////////////////////// ACTIONS

    public void setNowPlayingInfo() {
        nowPlayingTitle.setText(musicSrv.getSongTitle());
        nowPlayingArtist.setText(musicSrv.getSongArtist());
    }

    private Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getArtURL(String url) {
        try {
            XMLParser parser = new XMLParser();
            String xml = parser.getXmlFromUrl(url); // getting XML from URL
            Document doc = parser.getDomElement(xml);
            NodeList nl = doc.getElementsByTagName("image");

            for (int i = 0; i < nl.getLength(); i++) {
                Element e = (Element) nl.item(i);
                //Log.d("BHCA","Size = " + e.getAttribute("size") + " = " + parser.getElementValue(e));
                if (e.getAttribute("size").contentEquals("mega")) {
                    return parser.getElementValue(e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("BHCA", e.getMessage());
        }

        return null;
    }

    private String getArtistSummary(String url) {
        try {
            XMLParser parser = new XMLParser();
            String xml = parser.getXmlFromUrl(url); // getting XML from URL
            Document doc = parser.getDomElement(xml);
            NodeList nl = doc.getElementsByTagName("bio");
            nl = nl.item(0).getChildNodes();

            for(int i=0; i < nl.getLength(); i++)
            {
                Node n = nl.item(i);

                if(n.getNodeName().equals("summary")) {
                    Text t = (Text)n.getFirstChild();
                    return t.getWholeText();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("BHCA1", e.getMessage());
        }

        return null;
    }

    public void shufflePressed(View v) {
        musicSrv.setShuffle();
        if (musicSrv.shuffle) {
            musicSrv.resumePlayer();
            shuffleButton.startAnimation(shuffleAnimation);
            shuffleButton.setSelected(true);
            if (NowPlayingActivity.shuffleButton != null)
                NowPlayingActivity.shuffleButton.setSelected(true);
        } else {
            shuffleButton.startAnimation(shuffleAnimation);
            shuffleButton.setSelected(false);
            if (NowPlayingActivity.shuffleButton != null)
                NowPlayingActivity.shuffleButton.setSelected(false);
        }
    }

    public void repeatPressed(View v) {
        musicSrv.setRepeat();

        if (musicSrv.getRepeat() == MusicService.REPEAT_ALL) {
            repeatButton.startAnimation(repeatRotationAnimation);
            repeatButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_white_24dp));
            repeatButton.setSelected(true);
        } else if (musicSrv.getRepeat() == MusicService.REPEAT_ONE) {
            repeatButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_once_white_24dp));
            repeatButton.startAnimation(repeatRotationAnimation);
            repeatButton.setSelected(true);
        } else {
            repeatButton.startAnimation(repeatRotationAnimation);
            repeatButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_white_24dp));
            repeatButton.setSelected(false);
        }
    }

    public void nowPlayingButtonPressed(View v) {
        Intent intent = new Intent(this, NowPlayingActivity.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create((View) fab, "fab"));

        if (musicSrv.getCurrSong().getCoverURI() != null) {
            options = ActivityOptions.makeSceneTransitionAnimation(this,
                    Pair.create((View) coverArt, "coverArt"),
                    Pair.create((View) fab, "fab"));
        }

        startActivity(intent, options.toBundle());
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Bundle args = getArguments();
            int page = args.getInt(ARG_SECTION_NUMBER);
            View rootView;
            if (page == 1) {
                rootView = inflater.inflate(R.layout.song_list, container, false);


                songView = (ListView) rootView.findViewById(R.id.song_list);
                mListView = (IndexableListView) rootView.findViewById(R.id.song_list);


                if (songView != null)
                    songView.setAdapter(songAdt);
                if (mListView != null) {
                    mListView.setAdapter(songAdt);
                    mListView.setFastScrollEnabled(true);
                    mListView.setFastScrollAlwaysVisible(true);
                }
            } else if (page == 2) {
                rootView = inflater.inflate(R.layout.album_list, container, false);

                albumView = (GridView) rootView.findViewById(R.id.album_grid);

                if (albumView != null)
                    albumView.setAdapter(albumAdt);
            } else if (page == 3) {
                rootView = inflater.inflate(R.layout.artist_list, container, false);
                artistView = (GridView) rootView.findViewById(R.id.artistGrid);
                if (artistView != null)
                    artistView.setAdapter(artistAdt);
            } else
                rootView = inflater.inflate(R.layout.song_list, container, false);

            return rootView;
        }
    }

    //Begin AsyncTask Class
    public class ArtistArtUtil extends AsyncTask<Void, Void, String> {

        long t1, t2;
        private String key = "89b0d2bf4200f9b85e3741e5c07b807d";
        private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;
        private int mCompressQuality = 100;

        @Override
        protected String doInBackground(Void... artists) {

            t1 = System.currentTimeMillis();
            String artistArtUrl = "", artistName, encodedArtistName = "", key, sumKey, artistSummary = "No Info Available.";
            String BaseURL = "http://ws.audioscrobbler.com/2.0/?method=artist.getinfo&api_key=89b0d2bf4200f9b85e3741e5c07b807d&artist=";
            Bitmap artistImage = null;
            mDiskLruCache = new DiskLruImageCache(getApplicationContext(), "artists",
                    1024 * 1024 * 10, mCompressFormat, mCompressQuality);
            SharedPreferences.Editor mEditor = sharedPref.edit();


            for (int i = 0; i < artistList.size(); i++) {
                artistName = artistList.get(i).getName();
                key = artistName.toLowerCase();
                key = key.replaceAll("[^a-z0-9_-]+", "");
                sumKey = key + "summary";

                if (mDiskLruCache.containsKey(key)) {
                    artistImage = mDiskLruCache.getBitmap(key);
                    artistList.get(i).setImage(artistImage);
                    artistList.get(i).setAccentColor();
                    if(sharedPref.contains(sumKey))
                    {
                        artistSummary = sharedPref.getString(sumKey, artistSummary);
                        artistList.get(i).setSummary(artistSummary);
                    }
                } else {
                    try {
                        encodedArtistName = URLEncoder.encode(artistName, "UTF-8");
                    } catch (Exception e) {
                        e.getMessage();
                    }
                    artistArtUrl = getArtURL(BaseURL + encodedArtistName);
                    artistSummary = getArtistSummary(BaseURL + encodedArtistName);
                    if(artistName.contains("<")){
                        artistImage = null;
                    } else
                        artistImage = getBitmapFromURL(artistArtUrl);

                    artistList.get(i).setImage(artistImage);
                    artistList.get(i).setAccentColor();
                    artistList.get(i).setSummary(artistSummary);
                    if (artistImage != null)
                        mDiskLruCache.put(key, artistImage);
                    if(artistSummary != null)
                    {
                        mEditor.putString(sumKey, artistSummary);
                        mEditor.apply();
                    }

                }
            }

            mDiskLruCache.put("null", artistImage);

            return artistArtUrl;

        }

        @Override
        protected void onPostExecute(String url) {
            t2 = System.currentTimeMillis();
            Log.d("BHCA", "Async Complete: " + (t2 - t1) + " ms");
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }


}
