package com.blockhead.bhmusic;

import java.io.File;
import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import java.util.Random;

import org.w3c.dom.Text;

/**
 * Created by Gus on 2/25/2015.
 */
public class npTracksAdapter extends BaseAdapter {

    private ArrayList<Song> songs;
    private LayoutInflater songInf;
    private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";


    public npTracksAdapter( Context c, ArrayList<Song> theSongs)
    {
        songs = theSongs;
        songInf = LayoutInflater.from(c);
    }

    @Override
    public int getCount()
    {
        return songs.size();
    }

    @Override
    public Object getItem(int arg0)
    {
        return songs.get(arg0).getTitle();
    }

    @Override
    public long getItemId(int arg0)
    {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        //map to song layout
        LinearLayout trackLay = (LinearLayout)songInf.inflate(R.layout.np_track, parent, false);

        //get title and artist views
        TextView tracksTitleView = (TextView)trackLay.findViewById(R.id.np_track_title);
        TextView tracksDurationView = (TextView)trackLay.findViewById(R.id.np_track_duration);

        //get song using position
        Song currSong = songs.get(position);

        //get/set title and artist strings
        tracksTitleView.setText(currSong.getTitle());
        tracksDurationView.setText(currSong.getDuration());

        //set position as tag
        trackLay.setTag(position);

        return trackLay;
    }
}
