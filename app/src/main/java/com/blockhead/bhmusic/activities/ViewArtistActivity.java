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
import android.util.Pair;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.blockhead.bhmusic.R;
import com.blockhead.bhmusic.adapters.ArtistsTracksAdapter;
import com.blockhead.bhmusic.objects.Album;
import com.blockhead.bhmusic.objects.Artist;
import com.blockhead.bhmusic.objects.Song;
import com.blockhead.bhmusic.utils.NotifyingScrollView;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ViewArtistActivity extends Activity {

    Handler monitorHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            mediaPlayerMonitor();
        }

    };
    private Artist currArtist;
    private ImageView artistImageView, fadeCoverView;
    private TextView tracksView;
    private Drawable mActionBarBackgroundDrawable, mActionBarCoverDrawable;
    private ActionBar actionBar;
    private ArrayList<Album> albumList;
    private NotifyingScrollView bgScrollView, scrollView;
    private ArtistsTracksAdapter artistsTracksAdapter;
    private ExpandableListView trackListView;
    private Song currTrack;
    private MusicService musicSrv;
    private ImageButton fab;
    private int vibrantColor;
    private NotifyingScrollView.OnScrollChangedListener mOnScrollChangedListener = new NotifyingScrollView.OnScrollChangedListener() {
        public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
            final int headerHeight = artistImageView.getHeight() - getActionBar().getHeight();
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

    /**
     * * Method for Setting the Height of the ListView dynamically.
     * *** Hack to fix the issue of not showing all the items of the ListView
     * *** when placed inside a ScrollView  ***
     */
    public static void setListViewHeightBasedOnChildren(ExpandableListView listView, Context context) {
        ExpandableListAdapter listAdapter = listView.getExpandableListAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        int groupCount = listAdapter.getGroupCount();
        int groupHeight = 200;
        Toast.makeText(context, "groupHeight: " + groupHeight, Toast.LENGTH_SHORT).show();
        //view = listAdapter.getChildView(0, 0, false, view, listView);
        int childHeight = (int) (groupHeight * 0.4);
        Toast.makeText(context, "childHeight: " + groupHeight, Toast.LENGTH_SHORT).show();
        int childrenCount = 0;
        for (int i = 0; i < groupCount; i++)
            childrenCount += listAdapter.getChildrenCount(i);


        ViewGroup.LayoutParams params = listView.getLayoutParams();
        totalHeight += (listView.getDividerHeight() * (listAdapter.getGroupCount()));
        totalHeight += (groupHeight * groupCount) + (childHeight * childrenCount);
        params.height = totalHeight;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_artist);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        currArtist = MainActivity.currArtist;
        albumList = currArtist.getAlbums();
        artistImageView = (ImageView) findViewById(R.id.artistImage);
        fadeCoverView = (ImageView) findViewById(R.id.artist_fadeCover);
        actionBar = getActionBar();
        abBackground = (RelativeLayout) findViewById(R.id.artist_ab_background);
        header = (RelativeLayout) findViewById(R.id.artist_header);
        fab = (ImageButton) findViewById(R.id.artistFab);
        musicSrv = MainActivity.getMusicService();
        if (!musicSrv.isPng())
            fab.setImageDrawable(getResources().getDrawable(R.drawable.play));

        //Setup Show ActionBar Variables
        contentGapper = (FrameLayout) findViewById(R.id.artist_content_gapper);

        //Set ActionBar Title & Cover Image
        if (currArtist != null) {
            if (actionBar != null)
                actionBar.setTitle("");
            if (currArtist.getImage() != null)
                artistImageView.setImageBitmap(currArtist.getImage());
            // artistImageView.notify();
        }

        //Set Track List
        artistsTracksAdapter = new ArtistsTracksAdapter(this, albumList);
        trackListView = (ExpandableListView) findViewById(R.id.artist_trackListView);

        //Move Indicator
        Display newDisplay = getWindowManager().getDefaultDisplay();
        int width = newDisplay.getWidth();
        trackListView.setIndicatorBoundsRelative(width - 100, width);


        trackListView.setAdapter(artistsTracksAdapter);
        setListViewHeightBasedOnChildren(trackListView, getApplicationContext()); //TODO adjust for expandable
        trackListView.setFocusable(false);

        //Set Header Info
        TextView headerTitle = (TextView) findViewById(R.id.artist_header_title);
        TextView abTitle = (TextView) findViewById(R.id.artist_ab_title);
        headerTitle.setText(currArtist.getName());
        abTitle.setText(currArtist.getName());

        //Set Colors
        vibrantColor = currArtist.getAccentColor();
        if (vibrantColor == Color.WHITE)
            vibrantColor = MainActivity.primaryColor;

        mActionBarBackgroundDrawable = getResources().getDrawable(R.drawable.ab_background);
        mActionBarBackgroundDrawable.setColorFilter(vibrantColor, PorterDuff.Mode.SRC_ATOP);
        mActionBarCoverDrawable = getResources().getDrawable(R.drawable.ab_background);
        mActionBarCoverDrawable.setColorFilter(vibrantColor, PorterDuff.Mode.SRC_ATOP);


        abBackground.setBackgroundColor(vibrantColor);
        header.setBackgroundColor(vibrantColor);
        abBackground.setAlpha(0);


        if (mActionBarCoverDrawable != null)
            mActionBarCoverDrawable.setAlpha(0);


        fadeCoverView.setImageDrawable(mActionBarCoverDrawable);

        scrollView = ((NotifyingScrollView) findViewById(R.id.artist_scroll_view)); // TODO adjust for expandable

        scrollView.setOnScrollChangedListener(mOnScrollChangedListener);
        bgScrollView = (NotifyingScrollView) findViewById(R.id.artist_bg_scroll_view);

        ScheduledExecutorService myScheduledExecutorService = Executors.newScheduledThreadPool(1);

        myScheduledExecutorService.scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        monitorHandler.sendMessage(monitorHandler.obtainMessage());
                    }
                },
                200, //initialDelay
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
                Drawable pauseDrawable = getResources().getDrawable(R.drawable.pause);
                fab.setImageDrawable(pauseDrawable);
            } else {
                //set playButton icon to play
                Drawable playDrawable = getResources().getDrawable(R.drawable.play);
                fab.setImageDrawable(playDrawable);
            }
        }
        /*
        else
        {
            //set playButton icon to play
            Drawable playDrawable = getResources().getDrawable(R.drawable.play);
            fab.setImageDrawable(playDrawable);
        }
        */

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_album, menu);
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

    public void trackPicked(View view) {

        int pos = Integer.parseInt(view.getTag().toString());
        currTrack = currArtist.getTracks().get(pos);
        musicSrv.setSong(pos);
        musicSrv.playAlbum(currArtist.getTracks().get(pos).getAlbumObj(), pos);

        Intent intent = new Intent(this, NowPlayingActivity.class);

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create((View) fab, "fab"));

        startActivity(intent, options.toBundle());
    }


    public void albumFabPressed(View v) {
        MainActivity.fabPressed(v);
    }
}
