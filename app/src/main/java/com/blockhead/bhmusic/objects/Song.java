package com.blockhead.bhmusic.objects;

import com.blockhead.bhmusic.activities.MainActivity;

/**
 * Created by Gus on 2/25/2015.
 */

public class Song {

    private long id;
    private String title, artist, albumTitle, duration, coverURI;
    private Album albumObj;
    private int track, accentColor, randomColor;

    public Song(long songID, String songTitle, String songArtist, String songAlbum, int trackNumber, String songDuration) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        albumTitle = songAlbum;
        track = trackNumber;
        duration = songDuration;
        randomColor = MainActivity.randomColor();
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

    public String getAlbumTitle() {
        return albumTitle;
    }

    public int getTrackNumber() {
        return track;
    }

    public String getDuration() {
        return duration;
    }

    private void setCover() {
        for (int i = 0; i < MainActivity.albumList.size(); i++) {
            if (albumTitle.equalsIgnoreCase(MainActivity.albumList.get(i).getTitle())
                    && artist.equalsIgnoreCase(MainActivity.albumList.get(i).getArtist()))
            {
                albumObj = MainActivity.albumList.get(i);
                coverURI = albumObj.getCoverURI();
                albumObj.addSong(this);
                accentColor = albumObj.getAccentColor();
            }
        }
    }

    public String getCoverURI(){ return coverURI;}

    public Album getAlbumObj() {
        return albumObj;
    }

    public void setAlbumObj(Album a){albumObj = a;}

    public int getAccentColor() {
        return accentColor;
    }

    public int getRandomColor(){ return randomColor; }

}
