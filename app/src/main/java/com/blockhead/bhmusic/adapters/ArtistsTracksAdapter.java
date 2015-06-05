package com.blockhead.bhmusic.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blockhead.bhmusic.R;
import com.blockhead.bhmusic.objects.Album;
import com.blockhead.bhmusic.objects.Artist;
import com.blockhead.bhmusic.objects.Song;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Gus on 2/25/2015.
 */
public class ArtistsTracksAdapter extends BaseExpandableListAdapter {

    private final ArrayList<Album> albumList;
    private final LayoutInflater inflater;

    public ArtistsTracksAdapter(Context context, ArrayList<Album> itemList) {
        this.inflater = LayoutInflater.from(context);
        this.albumList = itemList;
    }

    @Override
    public Song getChild(int groupPosition, int childPosition) {

        return albumList.get(groupPosition).getTracks().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return albumList.get(groupPosition).getTracks().size();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             final ViewGroup parent) {
        View resultView = convertView;
        Song song = getChild(groupPosition, childPosition);
        Artist mArtist = albumList.get(groupPosition).getArtistObj();

        try {
            if (groupPosition == 0) {
                resultView = inflater.inflate(R.layout.view_artist_header_child, null);

                TextView info = (TextView) resultView.findViewById(R.id.view_artist_header_child_text);
                LinearLayout lin = (LinearLayout) resultView.findViewById(R.id.view_artist_header_child_lin);

                if (mArtist != null) {
                    Log.d("BHCA1", "Artist: " + mArtist.getName());
                    if(mArtist.getSummaryHTML() != null)
                        info.setText(Html.fromHtml(mArtist.getSummaryHTML()));
                    else{
                        lin.removeAllViews();
                    }
                    lin.setBackgroundColor(mArtist.getAccentColor());
                }
            } else {

                resultView = inflater.inflate(R.layout.artist_tracks_child, null);

                LinearLayout trackLay = (LinearLayout)resultView.findViewById(R.id.artist_track_lay);
                TextView title = (TextView) resultView.findViewById(R.id.artist_songTitle);
                TextView dur = (TextView) resultView.findViewById(R.id.artist_songDur);

                title.setText(song.getTitle());
                dur.setText(song.getDuration());

                trackLay.setTag(song.getTitle());
            }
        } catch(Exception e)
        {
            Log.d("BHCA1", e.getMessage());
        }

        return resultView;
    }

    @Override
    public Album getGroup(int groupPosition) {
        return albumList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return albumList.size();
    }

    @Override
    public long getGroupId(final int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View theConvertView, ViewGroup parent) {
        View resultView = theConvertView;
        Album album = getGroup(groupPosition);

        try {
            if (groupPosition == 0) {
                Artist artistObj = album.getArtistObj();
                resultView = inflater.inflate(R.layout.view_artist_header, null);
                resultView.setMinimumHeight(245);
                TextView title = (TextView) resultView.findViewById(R.id.view_artist_header_title);
                LinearLayout linLay = (LinearLayout) resultView.findViewById(R.id.view_artist_header_lin);

                title.setText(album.getArtist());
                if (artistObj != null) {
                    if(artistObj.getAccentColor() != Color.WHITE)
                        linLay.setBackgroundColor(artistObj.getAccentColor());
                }

            } else {
                resultView = inflater.inflate(R.layout.artist_tracks_parent, null);

                ImageView cover = (ImageView) resultView.findViewById(R.id.artist_albumImage);
                TextView title = (TextView) resultView.findViewById(R.id.artist_albumTitle);
                TextView info = (TextView) resultView.findViewById(R.id.artist_albumInfo);

                if (album.getCoverURI() != null && cover!=null)
                    Picasso.with(parent.getContext()).load(album.getCoverURI()).fit().centerCrop().into(cover);
                if(title != null)
                    title.setText(album.getTitle());
                if(info != null)
                    info.setText(album.getTracks().size() + " songs");
            }
        }
        catch(Exception e)
        {
            Log.d("BHCA1", "Cause: " + e.getCause());
            Log.d("BHCA1", "Caught: " + e.getMessage());
            Log.d("BHCA1", e.getLocalizedMessage());
        }

        return resultView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private static final class ViewHolder {
        TextView textLabel;
    }

}