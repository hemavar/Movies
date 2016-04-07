package com.example.android.movies;

import android.content.Context;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by Hems&Hari on 3/18/2016.
 */
public class ImageAdapter extends CursorAdapter {
    private DisplayMetrics mMetrics;
    private int halfScreenwidth;
    private int halfScreenHeight;

    public ImageAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mMetrics = context.getResources().getDisplayMetrics();

        halfScreenwidth = mMetrics.widthPixels / 2;
        halfScreenHeight = (mMetrics.heightPixels) / 2;
    }

    /**
     * Makes a new view to hold the data pointed to by cursor.
     *
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //Choose the layout type
    //    Log.d("MyAdapter","Inside newView ");
        //return LayoutInflater.from(context).inflate(R.layout.movieposter,parent,false);

        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new GridView.LayoutParams(halfScreenwidth,halfScreenHeight));

        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        Log.d("ImageAdapter","Inside newView : width :" + halfScreenwidth + "Height : "+ halfScreenHeight);
        return imageView;

    }

    /**
     * Bind an existing view to the data pointed to by cursor
     *
     * @param view    Existing view, returned earlier by newView
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

       ImageView imageview=(ImageView) view;;
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        String posterPath = cursor.getString(MainActivityFragment.COL_MOVIE_POSTERPATH);
       // Log.d("MyAdapter","Using old view inside bindView  " + posterPath);


        Uri posterUri =Utility.buildMoviePosterUri(posterPath);
        Picasso.with(context).load(posterUri).into(imageview);

    }

}
