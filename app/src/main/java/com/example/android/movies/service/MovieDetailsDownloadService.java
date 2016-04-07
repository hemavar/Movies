package com.example.android.movies.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.android.movies.BuildConfig;
import com.example.android.movies.data.MovieContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Hems&Hari on 3/23/2016.
 */
public class MovieDetailsDownloadService extends IntentService{
    public static final String MOVIEID_QUERY_EXTRA = "movieId";


    public MovieDetailsDownloadService() {
        super("MovieDetailsDownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String movieId=  intent.getStringExtra(MOVIEID_QUERY_EXTRA);

        HttpURLConnection urlConnection = null;
        BufferedReader bufferedReader = null;

        String movieJsonStr = null;
        try {
            final String BASE_URI = "http://api.themoviedb.org/3/movie";
            final String APPID_PARAM = "api_key";

            //Get the user selected sorting order
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

            String sortOrder = pref.getString("sort_key", "top_rated");

            //Build URI
            Uri builtUri = Uri.parse(BASE_URI).buildUpon()
                    .appendPath(String.valueOf(movieId))
                    .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIEDB_API_KEY)
                    .build();
            URL url = new URL(builtUri.toString());
            //  Log.d("MainActivity","BASE URI ="+ BASE_URI);
            Log.d("DetailActivity", "Built Movie URI =" + builtUri.toString());

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
            Log.v("DownloadMovieDetService","Completed!! ");
            parseMovieIdFromJson(movieJsonStr);
        } catch (IOException e) {
            Log.e("PlaceholderFragment", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            Log.e("DownloadMovieTask", e.getMessage(), e);
            e.printStackTrace();
        }
        finally {
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
        return;
    }

    private void parseMovieIdFromJson(String movieJsonStr) throws JSONException {
        ArrayList result = new ArrayList();

        //These are the names of the JSON objects to be extracted

        final String MOVIEID = "id";
        final String RUNTIME = "runtime";
        final String TRAILER = "video";

        try{

            JSONObject jsonObject = new JSONObject(movieJsonStr);
            Long movieId = jsonObject.getLong(MOVIEID);
            Integer runTime = jsonObject.getInt(RUNTIME);
            String trailer = jsonObject.getString(TRAILER);

            //Log.d("DownloadMovie","json " + movieJsonStr);
            Log.d("DownloadMovie","trailer " + trailer);
            if(trailer.equals("true"))
            {
                Toast.makeText(this,"Trailer !!!!",Toast.LENGTH_SHORT).show();
            }
            ContentValues movieData = new ContentValues();
            movieData.put(MovieContract.MovieEntry.RUNTIME, runTime);
            String selection= MovieContract.MovieEntry.MOVIE_ID + " = ? ";
            String[] selectionArgs = {Long.toString(movieId)};
            // add to database
            int UpdatedRowId = this.getContentResolver().update(MovieContract.MovieEntry.CONTENT_URI,
                    movieData,
                    selection,
                    selectionArgs);

            Log.d("DownLoadMovieIdTask", "Inserted MovieDetails to DB Complete!! " + UpdatedRowId + " UpdatedRowId");
        }catch (JSONException e) {
            Log.e("DownloadMovieTask", e.getMessage(), e);
            e.printStackTrace();
        }

    }
}
