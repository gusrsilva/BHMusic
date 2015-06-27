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
import com.blockhead.bhmusic.objects.Artist;

import java.util.ArrayList;

/**
 * Created by Gus on 2/25/2015.
 */
public class ArtistAdapter extends BaseAdapter implements SectionIndexer {

    private ArrayList<Artist> artists;
    private LayoutInflater artistInflater;
    private Context context;
    private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private ImageView coverView;

    public ArtistAdapter(Context c, ArrayList<Artist> theArtists) {
        artists = theArtists;
        artistInflater = LayoutInflater.from(c);
        context = c;
    }

    @Override
    public int getCount() {
        return artists.size();
    }

    @Override
    public Object getItem(int arg0) {
        return artists.get(arg0).getName();
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int postion, View convertView, ViewGroup parent) {

        LinearLayout albumLay;
        if(convertView == null)
            albumLay = (LinearLayout) artistInflater.inflate(R.layout.artist, parent, false);
        else        //Else recycle view
            albumLay = (LinearLayout)convertView;

        //get title and artist views
        TextView artistTitleView = (TextView) albumLay.findViewById(R.id.artist_title);
        TextView artistTrackNumView = (TextView) albumLay.findViewById(R.id.artist_tracknum);
        coverView = (ImageView) albumLay.findViewById(R.id.artistImage);
        CardView cardView = (CardView) albumLay.findViewById(R.id.artistCard);

        //get song using position
        Artist currArtist = artists.get(postion);

        //get/set title and artist strings
        artistTitleView.setText(currArtist.getName());
        artistTrackNumView.setText(currArtist.getTracks().size() + " Tracks");

        //Try to set Cover
        Bitmap cover = currArtist.getImage();
        if (cover != null)
            coverView.setImageBitmap(cover);
        else {
            //set random cover color
            coverView.setBackgroundColor(parent.getResources().getColor(MainActivity.randomColor()));
        }

        //Accent Color
        int accentColor = currArtist.getAccentColor();
        if (accentColor != Color.WHITE) {
            cardView.setCardBackgroundColor(accentColor);
            artistTitleView.setTextColor(parent.getResources().getColor(R.color.white));
            artistTrackNumView.setTextColor(parent.getResources().getColor(R.color.hint_white));
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
        for (int i = 0; i < artists.size(); i++) {
            String temp = artists.get(i).getName().charAt(0) + "";
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
