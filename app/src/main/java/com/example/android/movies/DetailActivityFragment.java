package com.example.android.movies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {


    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Movie.INTENT_MOVIE)) {

            //Get the movieBundle sent from MainActivity fragment (movieposter click)
            Bundle movieBundle = intent.getBundleExtra(Movie.INTENT_MOVIE);

            //Get the movie Object data from the movieBundle
            Movie movie = new Movie(movieBundle);

            //The output format is vote_avergae/10
            String voteAvg = movie.voteAverage + "/10";

            //For displaying only the year from the date (YYYY-MM-DD)
            String[] date = movie.releaseDate.split("-");

            //Displaying the movie details to screen
            ((TextView) rootview.findViewById(R.id.textView)).setText(movie.title);
            ((TextView) rootview.findViewById(R.id.movie_synopsys)).setText(movie.overview);
            ((TextView) rootview.findViewById(R.id.release_date)).setText(date[0]);
            ((TextView) rootview.findViewById(R.id.vote_average)).setText(voteAvg);
            Uri builtUri = movie.buildMoviePosterUri();
            Picasso.with(getContext()).load(builtUri).into((ImageView) rootview.findViewById(R.id.movie_poster));
        } else {
            Toast.makeText(getActivity(), "Null intent received", Toast.LENGTH_SHORT);
        }
        //play trailer
        ImageButton button = (ImageButton) rootview.findViewById(R.id.video_button);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Do something in response to button click
                    Toast.makeText(getActivity(), "Clicked!!!", Toast.LENGTH_SHORT).show();

                    Log.d("DetailActFrag", "Clicked trailer button");
                    //Start intent for playing trailer
                    Intent playback = new Intent();
                    Uri uri = Uri.parse("http://www.youtube.com");
                    playback.setData(uri);
                    if (playback.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(playback);
                    }
                }
            });
        }
        return rootview;
    }


}
