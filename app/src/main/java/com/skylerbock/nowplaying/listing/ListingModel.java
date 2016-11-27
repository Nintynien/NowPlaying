package com.skylerbock.nowplaying.listing;

import android.content.Context;
import android.util.Log;

import com.skylerbock.nowplaying.AppPreferences;
import com.skylerbock.nowplaying.BuildConfig;
import com.skylerbock.nowplaying.DBHelper;
import com.skylerbock.nowplaying.NetworkHelper;
import com.skylerbock.nowplaying.movie.Movie;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

    public void updateDatabase(Context c, String zipcode) {
        new Thread(new UpdateThread(c, zipcode)).start();
    }

    public void getLocation(Context c) {
        new Thread(new LocationThread(c)).start();
    }

    public class LocationFoundEvent {
        public boolean successful = false;
        public String zipCode = null;

        public LocationFoundEvent(boolean successful, String zipCode) {
            this.successful = successful;
            this.zipCode = zipCode;
        }
    }

    public class LocationThread implements Runnable {
        public static final String TAG = "LocationThread";

        String zipcode;
        Context context;
        boolean successful = false;

        public LocationThread(Context context) {
            this.context = context;
        }

        public void run() {
            Log.i(TAG, "LocationThread starting");
            long start = System.currentTimeMillis();

            zipcode = NetworkHelper.getLocation();

            if (zipcode != null) {
                // Save zipcode to our database so we can debug our requests
                successful = DBHelper.saveLocation(context, zipcode);
                if (successful) {
                    // Save zipcode in preferences
                    new AppPreferences(context).setKeyPrefsLocation(zipcode);
                }
            }

            // Post the completed event so the listeners know to update themselves
            EventBus.getDefault().post(new LocationFoundEvent(successful, zipcode));
            Log.i(TAG, "LocationThread finished with " + zipcode + " in " + (System.currentTimeMillis() - start) + "ms");
        }
    }

    public class RefreshCompletedEvent {
        public boolean successful = false;
        public String zipcode = null;
        public int movieCount = 0;
        public RefreshCompletedEvent(boolean successful, String zipcode, int movieCount) {
            this.movieCount = movieCount;
            this.zipcode = zipcode;
            this.successful = successful;
        }
    }

    // This is the data that we pull from HTML
    public class MovieData {
        public String imdb;
        public String trailer;
        public String mid;
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

            // Check if we should blur posters
            if (new AppPreferences(context).getKeyPrefsBlur())
            {
                String current = BuildConfig.VERSION_NAME;
                String released = NetworkHelper.getPlayStoreVersion();

                if (current.endsWith("(debug)"))
                {
                    Log.v(TAG, "Removing 'debug' from current version");
                    current = current.substring(0, current.lastIndexOf("(debug)"));
                }

                if (released != null)
                {
                    boolean blur = false;

                    current = current.trim();
                    released = released.trim();

                    String[] curParts = current.split("\\.");
                    String[] relParts = released.split("\\.");

                    // Compare each version part to see if this verion hasn't been released yet
                    for(int i = 0; i < Math.min(curParts.length, relParts.length); i++)
                    {
                        int curVal = Integer.parseInt(curParts[i]);
                        int relVal = Integer.parseInt(relParts[i]);

                        if (curVal > relVal)
                        {
                            // Still need to blur
                            blur = true;
                            break;
                        }
                    }

                    if (!blur)
                    {
                        // All parts of the smallest version match
                        // if the released version still has more parts, we should still blur
                        blur = relParts.length > curParts.length;
                    }

                    if (!blur)
                    {
                        // Stop bluring
                        new AppPreferences(context).setKeyPrefsBlur(false);
                    }
                }
            }

            // Get zipcode if we don't have one currently
            if (zipcode == null)
            {
                zipcode = NetworkHelper.getLocation();
                if (zipcode != null) {
                    // Save zipcode in preferences
                    new AppPreferences(context).setKeyPrefsLocation(zipcode);
                }
            }

            List<String> movies = NetworkHelper.getMovies(zipcode);

            if (movies != null) {
                List<Movie> movieList = new ArrayList<>();
                // Get movie information from omdb and populate the movie object
                for (String imdbid : movies) {

                    // Retrieve the movie object
                    Movie m = getMovie(imdbid, null, null);

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

            // Post the completed event so the listeners know to update themselves
            EventBus.getDefault().post(new RefreshCompletedEvent(successful, zipcode, movieCount));
            Log.i(TAG, "UpdateThread finished with " + movieCount + " movies in " + (System.currentTimeMillis() - start) + "ms");
        }
    }

    // This returns a list of IMDB id's by parsing html
    private Map<String, MovieData> getMovies(String doc) {
        // Using a map to prevent duplicate movies (since movies can play at multiple theaters)
        Map<String, MovieData> map = new HashMap<>();

        Document html = Jsoup.parse(doc);
        Element body = html.body();

        // Go through each movie element
        Elements movies = body.getElementsByClass("movie");
        for (Element m : movies) {
            // Get the name element for the movie as that contains the movie id
            String name = m.getElementsByClass("name").html();
            try {
                // We need to URL decode the html so we can correctly match the youtube url below
                name = java.net.URLDecoder.decode(name, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.e("URLDecode", "ERROR DECODING HTML");
                e.printStackTrace();
            }

            String movieId = null;
            Matcher matcher = Pattern.compile("mid=([^&]*)").matcher(name);
            if (matcher.find()) {
                movieId = matcher.group(1);
            }

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
            matcher = Pattern.compile("/(tt\\d+)/").matcher(info);
            if (matcher.find()) {
                String imdbid = matcher.group(1);

                // Parse trailer youtube id
                String trailer = null;
                matcher = Pattern.compile("(?:(?:youtu\\.be\\/|v\\/|vi\\/|u\\/\\w\\/|embed\\/)|(?:(?:watch)?\\?v(?:i)?=|\\&v(?:i)?=))([^#\\&\\?]*)").matcher(info);
                if (matcher.find()) {
                    trailer = matcher.group(1);
                }

                MovieData data = new MovieData();
                data.imdb = imdbid;
                data.trailer = trailer;
                data.mid = movieId;

                // Save the movie into our list
                map.put(imdbid, data);
            }
        }

        return map;
    }

    // Returns a movie object from an OMDB API REST call
    private Movie getMovie(final String imdbId, final String trailer, final String movieid) {
        Movie movie = null;
        try {
            String json = NetworkHelper.getMovie(imdbId);

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
        } catch (JSONException e) {
            Log.i("", "Error getting movie_content");
            e.printStackTrace();
        }

        return movie;
    }
}
