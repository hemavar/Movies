package com.example.android.movies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.movies.data.MovieContract.MovieEntry;


/**
 * Created by Hems&Hari on 3/18/2016.
 */
public class MovieDbHelper  extends SQLiteOpenHelper{

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 16;
    static final String DATABASE_NAME = "movies.db";





    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        //Create Movie table
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME +  " ( " +
                MovieEntry._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieEntry.POSTER_PATH  + " TEXT  NOT NULL, " +
                MovieEntry.ADULT  + " INTEGER NOT NULL, "  +
                MovieEntry.OVERVIEW  + " TEXT NOT NULL, "  +
                MovieEntry.RELEASE_DATE + "  TEXT NOT NULL,  " +
                MovieEntry.MOVIE_ID + " LONG  UNIQUE NOT NULL, " +
                MovieEntry.TITLE + " TEXT NOT NULL, " +
                MovieEntry.POPULARITY + " REAL NOT NULL, " +
                MovieEntry.VOTE_COUNT + " INTEGER NOT NULL, " +
                MovieEntry.VIDEO + " INTEGER NOT NULL, " +
                MovieEntry.VOTE_AVERAGE + " REAL NOT NULL, " +
                MovieEntry.RUNTIME + " INTEGER, " +
                MovieEntry.TRAILER1 + " TEXT, " +
                MovieEntry.FAVORITE + " TEXT NOT NULL,  " +
                MovieEntry.CATEGORY + " TEXT NOT NULL,   " +

                " UNIQUE ( " + MovieEntry.MOVIE_ID + " ) ON CONFLICT REPLACE ); ";


        db.execSQL(SQL_CREATE_MOVIE_TABLE);

    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p/>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //This database is only a cache for online data,so its upgrade policy is to simply
        //disregard the data and start over.This fires only if you chnage the database version
        //It doesnt depend on the version of the application
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
