package com.skylerbock.nowplaying;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by sbock on 10/2/16.
 */

public class NetworkHelper {
    private static OkHttpClient client = new OkHttpClient();

    public static String getLocation()
    {
        String zipcode = null;

        Request request = new Request.Builder()
                .url("http://ipinfo.io/postal")
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful())
            {
                String body = response.body().string();
                zipcode = body.trim();

                // Check if we're unable to find the zipcode
                if (zipcode.equals("undefined"))
                {
                    zipcode = null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return zipcode;
    }

    public static String getMovies(String zipcode)
    {
        String body = null;

        // Use a zipcode if provided, otherwise don't send a location
        // Google will attempt to guess for us, presumably by IP address? (it doesn't matter, it's out of our hands)
        String url = "http://google.com/movies" + ((zipcode==null) ? "" : ("?near=" + zipcode));

        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            // TODO: Fix the slowness of this request (looks like it's only the first call?) [Seems slowest on simulator]
            Response response = client.newCall(request).execute();
            if (response.isSuccessful())
            {
                body = response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return body;
    }

    public static String getMovie(String imdbId)
    {
        String json = null;

        //http://www.omdbapi.com/?i=tt1951266&plot=short&r=json
        Request request = new Request.Builder()
                .url("http://www.omdbapi.com/?i="+imdbId+"&plot=short&r=json")
                .build();

        try {
            Response response = client.newCall(request).execute();
            // Make sure the request was successful
            if (response.isSuccessful()) {
                json = response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return json;
    }
}
