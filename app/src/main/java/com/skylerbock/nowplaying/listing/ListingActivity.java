package com.skylerbock.nowplaying.listing;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.hannesdorfmann.mosby.mvp.viewstate.lce.LceViewState;
import com.hannesdorfmann.mosby.mvp.viewstate.lce.MvpLceViewStateActivity;
import com.hannesdorfmann.mosby.mvp.viewstate.lce.data.CastedArrayListLceViewState;
import com.skylerbock.nowplaying.AppPreferences;
import com.skylerbock.nowplaying.BuildConfig;
import com.skylerbock.nowplaying.GridAutofitLayoutManager;
import com.skylerbock.nowplaying.R;
import com.skylerbock.nowplaying.movie.Movie;
import com.skylerbock.nowplaying.movie.MovieActivity;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class ListingActivity extends MvpLceViewStateActivity<SwipeRefreshLayout, List<Movie>, IListingView, IListingPresenter>
        implements IListingView, SwipeRefreshLayout.OnRefreshListener, ListingAdapter.ViewHolderPress {

    @Bind(R.id.rv) RecyclerView rv;
    ListingAdapter adapter;
    Snackbar snackbar;

    public static final long warning_threshold = 1000 * 60 * 60 * 24; // 1 day in milliseconds

    private boolean sortDescending = true; // Default sort descending
    private ListingAdapter.SortType sortLastType = ListingAdapter.SortType.Rating; // Default sort by rating
    private boolean viewGrid = true; // Default to grid view

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

        setView(false);
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

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_listing, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort:
                // Popup menu for user to select sort option (pressing again toggles sort direction)
                View menuItemView = findViewById(R.id.sort);
                PopupMenu popupMenu = new PopupMenu(this, menuItemView);
                popupMenu.inflate(R.menu.menu_sort_options);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.sort_option_name:
                                if (sortLastType == ListingAdapter.SortType.Title) {
                                    sortDescending = !sortDescending;
                                }
                                else {
                                    sortLastType = ListingAdapter.SortType.Title;
                                    sortDescending = true;
                                }
                                adapter.sortMovies(ListingAdapter.SortType.Title, sortDescending);
                                return true;
                            case R.id.sort_option_rating:
                                if (sortLastType == ListingAdapter.SortType.Rating) {
                                    sortDescending = !sortDescending;
                                }
                                else {
                                    sortLastType = ListingAdapter.SortType.Rating;
                                    sortDescending = true;
                                }
                                adapter.sortMovies(ListingAdapter.SortType.Rating, sortDescending);
                                return true;
                            default:
                        }
                        return false;
                    }
                });
                popupMenu.show();
                return true;
            case R.id.view:
                // Toggle view mode (grid vs list)
                if (viewGrid) {
                    // Switch to grid, so show list icon
                    item.setIcon(R.drawable.ic_view_list_white_24dp);
                }
                else {
                    // Switch to list, so show grid icon
                    item.setIcon(R.drawable.ic_view_module_white_24dp);
                }
                setView(viewGrid);
                viewGrid = !viewGrid;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setView(boolean grid) {
        List<Movie> data = getData();
        RecyclerView.LayoutManager mgr = null;
        if (grid)
        {
            adapter = new ListingAdapter(this, R.layout.item_grid);
            int columnWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 170, getResources().getDisplayMetrics());
            mgr = new GridAutofitLayoutManager(this, columnWidth);
        }
        else
        {
            adapter = new ListingAdapter(this, R.layout.item_listing);
            mgr = new GridLayoutManager(this, 1);
        }
        setData(data);
        rv.setLayoutManager(mgr);
        rv.setAdapter(adapter);
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
        if (adapter == null) return;

        adapter.setMovies(data);
        adapter.notifyDataSetChanged();
        adapter.sortMovies(sortLastType, sortDescending); // Sort using the type that we have set
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

        // Show banner warning user of old data
        Date lastUpdated = new AppPreferences(this).getKeyPrefsLastUpdated();
        // Check to see if the time since the last update was over our threshold
        if ((System.currentTimeMillis() - lastUpdated.getTime()) > warning_threshold) {
            snackbar = Snackbar.make(rv, "Last updated " + new PrettyTime().format(lastUpdated), Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }
        else
        {
            // Dismiss any snackbar that might be showing
            if (snackbar != null)
                snackbar.dismiss();
        }
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
    public void onEventMainThread(ListingModel.RefreshCompletedEvent value) {
        // Refresh completed, refresh our shown data
        presenter.updateData(this);
    }
}
