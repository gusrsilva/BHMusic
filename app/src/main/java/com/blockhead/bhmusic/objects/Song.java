package com.blockhead.bhmusic.objects;

import android.graphics.Bitmap;

import com.blockhead.bhmusic.activities.MainActivity;

/**
 * Created by Gus on 2/25/2015.
 */

public class Song {

    private long id;
    private String title, artist, album, duration;
    private Album albumObj;
    private int track, accentColor, randomColor;

    private Bitmap cover;
    private Bitmap smallCover;
    private Bitmap croppedCover;

    public Song(long songID, String songTitle, String songArtist, String songAlbum, int trackNumber, String songDuration) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        album = songAlbum;
        track = trackNumber;
        duration = songDuration;
        setCover();
    }

    public long getID() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public int getTrackNumber() {
        return track;
    }

    public String getDuration() {
        return duration;
    }

    private void setCover() {
        for (int i = 0; i < MainActivity.albumList.size(); i++) {
            if (album.equalsIgnoreCase(MainActivity.albumList.get(i).getTitle())) {
                albumObj = MainActivity.albumList.get(i);
                cover = albumObj.getCover();
                smallCover = albumObj.getSmallCover();
                albumObj.addSong(this);
                accentColor = albumObj.getAccentColor();
                if( cover == null )
                    randomColor = MainActivity.randomColor();
            }
        }
    }

    public Bitmap getCover() {
        return cover;
    }

    public Bitmap getSmallCover() {
        return smallCover;
    }

    public Album getAlbumObj() {
        return albumObj;
    }

    public void setAlbumObj(Album a){albumObj = a;}

    public int getAccentColor() {
        return accentColor;
    }

    public int getRandomColor(){ return randomColor; }

}
