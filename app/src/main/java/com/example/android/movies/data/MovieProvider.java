package com.example.android.movies.data;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.movies.Movie;

import java.util.HashMap;

/**
 * Created by Hems&Hari on 3/18/2016.
 */
public class MovieProvider extends ContentProvider {

    private MovieDbHelper mOpenHelper;
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    static final int MOVIE = 100;
    static final int MOVIEID=101;
    static final int FAVORITE=102;
    static final int SEARCH_SUGGEST=103;

    private static final String sMovieIdSelection =
            MovieContract.MovieEntry.TABLE_NAME+
                    "." + MovieContract.MovieEntry.MOVIE_ID + " = ? ";

    private Cursor getMovieDetails(Uri uri) {
        // content://app.example.com.android.movies/movie/123
        Cursor retCursor;
        //Get movie id from the URI

        long movieId = MovieContract.MovieEntry.getMovieIdFromUri(uri);
        Log.d("ContentProvider","movieId " + movieId + " extracted from URI " + uri);

        retCursor = mOpenHelper.getReadableDatabase().query(MovieContract.MovieEntry.TABLE_NAME,
                null,
                sMovieIdSelection,
                        new String[]{Long.toString(movieId)},
                        null,
                        null,
                        null);
        return retCursor;
    }

    @Override
    public boolean onCreate() {
        //Create MovieDbHelper for later use
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    static UriMatcher buildUriMatcher(){
        //The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        //Use ADDURI function to match each of the types. USe the constants from the MovieContract
        //to help define the types to the URIMatcher

        matcher.addURI(authority,MovieContract.PATH_MOVIE,MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE+"/favorite", FAVORITE);
        matcher.addURI(authority,MovieContract.PATH_MOVIE  + "/#",MOVIEID);

        matcher.addURI(authority,SearchManager.SUGGEST_URI_PATH_QUERY ,SEARCH_SUGGEST);
        matcher.addURI(authority,SearchManager.SUGGEST_URI_PATH_QUERY + "/*",SEARCH_SUGGEST);


        return matcher;
    }



    /*
     * @param uri           The URI to query. This will be the full URI sent by the client;
     *                      if the client is requesting a specific record, the URI will end in a record number
     *                      that the implementation should parse and add to a WHERE or HAVING clause, specifying
     *                      that _id value.
     * @param projection    The list of columns to put into the cursor. If
     *                      {@code null} all columns are included.
     * @param selection     A selection criteria to apply when filtering rows.
     *                      If {@code null} then all rows are included.
     * @param selectionArgs You may include ?s in selection, which will be replaced by
     *                      the values from selectionArgs, in order that they appear in the selection.
     *                      The values will be bound as Strings.
     * @param sortOrder     How the rows in the cursor should be sorted.
     *                      If {@code null} then the provider is free to define the sort order.
     * @return a Cursor or {@code null}.
     */
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;

        switch (sUriMatcher.match(uri)){
            //"movie"
            case MOVIE:{

                retCursor = mOpenHelper.getReadableDatabase().query(MovieContract.MovieEntry.TABLE_NAME,
                        projection,selection,selectionArgs,null,null,sortOrder);

                break;
            }
            //"movie"/movieid
            case MOVIEID:{
                retCursor =getMovieDetails(uri);
                break;
            }
           //movie/favorite
          case FAVORITE:{
               retCursor = getFavoriteMovies();
                break;
         }
            case SEARCH_SUGGEST:{
                Log.d("CP", "SEARCH_SUGGEST :  selectionArgs:"  + selectionArgs[0] );
                SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
                queryBuilder.setTables(MovieContract.MovieEntry.TABLE_NAME);

                HashMap<String, String> columnMap = new HashMap<String, String>();
                columnMap.put(BaseColumns._ID, MovieContract.MovieEntry.MOVIE_ID + " AS " + BaseColumns._ID);
                columnMap.put( MovieContract.MovieEntry.TITLE, MovieContract.MovieEntry.TITLE + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
                columnMap.put(MovieContract.MovieEntry.RELEASE_DATE,MovieContract.MovieEntry.RELEASE_DATE + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_2);

              //  columnMap.put(MovieContract.MovieEntry.TITLE, MovieContract.MovieEntry.TITLE + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA);
                columnMap.put(MovieContract.MovieEntry.MOVIE_ID, MovieContract.MovieEntry.MOVIE_ID + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
              //  columnMap.put(MovieContract.MovieEntry.TITLE, MovieContract.MovieEntry.TITLE + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA);
                queryBuilder.setProjectionMap(columnMap);

                String limit = uri.getQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT);
                SQLiteDatabase db = mOpenHelper.getWritableDatabase();





                final String sMovieIdSelection =
                        MovieContract.MovieEntry.TABLE_NAME+
                                "." + MovieContract.MovieEntry.TITLE + " LIKE ?";
                selectionArgs[0] = "%"+selectionArgs[0] + "%";

                retCursor = queryBuilder.query(db, projection, sMovieIdSelection, selectionArgs, null, null, sortOrder, limit);

                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Log.d("CP", "query Set notification uri: " + uri);
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    public Cursor getFavoriteMovies() {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
         final String sMovieIdSelection =
                MovieContract.MovieEntry.TABLE_NAME+
                        "." + MovieContract.MovieEntry.FAVORITE + " = ? ";


        Cursor result =
                db.query(true,
                        MovieContract.MovieEntry.TABLE_NAME,
                        null,//Projection
                        sMovieIdSelection, //Selection
                        new String[]{"true"},//Selection Arguments
                        null,//Groupby
                        null,//Having
                        null,//OrderBy
                        null);//Limits the number of rows returned


        return result;
    }

    /**
     * Implement this to handle requests for the MIME type of the data at the
     * given URI.  The returned MIME type should start with
     * <code>vnd.android.cursor.item</code> for a single record,
     * or <code>vnd.android.cursor.dir/</code> for multiple items.
     * This method can be called from multiple threads, as described in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html#Threads">Processes
     * and Threads</a>.
     * <p/>
     * <p>Note that there are no permissions needed for an application to
     * access this information; if your content provider requires read and/or
     * write permissions, or is not exported, all applications can still call
     * this method regardless of their access permissions.  This allows them
     * to retrieve the MIME type for a URI when dispatching intents.
     *
     * @param uri the URI to query.
     * @return a MIME type string, or {@code null} if there is no type.
     */

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch(match){
            case MOVIE:return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIEID:return MovieContract.MovieEntry.CONTENT_TYPE;
            case FAVORITE:return MovieContract.MovieEntry.CONTENT_TYPE;
            case SEARCH_SUGGEST:return SearchManager.SUGGEST_MIME_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }



/*
    private boolean checkIfMovieExistsinDb(long movieId)
    {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor locationCursor = db.rawQuery("SELECT " + MovieContract.MovieEntry.MOVIE_ID +
        " WHERE " + MovieContract.MovieEntry.MOVIE_ID + " = ? " ,new String[]{"qwe"});


        return false;
    }
    */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri=null;

        switch (match) {
            case MOVIE: {

                long _id = db.insertWithOnConflict(MovieContract.MovieEntry.TABLE_NAME, null, values,SQLiteDatabase.CONFLICT_IGNORE);

                if (_id > 0)
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
              else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown URI :" + uri);
        }
        Log.d("CP", "insert query notifychange uri: " + uri);
        if(returnUri != null)
            getContext().getContentResolver().notifyChange(uri,null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        //This makes delete all rows return the number of rows deleted
        if(selection == null) selection="1";
        switch(match){
            case MOVIE:{
                rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME,selection,selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(rowsDeleted != 0 )
            getContext().getContentResolver().notifyChange(uri,null);
        Log.d("CP","delete query notifychange uri: " + uri);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match= sUriMatcher.match(uri);
        int retValue;

        switch (match){
            case MOVIE:
                retValue = db.update(MovieContract.MovieEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri :" + uri);
        }
        if(retValue!=0) {
            getContext().getContentResolver().notifyChange(uri, null);
            Log.d("CP", "update query notifychange uri: " + uri);
        }
        return retValue;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch(match){
            case MOVIE:
                db.beginTransaction();
                int returnCount = 0;
                try{
                    for ( ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME,null,value);
                        if(_id != -1){
                            returnCount++;
                        }

                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri,null);
                Log.d("CP", "BulkInsert query notifychange uri: " + uri);
                return returnCount;
            default:
                return super.bulkInsert(uri,values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
