package com.blockhead.bhmusic.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.CardView;
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
import com.blockhead.bhmusic.objects.Album;

import java.util.ArrayList;

/**
 * Created by Gus on 2/25/2015.
 */
public class AlbumAdapter extends BaseAdapter implements SectionIndexer {

    private ArrayList<Album> albums;
    private LayoutInflater albumInf;
    private Context context;
    private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public AlbumAdapter(Context c, ArrayList<Album> theAlbums) {
        albums = theAlbums;
        albumInf = LayoutInflater.from(c);
        context = c;
    }

    @Override
    public int getCount() {
        return albums.size();
    }

    @Override
    public Object getItem(int arg0) {
        return albums.get(arg0).getTitle();
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int postion, View convertView, ViewGroup parent) {
        //map to song layout
        LinearLayout albumLay = (LinearLayout) albumInf.inflate(R.layout.album, parent, false);

        //get title and artist views
        TextView albumTitleView = (TextView) albumLay.findViewById(R.id.album_title);
        TextView artistView = (TextView) albumLay.findViewById(R.id.album_artist);
        ImageView coverView = (ImageView) albumLay.findViewById(R.id.artImage);
        CardView cardView = (CardView) albumLay.findViewById(R.id.albumCard);

        //get song using position
        Album currAlbum = albums.get(postion);

        //get/set title and artist strings
        albumTitleView.setText(currAlbum.getTitle());
        artistView.setText(currAlbum.getArtist());

        //Accent Color
        int accentColor = currAlbum.getAccentColor();
        if (accentColor != Color.WHITE) {
            cardView.setCardBackgroundColor(accentColor);
            albumTitleView.setTextColor(parent.getResources().getColor(R.color.white));
            artistView.setTextColor(parent.getResources().getColor(R.color.hint_white));
        }

        //set album artwork
        Bitmap cover = currAlbum.getCover();
        if (cover != null) {
            coverView.setImageBitmap(cover);
        } else {
            //set random cover color
            coverView.setBackgroundColor(parent.getResources().getColor(MainActivity.randomColor()));
        }

        //set position as tag
        albumLay.setTag(postion);

        return albumLay;
    }


    //Section Indexer Functions
    @Override
    public int getPositionForSection(int section) {
        if (section == 0)
            return 0;
        int count = 0;
        //while first letter does not match section
        for (int i = 0; i < albums.size(); i++) {
            String temp = albums.get(i).getTitle().charAt(0) + "";
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
