package com.blockhead.bhmusic.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.blockhead.bhmusic.R;
import com.blockhead.bhmusic.adapters.TracksAdapter;
import com.blockhead.bhmusic.objects.Album;
import com.blockhead.bhmusic.objects.Song;
import com.blockhead.bhmusic.utils.NotifyingScrollView;
import com.nirhart.parallaxscroll.views.ParallaxListView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



public class ViewAlbumActivity extends AppCompatActivity {

    private Album currAlbum;
    private MusicService musicSrv;
    private FloatingActionButton fab;
    private ImageButton shuffleButton;
    private int albumSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        ImageLoader imageLoader = ImageLoader.getInstance(); // Get single instance
        final android.support.v7.app.ActionBar mActionBar = getSupportActionBar();

        currAlbum = MainActivity.currAlbum;

        /* Cannot open activity if no Album is set */
        if(currAlbum == null)
        {
            Toast.makeText(getApplicationContext(),
                    "Please select an album first."
                    , Toast.LENGTH_SHORT)
                    .show();
            finish();
        }

        String coverUri = currAlbum.getCoverURI();
        albumSize = currAlbum.getSize();
        ArrayList<Song> trackList = currAlbum.tracks;
        fab = (FloatingActionButton) findViewById(R.id.albumFab);
        musicSrv = MainActivity.getMusicService();
        setFabDrawable();

        /* Setup ActionBar */
        int actionBarColor = MainActivity.primaryColor;
        if(currAlbum != null && currAlbum.getAccentColor() != Color.WHITE)
            actionBarColor = currAlbum.getAccentColor();
        if(mActionBar != null)
            mActionBar.setTitle("");

        /* Setup ActionBar Background */
        final RelativeLayout abBackground = (RelativeLayout)findViewById(R.id.album_ab_background);
        if(abBackground != null)
        {
            abBackground.setBackgroundColor(actionBarColor);
            abBackground.setAlpha(0);
        }

