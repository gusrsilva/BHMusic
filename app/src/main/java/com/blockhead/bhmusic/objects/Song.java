package com.blockhead.bhmusic.objects;

import android.util.Log;

import com.blockhead.bhmusic.activities.MainActivity;

/**
 * Created by Gus on 2/25/2015.
 */

public class Song {

    private long id, albumId;
    private String title, artist, albumTitle, duration, coverURI, path, extension;
    private Album albumObj;
    private int track, accentColor, randomColor = 0, size;

    public Song(long songID, String songTitle, String songArtist, int trackNumber
            , String songDuration, String songPath, int songSize, String songExtenstion, long albId) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        track = trackNumber;
        duration = songDuration;
        path = songPath;
        size = songSize;
        albumId = albId;

        if(songExtenstion != null)
            extension = songExtenstion.toUpperCase();

        setCover();
    }

    public Song(String songArtist)
    {
        artist = songArtist;
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

    public String getPath(){ return path; }

    public String getExtension(){ return extension; }

    public int getSizeRaw(){ return size; }

    public String getSizeFormatted()
    {
        return humanReadableByteCount(size, true);
    }
    /**
     *  Function to Display Human Readable File Size
     *  Written By: "aioobe" on StackOverFlow
     */
    public String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private void setCover() {

        Album tempAlbum = MainActivity.albumHashMap.get(albumId);
        if(tempAlbum != null) {
            albumObj = tempAlbum;
            albumTitle = albumObj.getTitle();
            coverURI = albumObj.getCoverURI();
            albumObj.addSong(this);
            accentColor = albumObj.getAccentColor();
        }
        else
            Log.d("BHCA-OPTIMIZATION", "TEMP ALBUM NULL: " + albumTitle + " ID: " + albumId);
    }

    public String getCoverURI(){ return coverURI;}

    public Album getAlbumObj() {
        return albumObj;
    }

    public void setAlbumObj(Album a){albumObj = a;}

    public int getAccentColor() {
        return accentColor;
    }

    public int getRandomColor()
    {
        if(randomColor == 0)
            randomColor = MainActivity.randomColor();

        return randomColor;
    }

    @Override
    public int hashCode() {
        return (int)id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        Song other = (Song) obj;
        return (other.getID() == this.getID());
    }

}
