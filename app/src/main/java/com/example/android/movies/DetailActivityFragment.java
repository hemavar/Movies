package com.example.android.movies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.movies.data.MovieContract;
import com.example.android.movies.service.MovieDetailsDownloadService;
import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    static final String DETAIL_URI = "URI";

    long mMovieId;
    Uri mMovieUri;
    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.POSTER_PATH,
            MovieContract.MovieEntry.ADULT,
            MovieContract.MovieEntry.OVERVIEW,
            MovieContract.MovieEntry.RELEASE_DATE,
            MovieContract.MovieEntry.MOVIE_ID,
            MovieContract.MovieEntry.TITLE,
            MovieContract.MovieEntry.POPULARITY,
            MovieContract.MovieEntry.VOTE_COUNT,
            MovieContract.MovieEntry.VIDEO,
            MovieContract.MovieEntry.VOTE_AVERAGE,
            MovieContract.MovieEntry.RUNTIME,
            MovieContract.MovieEntry.TRAILER1,
            MovieContract.MovieEntry.FAVORITE,
            MovieContract.MovieEntry.CATEGORY
    };
    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_POSTERPATH= 1;
    static final int COL_MOVIE_ADULT = 2;
    static final int COL_MOVIE_OVERVIEW = 3;
    static final int COL_MOVIE_RELEASEDATE = 4;
    static final int COL_MOVIE_MOVIEID= 5;
    static final int COL_MOVIE_TITLE = 6;
    static final int COL_MOVIE_POPULARITY= 7;
    static final int COL_MOVIE_VOTECOUNT= 8;
    static final int COL_MOVIE_VIDEO= 9;
    static final int COL_MOVIE_VOTEAVERAGE =10;
    static final int COL_MOVIE_RUNTIME=11;
    static final int COL_MOVIE_TRAILER1=12;
    static final int COL_MOVIE_FAVORITE=13;

    //For easy access
    private TextView movieTitle;
    private ImageView posterImage;
    private TextView releaseDate;
    private TextView voteAverage;
    private TextView synopsys;
    private TextView runTime;
    private  Button favBbutton;
    private String fav;
    View rootview;

    private static final String MOVIE_SHARE_HASHTAG = " #MyMoviesApp";
    ShareActionProvider mShareActionProvider;
    private String mShareMovie;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

       Bundle arguments = getArguments();
        if (arguments != null) {
            mMovieUri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);

            mMovieId = MovieContract.MovieEntry.getMovieIdFromUri(mMovieUri);
            Log.d("3 DetailActFragment","movieUri : " + mMovieUri + "movieId " + mMovieId);
        }

        rootview = inflater.inflate(R.layout.fragment_detail, container, false);

        movieTitle =(TextView)rootview.findViewById(R.id.movie_title);
        posterImage = (ImageView)rootview.findViewById(R.id.movie_poster);
        releaseDate = (TextView)rootview.findViewById(R.id.release_date);
        voteAverage =(TextView)rootview.findViewById(R.id.vote_average);
        synopsys = (TextView)rootview.findViewById(R.id.movie_synopsys);
        runTime = (TextView)rootview.findViewById(R.id.runtime);


       /* Intent intent = getActivity().getIntent();
        if(intent == null){
            return null;
        }
        mMovieUri = intent.getData();
        if(mMovieUri == null){
            return null;
        }
        */

       loadMovieId();

        /*
        //play trailer
        ImageButton trailerButton = (ImageButton) rootview.findViewById(R.id.video_button);
        if (trailerButton != null) {
            trailerButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Do something in response to button click
                    Toast.makeText(getActivity(), "Clicked!!!", Toast.LENGTH_SHORT).show();

                    Log.d("DetailActFrag", "Clicked trailer button");
                    //Start intent for playing trailer
                    Intent playback = new Intent();
                    Uri uri = Uri.parse("http://www.youtube.com");
                    playback.setData(uri);
                    if (playback.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(playback);
                    }
                }
            });
        }
*/

        favBbutton = (Button)rootview.findViewById(R.id.favorite);
        favBbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Onclick","Onclick");

                String text = (favBbutton.getText()).toString();
                Log.e("Onclick", "text " + text);

                ContentValues movieData = new ContentValues();

                if(text.equals("- MY LIST")){
                    movieData.put(MovieContract.MovieEntry.FAVORITE, "false");
                    favBbutton.setText("+ MY LIST");
                }
                else if(text.equals("+ MY LIST")) {
                    movieData.put(MovieContract.MovieEntry.FAVORITE, "true");
                    favBbutton.setText("- MY LIST");
                }
                String selection= MovieContract.MovieEntry.MOVIE_ID + " = ? ";
                String[] selectionArgs = {Long.toString(mMovieId)};

                // add to database
                int UpdatedRowId = getContext().getContentResolver().update(MovieContract.MovieEntry.CONTENT_URI,
                        movieData,
                        selection,
                        selectionArgs);

                Log.d("DetailFrag", "Added to favorites " + UpdatedRowId + " UpdatedRowId");

            }
        });
        return rootview;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_detail, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mShareMovie != null) {

            mShareActionProvider.setShareIntent(createShareMovieIntent());
        } else {
            Log.d("DetailActivityFragment", "Share Action Provider is null?");
        }
    }


    private void loadMovieId() {
        Intent intent = new Intent(getActivity(),MovieDetailsDownloadService.class);
        intent.putExtra(MovieDetailsDownloadService.MOVIEID_QUERY_EXTRA,Long.toString(mMovieId));
        getActivity().startService(intent);
    }



    private Intent createShareMovieIntent()
    {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");

        shareIntent.putExtra(Intent.EXTRA_TEXT, mShareMovie + MOVIE_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mMovieUri != null) {

            return new CursorLoader(getActivity(),
                    mMovieUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(data!= null && data.moveToFirst()) {
            String title = data.getString(COL_MOVIE_TITLE);
            movieTitle.setText(title);

            double voteAvg = data.getDouble(COL_MOVIE_VOTEAVERAGE);
          //  Log.d("DetailActFrag","voteAvg " + voteAvg);
            //The output format is vote_average/10
            String average = voteAvg +"/10" ;
            voteAverage.setText(average);

            String relDate = data.getString(COL_MOVIE_RELEASEDATE);
            //For displaying only the year from the date (YYYY-MM-DD)
           // String[] date = relDate.split("-");
            releaseDate.setText(relDate);

            Double popularity  = data.getDouble(COL_MOVIE_POPULARITY);
           // Log.d("DetailActFrag","popularity: " + popularity);

            Integer runtime = data.getInt(COL_MOVIE_RUNTIME);
           // Log.d("DetailActFrag","Runtime: " + runtime);
            runTime.setText(String.valueOf(runtime) + "min");
            String poster= data.getString(COL_MOVIE_POSTERPATH);
            Uri builtUri = Utility.buildMoviePosterUri(poster);
            Picasso.with(getContext()).load(builtUri).into((ImageView)posterImage);

            String overview = data.getString(COL_MOVIE_OVERVIEW);
            synopsys.setText(overview);


             fav = data.getString(COL_MOVIE_FAVORITE);
            Log.d("DetailActFrag", "fav: " + fav);
            if(fav.equals("true"))
                favBbutton.setText("- MY LIST");
            else if(fav.equals("false"))
                favBbutton.setText("+ MY LIST");



            // We still need this for the share intent
            mShareMovie = String.format("Lets watch this.. \nMovie : %s \n\nSynopsys: %s \n",title, overview);
            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareMovieIntent());
            }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }



}
