package com.blockhead.bhmusic.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blockhead.bhmusic.R;
import com.blockhead.bhmusic.adapters.ArtistsTracksAdapter;
import com.blockhead.bhmusic.objects.Album;
import com.blockhead.bhmusic.objects.Artist;
import com.nirhart.parallaxscroll.views.ParallaxExpandableListView;

import java.util.ArrayList;


public class ViewArtistActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_artist);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        final ActionBar mActionBar = getActionBar();
        if(mActionBar != null)
            mActionBar.setTitle("");

        ParallaxExpandableListView xLV = (ParallaxExpandableListView)findViewById(R.id.expandableListView);
        Artist currArtist = MainActivity.currArtist;
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
        header.setImageBitmap(currArtist.getImage());
        header.setAdjustViewBounds(true);
        header.setScaleType(ImageView.ScaleType.CENTER_CROP);

        //Set Header Max Height
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        double maxHeight = size.y * 0.6667;
        header.setMaxHeight((int) maxHeight);

        xLV.addParallaxedHeaderView(header);
        xLV.setDivider(null);
        xLV.setBackgroundColor(currArtist.getAccentColor());
        ArtistsTracksAdapter mArtistsTracksAdapter = new ArtistsTracksAdapter(getApplicationContext(), albums);
        xLV.setAdapter(mArtistsTracksAdapter);
        xLV.setOnScrollListener(mOnScrollListener);
    }//END ON CREATE METHOD

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

            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
