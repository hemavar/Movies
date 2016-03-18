package com.example.android.movies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    ArrayList<Movie> movies;
    ImageAdapter mImageAdapter;
    static final int MAXPAGES = 1000;
    int mPagesLoaded;

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


        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        /* Find GridView id and attach the custom ImageAdapter to the grid for populating images dynamically */
        GridView gridview = (GridView) rootView.findViewById(R.id.gridview);
        if (gridview == null) {
            return null;
        }

        mImageAdapter = new ImageAdapter(getActivity());
        gridview.setAdapter(mImageAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                //Start DetailActivity of  the selected movie poster
                ImageAdapter imageadapter = (ImageAdapter) parent.getAdapter();
                Movie movie = imageadapter.getItem(position);
                if (movie == null) {
                    return;
                }
                /*Open DetailActivity of the selected item (movie poster)
                    Make the movie Object to bundle for sending via intent */
                Bundle movieBundle = movie.makeMovieBundle(movie);
                Intent detailedActivity = new Intent(getActivity(), DetailActivity.class);
                detailedActivity.putExtra(Movie.INTENT_MOVIE, movieBundle);
                getActivity().startActivity(detailedActivity);
            }
        });


        gridview.setOnScrollListener(new AbsListView.OnScrollListener() {
            /**
             * Callback method to be invoked while the list view or grid view is being scrolled. If the
             * view is being scrolled, this method will be called before the next frame of the scroll is
             * rendered. In particular, it will be called before any calls to
             * {@link //Adapter getView(int, View, ViewGroup)}.
             *
             * @param view        The view whose scroll state is being reported
             * @param scrollState The current scroll state. One of
             *                    {@link #SCROLL_STATE_TOUCH_SCROLL} or {@link #SCROLL_STATE_IDLE}.
             */
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            /**
             * Callback method to be invoked when the list or grid has been scrolled. This will be
             * called after the scroll has completed
             *
             * @param view             The view whose scroll state is being reported
             * @param firstVisibleItem the index of the first visible cell (ignore if
             *                         visibleItemCount == 0)
             * @param visibleItemCount the number of visible cells
             * @param totalItemCount   the number of items in the list adaptor
             */
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastInScreen = firstVisibleItem + visibleItemCount;
                if (lastInScreen == totalItemCount) {
                    loadMoviePosters();
                }
            }
        });


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mainactivity_fragment, menu);
    }

    private void editSortPref(MenuItem item, String sortOrder) {
        /*
        Context context = getActivity();
        SharedPreferences preferences = context.getSharedPreferences(getString(R.string.pref_sortby_key),
                                        Context.MODE_PRIVATE);
        */
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //Read from SharedPreference
        String storedPreference = preferences.getString(getString(R.string.pref_sortby_key),
                getString(R.string.pref_sort_highestrated));
        Log.d("Before changing", "sort order " + storedPreference);


        //Write to SharedPreference
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.pref_sortby_key), sortOrder).commit();

        storedPreference = preferences.getString(getString(R.string.pref_sortby_key), "top_rated");
        Log.d("After changing", "sort order " + storedPreference);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        int id = item.getItemId();

        if (id == R.id.action_rating) {
            Log.d("Entered", " R.id.action_rating ");
            editSortPref(item, "top_rated");
            mImageAdapter.clearAll();
            loadMoviePosters();

            if (item.isChecked()) item.setChecked(false);
            else item.setChecked(true);

            return true;
        } else if (id == R.id.action_relevance) {
            Log.d("Entered", " R.id.action_popular ");
            editSortPref(item, "popular");

            mImageAdapter.clearAll();
            loadMoviePosters();

            if (item.isChecked()) item.setChecked(false);
            else item.setChecked(true);

            return true;
        } else
            return super.onOptionsItemSelected(item);

    }

    @Override
    public void onStart() {
        super.onStart();
        loadMoviePosters();
    }

    private void loadMoviePosters() {
        if (mPagesLoaded >= MAXPAGES)
            return;

        DownloadMoviesTask downloadMoviesTask = new DownloadMoviesTask();
        downloadMoviesTask.execute(mPagesLoaded + 1);
    }

    public class DownloadMoviesTask extends AsyncTask<Integer, Void, Collection<Movie>> {
        @Override
        protected Collection<Movie> doInBackground(Integer... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            String movieJsonStr = null;
            try {
                final String BASE_URI = "http://api.themoviedb.org/3/movie";
                final String APPID_PARAM = "api_key";
                final String API_PARAM_PAGE = "page";

                //Get the user selected sorting order
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String sortOrder = pref.getString(getString(R.string.pref_sortby_key),
                        getString(R.string.pref_sort_highestrated));

                //Build URI
                Uri builtUri = Uri.parse(BASE_URI).buildUpon()
                        .appendPath(sortOrder).appendQueryParameter(API_PARAM_PAGE, String.valueOf(params[0]))
                        .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIEDB_API_KEY)
                        .build();
                URL url = new URL(builtUri.toString());
                //  Log.d("MainActivity","BASE URI ="+ BASE_URI);
                Log.d("MainActivity", "Built URI =" + builtUri.toString());

                //Create the request to Open themoviedb and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                //Read input stream to a string
                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null) {
                    //Nothig to do
                    movieJsonStr = null;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer buffer = new StringBuffer();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    //stream was empty. no point in parsing
                    movieJsonStr = null;
                }
                movieJsonStr = buffer.toString();
                // Log.v("MainActivFrag","movieJsonStr : " + movieJsonStr);

            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.

                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }


            // Parse JSON string
            try {
                return parseMovieFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.d("MainActFrag", "Can't parse JSON: ");
                return null;
            }
        }

        private Collection<Movie> parseMovieFromJson(String movieJsonStr) throws JSONException {
            ArrayList result = new ArrayList();

            JSONObject jsonObject = new JSONObject(movieJsonStr);
            JSONArray jsonMovieArray = jsonObject.getJSONArray("results");

            for (int i = 0; i < jsonMovieArray.length(); i++) {
                result.add(Movie.fromJson(jsonMovieArray.getJSONObject(i)));
            }
            return result;
        }

        protected void onPostExecute(Collection<Movie> movies) {
            if (movies != null) {
                Log.d("MainFrag", "onPostExecute()-Page Loaded " + mPagesLoaded);
                mImageAdapter.addAll(movies);
                mPagesLoaded++;
            }
        }

    }


}
