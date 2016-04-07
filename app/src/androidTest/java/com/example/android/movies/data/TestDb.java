package com.example.android.movies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Movie;
import android.test.AndroidTestCase;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Hems&Hari on 3/18/2016.
 */
public class TestDb extends AndroidTestCase {
    public static final String LOG_TAG=TestDb.class.getSimpleName();

    //Since we ant to test each test with a clean slate
    void deleteTheDatabase( ) {mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);}

    /*This funcction gets called before each test is exceuted to delete the database.This makes
    * sure that we always have a clean slate*/

    public void setUp() { deleteTheDatabase();}

    /*
        Students: Uncomment this test once you've written the code to create the Location
        table.  Note that you will have to have chosen the same column names that I did in
        my solution for this test to compile, so if you haven't yet done that, this is
        a good time to change your column names to match mine.
        Note that this only tests that the Movie table has the correct columns
     */
    public void testCreateDb() throws Throwable{
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)

        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MovieContract.MovieEntry.TABLE_NAME);
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);

        SQLiteDatabase db = new MovieDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without movie entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.MovieEntry.TABLE_NAME + ")",
                null);
        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());


        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(MovieContract.MovieEntry._ID);
        locationColumnHashSet.add(MovieContract.MovieEntry.POSTER_PATH);
        locationColumnHashSet.add(MovieContract.MovieEntry.ADULT);
        locationColumnHashSet.add(MovieContract.MovieEntry.OVERVIEW);
        locationColumnHashSet.add(MovieContract.MovieEntry.RELEASE_DATE);
        locationColumnHashSet.add(MovieContract.MovieEntry.MOVIE_ID);
        locationColumnHashSet.add(MovieContract.MovieEntry.TITLE);
        locationColumnHashSet.add(MovieContract.MovieEntry.POPULARITY);
        locationColumnHashSet.add(MovieContract.MovieEntry.VOTE_COUNT);
        locationColumnHashSet.add(MovieContract.MovieEntry.VIDEO);
        locationColumnHashSet.add(MovieContract.MovieEntry.VOTE_AVERAGE);
        locationColumnHashSet.add(MovieContract.MovieEntry.RUNTIME);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }


    public void TestMovieTable(){
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Create content values to insert
        ContentValues values =  new ContentValues();
        values.put(MovieContract.MovieEntry.POSTER_PATH,"test");
        values.put(MovieContract.MovieEntry.ADULT,0);
        values.put(MovieContract.MovieEntry.OVERVIEW,"Overview");
        values.put(MovieContract.MovieEntry.RELEASE_DATE,"3-18-2016");
        values.put(MovieContract.MovieEntry.MOVIE_ID,155);
        values.put(MovieContract.MovieEntry.TITLE,"title");
        values.put(MovieContract.MovieEntry.POPULARITY,9.617629);
        values.put(MovieContract.MovieEntry.VOTE_COUNT,10);
        values.put(MovieContract.MovieEntry.VIDEO,6872);
        values.put(MovieContract.MovieEntry.VOTE_AVERAGE,7.99);
        values.put(MovieContract.MovieEntry.RUNTIME,120);
        // Third Step: Insert ContentValues into database and get a row ID back
        long rowId = db.insert(MovieContract.MovieEntry.TABLE_NAME,null,values);

        // Verify we got a row back.
        assertTrue(rowId != 1);

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                MovieContract.MovieEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        validateCurrentRecord("Error: Location Query Validation Failed",cursor,values);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned from location query",
                cursor.moveToNext());

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();

    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

}
