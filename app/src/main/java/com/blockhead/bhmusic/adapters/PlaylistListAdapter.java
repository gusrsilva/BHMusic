package com.blockhead.bhmusic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blockhead.bhmusic.R;
import com.blockhead.bhmusic.objects.Playlist;
import com.blockhead.bhmusic.objects.Song;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by Gus on 2/25/2015.
 */
public class PlaylistListAdapter extends BaseAdapter {

    private ArrayList<Playlist> playlists;
    private LayoutInflater songInf;
    private ImageLoader imageLoader;


    public PlaylistListAdapter(Context c, ArrayList<Playlist> arr) {
        playlists = arr;
        songInf = LayoutInflater.from(c);
        imageLoader = ImageLoader.getInstance();
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
        LinearLayout trackLay;
        if(convertView == null)
            trackLay = (LinearLayout) songInf.inflate(R.layout.playlist, parent, false);
        else        //Recycle view
            trackLay = (LinearLayout)convertView;

        //get title and artist views
        TextView title = (TextView) trackLay.findViewById(R.id.playlist_title);
        TextView numTracks = (TextView) trackLay.findViewById(R.id.playlist_numtracks);
        ImageView img = (ImageView) trackLay.findViewById(R.id.playlist_img);
        ImageButton shuffleButton = (ImageButton)trackLay.findViewById(R.id.playlist_shuffleButton);

        title.setText(playlists.get(position).getTitle());
        int size = playlists.get(position).getSize();
        numTracks.setText(size + (size==1?" Song":" Songs"));

        String coverUri = null; int i = 0;
        while(coverUri == null && i < playlists.get(position).getSize())
            coverUri = playlists.get(position).getMembers().get(i++).getCoverURI();
        if(coverUri != null)
            imageLoader.displayImage(coverUri, img);

        //set position as tag
        trackLay.setTag(position);
        shuffleButton.setTag(position);

        return trackLay;
    }
}
