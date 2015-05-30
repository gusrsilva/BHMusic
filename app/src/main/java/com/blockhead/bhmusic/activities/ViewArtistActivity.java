package com.blockhead.bhmusic.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
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
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blockhead.bhmusic.R;
import com.blockhead.bhmusic.adapters.ArtistsTracksAdapter;
import com.blockhead.bhmusic.objects.Album;
import com.blockhead.bhmusic.objects.Artist;
import com.nirhart.parallaxscroll.views.ParallaxExpandableListView;

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
    MusicService musicSrv;
    ImageButton fab;
    Artist currArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_artist);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        final ActionBar mActionBar = getActionBar();
        if(mActionBar != null)
            mActionBar.setTitle("");

        musicSrv = MainActivity.getMusicService();
        fab = (ImageButton)findViewById(R.id.artistFab);
        Drawable fabBG = fab.getBackground();
        fabBG.setColorFilter(MainActivity.accentColor, PorterDuff.Mode.SRC_ATOP);


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
                    if (firstVisibleItem == 0 && abBackground.getAlpha() == 255) {
                        abBackground.setAlpha(0);
                    } else if (firstVisibleItem >= 1 && abBackground.getAlpha() == 0) {
                        abBackground.setAlpha(255);
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

        ArtistsTracksAdapter mArtistsTracksAdapter = new ArtistsTracksAdapter(getApplicationContext(), albums);
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
            header.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int imgHeight = header.getMeasuredHeight();
            final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
            int headerHeight = (int) (60 * scale + 0.5f);
            int albumsHeight = (int) (110 * scale + 0.5f);
            albumsHeight *= currArtist.getAlbums().size();
            double footerHeight = size.y - (imgHeight + headerHeight + albumsHeight);
            footer.setMinimumHeight((int) footerHeight);
            xLV.addFooterView(footer);


        }

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
    }

    public void artistFabPressed(View v)
    {
        MainActivity.fabPressed(v);
    }

    public void artistTrackPicked(int groupPostion, int childPosition)
    {
        Album currAlbum = currArtist.albums.get(groupPostion);
        //musicSrv.setSong(pos);
        musicSrv.playAlbum(currAlbum, childPosition);

        Intent intent = new Intent(this, NowPlayingActivity.class);

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create((View)fab, "fab"));

        startActivity(intent, options.toBundle());
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
}
