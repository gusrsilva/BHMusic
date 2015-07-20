package com.blockhead.bhmusic.objects;

import com.blockhead.bhmusic.activities.MainActivity;

/**
 * Created by Gus on 2/25/2015.
 */

public class Song {

    private long id;
    private String title, artist, albumTitle, duration, coverURI, path, extension;
    private Album albumObj;
    private int track, accentColor, randomColor, size;

    public Song(long songID, String songTitle, String songArtist, String songAlbum, int trackNumber
            , String songDuration, String songPath, int songSize, String songExtenstion) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        albumTitle = songAlbum;
        track = trackNumber;
        duration = songDuration;
        path = songPath;
        size = songSize;

        if(songExtenstion != null)
            extension = songExtenstion.toUpperCase();

        randomColor = MainActivity.randomColor();
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
