package com.example.android.movies;

import android.content.Context;
import android.content.res.Resources;
import android.media.Image;
import android.net.Uri;
import android.os.Debug;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Hems&Hari on 3/5/2016.
 */
public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private DisplayMetrics mMetrics;
    private int halfScreenwidth;
    private int halfScreenHeight;
    private ArrayList<Movie> mMovies;



    public ImageAdapter(Context context) {
        mContext = context;
        mMetrics = mContext.getResources().getDisplayMetrics();

        halfScreenwidth = mMetrics.widthPixels / 2;
        halfScreenHeight = (mMetrics.heightPixels) / 2;

        mMovies = new ArrayList<>();
    }
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

    }
    public void addAll(Collection<Movie> movie) {
        mMovies.addAll(movie);
        notifyDataSetChanged();
    }
    public void clearAll() {
        mMovies.clear();
        notifyDataSetChanged();
    }
    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return mMovies.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Movie getItem(int position) {
       return mMovies.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        Movie movie = mMovies.get(position);

        return movie.id ;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageview;

        //Movie movie = (Movie) getItem(position);
       // if (movie == null) {
      //      return null;
      //  }
        if (convertView == null) {
            imageview = (ImageView)LayoutInflater.from(mContext).inflate(R.layout.movieposter, parent, false);
            imageview.setLayoutParams(new GridView.LayoutParams(halfScreenwidth, halfScreenHeight));
            // if it's not recycled, initialize some attributes
       /*     imageview = new ImageView(mContext);
            imageview.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageview.setPadding(8, 8, 8, 8);
        */
        }
        else {
            imageview = (ImageView) convertView;
        }



        Movie m =mMovies.get(position);

       Uri moviePosterUri = m.buildMoviePosterUri();
        Picasso.with(mContext).load(moviePosterUri).into(imageview);

        return imageview;
    }

}
