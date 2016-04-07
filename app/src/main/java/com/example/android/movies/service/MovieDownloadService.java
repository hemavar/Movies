package com.example.android.movies.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.android.movies.BuildConfig;
import com.example.android.movies.data.MovieContract;

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
import java.util.Vector;

/**
 * Created by Hems&Hari on 3/23/2016.
 */
public class MovieDownloadService extends IntentService {
    public static final String PAGE_QUERY_EXTRA = "pageNo";
    public MovieDownloadService() {
        super("MovieDownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {



        int pageNo = intent.getIntExtra(PAGE_QUERY_EXTRA,0);
        Log.d("MovieDownloadService","Starting to download page no: " + pageNo);
        HttpURLConnection urlConnection = null;
        BufferedReader bufferedReader = null;

        String movieJsonStr = null;
        try {
            final String BASE_URI = "http://api.themoviedb.org/3/movie";
            final String APPID_PARAM = "api_key";
            final String API_PARAM_PAGE = "page";

            //Get the user selected sorting order
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

            String sortOrder = pref.getString("sort_key", "top_rated");

            //Build URI
            Uri builtUri = Uri.parse(BASE_URI).buildUpon()
                    .appendPath(sortOrder).appendQueryParameter(API_PARAM_PAGE, String.valueOf(pageNo))
                    .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIEDB_API_KEY)
                    .build();
            URL url = new URL(builtUri.toString());
            //  Log.d("MainActivity","BASE URI ="+ BASE_URI);
            //Log.d("MDS", "Built URI =" + builtUri.toString());

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
            //Log.v("DownloadMovieTask", "movieJsonStr : " + movieJsonStr);
            parseMovieFromJson(movieJsonStr, sortOrder);
        } catch (IOException e) {
            Log.e("PlaceholderFragment", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            Log.e("DownloadMovieTask", e.getMessage(), e);
            e.printStackTrace();
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
        Log.d("MovieDownloadService", "Downloaded!!!!");
        return;
    }

    private void parseMovieFromJson(String movieJsonStr, String sortOrder) throws JSONException {
        ArrayList result = new ArrayList();

        //These are the names of the JSON objects to be extracted

        final String POSTERPATH = "poster_path";
        final String ADULT = "adult";
        final String OVERVIEW = "overview";
        final String RELEASEDATE = "release_date";
        final String MOVIEID = "id";
        final String TITLE = "title";
        final String POPULARITY = "popularity";
        final String VOTECOUNT = "vote_count";
        final String VIDEO = "video";
        final String VOTEAVERAGE = "vote_average";

        try {


            JSONObject jsonObject = new JSONObject(movieJsonStr);
            JSONArray jsonMovieArray = jsonObject.getJSONArray("results");
            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(jsonMovieArray.length());


            for (int i = 0; i < jsonMovieArray.length(); i++) {
                String posterPath = jsonMovieArray.getJSONObject(i).getString("poster_path");
                boolean isAdult = jsonMovieArray.getJSONObject(i).getBoolean("adult");
                String overview = jsonMovieArray.getJSONObject(i).getString("overview");
                String releaseDate = jsonMovieArray.getJSONObject(i).getString("release_date");
                //We only need the year for display
                String[] date = releaseDate.split("-");


                String title = jsonMovieArray.getJSONObject(i).getString("title");
                double popularity = jsonMovieArray.getJSONObject(i).getDouble("popularity");
                long vote_count = jsonMovieArray.getJSONObject(i).getLong("vote_count");
                boolean isVideo = jsonMovieArray.getJSONObject(i).getBoolean("video");
                double voteAverage = jsonMovieArray.getJSONObject(i).getDouble("vote_average");
                int id = jsonMovieArray.getJSONObject(i).getInt("id");

                ContentValues movieData = new ContentValues();
                movieData.put(MovieContract.MovieEntry.POSTER_PATH, posterPath);
                movieData.put(MovieContract.MovieEntry.ADULT, isAdult);
                movieData.put(MovieContract.MovieEntry.OVERVIEW, overview);
                movieData.put(MovieContract.MovieEntry.RELEASE_DATE, date[0]);//Only year will be stored in db
                movieData.put(MovieContract.MovieEntry.TITLE, title);
                movieData.put(MovieContract.MovieEntry.POPULARITY, popularity);
                movieData.put(MovieContract.MovieEntry.VOTE_COUNT, vote_count);
                movieData.put(MovieContract.MovieEntry.VIDEO, isVideo);
                movieData.put(MovieContract.MovieEntry.VOTE_AVERAGE, voteAverage);
                movieData.put(MovieContract.MovieEntry.MOVIE_ID, id);

                //Added the top_rated/popular category for later use
                movieData.put(MovieContract.MovieEntry.CATEGORY, sortOrder);

                movieData.put(MovieContract.MovieEntry.FAVORITE,"false"); //By default

                cVVector.add(movieData);

            }
            int inserted = 0;
            // add to database

            if (cVVector.size() > 0) {
                // Student: call bulkInsert to add the weatherEntries to the database here
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);

                inserted = this.getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);
            }
              Log.d("DownLoadMovieTask", "DownloadMoviesTask Complete. " + inserted + " Inserted");
        } catch (JSONException e) {
            Log.e("DownloadMovieTask", e.getMessage(), e);
            e.printStackTrace();
        }

    }



/*
    public static  class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent sendIntent = new Intent(context,MovieDownloadService.class);
            sendIntent.putExtra(PAGE_QUERY_EXTRA,intent.getIntExtra(PAGE_QUERY_EXTRA,0));
            context.startService(sendIntent);
        }
    }
    */
}
