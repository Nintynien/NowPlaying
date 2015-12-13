package com.skylerbock.nowplaying.listing;

import android.content.Context;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;
import com.skylerbock.nowplaying.DBHelper;
import com.skylerbock.nowplaying.movie.Movie;

import java.util.List;

/**
 * Created by sbock on 12/5/15.
 */
public class ListingPresenter extends MvpBasePresenter<IListingView> implements IListingPresenter {
    @Override
    // This is a hard refresh that retrieves information from the network in a background thread
    public void loadMovies(Context context, boolean pullToRefresh) {
        getView().showLoading(pullToRefresh);
        new ListingModel().updateDatabase(context, null);
    }

    @Override
    // This is a soft refresh that doesn't go to the network (only gets values from the database)
    public void updateData(Context context) {
        List<Movie> results = DBHelper.getMovies(context);

        if (isViewAttached() && !results.isEmpty()) {
            IListingView v = getView();
            if (v != null) {
                v.setData(results);
                v.showContent();
            }
        }
    }
}
