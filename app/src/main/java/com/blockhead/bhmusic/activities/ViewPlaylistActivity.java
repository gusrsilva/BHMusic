package com.blockhead.bhmusic.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blockhead.bhmusic.R;
import com.blockhead.bhmusic.adapters.PlaylistMembersAdapter;
import com.blockhead.bhmusic.objects.Playlist;
import com.blockhead.bhmusic.objects.Song;
import com.nirhart.parallaxscroll.views.ParallaxListView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class ViewPlaylistActivity extends Activity {

    Playlist currPlaylist;
    ArrayList<Song> songList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_playlist);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        currPlaylist = MainActivity.currPlaylist;
        ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance

        final ActionBar mActionBar = getActionBar();
        if(mActionBar != null)
            mActionBar.setTitle(currPlaylist.getTitle());

        int songPos = 0, i=0;
        String coverUri = null;
        songList = currPlaylist.getMembers();

        //Get first valid coverURI from songs in playlist
        while(coverUri == null && songList != null && i < songList.size() -1)
        {
            coverUri = songList.get(i++).getCoverURI();
        }

        //Set accent color
        int accentColor = MainActivity.primaryColor;
        if(songList != null && songList.get(i).getAccentColor() != Color.WHITE)
            accentColor = songList.get(i).getAccentColor();

        final RelativeLayout abBackground = (RelativeLayout)findViewById(R.id.artist_ab_background);
        abBackground.setBackgroundColor(accentColor);
        abBackground.setAlpha(0);

        //Define On Scroll Listener for ParallaxListView
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

        //Set Header attributes
        ImageView header = new ImageView(this);
        if(coverUri == null)
        { //Set default artist art if none
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

        //Initialize ParralaxListView
        ParallaxListView memberList = (ParallaxListView) findViewById(R.id.playlist_members);
        //Add Header to it
        memberList.addParallaxedHeaderView(header);
        //Set Scroll Listener
        memberList.setOnScrollListener(mOnScrollListener);
        //Set BG Color
        memberList.setBackgroundColor(accentColor);
        //Remove dividers
        memberList.setDivider(null);

        PlaylistMembersAdapter plAdt = new PlaylistMembersAdapter(getApplicationContext(), currPlaylist);
        memberList.setAdapter(plAdt);

        //Fill the rest of the list if its not long enough to cover the background
        if( currPlaylist.getSize() <= 3)
        {
            View footer = new View(this);
            footer.setBackgroundColor(getResources().getColor(R.color.background_color));
            footer.setMinimumWidth(size.x);
            footer.setMinimumHeight((int) maxHeight);
            memberList.addFooterView(footer);
        }

        memberList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "Picked: " + (position-1),Toast.LENGTH_SHORT).show();
                playlistTrackPicked(view, position);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_playlist, menu);
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

        return super.onOptionsItemSelected(item);
    }

    public void playlistTrackPicked(View view, int position)
    {
        int pos = Integer.parseInt(view.getTag().toString());
        Toast.makeText(getApplicationContext(), "Pos: " + position, Toast.LENGTH_SHORT).show();
        MusicService musicSrv = MainActivity.getMusicService();
        musicSrv.setSong(pos);
        musicSrv.playPlaylist(currPlaylist, pos);

        Intent intent = new Intent(this, NowPlayingActivity.class);
        startActivity(intent);
    }
}
