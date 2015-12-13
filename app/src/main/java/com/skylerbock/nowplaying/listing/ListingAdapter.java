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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listing, parent, false);
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