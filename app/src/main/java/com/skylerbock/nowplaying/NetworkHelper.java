package com.skylerbock.nowplaying;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by sbock on 10/2/16.
 */

public class NetworkHelper {
    public static final String TAG = NetworkHelper.class.getSimpleName();
    private static OkHttpClient client = new OkHttpClient();

    public static String getLocation()
    {
        String zipcode = null;
        long start = System.currentTimeMillis();

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

        if (BuildConfig.DEBUG)
            Log.i(TAG, "Took " + (System.currentTimeMillis() - start) + "ms to " + request.urlString());

        return zipcode;
    }

    public static List<String> getMovies(String zipcode)
    {
        // Zipcode is required
        if (zipcode == null)
            return null;

        List<String> movies = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        long start = System.currentTimeMillis();

        // /showtimes/location?api=v1&location=US,66617&date=2016-11-06
        String url = "https://app.domain.com/showtimes/location?api=v1&location=US,"+zipcode+"&date=" + sdf.format(new Date());
        url = url.replace("domain","imdb");

        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            // TODO: Fix the slowness of this request (looks like it's only the first call?) [Seems slowest on simulator]
            Response response = client.newCall(request).execute();
            if (response.isSuccessful())
            {
                String body = response.body().string();
                movies = parseMovies(body);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (BuildConfig.DEBUG)
            Log.i(TAG, "Took " + (System.currentTimeMillis() - start) + "ms to " + request.urlString());

        return movies;
    }

    // This returns a list of IMDB id's by parsing JSON
    private static List<String> parseMovies(String strBody) {

        List<String> movies = new ArrayList<>();

        try {
            JSONObject body = new JSONObject(strBody);

            JSONObject data = body.getJSONObject("data");
            JSONObject titles = data.getJSONObject("titles");

            Iterator<?> keys = titles.keys();
            while( keys.hasNext() ) {
                String key = (String) keys.next();
                if (movies.contains(key)) {
                    // Key already exists in list
                }
                else {
                    movies.add(key);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return movies;
    }

    public static String getMovie(String imdbId)
    {
        String json = null;
        long start = System.currentTimeMillis();

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

        if (BuildConfig.DEBUG)
            Log.i(TAG, "Took " + (System.currentTimeMillis() - start) + "ms to " + request.urlString());

        return json;
    }

    public static String getPlayStoreVersion()
    {
        String version = null;
        long start = System.currentTimeMillis();

        String appID = BuildConfig.APPLICATION_ID;

        // Remove debug
        if (appID.endsWith("debug"))
        {
            Log.v(TAG, "Removing 'debug' from application ID");
            appID = appID.substring(0, appID.lastIndexOf("."));
        }

        try {
            Document doc = Jsoup.connect("http://play.google.com/store/apps/details?id=" + appID).get();
            version = doc.getElementsByAttributeValue("itemprop","softwareVersion").first().text();
        } catch (Exception e) {
            Log.e(TAG, "Error getting version from Google Play", e);
        }

        if (BuildConfig.DEBUG)
            Log.i(TAG, "Took " + (System.currentTimeMillis() - start) + "ms to http://play.google.com/store/apps/details?id=" + appID);

        return version;
    }
}
