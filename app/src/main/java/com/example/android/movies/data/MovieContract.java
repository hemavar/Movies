package com.example.android.movies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.sql.Statement;

/**
 * Created by Hems&Hari on 3/18/2016.
 */
//Defines tables and column names for the Movie Database

public class MovieContract {
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.example.android.movies";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.movies/movies is a valid path for
    // looking at movie posters.
    public static final String PATH_MOVIE = "movie";

    public static final String PATH_FAVORITE ="movie/favorite";
    /* Inner class that defines the table contents of the movie table */
    public static final class MovieEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final Uri FAVORITE_URI =
                CONTENT_URI.buildUpon().appendPath("favorite").build();


        public static final String CONTENT_TYPE=
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE ;

        // Table name
        public static final String TABLE_NAME = "movie";

        // Movie attributes
        public static final String POSTER_PATH ="poster_path";
        public static final String ADULT = "adult";
        public static final String OVERVIEW ="overview";
        public static final String RELEASE_DATE ="release_date";
        public static final String MOVIE_ID = "id";
        public static final String TITLE = "title";
        public static final String POPULARITY = "popularity";
        public static final String VOTE_COUNT="vote_count";
        public static final String VIDEO="video";
        public static final String VOTE_AVERAGE="vote_average";

        //Trailer details and runtime
        public static final String RUNTIME="runtime";
        public static final String TRAILER1="trailer";

        public static final String FAVORITE="favorite";
        public static final String CATEGORY="category";
        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        //movie/movieId
        public static Uri buildMovieWithMovieId( long movieId) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(movieId)).build();
        }


        public static long getMovieIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }
    }

}
