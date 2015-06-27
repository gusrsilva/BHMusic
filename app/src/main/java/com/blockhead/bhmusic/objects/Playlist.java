package com.blockhead.bhmusic.objects;

import android.util.Log;

import com.blockhead.bhmusic.activities.MainActivity;

import java.util.ArrayList;

/**
 * Created by GusSilva on 6/25/15.
 */
public class Playlist {
    String title;
    ArrayList<Long> songIds;
    ArrayList<Song> members;

    public Playlist(String name)
    {
        title = name;
        songIds = new ArrayList<>();
    }

    public String getTitle(){ return title; }
    public void setTitle(String name)
    {
        if(name != null)
            title = name;
    }

    public void addSong(Long trackId)
    {
        songIds.add(trackId);
    }

    public ArrayList<Long> getSongIds(){ return songIds; }
    public int getSize(){ return songIds.size(); }

    public int getPosFromId(Long trackID, ArrayList<Song> songList)
    {
        if(songList == null)
            return -1;

        for(int i = 0; i < songList.size(); i++)
        {
            if(songList.get(i).getID() == trackID)
                return i;
        }

        return -1;
    }

    private void generateMembers()
    {
        members = new ArrayList<>();
        ArrayList<Song> songList = MainActivity.songList;
        for(int i = 0; i < songIds.size(); i ++)
            members.add(songList.get(getPosFromId(songIds.get(i), songList)));
    }

    public ArrayList<Song> getMembers()
    {
        if(members == null)
            generateMembers();
        return members;
    }

}
