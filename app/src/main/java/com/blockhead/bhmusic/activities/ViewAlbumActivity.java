package com.blockhead.bhmusic.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.blockhead.bhmusic.R;
import com.blockhead.bhmusic.adapters.TracksAdapter;
import com.blockhead.bhmusic.objects.Album;
import com.blockhead.bhmusic.objects.Song;
import com.blockhead.bhmusic.utils.NotifyingScrollView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



public class ViewAlbumActivity extends Activity {

    private android.support.design.widget.CollapsingToolbarLayout mCollapsingTB;
    Handler monitorHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            mediaPlayerMonitor();
        }

    };
    private Album currAlbum;
    private ImageView coverView, fadeCoverView;
    private TextView tracksView, headerTrackCount;
    private Drawable mActionBarBackgroundDrawable, mActionBarCoverDrawable;
    private ActionBar actionBar;
    private ArrayList<Song> trackList;
    private NotifyingScrollView bgScrollView, scrollView;
    private TracksAdapter tracksAdt;
    private ListView trackListView;
    private Song currTrack;
    private MusicService musicSrv;
    private ImageButton fab;
    private int vibrantColor;
    private NotifyingScrollView.OnScrollChangedListener mOnScrollChangedListener = new NotifyingScrollView.OnScrollChangedListener() {
        public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
            final int headerHeight = coverView.getHeight() - getActionBar().getHeight();
            final float ratio = (float) Math.min(Math.max(t, 0), headerHeight) / headerHeight;
            final int newAlpha = (int) (ratio * 255);
            final int coverAlpha = (int) (newAlpha * 1.15);
            //mActionBarBackgroundDrawable.setAlpha(newAlpha);
            if (coverAlpha < 255) {
                mActionBarCoverDrawable.setAlpha(coverAlpha);
                mActionBarCoverDrawable.setTint(vibrantColor);
            }

            //Show Hide Action Bar
            int coverHeight = contentGapper.getMeasuredHeight() - 103;
            if (t >= coverHeight) {
                abBackground.setAlpha(255);
            } else {
                abBackground.setAlpha(0);
            }

            bgScrollView.scrollTo(0, (int) (t * .4));
        }
    };
    private RelativeLayout abBackground, header;
    private FrameLayout contentGapper;
    private Drawable pauseDrawable, playDrawable;

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView, Context context) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, RelativeLayout.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
            if (i == listAdapter.getCount() - 1) {
                totalHeight += view.getMeasuredHeight() * 2;
            }
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        totalHeight += (listView.getDividerHeight() * (listAdapter.getCount()));
        params.height = totalHeight;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance


        currAlbum = MainActivity.currAlbum;
        if(currAlbum == null)
            System.exit(0);
        trackList = currAlbum.tracks;
        coverView = (ImageView) findViewById(R.id.coverArtAlbum);
        fadeCoverView = (ImageView) findViewById(R.id.fadeCover);
        actionBar = getActionBar();
        abBackground = (RelativeLayout) findViewById(R.id.ab_background);
        header = (RelativeLayout) findViewById(R.id.header);
        fab = (ImageButton) findViewById(R.id.albumFab);
        musicSrv = MainActivity.getMusicService();
        if (!musicSrv.isPng())
            fab.setImageDrawable(getResources().getDrawable(R.drawable.play));

        //Setup Show ActionBar Variables
        contentGapper = (FrameLayout) findViewById(R.id.content_gapper);

        //Set ActionBar Title & Cover Image
        if (currAlbum != null) {
            if (actionBar != null)
                actionBar.setTitle("");
            if (currAlbum.getCoverURI() != null) {
                //Picasso.with(getApplicationContext()).load(currAlbum.getCoverURI()).fit().centerCrop().noFade().into(coverView);
                imageLoader.displayImage(currAlbum.getCoverURI(), coverView);
                Log.d("DEBUG-BHCA","Setting cover: " + currAlbum.getCoverURI());
            }
            // coverView.notify();
        }

        //Set Track List
        tracksAdt = new TracksAdapter(this, trackList);
        trackListView = (ListView) findViewById(R.id.trackListView);
        trackListView.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        trackListView.setAdapter(tracksAdt);
        setListViewHeightBasedOnChildren(trackListView, getApplicationContext());
        trackListView.setFocusable(false);

        //Set Header Info
        TextView headerTitle = (TextView) findViewById(R.id.header_title);
        TextView abTitle = (TextView) findViewById(R.id.ab_title);
        headerTrackCount = (TextView) findViewById(R.id.header_track_count);
        headerTitle.setText(currAlbum.getTitle());
        abTitle.setText(currAlbum.getTitle());
        headerTrackCount.setText(currAlbum.getArtist() + " (" + trackList.size() + " songs)");

        //Set Colors
        vibrantColor = currAlbum.getAccentColor();
        if (vibrantColor == Color.WHITE)
            vibrantColor = MainActivity.primaryColor;

        mActionBarBackgroundDrawable = getResources().getDrawable(R.drawable.ab_background);
        mActionBarBackgroundDrawable.setColorFilter(vibrantColor, PorterDuff.Mode.SRC_ATOP);
        mActionBarCoverDrawable = getResources().getDrawable(R.drawable.ab_background);
        mActionBarCoverDrawable.setColorFilter(vibrantColor, PorterDuff.Mode.SRC_ATOP);

        pauseDrawable = getResources().getDrawable(R.drawable.pause);
        playDrawable = getResources().getDrawable(R.drawable.play);
        Drawable fabBG = fab.getBackground();
        fabBG.setColorFilter(MainActivity.accentColor, PorterDuff.Mode.SRC_ATOP);


        abBackground.setBackgroundColor(vibrantColor);
        header.setBackgroundColor(vibrantColor);
        abBackground.setAlpha(0);


        if (mActionBarCoverDrawable != null)
            mActionBarCoverDrawable.setAlpha(0);


        fadeCoverView.setImageDrawable(mActionBarCoverDrawable);

        scrollView = ((NotifyingScrollView) findViewById(R.id.scroll_view));

        scrollView.setOnScrollChangedListener(mOnScrollChangedListener);
        bgScrollView = (NotifyingScrollView) findViewById(R.id.bg_scroll_view);

        ScheduledExecutorService myScheduledExecutorService = Executors.newScheduledThreadPool(1);

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
    }//END ON CREATE METHOD


        //End Cyril Code

    //Media Player Monitor
    private void mediaPlayerMonitor() {
        //set FAB icon
        if (musicSrv != null) {
            if (musicSrv.isPng()) {
                //set playButton icon to pause
                fab.setImageDrawable(pauseDrawable);
            } else {
                //set playButton icon to play
                fab.setImageDrawable(playDrawable);
            }
        }
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
        currTrack = trackList.get(pos);
        musicSrv.setSong(pos);
        musicSrv.playAlbum(currAlbum, pos);

        Intent intent = new Intent(this, NowPlayingActivity.class);

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create((View)fab, "fab"));

        startActivity(intent, options.toBundle());
    }


    public void albumFabPressed(View v)
    {
        MainActivity.fabPressed(v);
    }
}
