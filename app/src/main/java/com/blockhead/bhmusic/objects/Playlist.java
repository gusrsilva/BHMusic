package com.blockhead.bhmusic.objects;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by GusSilva on 6/25/15.
 */
public class Playlist {
    String title;
    ArrayList<Song> songList;
    ArrayList<Integer> songPositions;

    public Playlist(String name, ArrayList<Song> tracks)
    {
        title = name;
        songPositions = new ArrayList<>();
        songList = tracks;
    }

    public String getTitle(){ return title; }
    public void setTitle(String name)
    {
        if(name != null)
            title = name;
    }

    public boolean addSong(Long trackId)
    {
        for(int i=0; i < songList.size(); i++)
        {
            if(trackId == songList.get(i).getID())
            {
                songPositions.add(i);
                Log.d("BHCA", "Added Song with ID: " + trackId + " to " + title);
                return true;
            }
        }
        return false;
    }

    public ArrayList<Integer> getSongPositions(){ return songPositions; }
    public int getSize(){ return songPositions.size(); }
}
