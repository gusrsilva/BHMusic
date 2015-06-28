package com.blockhead.bhmusic.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.blockhead.bhmusic.R;
import com.blockhead.bhmusic.activities.MainActivity;
import com.blockhead.bhmusic.objects.Song;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Gus on 2/25/2015.
 */
public class SongAdapter extends BaseAdapter implements SectionIndexer {

    private ArrayList<Song> songs;
    private LayoutInflater songInf;
    private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private Context context;


    public SongAdapter(Context c, ArrayList<Song> theSongs) {
        songs = theSongs;
        songInf = LayoutInflater.from(c);
        imageLoader = ImageLoader.getInstance(); // Get singleton instance
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.default_cover) // resource or drawable
                .showImageOnFail(R.drawable.default_cover)
                .build();
        context = c;

    }

    @Override
    public int getCount() {
        return (songs == null ? 0 : songs.size());
    }

    @Override
    public Object getItem(int arg0) {
        return songs.get(arg0).getTitle();
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int postion, View convertView, ViewGroup parent) {

        LinearLayout songLay;
        if(convertView == null)
         songLay = (LinearLayout) songInf.inflate(R.layout.song, parent, false);
        else        //Else recycle view
            songLay = (LinearLayout)convertView;

        //get title and artist views
        TextView songView = (TextView) songLay.findViewById(R.id.song_title);
        TextView artistView = (TextView) songLay.findViewById(R.id.song_artist);
        ImageView coverView = (ImageView) songLay.findViewById(R.id.artImage);

        //get song using position
        Song currSong = songs.get(postion);

        //get/set title and artist strings
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());

        //set album artwork
        if(currSong.getCoverURI() != null)
            imageLoader.displayImage(currSong.getCoverURI(), coverView);
        else
        {
            int color = parent.getResources().getColor(currSong.getRandomColor());
            coverView.setImageDrawable(context.getDrawable(R.drawable.default_cover));
            coverView.setBackgroundColor(color);
            Log.d("DEBUG-BHCA", "Setting random color with: " + (color));
        }
        //imageLoader.displayImage(currSong.getCoverURI(), coverView);

        //set position as tag
        songLay.setTag(postion);

        return songLay;
    }


    //Section Indexer Functions
    @Override
    public int getPositionForSection(int section) {
        if (section == 0)
            return 0;
        int count = 0;
        //while first letter does not match section
        for (int i = 0; i < songs.size(); i++) {
            String temp = songs.get(i).getTitle().charAt(0) + "";
            String key = (char) (section + 64) + "";
            String keyPassed = (char) (section + 65) + "";
            if (temp.equalsIgnoreCase(key) || temp.equalsIgnoreCase(keyPassed))
                return count;
            else
                count++;
        }
        return count;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public Object[] getSections() {
        String[] sections = new String[mSections.length()];
        for (int i = 0; i < mSections.length(); i++)
            sections[i] = String.valueOf(mSections.charAt(i));
        return sections;
    }
}
