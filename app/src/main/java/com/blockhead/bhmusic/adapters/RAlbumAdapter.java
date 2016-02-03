package com.blockhead.bhmusic.adapters;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blockhead.bhmusic.R;
import com.blockhead.bhmusic.objects.Album;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.ArrayList;

/**
 * Created by GusSilva on 1/2/16.
 */
public class RAlbumAdapter extends RecyclerView.Adapter<RAlbumAdapter.ViewHolder> {

    private ArrayList<Album> albums;
    private LayoutInflater albumInf;
    private Context context;
    private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private RecyclerView.OnItemTouchListener onItemTouchListener;

    public RAlbumAdapter(Context c, ArrayList<Album> theAlbums) {
        super();
        imageLoader = ImageLoader.getInstance(); // Get singleton instance

        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.default_cover_xlarge) // resource or drawable
                .showImageOnFail(R.drawable.default_cover_xlarge)
                .resetViewBeforeLoading(true)  // default
                .cacheInMemory(true)
                .displayer(new FadeInBitmapDisplayer(500))
                .build();

        albums = theAlbums;
        albumInf = LayoutInflater.from(c);
        context = c;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.album, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        //get song using position
        Album currAlbum = albums.get(i);

        //get/set title and artist strings
        viewHolder.albumTitleView.setText(currAlbum.getTitle());
        viewHolder.artistView.setText(currAlbum.getArtist());
        viewHolder.cardView.setTag(i);


        imageLoader.displayImage(currAlbum.getCoverURI(), viewHolder.coverView, options);

        //Accent Color
        int accentColor = currAlbum.getAccentColor();
        if (accentColor != Color.WHITE && currAlbum.getCoverURI() != null)
        {
            viewHolder.cardView.setCardBackgroundColor(accentColor);
            viewHolder.coverView.setBackgroundColor(accentColor);
            viewHolder.albumTitleView.setTextColor(Color.WHITE);
            viewHolder.artistView.setTextColor(context.getResources().getColor(R.color.hint_white));
        }
        else
        {
            viewHolder.cardView.setCardBackgroundColor(Color.WHITE);
            viewHolder.coverView.setBackgroundColor(context.getResources().getColor(currAlbum.getRandomColor()));
            viewHolder.albumTitleView.setTextColor(Color.BLACK);
            viewHolder.artistView.setTextColor(context.getResources().getColor(R.color.secondary_text_default_material_light));
        }
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView albumTitleView, artistView;
        public ImageView coverView;
        public CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            //get title and artist views
            albumTitleView = (TextView) itemView.findViewById(R.id.album_title);
            artistView = (TextView) itemView.findViewById(R.id.album_artist);
            coverView = (ImageView) itemView.findViewById(R.id.artImage);
            cardView = (CardView) itemView.findViewById(R.id.albumCard);
        }

    }
}
