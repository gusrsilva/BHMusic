package com.blockhead.bhmusic.activities;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.blockhead.bhmusic.R;
import com.blockhead.bhmusic.utils.SongOptions;
import com.blockhead.bhmusic.adapters.SearchAdapter;
import com.blockhead.bhmusic.objects.Album;
import com.blockhead.bhmusic.objects.Artist;
import com.blockhead.bhmusic.objects.Song;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class SearchActivity extends AppCompatActivity {
    private FloatingActionButton fab;
    private MusicService musicSrv;
    private ArrayList<Song> foundSongs = new ArrayList<>();
    private ArrayList<Album> foundAlbums = new ArrayList<>();
    private ArrayList<Artist> foundArtists = new ArrayList<>();
    private SearchAdapter songAdt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        musicSrv = MainActivity.getMusicService();

        /* Set up Action Bar */
        ActionBar mActionBar = getSupportActionBar();
        if(mActionBar != null)
            mActionBar.hide();

        final CoordinatorLayout coordLay = (CoordinatorLayout)findViewById(R.id.search_coordinator);
        LinearLayout fauxAB = (LinearLayout)findViewById(R.id.search_faux_ab);
        if( fauxAB != null)
            fauxAB.setBackgroundColor(MainActivity.primaryColor);

        /* Set up FAB */
        fab = (FloatingActionButton)findViewById(R.id.search_fab);
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
        fab.setBackgroundTintList(ColorStateList.valueOf(MainActivity.accentColor));

        /**
        /* Set up Result Lists
        **/
        /* Song Results */
        songAdt = new SearchAdapter(getApplicationContext(), foundSongs, foundAlbums, foundArtists);
        StickyListHeadersListView songListView = (StickyListHeadersListView)findViewById(R.id.search_song_list);
        songListView.setAdapter(songAdt);
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object obj = songAdt.getItem(position);
                if (obj instanceof Song)
                    songClicked((Song) obj);
                else if (obj instanceof Album)
                    albumClicked((Album) obj);
                else if (obj instanceof Artist)
                    artistClicked((Artist) obj);
                else {
                    Toast.makeText(getApplicationContext(), "Invalid Selection", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
        songListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                Object obj = songAdt.getItem(position);
                if (obj instanceof Song)
                    SongOptions.openSongOptions((Song) obj, SearchActivity.this, coordLay);
                else {
                    Toast.makeText(getApplicationContext(), "Invalid Selection", Toast.LENGTH_SHORT)
                            .show();
                }

                return true;
            }
        });

        /* Setup EditText */
        EditText searchText = (EditText)findViewById(R.id.search_text);
        searchText.setBackgroundColor(MainActivity.primaryColor);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //TODO: Make search more efficient, dont add already added items
                search(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        Drawable editTextBg = ContextCompat.getDrawable(getApplicationContext(), R.drawable.edit_text_bg);
        editTextBg.setColorFilter(MainActivity.accentColor, PorterDuff.Mode.SRC_ATOP);
        searchText.setBackground(editTextBg);

        ImageView backButton = (ImageView)findViewById(R.id.search_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_search, menu);
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

    private void fabPressed()
    {
        if(musicSrv == null)
            musicSrv = MainActivity.getMusicService();
        if(musicSrv.isPng())
            musicSrv.pausePlayer();
        else
            musicSrv.resumePlayer();
        setFabDrawable();
    }

    @TargetApi(21)
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

        if(MainActivity.isLollipop())
        {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                    Pair.create((View) fab, "fab"));
            startActivity(intent, options.toBundle());
        }
        else
            startActivity(intent);
    }

    private void setFabDrawable()
    {
        Drawable pauseDrawable = getResources().getDrawable(R.drawable.ic_pause_white_36dp);
        Drawable playDrawable = getResources().getDrawable(R.drawable.ic_play_white_36dp);
        if(musicSrv.isPng())
            fab.setImageDrawable(pauseDrawable);
        else
            fab.setImageDrawable(playDrawable);
    }

    private void search(CharSequence cs)
    {
        foundSongs.clear();foundAlbums.clear();foundArtists.clear();
        if(cs == null)
            return;

        String s = cs.toString().toLowerCase();
        if(s.isEmpty())
            return;

        /* Add all found songs */
        for(Song song : MainActivity.songList)
        {
            if(song.getTitle().toLowerCase().contains(s))
                foundSongs.add(song);
        }

        /* Add all found albums */
        for(Album album : MainActivity.albumList)
        {
            if(album.getTitle().toLowerCase().contains(s))
                foundAlbums.add(album);
        }

        /* Add all found artists */
        for(Artist artist : MainActivity.artistList)
        {
            if(artist.getName().toLowerCase().contains(s))
                foundArtists.add(artist);
        }

        songAdt.notifyDataSetChanged();
    }

    private void songClicked(Song song)
    {
        int pos = MainActivity.songList.indexOf(song);
        musicSrv.setSong(pos);
        musicSrv.playSong();
        Intent intent = new Intent(this, NowPlayingActivity.class);
        startActivity(intent);
    }

    private void albumClicked(Album album)
    {
        MainActivity.currAlbum = album;
        Intent intent = new Intent(this, ViewAlbumActivity.class);
        startActivity(intent);
    }

    private void artistClicked(Artist artist)
    {
        MainActivity.currArtist = artist;
        Intent intent = new Intent(this, ViewArtistActivity.class);
        startActivity(intent);
    }
}
