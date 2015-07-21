package com.blockhead.bhmusic.objects;

import android.widget.Toast;

import com.blockhead.bhmusic.activities.MainActivity;

import java.util.ArrayList;

/**
 * Created by GusSilva on 6/25/15.
 */
public class Playlist {
    String title;
    long playlistId;
    ArrayList<Long> songIds;
    ArrayList<Song> members;
    boolean changed;

    public Playlist(String name, long Id)
    {
        title = name;
        playlistId = Id;
        songIds = new ArrayList<>();
        members = new ArrayList<>();
        changed = false;
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

    public void addSong(Song song)
    {
        if(song != null)
        {
            if(members == null || members.size() == 0)
            {
                songIds.add(song.getID());
                generateMembers();
            }
            else
            {
                songIds.add(song.getID());
                members.add(song);
            }
        }
        changed = true;

    }

    public ArrayList<Long> getSongIds(){ return songIds; }
    public int getSize(){ return songIds.size(); }

    public int getPosFromId(Long trackID, ArrayList<Song> songList)
    {
        if(songList == null)
            return -1;

        int i = 0;
        for(Song temp : songList)
        {
            if(temp.getID() == trackID)
                return i;
            i++;
        }

        return -1;
    }

    private void generateMembers()
    {
        members = new ArrayList<>();
        ArrayList<Song> songList = MainActivity.songList;
        for(Long temp : songIds)
            members.add(songList.get(getPosFromId(temp, songList)));
    }

    public ArrayList<Song> getMembers()
    {
        if(members == null || members.size() == 0)
            generateMembers();
        return members;
    }

    public boolean isChanged(){ return changed; }

    public void setChanged()
    {
        if(!changed)
            changed = true;
    }

    public long getPlaylistId(){ return playlistId; }

}
