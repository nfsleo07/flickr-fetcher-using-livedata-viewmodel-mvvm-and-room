package com.example.livedataroomlearning.common;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;

/**
 * Created by Manpreet Anand on 4/4/19.
 */
abstract public class PaginationScrollListener extends RecyclerView.OnScrollListener {
    protected RecyclerView.LayoutManager mLayoutManager;

    public PaginationScrollListener(FlexboxLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
    }

    @CallSuper
    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = mLayoutManager.getChildCount();
        int totalItemCount = mLayoutManager.getItemCount();
        int firstVisibleItemPosition = getFirstVisibleItemPosition();

        if (!isLoading() && !isLastPage()) {
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                loadMoreItems();
            }
        }
    }

    protected abstract void loadMoreItems();

    public abstract boolean isLastPage();

    public abstract boolean isLoading();

    public abstract int getFirstVisibleItemPosition();
}
