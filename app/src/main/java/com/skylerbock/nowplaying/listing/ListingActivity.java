package com.skylerbock.nowplaying.listing;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;

import com.hannesdorfmann.mosby.mvp.viewstate.lce.LceViewState;
import com.hannesdorfmann.mosby.mvp.viewstate.lce.MvpLceViewStateActivity;
import com.hannesdorfmann.mosby.mvp.viewstate.lce.data.CastedArrayListLceViewState;
import com.skylerbock.nowplaying.BuildConfig;
import com.skylerbock.nowplaying.GridAutofitLayoutManager;
import com.skylerbock.nowplaying.R;
import com.skylerbock.nowplaying.movie.Movie;
import com.skylerbock.nowplaying.movie.MovieActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class ListingActivity extends MvpLceViewStateActivity<SwipeRefreshLayout, List<Movie>, IListingView, IListingPresenter>
        implements IListingView, SwipeRefreshLayout.OnRefreshListener, ListingAdapter.ViewHolderPress {

    @Bind(R.id.rv) RecyclerView rv;
    ListingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        if (BuildConfig.DEBUG) {
            Picasso.with(getApplicationContext()).setIndicatorsEnabled(true);
            //Picasso.with(getApplicationContext()).setLoggingEnabled(true);
        }

        contentView.setOnRefreshListener(this);

        adapter = new ListingAdapter(this);
        int columnWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 170, getResources().getDisplayMetrics());
        rv.setLayoutManager(new GridAutofitLayoutManager(this, columnWidth));
        rv.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @NonNull
    @Override
    public IListingPresenter createPresenter() {
        return new ListingPresenter();
    }

    @NonNull
    @Override
    public LceViewState<List<Movie>, IListingView> createViewState() {
        return new CastedArrayListLceViewState<>();
    }

    @Override
    protected String getErrorMessage(Throwable e, boolean pullToRefresh) {
        return "An error occurred.";
    }

    @Override
    public List<Movie> getData() {
        return adapter == null ? null : adapter.getMovies();
    }

    @Override
    public void setData(List<Movie> data) {
        adapter.setMovies(data);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void loadData(boolean pullToRefresh) {
        presenter.loadMovies(this, pullToRefresh);
    }

    @Override
    public void onRefresh() {
        loadData(true);
    }

    @Override
    public void showContent() {
        super.showContent();
        contentView.setRefreshing(false);
    }

    @Override
    public void showError(Throwable e, boolean pullToRefresh) {
        super.showError(e, pullToRefresh);
        contentView.setRefreshing(false);
    }

    @Override
    public void onItemPress(Movie movie, ListingAdapter.ViewHolder viewHolder) {
        // User pressed a movie, opening a movie activity that shows the detailed information
        Intent intent = new Intent(this, MovieActivity.class);
        intent.putExtra("movie_content", movie);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, viewHolder.poster, "poster"); // Poster animation
        startActivity(intent, options.toBundle());
    }

    // Event that's fired when the background refresh is completed
    public void onEventMainThread(ListingModel.RefreshCompleted value) {
        // Refresh completed, refresh our shown data
        presenter.updateData(this);
    }
}
