package com.skylerbock.nowplaying.movie;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hannesdorfmann.mosby.mvp.MvpActivity;
import com.skylerbock.nowplaying.R;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MovieActivity extends MvpActivity<IMovieView, IMoviePresenter> {
    @Bind(R.id.poster) ImageView header;
    @Bind(R.id.fab) FloatingActionButton fab;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.year) TextView year;
    @Bind(R.id.mpaa_rating) TextView mpaa;
    @Bind(R.id.name) TextView name;
    @Bind(R.id.genre) TextView genre;
    @Bind(R.id.director) TextView director;
    @Bind(R.id.cast) TextView cast;
    @Bind(R.id.duration) TextView duration;
    @Bind(R.id.stars) TextView stars;
    @Bind(R.id.plot) TextView plot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        Intent i = getIntent();
        final Movie movie = i.getParcelableExtra("movie_content");

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        // Header image
        Picasso.with(this)
                .load(movie.getPosterUrl())
                .into(header);

        year.setText(movie.getYear());
        mpaa.setText(movie.getMpaaRating());
        name.setText(movie.getTitle());
        genre.setText(movie.getGenre());
        director.setText(movie.getDirector());
        cast.setText(movie.getCast());
        duration.setText(movie.getDuration());
        stars.setText(movie.getImdbRating());
        plot.setText(movie.getPlot());

        // Show or hide the fab depending on if there's a trailer
        final String trailer = movie.getTrailerUrl();
        if (trailer != null) {
            fab.show();
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    watchYoutubeVideo(trailer);
                }
            });
        } else {
            // In order to hide the fab, we need to remove it's anchor
            CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
            p.setAnchorId(View.NO_ID);
            fab.setLayoutParams(p);
            fab.hide();
        }
    }

    // Takes youtube ID or youtube url
    public void watchYoutubeVideo(String id) {
        boolean idOnly = true;

        if (id == null || id.isEmpty()) {
            Toast.makeText(this, "Movie trailer not available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (id.toLowerCase().contains("youtube"))
        {
            idOnly = false;
        }

        // If just the ID is passed, we can attempt to open the youtube app
        if (idOnly)
        {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
                startActivity(intent);
                return;
            } catch (ActivityNotFoundException ex) {
                // Try just sending the URL
            }
        }

        Uri video;
        if (!idOnly)
            video = Uri.parse(id);
        else
            video = Uri.parse("http://www.youtube.com/watch?v=" + id);

        Intent intent=new Intent(Intent.ACTION_VIEW, video);
        startActivity(intent);
    }

    @NonNull
    @Override
    public IMoviePresenter createPresenter() {
        return new MoviePresenter();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
