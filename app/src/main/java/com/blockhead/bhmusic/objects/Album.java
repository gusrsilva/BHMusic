package com.blockhead.bhmusic.objects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.graphics.Palette;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Gus on 2/26/2015.
 */
public class Album {

    public ArrayList<Song> tracks = new ArrayList<Song>();
    private String title, artist;
    private Bitmap cover, smallCover;
    private int accentColor = Color.WHITE;
    private Artist artistObj;

    public Album(String albumTitle, String albumCoverURI, String artistTitle) {
        title = albumTitle;
        artist = artistTitle;
        cover = decodeSampledBitmapFromResource(albumCoverURI, 500, 500);
        smallCover = decodeSampledBitmapFromResource(albumCoverURI, 100, 100);

        if (smallCover != null) {
            Palette.generateAsync(smallCover, new Palette.PaletteAsyncListener() {
                public void onGenerated(Palette palette) {
                    accentColor = palette.getVibrantColor(Color.WHITE);
                    if (accentColor == Color.WHITE)
                        accentColor = palette.getMutedColor(Color.WHITE);
                }
            });
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

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public Bitmap getCover() {
        return cover;
    }

    public ArrayList<Song> getTracks() {
        return tracks;
    }

    public Bitmap getSmallCover() {
        return smallCover;
    }

    public int getAccentColor() {
        return accentColor;
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
}
