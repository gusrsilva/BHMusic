package com.blockhead.bhmusic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
public class PlaylistMembersAdapter extends BaseAdapter {

    private ArrayList<Song> members;
    private LayoutInflater songInf;
    private ImageLoader imageLoader;


    public PlaylistMembersAdapter(Context c, ArrayList<Song> arrayList) {
        members = arrayList;
        songInf = LayoutInflater.from(c);
        imageLoader = ImageLoader.getInstance(); // Get singleton instance

    }

    @Override
    public int getCount() {
        return members.size();
    }

    @Override
    public Song getItem(int arg0) {
        return members.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return members.get(arg0).getID();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LinearLayout trackLay;
        if(convertView == null)
            trackLay = (LinearLayout) songInf.inflate(R.layout.song_in_playlist, parent, false);
        else        //Recycle view
            trackLay = (LinearLayout) convertView;

        //get title and artist views
        TextView title = (TextView) trackLay.findViewById(R.id.song_in_playlist_title);
        TextView artist = (TextView) trackLay.findViewById(R.id.song_in_playlist_artist);
        ImageView cover = (ImageView) trackLay.findViewById(R.id.artImage_in_playlist);

        title.setText(members.get(position).getTitle());
        artist.setText(members.get(position).getArtist());
        imageLoader.displayImage(members.get(position).getCoverURI(), cover);


        //set position as tag
        trackLay.setTag(position);

        return trackLay;
    }
}