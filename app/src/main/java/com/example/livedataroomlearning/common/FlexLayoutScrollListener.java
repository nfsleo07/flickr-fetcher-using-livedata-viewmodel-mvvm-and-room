package com.example.livedataroomlearning.common;

import com.google.android.flexbox.FlexboxLayoutManager;

/**
 * Created by Manpreet Anand on 4/4/19.
 */
public abstract class FlexLayoutScrollListener extends PaginationScrollListener {

    public FlexLayoutScrollListener(FlexboxLayoutManager layoutManager) {
        super(layoutManager);
    }

    @Override
    public int getFirstVisibleItemPosition() {
        return ((FlexboxLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
    }
}
