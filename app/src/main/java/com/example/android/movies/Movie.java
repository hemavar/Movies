package com.example.android.movies;

import android.net.Uri;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Hems&Hari on 3/6/2016.
 */
public class Movie {


    // Movie attributes
    public String posterPath;
    public boolean isAdult ;
    public String overview;
    public String releaseDate;

    public String originalTitle;
    public String originalLanguage;
    public String title;
    public String backDropPath;
    public double popularity;
    public long vote_count;
    public boolean isVideo;
    public double voteAverage;

    public int id;

    public static String INTENT_MOVIE = "com.example.android.movies.INTENT_MOVIE";


    public Uri posterUri;
    public String testMovieLoc;
    public int resId;

    public Movie(int id,String testMovieLoc,int resId,String title){
      //Add later
        this.id = id;
        this.title = title;
        this.testMovieLoc = testMovieLoc;
        this.resId = resId;
    }

    public Movie(Bundle movieBundle){
        this.posterPath = movieBundle.getString("poster_path");
        this.isAdult = movieBundle.getBoolean("adult");
        this.overview= movieBundle.getString("overview") ;
        this.releaseDate = movieBundle.getString("release_date");
        this.id= movieBundle.getInt("id");
        this.title=movieBundle.getString("title");
        this.originalLanguage=movieBundle.getString("original_language");
        this.backDropPath=movieBundle.getString("backdrop_path");
        this.popularity=movieBundle.getDouble("popularity");
        this.vote_count=movieBundle.getLong("vote_count");
        this.isVideo= movieBundle.getBoolean("video");
        this.voteAverage=movieBundle.getDouble("vote_average");
    }

    public Movie(String poster_path, boolean adult, String overview, String release_date, int id, String title, String original_language,
                 String backdrop_path, double popularity, long vote_count, boolean video, double vote_average) {

        this.posterPath = poster_path;
        this.isAdult = adult;
        this.overview =overview;
        this.releaseDate = release_date;
        this.id = id;
        this.title = title;
        this.originalLanguage = original_language;
        this.backDropPath = backdrop_path;
        this.popularity = popularity;
        this.vote_count = vote_count;
        this.isVideo = video;
        this.voteAverage = vote_average;
    }
    public Bundle makeMovieBundle(Movie movie)
    {
        Bundle movieBundle = new Bundle();
        movieBundle.putString("poster_path", movie.posterPath);
        movieBundle.putBoolean("adult", movie.isAdult);
        movieBundle.putString("overview", movie.overview);
        movieBundle.putString("release_date", movie.releaseDate);
        movieBundle.putInt("id", movie.id);
        movieBundle.putString("title",movie.title);
        movieBundle.putString("original_language",movie.originalLanguage);
        movieBundle.putString("backdrop_path", movie.backDropPath);
        movieBundle.putDouble("popularity", movie.popularity);
        movieBundle.putLong("vote_count", movie.vote_count);
        movieBundle.putBoolean("video", movie.isVideo);
        movieBundle.putDouble("vote_average", movie.voteAverage);
        return movieBundle;
    }
    public static Movie fromJson(JSONObject jsonObject) throws JSONException {
        return new Movie(
                jsonObject.getString("poster_path"),
                jsonObject.getBoolean("adult"),
                jsonObject.getString("overview"),
                jsonObject.getString("release_date"),
                jsonObject.getInt("id"),
                jsonObject.getString("title"),
                jsonObject.getString("original_language"),
                jsonObject.getString("backdrop_path"),
                jsonObject.getDouble("popularity"),
                jsonObject.getLong("vote_count"),
                jsonObject.getBoolean("video"),
                jsonObject.getDouble("vote_average")
        );
    }
    public Uri buildMoviePosterUri(){
        final String BASE_PATH="http://image.tmdb.org/t/p";

        //
        String size = "w185";
        Uri builtUri = Uri.parse(BASE_PATH).buildUpon()
                .appendPath(size)
                .appendEncodedPath(posterPath).build();

        return builtUri;
    }

}
