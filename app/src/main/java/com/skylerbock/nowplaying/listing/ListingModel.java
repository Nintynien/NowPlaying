package com.skylerbock.nowplaying.listing;

import android.content.Context;
import android.util.Log;

import com.skylerbock.nowplaying.AppPreferences;
import com.skylerbock.nowplaying.DBHelper;
import com.skylerbock.nowplaying.movie.Movie;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.greenrobot.event.EventBus;

/**
 * Created by sbock on 12/6/15.
 */
public class ListingModel {

    OkHttpClient client = new OkHttpClient();

    public void updateDatabase(Context c, String zipcode) {
        new Thread(new UpdateThread(c, zipcode)).start();
    }

    public class RefreshCompletedEvent {
        public boolean successful = false;
        public int movieCount = 0;
        public RefreshCompletedEvent(boolean successful, int movieCount) {
            this.movieCount = movieCount;
            this.successful = successful;
        }
    }

    public class UpdateThread implements Runnable {

        public static final String TAG = "UpdateThread";

        String zipcode;
        Context context;
        boolean successful = false;
        int movieCount = 0;

        public UpdateThread(Context context, String zipcode) {
            this.context = context;
            this.zipcode = zipcode;
        }

        public void run() {
            Log.i(TAG, "UpdateThread starting");
            long start = System.currentTimeMillis();

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
                    String body = response.body().string();

                    // Gets a list of imdb movie ids and their trailer
                    Map<String,String> movies = getMovies(body);

                    List<Movie> movieList = new ArrayList<>();
                    // Get movie information from omdb and populate the movie object
                    for (Map.Entry<String, String> entry : movies.entrySet()) {
                        String imdb = entry.getKey();
                        String trailer = entry.getValue();

                        // Retrieve the movie object
                        Movie m = getMovie(imdb, trailer, "3db64253dbf295f3"); //TODO: Remove debug movieid

                        // Save the movie to our list to update the database with
                        if (m != null) {
                            movieList.add(m);
                            movieCount++;
                        }
                    }

                    // Save all the movies to our database (also removes old movies)
                    successful = DBHelper.saveMovies(context, movieList);
                    if (successful) {
                        // Save time that we last updated
                        new AppPreferences(context).setKeyPrefsLastUpdated(new Date());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Post the completed event so the listeners know to update themselves
            EventBus.getDefault().post(new RefreshCompletedEvent(successful, movieCount));
            Log.i(TAG, "UpdateThread finished with " + movieCount + " movies in " + (System.currentTimeMillis() - start) + "ms");
        }
    }

    // This returns a list of IMDB id's by parsing html
    private Map<String, String> getMovies(String doc) {
        // Using a map to prevent duplicate movies (since movies can play at multiple theaters)
        Map<String, String> map = new HashMap<>();

        Document html = Jsoup.parse(doc);
        Element body = html.body();

        // Go through each movie element
        Elements movies = body.getElementsByClass("movie");
        for (Element m : movies) {
            // Get the info element for the movie as that contains the information we want to parse
            String info = m.getElementsByClass("info").html();
            try {
                // We need to URL decode the html so we can correctly match the youtube url below
                info = java.net.URLDecoder.decode(info, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.e("URLDecode", "ERROR DECODING HTML");
                e.printStackTrace();
            }

            // Parse imdb id
            Matcher matcher = Pattern.compile("/(tt\\d+)/").matcher(info);
            if (matcher.find()) {
                String imdbid = matcher.group(1);

                // Parse trailer youtube id
                String trailer = null;
                matcher = Pattern.compile("(?:(?:youtu\\.be\\/|v\\/|vi\\/|u\\/\\w\\/|embed\\/)|(?:(?:watch)?\\?v(?:i)?=|\\&v(?:i)?=))([^#\\&\\?]*)").matcher(info);
                if (matcher.find()) {
                    trailer = matcher.group(1);
                }

                // Save the movie into our list
                map.put(imdbid, trailer);
            }
        }

        return map;
    }

    // Returns a movie object from an OMDB API REST call
    private Movie getMovie(final String imdbId, final String trailer, final String movieid) {
        //http://www.omdbapi.com/?i=tt1951266&plot=short&r=json
        Request request = new Request.Builder()
                .url("http://www.omdbapi.com/?i="+imdbId+"&plot=short&r=json")
                .build();

        Movie movie = null;
        try {
            Response response = client.newCall(request).execute();
            // Make sure the request was successful
            if (response.isSuccessful()) {
                String json = response.body().string();

                JSONObject j = new JSONObject(json);

                // Make sure OMDB found the movie we are looking for
                boolean ok = j.getBoolean("Response");
                if (ok) {
                    String title = j.getString("Title");
                    String year = j.getString("Year");
                    String poster = j.getString("Poster");
                    String rating = j.getString("imdbRating");
                    String plot = j.getString("Plot");
                    String cast = j.getString("Actors");
                    String director = j.getString("Director");
                    String genre = j.getString("Genre");
                    String runtime = j.getString("Runtime");
                    String mpaa = j.getString("Rated");

                    // Create the movie object from the data we received
                    movie = new Movie(title, poster, trailer, imdbId, year, plot, director, cast, genre, runtime, mpaa, rating, movieid);
                }
            }
        } catch (IOException | JSONException e) {
            Log.i("", "Error getting movie_content");
            e.printStackTrace();
        }

        return movie;
    }
}
