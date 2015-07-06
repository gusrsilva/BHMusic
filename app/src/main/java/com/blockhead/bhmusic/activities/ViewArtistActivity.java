package com.blockhead.bhmusic.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityOptions;
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
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blockhead.bhmusic.R;
import com.blockhead.bhmusic.adapters.ArtistsTracksAdapter;
import com.blockhead.bhmusic.objects.Album;
import com.blockhead.bhmusic.objects.Artist;
import com.nirhart.parallaxscroll.views.ParallaxExpandableListView;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ViewArtistActivity extends AppCompatActivity {
    MusicService musicSrv;
    FloatingActionButton fab;
    Artist currArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_artist);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        final android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        if(mActionBar != null)
            mActionBar.setTitle("");

        musicSrv = MainActivity.getMusicService();
        /* Setup Floating Action Button */
        fab = (FloatingActionButton)findViewById(R.id.artistFab);
        setFabDrawable();
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


        ParallaxExpandableListView xLV = (ParallaxExpandableListView)findViewById(R.id.expandableListView);
        currArtist = MainActivity.currArtist;
        ArrayList<Album> albums = currArtist.getAlbums();

        final RelativeLayout abBackground = (RelativeLayout)findViewById(R.id.artist_ab_background);
        TextView abTitle = (TextView)findViewById(R.id.artist_ab_title);

        abTitle.setText(currArtist.getName());
        abBackground.setBackgroundColor(currArtist.getAccentColor());
        abBackground.setAlpha(0);

        AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mActionBar != null) {
                    if (firstVisibleItem == 0 && abBackground.getAlpha() == 1) {
                        abBackground.setAlpha(0);
                    } else if (firstVisibleItem >= 1 && abBackground.getAlpha() == 0) {
                        abBackground.setAlpha(1);
                    }
                }
            }
        };

        ImageView header = new ImageView(this);
        if(currArtist.getImage() == null) { //Set default artist art if none
            header.setImageResource(R.drawable.default_artist_xlarge);
            header.setBackgroundColor(MainActivity.randomColor());
        }
        else
            header.setImageBitmap(currArtist.getImage());
        header.setAdjustViewBounds(true);
        header.setScaleType(ImageView.ScaleType.CENTER_CROP);

        //Set Header Max Height
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        double maxHeight = size.y * 0.6667;
        double minHeight = size.y * 0.5;
        header.setMaxHeight((int) maxHeight);
        header.setMinimumHeight((int) minHeight);

        //Set Expanded List View Properties
        xLV.addParallaxedHeaderView(header);
        xLV.setDivider(null);
        if(currArtist.getAccentColor() == Color.WHITE)
            xLV.setBackgroundColor(MainActivity.primaryColor);
        else
            xLV.setBackgroundColor(currArtist.getAccentColor());

        int headerHeight = getActionBarHeight() + getStatusBarHeight();
        ArtistsTracksAdapter mArtistsTracksAdapter = new ArtistsTracksAdapter(getApplicationContext(), albums, headerHeight);
        xLV.setAdapter(mArtistsTracksAdapter);
        xLV.setOnScrollListener(mOnScrollListener);
        ExpandableListView.OnChildClickListener mOnClickedListener = new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if(groupPosition != 0)
                    artistTrackPicked(groupPosition,childPosition);
                return true;
            }
        };
        xLV.setOnChildClickListener(mOnClickedListener);
        xLV.setIndicatorBoundsRelative(size.x - 150, size.x);

        //Fill the rest of the list if its not long enough to cover the background
        if( currArtist.getAlbums().size() <= 3)
        {
            View footer = new View(this);
            footer.setBackgroundColor(getResources().getColor(R.color.background_color));
            footer.setMinimumWidth(size.x);
            footer.setMinimumHeight((int) minHeight);
            xLV.addFooterView(footer);


        }

    }//END ON CREATE METHOD

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

        if(MainActivity.isLollipop()) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                    Pair.create((View) fab, "fab"));
            startActivity(intent, options.toBundle());
        }
        else
            startActivity(intent);
    }

    public void artistTrackPicked(int groupPostion, int childPosition)
    {
        Album currAlbum = currArtist.albums.get(groupPostion);
        musicSrv.setSong(childPosition);
        musicSrv.playAlbum(currAlbum, childPosition);
        setFabDrawable();

        Intent intent = new Intent(this, NowPlayingActivity.class);

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create((View)fab, "fab"));

        startActivity(intent, options.toBundle());
    }

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
            android.os.Process.killProcess(android.os.Process.myPid());
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
        setFabDrawable();
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getActionBarHeight(){
        final TypedArray styledAttributes = getApplicationContext().getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        int result = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        return result;
    }
}
