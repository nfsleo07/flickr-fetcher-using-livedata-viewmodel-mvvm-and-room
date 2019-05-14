package com.nfstech.flickrfetchermvvm.common.enums;

import android.support.annotation.StringRes;

import com.nfstech.flickrfetchermvvm.R;

/**
 * Different error cases that need to be handled on the view.
 * <p>
 * Created by Manpreet Anand on 16/4/19.
 */
public enum ErrorEnum {
    UNABLE_TO_FETCH_DATA(R.string.error_unable_fetch_data),
    INVALID_DATA(R.string.error_invalid_data);

    @StringRes
    int mErrorResource;

    ErrorEnum(int errorResource) {
        this.mErrorResource = errorResource;
    }

    @StringRes
    public int getErrorResource() {
        return this.mErrorResource;
    }
}
