package com.blockhead.bhmusic.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
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
import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.OnItemMovedListener;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.TouchViewDraggableManager;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.TimedUndoAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;
import com.nirhart.parallaxscroll.views.ParallaxListView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class ViewPlaylistActivity extends AppCompatActivity {

    Playlist currPlaylist;
    ArrayList<Song> songList;
    ArrayList<Long> idList;
    int playlistSize;
    MusicService musicSrv;
    ImageButton shuffleButton, editButton;
    FloatingActionButton fab;
    CoordinatorLayout coordLay;
    private int mNewItemCount;
    private DynamicListView editListView;
    private ActionBar mActionBar;
    private int actionBarColor;
    private Point size;
    private PlaylistMembersAdapter plAdt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_playlist);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        currPlaylist = MainActivity.currPlaylist;
        playlistSize = currPlaylist.getSize();
        ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance
        musicSrv = MainActivity.getMusicService();

        mActionBar = getSupportActionBar();
        if(mActionBar != null) {
            mActionBar.setTitle("");
        }

        int songPos = 0, i=0;
        String coverUri = null;
        songList = currPlaylist.getMembers();
        idList = currPlaylist.getSongIds();

        //Get first valid coverURI from songs in playlist
        while(coverUri == null && songList != null && i < songList.size() -1)
        {
            coverUri = songList.get(i++).getCoverURI();
        }

        //Set ActionBar color
        actionBarColor = MainActivity.primaryColor;
        if(songList != null && songList.get(i).getAccentColor() != Color.WHITE)
            actionBarColor = songList.get(i).getAccentColor();

        /* Setup Floating Action Button */
        coordLay = (CoordinatorLayout)findViewById(R.id.playlist_coordinator);
        fab = (FloatingActionButton)findViewById(R.id.playlist_FAB);
        fab.setBackgroundTintList(ColorStateList.valueOf(MainActivity.accentColor));
        setFabDrawable();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabPressed();
            }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                nowPlayingButtonPressed();
                return true;
            }
        });

        final RelativeLayout abHolder = (RelativeLayout)findViewById(R.id.playlist_ab_holder);
        FrameLayout abBackground = (FrameLayout)findViewById(R.id.playlist_ab_background);
        abBackground.setBackgroundColor(actionBarColor);
        abHolder.setAlpha(0);

        //Define On Scroll Listener for ParallaxListView
        AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mActionBar != null)
                {
                    if (firstVisibleItem == 0 && abHolder.getAlpha() == 1)
                    {
                        abHolder.setAlpha(0);
                        mActionBar.setTitle("");
                    }
                    else if (firstVisibleItem >= 1 && abHolder.getAlpha() == 0)
                    {
                        abHolder.setAlpha(1);
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
        size = new Point();
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
        linLay.setBackgroundColor(actionBarColor);

        /* Initialize and set up shuffle button */
        shuffleButton = (ImageButton) titleHeader.findViewById(R.id.playlist_title_shuffleButton);
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPlaylistShufflePressed(v);
            }
        });

        /* Initialize and set up the edit button */
        editButton = (ImageButton) titleHeader.findViewById(R.id.playlist_title_editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editButtonPressed(v);
            }
        });

        /* Set up ParralaxListView */
        ParallaxListView memberList = (ParallaxListView) findViewById(R.id.playlist_members);
        memberList.addParallaxedHeaderView(header);
        memberList.addHeaderView(titleHeader);
        memberList.setOnScrollListener(mOnScrollListener);
        memberList.setBackgroundColor(actionBarColor);
        memberList.setDivider(null);

        plAdt = new PlaylistMembersAdapter(getApplicationContext(), songList);
        memberList.setAdapter(plAdt);

        /* Fill the rest of the list if its not long enough to cover the background */
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

        /* Setup Dynamic List View */
        editListView = (DynamicListView) findViewById(R.id.dynamiclistview);

        /* Setup the adapter */
        MyListAdapter adapter = new MyListAdapter(this, currPlaylist);
        TimedUndoAdapter timedUndoAdapter = new TimedUndoAdapter(adapter, this, new MyOnDismissCallback(adapter));
        timedUndoAdapter.setTimeoutMs(1000);
        SwingBottomInAnimationAdapter animAdapter = new SwingBottomInAnimationAdapter(timedUndoAdapter);
        animAdapter.setAbsListView(editListView);
        assert animAdapter.getViewAnimator() != null;
        animAdapter.getViewAnimator().setInitialDelayMillis(300);
        editListView.setAdapter(animAdapter);

        /* Enable drag and drop functionality */
        editListView.enableDragAndDrop();
        editListView.setDraggableManager(new TouchViewDraggableManager(R.id.list_row_draganddrop_touchview));
        editListView.setOnItemMovedListener(new MyOnItemMovedListener(adapter));
        editListView.setOnItemLongClickListener(new MyOnItemLongClickListener(editListView));

        /* Enable swipe to dismiss */
        editListView.enableSimpleSwipeUndo();

        editListView.setVisibility(View.INVISIBLE);
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
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void playlistTrackPicked(View view, int position)
    {
        if(view.getTag() == null) {//Ignore clicks on header and footer
            return;
        }
        int pos = Integer.parseInt(view.getTag().toString());
        playTrack(pos);

    }
    private void playTrack(int pos)
    {
        musicSrv.setSong(pos);
        musicSrv.playPlaylist(currPlaylist, pos);
        setFabDrawable();

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

    public void viewPlaylistShufflePressed(View view)
    {
        musicSrv.shuffle = true;
        if(MainActivity.shuffleAnimation != null)
            shuffleButton.startAnimation(MainActivity.shuffleAnimation);
        int pos = new Random().nextInt(playlistSize - 1);
        musicSrv.setSong(pos);
        musicSrv.playPlaylist(currPlaylist, pos);
        setFabDrawable();
        MainActivity.shuffleButton.setSelected(true);
        if (NowPlayingActivity.shuffleButton != null)
            NowPlayingActivity.shuffleButton.setSelected(true);
        Snackbar.make(coordLay, "Now Shuffling: " + currPlaylist.getTitle(), Snackbar.LENGTH_SHORT)
                .show();
        setFabDrawable();
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
    private void fabPressed()
    {
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

    @TargetApi(21)
    public void editButtonPressed(View v)
    {
        if(editListView == null)
            return;

        if(editListView.getVisibility() == View.VISIBLE)
        {   //HIDE TRACKLIST IF SHOWING
            if(MainActivity.isLollipop()) //If supports circular reveal effect
            {
                // get the center for the clipping circle
                int cx = (editButton.getLeft() + editButton.getRight()) / 2;
                int cy = (size.y) / 2;
                // get the initial radius for the clipping circle
                int initialRadius = editListView.getWidth();
                // create the animation (the final radius is zero)
                Animator anim =
                        ViewAnimationUtils.createCircularReveal(editListView, cx, cy, initialRadius, 0);
                // make the view invisible when the animation is done
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        editListView.setVisibility(View.INVISIBLE);
                        fab.setVisibility(View.VISIBLE);
                    }
                });
                // start the animation
                anim.start();
            }
            else
            {
                editListView.setVisibility(View.INVISIBLE);
                fab.setVisibility(View.VISIBLE);
            }


            if (mActionBar != null) {
                mActionBar.setBackgroundDrawable(null);
                mActionBar.setTitle("");
            }
        }
        else
        {
            fab.setVisibility(View.INVISIBLE);
            if(MainActivity.isLollipop())
            {
                // get the center for the clipping circle
                int cx = (editButton.getLeft() + editButton.getRight()) / 2;
                int cy = (size.y) / 2;
                // get the final radius for the clipping circle
                int finalRadius = Math.max(editListView.getWidth(), editListView.getHeight());
                // create the animator for this view (the start radius is zero)
                Animator anim =
                        ViewAnimationUtils.createCircularReveal(editListView, cx, cy, 0, finalRadius);
                anim.setDuration(300);
                // make the view invisible when the animation is done
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (mActionBar != null) {
                            mActionBar.setBackgroundDrawable(new ColorDrawable(actionBarColor));
                            mActionBar.setTitle("Edit " + currPlaylist.getTitle());
                        }
                    }
                });

                // make the view visible and start the animation
                editListView.setVisibility(View.VISIBLE);
                anim.start();
            }
            else
                editListView.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public void onBackPressed() {
        if (editListView.getVisibility() == View.VISIBLE)
            editButtonPressed(null);
        else
            super.onBackPressed();
    }

    /**
     * The following classes are for the Dynamic List View used to
     * edit the playlist
     */
    private static class MyListAdapter extends ArrayAdapter<Song> implements UndoAdapter {

        private final Context mContext;
        private Playlist playlist;
        private LayoutInflater songInf;
        private ImageLoader imageLoader;

        MyListAdapter(final Context context, Playlist pl) {
            mContext = context;
            playlist = pl;
            ArrayList<Song> members;
            members = playlist.getMembers();
            for (int i = 0; i < members.size(); i++) {
                add(members.get(i));
            }
            songInf = LayoutInflater.from(context);
            imageLoader = ImageLoader.getInstance(); // Get singleton instance
        }

        @Override
        public long getItemId(final int position)
        {
            return getItem(position).getID();
        }
        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            if(position >= getCount()) {
                return null;
            }

            LinearLayout trackLay;
            if (convertView == null)
                trackLay = (LinearLayout) songInf.inflate(R.layout.edit_song_in_playlist, parent, false);
            else        //Recycle view
                trackLay = (LinearLayout) convertView;

            //get title and artist views
            TextView title = (TextView) trackLay.findViewById(R.id.song_in_edit_playlist_title);
            TextView artist = (TextView) trackLay.findViewById(R.id.song_in_edit_playlist_artist);
            ImageView cover = (ImageView) trackLay.findViewById(R.id.artImage_in_edit_playlist);

            title.setText(getItem(position).getTitle());
            artist.setText(getItem(position).getArtist());
            imageLoader.displayImage(getItem(position).getCoverURI(), cover);

            //set position as tag
            trackLay.setTag(position);

            return trackLay;
        }

        @NonNull
        @Override
        public View getUndoView(final int position, final View convertView, @NonNull final ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.undo_row, parent, false);
            }
            TextView tv = (TextView) view.findViewById(R.id.undo_row_texttv);
            tv.setText("Removed " + getItem(position).getTitle());
            return view;
        }

        @NonNull
        @Override
        public View getUndoClickView(@NonNull final View view) {
            return view.findViewById(R.id.undo_row_undobutton);
        }
    }

    private static class MyOnItemLongClickListener implements AdapterView.OnItemLongClickListener {

        private final DynamicListView mListView;

        MyOnItemLongClickListener(final DynamicListView listView) {
            mListView = listView;
        }

        @Override
        public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
            if (mListView != null) {
                mListView.startDragging(position - mListView.getHeaderViewsCount());
            }
            return true;
        }
    }

    private class MyOnDismissCallback implements OnDismissCallback {

        private final ArrayAdapter<Song> mAdapter;

        MyOnDismissCallback(final ArrayAdapter<Song> adapter) {
            mAdapter = adapter;
        }

        @Override
        public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
            for (int position : reverseSortedPositions)
            {
                mAdapter.remove(position);
                songList.remove(position);
                idList.remove(position);
                currPlaylist.setChanged();
                plAdt.notifyDataSetChanged();
            }
        }
    }

    private class MyOnItemMovedListener implements OnItemMovedListener {

        private final ArrayAdapter<Song> mAdapter;

        MyOnItemMovedListener(final ArrayAdapter<Song> adapter) {
            mAdapter = adapter;
        }

        @Override
        public void onItemMoved(final int originalPosition, final int newPosition)
        {

            if(originalPosition < newPosition)  //Moved item down
            {
                for(int i = originalPosition; i < newPosition; i++)
                {
                    Collections.swap(songList, i, i+1);
                    Collections.swap(idList, i, i+1);
                }
                plAdt.notifyDataSetChanged();
                currPlaylist.setChanged();

            }
            else if (originalPosition > newPosition)  //Moved item up
            {
                for(int i = originalPosition; i > newPosition; i--)
                {
                    Collections.swap(songList, i, i-1);
                    Collections.swap(idList, i, i-1);
                }
                plAdt.notifyDataSetChanged();
                currPlaylist.setChanged();
            }

        }
    }
}
