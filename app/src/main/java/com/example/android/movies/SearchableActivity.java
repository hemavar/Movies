package com.example.android.movies;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.movies.data.MovieContract;

import org.w3c.dom.Text;


/**
 * Created by Hems&Hari on 3/24/2016.
 */
public class SearchableActivity extends Activity implements SearchView.OnQueryTextListener {

    TextView mTextView;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.result);
        mTextView =(TextView)findViewById(R.id.search_no_result);

        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }


    public void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d("Searchableactivity","ACTION_SEARCH : query " + query);
            doSearch(query);
        }
        else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // Handle a suggestions click (because the suggestions all use ACTION_VIEW)
            Uri query = intent.getData();
            Log.d("Searchableactivity", "ACTION_VIEW: Uri " + query);
            showResults(query);
        }
    }
    private void showResults(Uri movieUri){

        Intent detailedActivity = new Intent(getApplicationContext(), DetailActivity.class);
        detailedActivity.setData(movieUri);
        detailedActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(detailedActivity);
        finish();
    }
    private void doSearch(String queryStr) {
        // get a Cursor, prepare the ListAdapter
        // and set it

        String selection =  MovieContract.MovieEntry.TABLE_NAME+
                "." + MovieContract.MovieEntry.TITLE + " = ? COLLATE NOCASE";
        String [] selectionArg= new String[]{queryStr};

        //Find MovieId
        Cursor cursor = this.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                null,//projection
                selection,//selection
                selectionArg,//selectionArgs
                null);//Sort order


        if(cursor!= null && cursor.moveToFirst()){

            long movieId =cursor.getLong(cursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_ID));
            Log.d("SearchableActivity", "MovieId: " + movieId);
            Uri movieUri = MovieContract.MovieEntry.buildMovieWithMovieId(movieId);
            showResults(movieUri);
            cursor.close();
        }
        else
        {
            mTextView.setVisibility(View.VISIBLE);
            //Toast toast = Toast.makeText(getBaseContext(), queryStr + " Not found!!!", Toast.LENGTH_LONG);
            //  toast.setGravity(Gravity.CENTER, 0, 0);
            //  toast.show();



            // There are no results
            //     mTextView.setText(getString(R.string.no_results, new Object[]{queryStr}));
        }
    }


    /**
     * Called when the user submits the query. This could be due to a key press on the
     * keyboard or due to pressing a submit button.
     * The listener can override the standard behavior by returning true
     * to indicate that it has handled the submit request. Otherwise return false to
     * let the SearchView handle the submission by launching any associated intent.
     *
     * @param query the query text that is to be submitted
     * @return true if the query has been handled by the listener, false to let the
     * SearchView perform the default action.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d("SearchableActivity", "onQueryTextSubmit query : " + query);
        return false;
    }

    /**
     * Called when the query text is changed by the user.
     *
     * @param newText the new content of the query text field.
     * @return false if the SearchView should perform the default action of showing any
     * suggestions if available, true if the action was handled by the listener.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
