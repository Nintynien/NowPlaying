package com.skylerbock.nowplaying;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apmem.tools.layouts.FlowLayout;

import java.util.List;
import java.util.Map;

/**
 * Created by sbock on 1/23/16.
 */
public class ShowtimesView extends LinearLayout {

    public ShowtimesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOrientation(VERTICAL);
    }

    public void loadShowtimes(String movieid) {
        // Show spinner while we are loading!
        ProgressBar spinner = new ProgressBar(getContext());
        spinner.setIndeterminate(true);
        this.addView(spinner);

        new ShowtimesTask(this).execute(movieid);
    }

    public void addShowtimes(Map<String, List<String>> result) {
        this.removeAllViews(); // Remove all the views since we're going to readd them all (removes spinner)

        if (result == null) return;

        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            addShowtimesForTheater(entry.getKey(), entry.getValue());
        }

        /*
        if (result.size() == 0)
        {
            // No showtimes found
            TextView view = new TextView(this.getContext());
            view.setTextSize(12);
            view.setText("Showtimes are currently unavailable");
            this.addView(view);
        }
        */
    }

    private void addShowtimesForTheater(String theater, List<String> showtimes) {
        // Create layout params for theater name
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llp.setMargins(0,10,0,10);

        // Create theater textview
        TextView view = new TextView(this.getContext());
        view.setLayoutParams(llp);
        view.setTextSize(20);
        view.setGravity(Gravity.LEFT);
        view.setText(theater);
        this.addView(view);

        // Create flow layout so showtimes wrap correctly
        FlowLayout times = new FlowLayout(getContext());
        times.setOrientation(HORIZONTAL);
        this.addView(times);

        for (String showtime : showtimes) {
            // Create layout params for the showtimes (must be unique for each view)
            FlowLayout.LayoutParams flp = new FlowLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            flp.setMargins(0, 0, 40, 20);

            // Create showtime textview
            view = new TextView(this.getContext());
            view.setLayoutParams(flp);
            view.setTextColor(Color.WHITE);
            view.setTextSize(16);
            view.setText(showtime);
            times.addView(view);
        }
    }
}
