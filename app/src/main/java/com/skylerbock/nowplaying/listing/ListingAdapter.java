package com.skylerbock.nowplaying.listing;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.skylerbock.nowplaying.movie.Movie;
import com.skylerbock.nowplaying.R;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by sbock on 12/5/15.
 */
public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(R.id.poster) public ImageView poster;
        @Bind(R.id.title) public TextView title;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null)
                listener.onItemPress(movies.get(getAdapterPosition()), this);
        }
    }

    // ViewHolder click callbacks
    public interface ViewHolderPress {
        void onItemPress(Movie movie, ViewHolder viewHolder);
    }

    private List<Movie> movies = null;
    private ViewHolderPress listener = null;

    public ListingAdapter(ViewHolderPress listener) {
        this.listener = listener;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public enum SortType {
        Title,
        Rating
    }

    public void sortMovies(SortType type) {
        sortMovies(type, true);
    }

    public void sortMovies(SortType type, final boolean descending) {
        switch (type) {
            case Title:
                Collections.sort(movies, new Comparator<Movie>() {
                    @Override
                    public int compare(Movie lhs, Movie rhs) {
                        if (descending)
                            return lhs.getTitle().compareTo(rhs.getTitle());
                        else
                            return rhs.getTitle().compareTo(lhs.getTitle());
                    }
                });
                this.notifyDataSetChanged();
                break;
            case Rating:
                Collections.sort(movies, new Comparator<Movie>() {
                    @Override
                    public int compare(Movie lhs, Movie rhs) {
                        float lRate = 0;
                        float rRate = 0;
                        // IMDB ratings just have 1 decimal place, so we're just making it more significant so it's not lost in the typecast
                        try {
                            lRate = Float.parseFloat(lhs.getImdbRating())*10;
                        } catch (Exception e) {
                            //ignore
                        }
                        try {
                            rRate = Float.parseFloat(rhs.getImdbRating())*10;
                        } catch (Exception e) {
                            //ignore
                        }

                        if (descending)
                            return (int)(rRate - lRate);
                        else
                            return (int)(lRate - rRate);
                    }
                });
                this.notifyDataSetChanged();
                break;
            default:
                // Don't sort
                break;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Movie movie = movies.get(position);
        Picasso.with(holder.poster.getContext())
                .load(movie.getPosterUrl())
                .placeholder(R.drawable.placeholder_poster)
                .error(R.drawable.error_poster)
                .into(holder.poster);
        holder.title.setText(movie.getTitle());
    }

    @Override
    public int getItemCount() {
        return movies == null ? 0 : movies.size();
    }
}