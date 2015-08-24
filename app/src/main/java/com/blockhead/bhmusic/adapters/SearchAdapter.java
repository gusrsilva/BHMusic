package com.blockhead.bhmusic.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.blockhead.bhmusic.R;
import com.blockhead.bhmusic.activities.MainActivity;
import com.blockhead.bhmusic.objects.Album;
import com.blockhead.bhmusic.objects.Artist;
import com.blockhead.bhmusic.objects.Song;
import com.nhaarman.listviewanimations.appearance.StickyListHeadersAdapterDecorator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by Gus on 2/25/2015.
 */
public class SearchAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private ArrayList<Song> songs;
    private ArrayList<Album> albums;
    private ArrayList<Artist> artists;
    private LayoutInflater songInf;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private Context context;
    private final int IS_SONG = 2, IS_ALBUM = 1, IS_ARTIST = 0;
    private final String DEBUG_TAG = "BHCA_SEARCH";


    public SearchAdapter(Context c, ArrayList<Song> theSongs, ArrayList<Album> theAlbums,
                         ArrayList<Artist> theArtist)
    {
        songs = theSongs;
        albums = theAlbums;
        artists = theArtist;
        songInf = LayoutInflater.from(c);
        imageLoader = ImageLoader.getInstance(); // Get singleton instance
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.default_cover) // resource or drawable
                .showImageOnFail(R.drawable.default_cover)
                .cacheOnDisk(true)
                .build();
        context = c;
    }

    @Override
    public int getCount()
    {
        return (songs == null ? 0 : songs.size())
                + (albums == null ? 0 : albums.size()
                + (artists == null ? 0 : artists.size()));
    }

    @Override
    public Object getItem(int arg0)
    {
        int type = getType(arg0);
        if(type == IS_ARTIST)
        {
            return artists.get(arg0);
        }
        else if(type == IS_ALBUM)
        {
            int pos = arg0 - artists.size();
            return albums.get(pos);
        }
        else if(type == IS_SONG)
        {
            int pos = arg0 - artists.size() - albums.size();
            return songs.get(pos);
        }
        else
            return null;
    }

    @Override
    public long getItemId(int arg0)
    {
        int type = getType(arg0);
        if(type == IS_ARTIST)
        {
            return artists.get(arg0).hashCode();
        }
        else if(type == IS_ALBUM)
        {
            int pos = arg0 - artists.size();
            return albums.get(pos).hashCode();
        }
        else if(type == IS_SONG)
        {
            int pos = arg0 - artists.size() - albums.size();
            return songs.get(pos).getID();
        }
        else
            return 0;
    }

    @Override
    public View getView(final int postion, View convertView, ViewGroup parent) {

        LinearLayout songLay;
        if(convertView == null)
            songLay = (LinearLayout) songInf.inflate(R.layout.song, parent, false);
        else        //Else recycle view
            songLay = (LinearLayout)convertView;

        //get title and artist views
        TextView title = (TextView) songLay.findViewById(R.id.song_title);
        TextView subTitle = (TextView) songLay.findViewById(R.id.song_artist);
        ImageView image = (ImageView) songLay.findViewById(R.id.artImage);

        Object obj = getItem(postion);

        if(obj instanceof Song)
        {
            Song currSong = (Song)obj;
            title.setText(currSong.getTitle());
            subTitle.setText(currSong.getArtist());
            imageLoader.displayImage(currSong.getCoverURI(), image, options);
            int color = parent.getResources().getColor(currSong.getRandomColor());
            image.setBackgroundColor(color);
            return songLay;
        }
        else if(obj instanceof Album)
        {
            Album currAlbum = (Album)obj;
            title.setText(currAlbum.getTitle());
            subTitle.setText(currAlbum.getArtist());
            imageLoader.displayImage(currAlbum.getCoverURI(), image, options);
            int color = parent.getResources().getColor(currAlbum.getRandomColor());
            image.setBackgroundColor(color);
            return songLay;
        }
        else if(obj instanceof Artist)
        {
            Artist currArtist = (Artist)obj;
            title.setText(currArtist.getName());
            int numTracks = currArtist.getTracks().size();
            subTitle.setText(numTracks + (numTracks == 1 ? " song" : " songs"));
            imageLoader.displayImage(currArtist.getImagePath(), image, options);
            int color = parent.getResources().getColor(currArtist.getRandomColor());
            image.setBackgroundColor(color);
            return songLay;
        }
        else
        {
            return convertView;
        }

    }

    @Override
    public View getHeaderView(int i, View view, ViewGroup viewGroup) {
        View v = LayoutInflater.from(context).inflate(R.layout.search_header, null);
        LinearLayout holder = (LinearLayout)v.findViewById(R.id.search_header_bg);
        holder.setBackgroundColor(MainActivity.primaryColor);

        int type = getType(i);
        switch(type)
        {
            case IS_SONG:
                Log.d(DEBUG_TAG, "HEADER: SONG");
                ((TextView) v.findViewById(R.id.search_header_text)).setText(" S O N G S ( "
                    + songs.size() + " )");
                break;
            case IS_ALBUM:
                Log.d(DEBUG_TAG, "HEADER: ALBUM");
                ((TextView)v.findViewById(R.id.search_header_text)).setText(" A L B U M S ( "
                    + albums.size() + " )");
                break;
            case IS_ARTIST:
                Log.d(DEBUG_TAG, "HEADER: ARTIST");
                ((TextView)v.findViewById(R.id.search_header_text)).setText(" A R T I S T S ( "
                    + artists.size() + " )");
                break;
            default:
                break;
        }

        return v;
    }

    @Override
    public long getHeaderId(int i) {
        return getType(i);
    }

    private int getType(int i)
    {
        int pos = i;
        if(pos < artists.size())
            return IS_ARTIST;
        else
            pos -= artists.size();

        if(pos < albums.size())
            return IS_ALBUM;
        else
            return IS_SONG;
    }
}
