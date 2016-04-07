package com.example.android.movies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

/**
 * Created by Hems&Hari on 3/18/2016.
 */
public class Utility
{
    public static Uri buildMoviePosterUri(String posterPath){
        final String BASE_PATH="http://image.tmdb.org/t/p";

        String size = "w342";
        Uri builtUri = Uri.parse(BASE_PATH).buildUpon()
                .appendPath(size)
                .appendEncodedPath(posterPath).build();

        return builtUri;
    }
    public static String getSortPref(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String storedPreference = preferences.getString("sort_key", "top_rated");
        return storedPreference;
    }
}
