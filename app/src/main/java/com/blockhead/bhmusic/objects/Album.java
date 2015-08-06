package com.blockhead.bhmusic.objects;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.graphics.Palette;
import android.util.Log;

import com.blockhead.bhmusic.activities.MainActivity;

import java.util.ArrayList;

/**
 * Created by Gus on 2/26/2015.
 */
public class Album {

    public ArrayList<Song> tracks = new ArrayList<Song>();
    private String title, artist, URI;
    private int accentColor = Color.WHITE, randomColor = 0;
    private long id;
    private Artist artistObj;
    private static SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.mContext);
    private static SharedPreferences.Editor mEditor = sharedPref.edit();

    public Album(String albumTitle, String albumCoverURI, String artistTitle, long thisId) {
        title = albumTitle;
        artist = artistTitle;
        URI = albumCoverURI;
        id = thisId;

        if(URI != null && !URI.isEmpty())
        {
            if(sharedPref.contains(albumCoverURI))
                accentColor = sharedPref.getInt(URI, Color.WHITE);
            else
            {
                int size = (MainActivity.improveColorSampling? 200 : 10);
                Bitmap smallCover = decodeSampledBitmapFromResource(URI, size, size);
                if(smallCover != null)
                {
                    Palette palette = Palette.from(smallCover).generate();
                    accentColor = palette.getVibrantColor(Color.WHITE);
                    if (accentColor == Color.WHITE)
                        accentColor = palette.getMutedColor(Color.WHITE);

                    mEditor.putInt(URI, accentColor);
                    mEditor.apply();
                }
            }
        }


    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(String pathName,
                                                         int reqWidth, int reqHeight) {

        try
        {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(pathName, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(pathName, options);
        }
        catch( Exception e )
        {
            Log.d("BHCA", "Error caught in decodeSampledBitmapFromResource");
            return null;
        }
    }

    public String getTitle() {
        return title;
    }

    public long getId(){ return id; }

    public String getArtist() {
        return artist;
    }

    public String getCoverURI()
    {   if(URI == null)
            return null;
        else if(URI.isEmpty())
            return null;
        else
            return "file:///" + URI;
    }


    public ArrayList<Song> getTracks() {
        return tracks;
    }
    public int getAccentColor() {
        return accentColor;
    }
    public int getRandomColor()
    {
        if(randomColor == 0)
            randomColor = MainActivity.randomColor();
        return randomColor;
    }

    public void addSong(Song newSong) {
        try {
            tracks.add(newSong);
        } catch (NullPointerException e) {
            Log.d("BHCA", "EXCEPTION CAUGHT:" + e.getMessage());
        }
    }

    public void setArtistObj(Artist aObj)
    {
        artistObj = aObj;
    }
    public Artist getArtistObj()
    {
        return artistObj;
    }

    @Override
    public int hashCode() {
        return (int)id;
    }

    @Override
    public boolean equals(Object object) {
        if(object == null || !object.getClass().equals(this.getClass()))
        {
            Log.d("DEBUG-BHCA", "Paramater type mismatch!");
            return false;
        }

        Album another = (Album) object;
        return(another.getId() == id);
    }

    public int getSize()
    {
        if(tracks == null)
            return 0;
        else
            return tracks.size();
    }
}
