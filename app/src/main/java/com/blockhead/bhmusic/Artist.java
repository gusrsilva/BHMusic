package com.blockhead.bhmusic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Gus on 2/26/2015.
 */
public class Artist {

    private String name;
    private Bitmap artistImage;
    public ArrayList<Song> tracks = new ArrayList<Song>();
    public ArrayList<Album> albums = new ArrayList<Album>();
    private Bitmap image;
    private String imageURL;

    public Artist(String artistName)
    {
        name = artistName;
    }

    public String getName()
    {
        return name;
    }

    public ArrayList<Song> getTracks(){ return tracks; }

    public ArrayList<Album> getAlbums(){ return albums; }

    public Bitmap getArtistImage(){ return artistImage; }

    public void addAlbum(Album newAlbum)
    {
        try {
            albums.add(newAlbum);
            tracks.addAll(newAlbum.getTracks());
        }
        catch(NullPointerException e)
        {
            Log.d("BHCA","EXCEPTION CAUGHT:" + e.getMessage());
        }
    }

    public void setImage(Bitmap bitmap)
    {
        image = bitmap;
    }

    public Bitmap getImage()
    {
        return image;
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



}
