package com.blockhead.bhmusic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blockhead.bhmusic.R;
import com.blockhead.bhmusic.objects.Playlist;
import com.blockhead.bhmusic.objects.Song;

import java.util.ArrayList;

/**
 * Created by Gus on 2/25/2015.
 */
public class PlaylistListAdapter extends BaseAdapter {

    private ArrayList<Playlist> playlists;
    private LayoutInflater songInf;


    public PlaylistListAdapter(Context c, ArrayList<Playlist> arr) {
        playlists = arr;
        songInf = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return playlists.size();
    }

    @Override
    public Object getItem(int arg0) {
        return playlists.get(arg0).getTitle();
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to song layout
        LinearLayout trackLay = (LinearLayout) songInf.inflate(R.layout.playlist, parent, false);

        //get title and artist views
        TextView title = (TextView) trackLay.findViewById(R.id.playlist_title);
        TextView numtracks = (TextView) trackLay.findViewById(R.id.playlist_numtracks);

        title.setText(playlists.get(position).getTitle());
        numtracks.setText(playlists.get(position).getSize() + " songs");

        //set position as tag
        trackLay.setTag(position);

        return trackLay;
    }
}
