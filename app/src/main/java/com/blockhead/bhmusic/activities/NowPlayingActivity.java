package com.blockhead.bhmusic.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.RenderScript;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blockhead.bhmusic.R;
import com.blockhead.bhmusic.adapters.npTracksAdapter;
import com.blockhead.bhmusic.objects.Album;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class NowPlayingActivity extends AppCompatActivity {

    public static ImageButton shuffleButton, repeatButton;
    Handler monitorHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            mediaPlayerMonitor();
        }

    };
    private MusicService musicSrv = new MusicService();
    private ImageButton playButton;
    private FloatingActionButton fab;
    private TextView trkTitle, trkArtist, trkAlbum, timePos, timeDur;
    private SeekBar seek;
    private RelativeLayout controlsHolder, fauxAB;
    private ImageView coverArt, bgBlurred;
    private String title, artist, album;
    private Bitmap regCov, blurCov;
    private RenderScript rs;
    private Drawable shuffleDrawable, repeatDrawable;
    private ListView npTrackListView;
    private npTracksAdapter tracksAdapter;
    private LinearLayout npTrackHolder;
    private Drawable mListHeader, seekThumb, seekThumbSelected, seekProgress, fabDrawable;
    private android.support.v7.app.ActionBar actionBar;
    private int vibrantColor;
    private Animation repeatAnimation, shuffleAnimation, vinylAnimation;
    private boolean needsRotation = false;
    private ImageLoader imageLoader;
    private DisplayImageOptions displayOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {    //TODO: Make FAB show playlist if playing playlist
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        musicSrv = MainActivity.getMusicService();
        musicSrv.isFinished = false;

        /* Set up Action Bar */
        actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        /* Initialize ImageLoader and options */
        imageLoader = ImageLoader.getInstance();
        displayOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.default_cover_xlarge) // resource or drawable
                .showImageOnFail(R.drawable.default_cover_xlarge)
                .build();

        /* Set up layout */
        seekThumb = getResources().getDrawable(R.drawable.seekbar_thumb);
        seekThumbSelected = getResources().getDrawable(R.drawable.seekbar_thumb_selected);
        seekProgress = getResources().getDrawable(R.drawable.now_playing_seekbar_progress);
        seekThumbSelected.setColorFilter(MainActivity.accentColor, PorterDuff.Mode.SRC_ATOP);
        seekThumb.setColorFilter(MainActivity.accentColor, PorterDuff.Mode.SRC_ATOP);
        seekProgress.setColorFilter(MainActivity.accentColor, PorterDuff.Mode.SRC_ATOP);

        /* Set up Floating Action Button */
        fab = (FloatingActionButton) findViewById(R.id.np_fab);
        fabDrawable = fab.getBackground();
        fab.setBackgroundTintList(ColorStateList.valueOf(MainActivity.accentColor));
        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_format_list_bulleted_white_24dp));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                npFabPressed(v);
            }
        });

        //Set Views
        playButton = (ImageButton) findViewById(R.id.floating_action_button);
        trkTitle = (TextView) findViewById(R.id.trackTitle);
        trkArtist = (TextView) findViewById(R.id.trackArtist);
        trkAlbum = (TextView) findViewById(R.id.trackAlbum);
        shuffleButton = (ImageButton) findViewById(R.id.shuffleButton);
        repeatButton = (ImageButton) findViewById(R.id.repeatButton);
        seek = (SeekBar) findViewById(R.id.progressBar);
        seek.setProgressDrawable(seekProgress);
        seek.setThumb(seekThumb);
        controlsHolder = (RelativeLayout) findViewById(R.id.controlsHolder);
        fauxAB = (RelativeLayout) findViewById(R.id.fauxAB);
        coverArt = (ImageView) findViewById(R.id.coverArt);
        bgBlurred = (ImageView) findViewById(R.id.bgBlurredCover);
        mListHeader = getResources().getDrawable(R.drawable.rect_ripple_semitransparent_black);


        //Check Shuffle & Repeat
        shuffleDrawable = shuffleButton.getDrawable();
        repeatDrawable = repeatButton.getDrawable();

        //Set Animations
        repeatAnimation = AnimationUtils.loadAnimation(this, R.anim.repeat_rotate_animation);
        shuffleAnimation = AnimationUtils.loadAnimation(this, R.anim.shuffle_rotate_animation);
        vinylAnimation = AnimationUtils.loadAnimation(this, R.anim.vinyl_rotation);


        if (musicSrv != null) {
            if (musicSrv.shuffle) {
                shuffleButton.setSelected(true);
                shuffleDrawable.setTint(MainActivity.accentColor);
            }
            setRepeatDrawable();
        }

        setInfo();


        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                if (fromUser)
                    progress = progressValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seek.setThumb(seekThumbSelected);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seek.setThumb(seekThumb);
                musicSrv.seek(progress);
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
                200, //initialDelay
                50, //delay
                TimeUnit.MILLISECONDS);

    }//End on create

    //Media Player Monitor
    private void mediaPlayerMonitor() {

        if(musicSrv == null || musicSrv.isFinished)
        {
            finish();
        }
        if (musicSrv != null)
        {
            if (musicSrv.isPng())
            {
                //set playButton icon to pause
                Drawable pauseDrawable = getResources().getDrawable(R.drawable.pause);
                playButton.setImageDrawable(pauseDrawable);

                //set seek bar
                seek.setVisibility(View.VISIBLE);

                int mediaDuration = musicSrv.getDur();
                int mediaPosition = musicSrv.getPosn();
                seek.setMax(mediaDuration);
                seek.setProgress(mediaPosition);
                timePos.setText(MainActivity.prettyTime(mediaPosition));
                timeDur.setText(MainActivity.prettyTime(mediaDuration));

                if(needsRotation)
                    coverArt.setRotation((float)(mediaPosition)/50);

                //If now playing does not match info showing set info
                if (!trkTitle.getText().toString().equalsIgnoreCase(musicSrv.getSongTitle())) {
                    setInfo();
                }
            }
            else
            {
                //set playButton icon to play
                Drawable playDrawable = getResources().getDrawable(R.drawable.play);
                playButton.setImageDrawable(playDrawable);
            }
        }
        else
        {
            //set playButton icon to play
            Drawable playDrawable = getResources().getDrawable(R.drawable.play);
            playButton.setImageDrawable(playDrawable);
        }

    }


    private void setTitle() {
        title = musicSrv.getSongTitle();
        trkTitle.setText(title);
    }

    private void setArtist() {
        artist = musicSrv.getSongArtist();
        trkArtist.setText(artist);
    }

    private void setAlbum() {
        album = musicSrv.getSongAlbum();
        //trkAlbum.setText(album);
    }

    private void setTracklist() {
        //Set Track List
        if (musicSrv.getCurrSong().getAlbumObj() != null) {
            npTrackListView = (ListView) findViewById(R.id.np_track_listview);
            tracksAdapter = new npTracksAdapter(this, musicSrv.getCurrSong().getAlbumObj().getTracks());
            npTrackListView.setAdapter(tracksAdapter);
            if (blurCov != null)
                npTrackListView.setBackground(new BitmapDrawable(getResources(), blurCov));
        }
    }

    private Album findAlbum(String albumTitle) {
        if (albumTitle != null) {
            for (int i = 0; i < MainActivity.albumList.size(); i++) {
                if (albumTitle.equalsIgnoreCase(MainActivity.albumList.get(i).getTitle())) {
                    return MainActivity.albumList.get(i);
                }
            }
        }
        return null;
    }


    private void setCover() {
        if (musicSrv.getCoverURI() != null) {
            coverArt.clearAnimation();
            coverArt.setRotation(0);
            needsRotation = false;
            vibrantColor = findAlbum(album).getAccentColor();
            fauxAB.setBackgroundColor(vibrantColor);
            //regCov = musicSrv.getSongCover().copy(musicSrv.getSongCover().getConfig(), true);
            //coverArt.setImageBitmap(regCov);
            blurCov = musicSrv.getSuperBlurredCover();
            imageLoader.displayImage(musicSrv.getCoverURI(), coverArt, displayOptions);
        } else {
            vibrantColor = getResources().getColor(R.color.dark_grey);
            fauxAB.setBackgroundColor(vibrantColor);
            coverArt.setImageDrawable(getResources().getDrawable(R.drawable.default_cover_xlarge));
            getWindow().setBackgroundDrawableResource(musicSrv.getCurrSong().getRandomColor());
            //coverArt.startAnimation(vinylAnimation);
            needsRotation = true;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_now_playing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (npTrackListView.getVisibility() == View.VISIBLE)
            npFabPressed(null);
        else
            super.onBackPressed();
    }

    //Button Press Actions
    public void npPlayPressed(View v) {
        MainActivity.fabPressed(v);
    }

    public void npFabPressed(View v) {
        if (npTrackListView.getVisibility() == View.VISIBLE) {
            //HIDE TRACKLIST IF SHOWING


            // get the center for the clipping circle
            int cx = (fab.getLeft() + fab.getRight()) / 2;
            int cy = (fab.getTop() + fab.getBottom()) / 2;

            // get the initial radius for the clipping circle
            int initialRadius = npTrackListView.getWidth();

            // create the animation (the final radius is zero)
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(npTrackListView, cx, cy, initialRadius, 0);

            // make the view invisible when the animation is done
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    npTrackListView.setVisibility(View.INVISIBLE);
                    fab.setVisibility(View.VISIBLE);
                }
            });
            // start the animation
            anim.start();
            if (actionBar != null) {
                actionBar.setBackgroundDrawable(null);
                actionBar.setTitle("");
            }

        } else { //SHOW TRACKLIST IF HIDING
            fab.setVisibility(View.INVISIBLE);
            // get the center for the clipping circle
            int cx = (fab.getLeft() + fab.getRight()) / 2;
            int cy = (fab.getTop() + fab.getBottom()) / 2;

            // get the final radius for the clipping circle
            int finalRadius = Math.max(npTrackListView.getWidth(), npTrackListView.getHeight());

            // create the animator for this view (the start radius is zero)
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(npTrackListView, cx, cy, 0, finalRadius);
            anim.setDuration(300);

            // make the view invisible when the animation is done
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (actionBar != null) {
                        actionBar.setBackgroundDrawable(mListHeader);
                        actionBar.setTitle(album);
                    }
                }
            });

            // make the view visible and start the animation
            npTrackListView.setVisibility(View.VISIBLE);
            anim.start();
        }
    }

    public void npShufflePressed(View v) {
        musicSrv.setShuffle();
        if (musicSrv.shuffle) {
            shuffleButton.startAnimation(shuffleAnimation);
            shuffleButton.setSelected(true);
            shuffleDrawable.setTint(MainActivity.accentColor);
            MainActivity.shuffleButton.setSelected(true);
        } else {
            shuffleButton.startAnimation(shuffleAnimation);
            shuffleButton.setSelected(false);
            MainActivity.shuffleButton.setSelected(false);
            shuffleDrawable.setTint(Color.BLACK);
        }
    }

    public void npRepeatPressed(View v) {
        musicSrv.setRepeat();
        setRepeatDrawable();

    }

    private void setRepeatDrawable() {
        try {
            if (musicSrv.getRepeat() == musicSrv.REPEAT_ALL) {
                repeatButton.startAnimation(repeatAnimation);
                repeatButton.setSelected(true);
                repeatButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_white_24dp));
                repeatDrawable = repeatButton.getDrawable();
                repeatDrawable.setTint(MainActivity.accentColor);
                MainActivity.repeatButton.setSelected(true);
            } else if (musicSrv.getRepeat() == musicSrv.REPEAT_ONE) {
                repeatButton.startAnimation(repeatAnimation);
                repeatButton.setSelected(true);
                repeatButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_once_white_24dp));
                repeatDrawable = repeatButton.getDrawable();
                repeatDrawable.setTint(MainActivity.accentColor);
                MainActivity.repeatButton.setSelected(true);
            } else { //Repeat is off
                repeatButton.startAnimation(repeatAnimation);
                repeatButton.setSelected(false);
                repeatButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_white_24dp));
                repeatDrawable = repeatButton.getDrawable();
                MainActivity.repeatButton.setSelected(false);
                repeatDrawable.setTint(Color.BLACK);
            }
        } catch (NullPointerException e) {
            Log.d("BHCA", "Error: " + e.getMessage());
        }
    }

    public void npTrackPicked(View view) {

        int pos = Integer.parseInt(view.getTag().toString());
        musicSrv.playAlbum(musicSrv.getCurrSong().getAlbumObj(), pos);

        Intent intent = new Intent(this, NowPlayingActivity.class);
        startActivity(intent);
    }

    public void prevPressed(View v)
    {
        MainActivity.prevPressed(v);
        setInfo();
    }

    public void nextPressed(View v) {
        MainActivity.nextPressed(v);
        setInfo();
    }

    private void setInfo() {
        try {
            setTitle();
            setArtist();
            setAlbum();
            setCover();
            setTracklist();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Please select a song first.", Toast.LENGTH_SHORT).show();
            Log.d("BHCA", "setInfo: " + e.getMessage());
            finish();
        }
    }


}


