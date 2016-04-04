package com.skylerbock.nowplaying;

import android.os.AsyncTask;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sbock on 4/3/16.
 */
public class ShowtimesTask extends AsyncTask<String, Void, Map<String, List<String>>> {

    ShowtimesView owner = null;

    public ShowtimesTask(ShowtimesView destination) {
        owner = destination;
    }

    @Override
    protected Map<String, List<String>> doInBackground(String... params) {
        Map<String, List<String>> data = new HashMap<>();

        String id = params[0];
        if (id == null) {
            // No movie id passed in, nothing to do
            return null;
        }

        Document doc = loadPage(id);
        if (doc == null) {
            // Failed to load showtimes (Unable to get page from network)
            return data;
        }

        Element showtimes = doc.select(".movie_results .movie .showtimes").first();
        if (showtimes != null) {
            Elements theaters = showtimes.select(".theater");

            if (theaters != null) {
                for (Element e : theaters) {

                    // Get theater name
                    String name = e.getElementsByClass("name").text();

                    // Get theater showtimes
                    List<String> times = Arrays.asList(parseShowtimes(e));

                    data.put(name, times);
                }
            }
        }
        return data;
        /*
        for (int i = 0; i < 5; i++) {
            switch (i) {
                case 1:
                    data.put("Test Theater", Arrays.asList("1:10", "2:30", "2:45", "5:00", "5:30", "7:15", "7:45"));
                    break;
                case 2:
                    data.put("Test Theater Again", Arrays.asList("1:10", "2:30", "2:45"));
                    break;
                case 3:
                    data.put("Mercado 20", Arrays.asList("1:10", "2:30", "2:45", "5:00", "5:30", "7:15", "7:45", "9:30", "10:00", "11:30", "11:45"));
                    break;
                default:
            }
            // Sleep for slow connection!
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
        return data;
        */
    }

    @Override
    protected void onPostExecute(Map<String, List<String>> result) {
        if (owner != null) {
            owner.addShowtimes(result);
        }
    }

    private Document loadPage(String movieId) {
        Document doc = null;
        OkHttpClient client = new OkHttpClient();
        // like http://www.google.com/movies?mid=3db64253dbf295f3
        String url = "http://google.com/movies?mid=" + movieId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String body = response.body().string();
                doc = Jsoup.parse(body);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

    /**
     * Returns the showtimes contained in the element
     */
    private String[] parseShowtimes(Element body) {
        boolean morning = false;
        String[] showtimes = body.getElementsByClass("times").text().split(" ");

        // Google displays showtimes like "10:00  11:20am  1:00  2:20  4:00  5:10  6:50  8:10  9:40  10:55pm". Since
        // they don't always apply am/pm to times, we need to run through the showtimes in reverse and then apply the
        // previous (later) meridiem to the next (earlier) movie showtime so we end up with something like
        // ["10:00am", "11:20am", "1:00pm", ...].
        for (int i = showtimes.length-1; i > 0; i--) {
            String showtime = showtimes[i];
            if (showtime.contains("pm")) {
                // Showtime already contains pm (save it for the next showtime)
                morning = false;
            }
            else if (showtimes[i].contains("am")) {
                // Showtime already contains am (save it for the next showtime)
                morning = true;
            }
            else {
                // Add ending am/pm to the showtime (we know which one based on the previous showtime)
                showtime = showtime + (morning ? "am" : "pm");
            }

            showtimes[i] = showtime;
        }

        return showtimes;
    }
}