        //Define On Scroll Listener for ParallaxListView
        AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mActionBar != null)
                {
                    if (firstVisibleItem == 0 && abBackground.getAlpha() == 1)
                    {
                        abBackground.setAlpha(0);
                        mActionBar.setTitle("");
                    }
                    else if (firstVisibleItem >= 1 && abBackground.getAlpha() == 0)
                    {
                        abBackground.setAlpha(1);
                        mActionBar.setTitle(currAlbum.getTitle());
                    }
                }
            }
        };

        /* Set Image Header attributes */
        ImageView header = new ImageView(this);
        if(coverUri == null)
        { //Set default art if none
            header.setImageResource(R.drawable.default_cover_xlarge);
            header.setBackgroundColor(MainActivity.randomColor());
        }
        else
            imageLoader.displayImage(coverUri, header);
        header.setAdjustViewBounds(true);
        header.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        double maxHeight = size.y * 0.6667;
        double minHeight = size.y * 0.5;
        header.setMaxHeight((int) maxHeight);
        header.setMinimumHeight((int) minHeight);

        /* Set title header */
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View titleHeader = inflater.inflate(R.layout.view_album_title_header, null);
        titleHeader.setMinimumHeight(getActionBarHeight() + getStatusBarHeight());
        TextView title = (TextView) titleHeader.findViewById(R.id.view_album_header_title);
        TextView subtitle = (TextView) titleHeader.findViewById(R.id.view_album_header_subtitle);
        LinearLayout linLay = (LinearLayout) titleHeader.findViewById(R.id.view_album_header_lin);
        title.setText(currAlbum.getTitle());
        subtitle.setText(currAlbum.getArtist() + " (" + albumSize + (albumSize==1?" song )":" songs )"));
        linLay.setBackgroundColor(actionBarColor);

        /* Initialize and set up shuffle button */
        shuffleButton = (ImageButton) titleHeader.findViewById(R.id.view_album_shuffleButton);
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewAlbumShufflePressed(v);
            }
        });

        /*  Set up Parallax ListView */
        ParallaxListView memberList = (ParallaxListView) findViewById(R.id.view_album_list);
        memberList.addParallaxedHeaderView(header);
        memberList.addHeaderView(titleHeader);
        memberList.setOnScrollListener(mOnScrollListener);
        memberList.setBackgroundColor(actionBarColor);
        memberList.setDivider(null);
        TracksAdapter tracksAdt = new TracksAdapter(getApplicationContext(), trackList);
        memberList.setAdapter(tracksAdt);
        memberList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                trackPicked(view);
            }
        });

        /* Fill the rest of the list if its not long enough to cover the background */
        if( albumSize <= 3)
        {
            View footer = new View(this);
            footer.setBackgroundColor(getResources().getColor(R.color.background_color));
            footer.setMinimumWidth(size.x);
            footer.setMinimumHeight((int) minHeight);
            footer.setClickable(false);
            footer.setLongClickable(false);
            memberList.addFooterView(footer);
        }

        /* Setup Floating Action Button */
        fab.setBackgroundTintList(ColorStateList.valueOf(MainActivity.accentColor));
        fab.setClickable(true);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabPressed();
            }
        });
        fab.setLongClickable(true);
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                nowPlayingButtonPressed();
                return true;
            }
        });


    }//END ON CREATE METHOD

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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
            Intent intent = new Intent(this, NowPlayingActivity.class);

            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                    Pair.create((View) fab, "fab"));

            startActivity(intent, options.toBundle());

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void shufflePressed(View v) {
        musicSrv.setShuffle();
        if (musicSrv.shuffle) {
            musicSrv.resumePlayer();
            MainActivity.shuffleButton.setSelected(true);
            if (NowPlayingActivity.shuffleButton != null)
                NowPlayingActivity.shuffleButton.setSelected(true);
        } else {
            MainActivity.shuffleButton.setSelected(false);
            if (NowPlayingActivity.shuffleButton != null)
                NowPlayingActivity.shuffleButton.setSelected(false);
        }
    }

    public void trackPicked(View view)
    {

        int pos = Integer.parseInt(view.getTag().toString());
        //currTrack = trackList.get(pos);
        musicSrv.setSong(pos);
        musicSrv.playAlbum(currAlbum, pos);

        Intent intent = new Intent(this, NowPlayingActivity.class);

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create((View)fab, "fab"));

        startActivity(intent, options.toBundle());
    }

    private void setFabDrawable()
    {
        Drawable pauseDrawable = getResources().getDrawable(R.drawable.pause);
        Drawable playDrawable = getResources().getDrawable(R.drawable.play);
        if(musicSrv.isPng())
            fab.setImageDrawable(pauseDrawable);
        else
            fab.setImageDrawable(playDrawable);
    }
    private void fabPressed()
    {
        if(musicSrv.isPng())
            musicSrv.pausePlayer();
        else
            musicSrv.resumePlayer();
        setFabDrawable();
    }

    private void nowPlayingButtonPressed()
    {
        if(musicSrv == null || musicSrv.getCurrSong() == null)
        {
            Toast
                    .makeText(getApplicationContext(), "Please select song first.", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        Intent intent = new Intent(this, NowPlayingActivity.class);

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                    Pair.create((View) fab, "fab"));

        startActivity(intent, options.toBundle());
    }

    public void viewAlbumShufflePressed(View view)
    {
        musicSrv.shuffle = true;
        if(MainActivity.shuffleAnimation != null)
            shuffleButton.startAnimation(MainActivity.shuffleAnimation);
        musicSrv.playAlbum(currAlbum, (new Random().nextInt(albumSize-1)));
        setFabDrawable();
        MainActivity.shuffleButton.setSelected(true);
        if (NowPlayingActivity.shuffleButton != null)
            NowPlayingActivity.shuffleButton.setSelected(true);
        Snackbar.make(findViewById(R.id.view_album_coordinator), "Now Shuffling: " + currAlbum.getTitle(), Snackbar.LENGTH_SHORT)
                .show();
    }

    public int getActionBarHeight(){
        final TypedArray styledAttributes = getApplicationContext().getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        int result = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        return result;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
