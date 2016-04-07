package com.example.android.movies;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.support.v7.widget.SearchView;


public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback{
    private final String MAINACTFRAGMENT_TAG = "MACTFRAGTAG";
   private static final String DETAILFRAGMENT_TAG = "DFTAG";


   private boolean mTwoPane;

    String mSortOrder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        if (findViewById(R.id.fragment_detail_container) != null) {

            //The detail container view will be present only for the large screen layouts
            //(res/layout-sw600dp).If this view is present ,then the activity should be in two pane mode
            mTwoPane = true;
            Log.d("MainActivity", "TwoPane Mode  : " );
            //In two pane mode,show the detail activity view in this activity by adding or replacing
            //the detail fragment using a fragment transaction

            if(savedInstanceState == null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_detail_container, new DetailActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        }else {
            Log.d("MainActivity", "SinglePane mode : " );
            mTwoPane = false;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Add SearchWidget.
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView)menu.findItem(R.id.search ).getActionView();

        ComponentName componentName = new ComponentName(this,SearchableActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));

        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setIconifiedByDefault(false);
       return super.onCreateOptionsMenu( menu );

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent settings = new Intent(this,
                    Settings.class);
            startActivity(settings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        String sortOrder = Utility.getSortPref(this);
        // update the location in our second pane using the fragment manager
        if (sortOrder != null && !sortOrder.equals(mSortOrder)) {
            MainActivityFragment ff = (MainActivityFragment)getSupportFragmentManager().findFragmentById(R.id.main_fragment);
            if ( null != ff ) {
                ff.onSortOrderChanged();
            }
            mSortOrder = sortOrder;
        }
    }


    @Override
    public void onItemSelected(Uri movieUri) {
        if (mTwoPane) {
            //in two pane mode,show the detail view in this activity by adding or replacing
            // the detail fragment using a fragment transaction

            Bundle args = new Bundle();
            args.putParcelable(DetailActivityFragment.DETAIL_URI, movieUri);
            Log.d("2 MainActivity", "onItemSelected ( ) tWO Pane movieUri : " + movieUri);
            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Log.d("2 MainActivity", "onItemSelected ( ) singlePane movieUri : " + movieUri);
            Intent detailedActivity = new Intent(this, DetailActivity.class);
            detailedActivity.setData(movieUri);
            startActivity(detailedActivity);
        }
    }

}
