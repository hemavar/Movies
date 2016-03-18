package com.example.android.movies;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Hems&Hari on 3/10/2016.
 */
public class MovieID {

    public int id;
    public String originalTitle;
    public int runTime;
    public boolean isVideo;

    public MovieID(Integer id, String title, int runtime, boolean video) {
        this.id = id;
        this.originalTitle = title;
        this.runTime = runtime;
        this.isVideo = video;
    }

    public static MovieID fromJson(JSONObject jsonObject) throws JSONException {
        return new MovieID(
                jsonObject.getInt("id"),
                jsonObject.getString("title"),
                jsonObject.getInt("runtime"),
                jsonObject.getBoolean("video")
        );
    }
}
