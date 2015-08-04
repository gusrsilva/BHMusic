package com.blockhead.bhmusic.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.blockhead.bhmusic.R;
import com.blockhead.bhmusic.activities.MainActivity;
import com.blockhead.bhmusic.activities.ViewAlbumActivity;
import com.blockhead.bhmusic.activities.ViewArtistActivity;
import com.blockhead.bhmusic.activities.ViewPlaylistActivity;
import com.blockhead.bhmusic.objects.Artist;
import com.blockhead.bhmusic.objects.Playlist;
import com.blockhead.bhmusic.objects.Song;

public class SongOptions {

    private static MaterialDialog md;
    private static CoordinatorLayout coordLay;
    //Define song options
    static final int ADD_TO_PLAYLIST = 0, GO_TO_ARTIST = 1, GO_TO_ALBUM = 2, FILE_INFO = 3;

    /* Function to define a CoordinatorLayout for the SnackBar */

    public static void openSongOptions(final Song song, final Context context, CoordinatorLayout cL)
    {
        coordLay = cL;
        openSongOptions(song, context);
    }

    @SuppressWarnings("unused")
    public static void openSongOptions(final Song song, final Context context)
    {
        /* Callback for when option is chosen */
        MaterialDialog.ListCallback callback = new MaterialDialog.ListCallback()
        {
            @Override
            public void onSelection(MaterialDialog materialDialog, View view, int position, CharSequence charSequence)
            {
                switch (position)
                {
                    case ADD_TO_PLAYLIST:
                        addToPlaylistPressed(song, context);
                        break;
                    case GO_TO_ARTIST:
                        goToArtistPressed(song, context);
                        break;
                    case GO_TO_ALBUM:
                        goToAlbumPressed(song, context);
                        break;
                    case FILE_INFO:
                        fileInfoPressed(song, context);
                        break;
                    default:
                        Toast.makeText(context, "Invalid Selection", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        /* Create song options dialog */
        MaterialDialog dialog = new MaterialDialog
                .Builder(context)
                .title(song.getTitle())
                .titleColor(MainActivity.accentColor)
                .items(R.array.song_options)
                .itemsCallback(callback)
                .negativeText("Cancel")
                .negativeColor(MainActivity.accentColor)
                .show();
    }

    @SuppressWarnings("unused")
    private static void addToPlaylistPressed(final Song song, final Context context )
    {
        /* Initialize playlists */
        final int size = MainActivity.playlistList.size();
        String[] playlists = new String[size + 1];
        for(int i = 0; i < size; i ++)
        {
            playlists[i] = MainActivity.playlistList.get(i).getTitle();
        }
        playlists[size] = " + Create New Playlist";

        MaterialDialog.ListCallback playlistCallBack = new MaterialDialog.ListCallback()
        {
            @Override
            public void onSelection(MaterialDialog materialDialog, View view, int playlistPosition, CharSequence charSequence)
            {
                if(playlistPosition < size) //Selected An Existing Playlist
                {
                    String title = MainActivity.playlistList.get(playlistPosition).getTitle();

                    if(context instanceof ViewPlaylistActivity
                            && title.equals(MainActivity.currPlaylist.getTitle()))
                    {
                        Toast.makeText(context
                                , "Song is already on this playlist"
                                , Toast.LENGTH_SHORT).show();
                        return;
                    }

                    MainActivity.playlistList.get(playlistPosition).addSong(song);
                    if(coordLay != null)
                        Snackbar.make(coordLay, "Added to " + title, Snackbar.LENGTH_SHORT).show();
                    else
                        Toast.makeText(context, "Added to " + title, Toast.LENGTH_SHORT).show();

                    //updatePlaylistInStore(MainActivity.playlistList.get(playlistPosition));
                }
                else    //Create New Playlist
                {
                    createNewPlaylistPressed(song, context);
                }
            }
        };
        md = new MaterialDialog
                .Builder(context)
                .title("Add to playlist")
                .titleColor(MainActivity.accentColor)
                .items(playlists)
                .itemsCallback(playlistCallBack)
                .show();
    }

    private static void goToArtistPressed(Song song, Context context )
    {
        if(context instanceof ViewArtistActivity)
            return; //Do nothing if already viewing artist

        String artist = song.getArtist();
        for( Artist tempArtist : MainActivity.artistList)
        {
            if(artist.equalsIgnoreCase(tempArtist.getName()))
            {
                MainActivity.currArtist = tempArtist;
                Intent intent = new Intent(context, ViewArtistActivity.class);
                context.startActivity(intent);
                return;
            }
        }
    }

    private static void goToAlbumPressed(Song song, Context context )
    {
        if(context instanceof ViewAlbumActivity)
            return; //Do nothing if already viewing album

        MainActivity.currAlbum = song.getAlbumObj();
        Intent intent = new Intent(context, ViewAlbumActivity.class);
        context.startActivity(intent);

    }

    @SuppressWarnings("unused")
    private static void fileInfoPressed(Song song, Context context )
    {
        MaterialDialog md = new MaterialDialog
                .Builder(context)
                .title("File Info")
                .customView(R.layout.file_info_dialog, false)
                .titleColor(MainActivity.accentColor)
                .show();

        View view = md.getView();
        try
        {
            TextView temp = (TextView) (view.findViewById(R.id.file_info_name));
            temp.setText(song.getTitle());
            temp = (TextView) (view.findViewById(R.id.file_info_duration));
            temp.setText(song.getDuration());
            temp = (TextView) (view.findViewById(R.id.file_info_path));
            temp.setText(song.getPath());
            temp = (TextView) (view.findViewById(R.id.file_info_type));
            temp.setText(song.getExtension());
            temp = (TextView) (view.findViewById(R.id.file_info_size));
            temp.setText(song.getSizeFormatted());
        }
        catch(NullPointerException e)
        {
            md.dismiss();
            Toast.makeText(context, "Unable to Retrieve Song Info", Toast.LENGTH_SHORT).show();
        }
    }

    private static void createNewPlaylistPressed(final Song song, final Context context)
    {
        View enterNameView = LayoutInflater
                .from(context)
                .inflate(R.layout.dialog_new_playlist_enter_name, null);

        final EditText editText = (EditText) enterNameView.findViewById(R.id.enter_playlist_name);
        Drawable editTextBg = ContextCompat.getDrawable(context, R.drawable.edit_text_bg);
        editTextBg.setColorFilter(MainActivity.accentColor, PorterDuff.Mode.SRC_ATOP);
        editText.setBackground(editTextBg);

        md = new MaterialDialog
                .Builder(context)
                .title("Create New Playlist")
                .titleColor(MainActivity.accentColor)
                .autoDismiss(false)
                .positiveText("Save")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        View cv = dialog.getCustomView();
                        if (cv != null) {
                            String str = ((EditText) cv.findViewById(R.id.enter_playlist_name)).getText().toString();
                            if (str.isEmpty()) {
                                Toast.makeText(context
                                        , "Must enter a name!"
                                        , Toast.LENGTH_SHORT)
                                        .show();
                            } else if (!isNewPlaylistName(str)) {
                                Toast.makeText(context
                                        , "Name is already taken!"
                                        , Toast.LENGTH_SHORT)
                                        .show();
                                editText.selectAll();
                            } else {
                                createNewPlaylist(str, song, context);
                                md.dismiss();
                            }
                        }
                    }
                })
                .positiveColor(MainActivity.accentColor)
                .customView(enterNameView, false)
                .show();
    }

    private static void createNewPlaylist(String title, Song song, Context context)
    {
        Playlist temp = new Playlist(title, title.hashCode());
        temp.addSong(song);
        temp.setNew();
        MainActivity.playlistList.add(temp);
        MainActivity.sortPlaylistList();
        if(coordLay != null)
            Snackbar.make(coordLay, "Added to: " + title, Snackbar.LENGTH_SHORT).show();
        else
            Toast.makeText(context , "Added to: " + title, Toast.LENGTH_SHORT).show();

    }

    private static boolean isNewPlaylistName(String str)
    {
        for(Playlist pl : MainActivity.playlistList)
        {
            if(pl.getTitle().equalsIgnoreCase(str))
                return false;
        }
        return true;
    }
}
