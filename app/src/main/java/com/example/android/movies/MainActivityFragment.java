package com.example.android.movies;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.example.android.movies.data.MovieContract;
import com.example.android.movies.service.MovieDownloadService;


public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final int MAXPAGES = 1000; //Specified by TDMB website
    int mPagesLoaded; //Stores the number of pages loaded

    GridView mGridView; //Gridview handle
  //  private static final String SELECTED_POSITION_KEY= "selected_position";
    //private int mPosition = GridView.INVALID_POSITION;

    private static final int MOVIE_LOADER = 0; //Unique loader name
    private ImageAdapter mImageAdapter;//Cursorloader handle


    private boolean favoriteFlag = false; //Flag for showing the favorite movies

    //MOVIE_COLUMNS order should maintain the database order
    private static final String[] MOVIE_COLUMNS = {
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
            MovieContract.MovieEntry.VOTE_AVERAGE
    };

    // These indices are tied to MOVIE_COLUMNS.  If MOVIE_COLUMNS changes, these must change.
    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_POSTERPATH= 1;
    static final int COL_MOVIE_ADULT = 2;
    static final int COL_MOVIE_OVERVIEW = 3;
    static final int COL_MOVIE_RELEASEDATE = 4;
    static final int COL_MOVIE_MOVIEID= 5;
    static final int COL_MOVIE_TITLE = 6;
    static final int COL_MOVIE_POPULARITY= 7;
    static final int COL_MOVIE_VOTECOUNT= 8;
    static final int COL_MOVIE_VIDEO = 9;
    static final int COL_MOVIE_VOTEAVERAGE =10;



    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri movieUri);
   }


    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedinstanceState) {
        super.onCreate(savedinstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mImageAdapter = new ImageAdapter(getActivity(),null,0);

        /* Find GridView id and attach MyAdapter-cursorAdapter to the grid for populating images dynamically */
        mGridView = (GridView) rootView.findViewById(R.id.gridview);
        if (mGridView == null) {
            return null;
        }

        mGridView.setAdapter(mImageAdapter);

        // Set the Required Animation to GridView and start the Animation
        // use fly_in_from_center to have 2nd type of animation effect (snapshot 2)
       // final Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_in_from_top_corner);
      //  mGridView.setAnimation(anim);



        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView adapterView, View v,
                                    int position, long id) {

                //Start DetailActivity of  the selected movie poster
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor == null) {
                    return;
                }

           //     mPosition = position;
                /*Open DetailActivity of the selected item (movie poster)
                    Make the movie Object to bundle for sending via intent */

                mGridView.setItemChecked(position, true);
                long movieId = cursor.getLong(MainActivityFragment.COL_MOVIE_MOVIEID);
                Uri movieUri = MovieContract.MovieEntry.buildMovieWithMovieId(movieId);
                Log.d("1 MainActivityFragment ","movieUri : "+movieUri);
                ((Callback)getActivity()).onItemSelected(movieUri);

                //Intent detailedActivity = new Intent(getActivity(), DetailActivity.class);
               // detailedActivity.setData(movieUri);
               // getActivity().startActivity(detailedActivity);
            }
        });

        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                int lastInScreen = firstVisibleItem + visibleItemCount;
                if (lastInScreen == totalItemCount) {
                    //anim.start();
                    loadMoviePosters();
                }
            }
        });


        //The user never knows that turning their device sideways does crazy lifecycle related things
        //App should never lose data on changing orientation

    /*    if(savedInstanceState != null && savedInstanceState.containsKey(SELECTED_POSITION_KEY)){
            // The gridview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_POSITION_KEY);
        }
*/
        return rootView;
    }
    /*
    @Override
    public void onSaveInstanceState(Bundle outState){
        // When tablets rotate, the currently selected grid item needs to be saved.
        // When no item is selected, mPosition will be set to Gridview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_POSITION_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }*/
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mainactivity_fragment, menu);


    }

    private void editSortPref(MenuItem item, String sortOrder) {
        //Read from SharedPreference
        String storedPreference =getSortPref();
        if(storedPreference!=null && !storedPreference.equals(sortOrder)) {
            //Write to SharedPreference
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(getString(R.string.pref_sortby_key), sortOrder).commit();
        }
    }

    public String getSortPref(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String storedPreference = preferences.getString(getString(R.string.pref_sortby_key),
                getString(R.string.pref_sort_highestrated));
        return storedPreference;
    }

    private void loadMoviePosters() {

        if(!favoriteFlag) {
            if (mPagesLoaded >= MAXPAGES)
                return;
            mPagesLoaded++;

            Log.d("MainActFrag", "loadMoviePosters(): sending intent for page: " + mPagesLoaded);
            Intent sendIntent = new Intent(getActivity(), MovieDownloadService.class);
            sendIntent.putExtra(MovieDownloadService.PAGE_QUERY_EXTRA, mPagesLoaded);
            getActivity().startService(sendIntent);
        }
        /*
        Intent alarmIntent = new Intent(getActivity(),MovieDownloadService.AlarmReceiver.class);
        alarmIntent.putExtra(MovieDownloadService.PAGE_QUERY_EXTRA,mPagesLoaded);

        //Wrap in a pending intent which only fires once.
        PendingIntent pi = PendingIntent.getBroadcast(getActivity(),0,alarmIntent,PendingIntent.FLAG_ONE_SHOT);
        AlarmManager am = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);

        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pi);
        */
    }

    void onSortOrderChanged(){
        mPagesLoaded=0;
        loadMoviePosters();
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        if (id == R.id.action_rating ) {
            favoriteFlag=false;
            editSortPref(item, "top_rated");
            onSortOrderChanged();

            if (item.isChecked()) item.setChecked(false);
            else item.setChecked(true);

            return true;

        }
        else if (id == R.id.action_relevance) {
            favoriteFlag=false;
            Log.d("Entered", " R.id.action_popular ");
            editSortPref(item, "popular");
            onSortOrderChanged();

            if (item.isChecked()) item.setChecked(false);
            else item.setChecked(true);

            return true;
        }
        else if(id == R.id.action_favorites){
            if (item.isChecked()) item.setChecked(false);
            else item.setChecked(true);

            //Query from database to select movies marked as favorites
            favoriteFlag = true;
            getLoaderManager().restartLoader(MOVIE_LOADER, null, this);;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d("Inside onCreateLoader", "called");

        if(favoriteFlag){

            Log.d("Favorite URI","URI : " + MovieContract.MovieEntry.FAVORITE_URI);
            return new CursorLoader(getActivity(),
                                     MovieContract.MovieEntry.FAVORITE_URI,
                                            MOVIE_COLUMNS,
                                            null,
                                            null,null);

        }
        String selection = MovieContract.MovieEntry.CATEGORY + " = ?   " ;
        String sort = getSortPref();
        String selectionArgs[] = {sort};
        Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;

        return new CursorLoader(getActivity(),
                movieUri,
                MOVIE_COLUMNS,
                selection,
                selectionArgs,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d("Inside onLoadFinished", "called");

        mImageAdapter.swapCursor(data);

    /*    if (mPosition != GridView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mGridView.smoothScrollToPosition(mPosition);
        }*/
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mImageAdapter.swapCursor(null);
    }

}
