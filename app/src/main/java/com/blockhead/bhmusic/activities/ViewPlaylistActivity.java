package com.blockhead.bhmusic.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.Random;

public class ViewPlaylistActivity extends AppCompatActivity {    //TODO: Make FAB work!

    Playlist currPlaylist;
    ArrayList<Song> songList;
    int playlistSize;
    MusicService musicSrv;
    ImageButton shuffleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_playlist);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        currPlaylist = MainActivity.currPlaylist;
        playlistSize = currPlaylist.getSize();
        ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance
        musicSrv = MainActivity.getMusicService();

        final android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        int mActionBarSize = getActionBarHeight() + getStatusBarHeight();
        if(mActionBar != null) {
            mActionBar.setTitle("");
        }

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
                        mActionBar.setTitle(currPlaylist.getTitle());
                    }
                }
            }
        };

        /* Set Image Header attributes */
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

        /* Set title header */
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View titleHeader = inflater.inflate(R.layout.playlist_title_header, null);
        titleHeader.setMinimumHeight(getActionBarHeight() + getStatusBarHeight());
        TextView title = (TextView) titleHeader.findViewById(R.id.playlist_title_header_title);
        TextView subtitle = (TextView) titleHeader.findViewById(R.id.playlist_title_header_subtitle);
        LinearLayout linLay = (LinearLayout) titleHeader.findViewById(R.id.playlist_title_header_lin);

        title.setText(currPlaylist.getTitle());
        subtitle.setText(playlistSize + (playlistSize==1?" Song":" Songs"));
        linLay.setBackgroundColor(accentColor);

        /* Initialize and set up shuffle button */
        shuffleButton = (ImageButton) titleHeader.findViewById(R.id.playlist_title_shuffleButton);
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPlaylistShufflePressed(v);
            }
        });

        //Initialize ParralaxListView
        ParallaxListView memberList = (ParallaxListView) findViewById(R.id.playlist_members);
        //Add Headers to it
        memberList.addParallaxedHeaderView(header);
        memberList.addHeaderView(titleHeader);
        //Set Scroll Listener
        memberList.setOnScrollListener(mOnScrollListener);
        //Set BG Color
        memberList.setBackgroundColor(accentColor);
        //Remove dividers
        memberList.setDivider(null);

        PlaylistMembersAdapter plAdt = new PlaylistMembersAdapter(getApplicationContext(), currPlaylist);
        memberList.setAdapter(plAdt);

        //Fill the rest of the list if its not long enough to cover the background
        if( playlistSize <= 3)
        {
            View footer = new View(this);
            footer.setBackgroundColor(getResources().getColor(R.color.background_color));
            footer.setMinimumWidth(size.x);
            footer.setMinimumHeight((int) minHeight);
            footer.setClickable(false);
            footer.setLongClickable(false);
            memberList.addFooterView(footer);
        }

        memberList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
        if(view.getTag() == null) {//Ignore clicks on header and footer
            Toast.makeText(getApplicationContext(), "Pos: " + (position), Toast.LENGTH_SHORT).show();
            return;
        }
        int pos = Integer.parseInt(view.getTag().toString());
        playTrack(pos);

    }
    private void playTrack(int pos)
    {
        musicSrv.setSong(pos);
        musicSrv.playPlaylist(currPlaylist, pos);

        Intent intent = new Intent(this, NowPlayingActivity.class);
        startActivity(intent);
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

    public void viewPlaylistShufflePressed(View view)   //TODO: Improve playPrev logic for shuffle playlist
    {
        musicSrv.shuffle = true;
        if(MainActivity.shuffleAnimation != null)
            shuffleButton.startAnimation(MainActivity.shuffleAnimation);
        musicSrv.playPlaylist(currPlaylist, (new Random().nextInt(playlistSize)));
        MainActivity.shuffleButton.setSelected(true);
        if (NowPlayingActivity.shuffleButton != null)
            NowPlayingActivity.shuffleButton.setSelected(true);
        Snackbar.make(view, "Now Shuffling: " + currPlaylist.getTitle(), Snackbar.LENGTH_SHORT)
                .show();
    }
}
