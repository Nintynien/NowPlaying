package com.skylerbock.nowplaying.listing;

import android.content.Context;

import com.hannesdorfmann.mosby.mvp.MvpPresenter;

/**
 * Created by sbock on 12/5/15.
 */
public interface IListingPresenter extends MvpPresenter<IListingView> {
    void loadMovies(final Context context, final boolean pullToRefresh);
    boolean updateData(Context context);
}
