package com.example.livedataroomlearning.responsemodel;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Manpreet Anand on 27/3/19.
 */
public class FlickrRootResponseModel {
    @SerializedName("photos")
    private PhotosResponseModel mPhotosResponseModel;

    public PhotosResponseModel getPhotosResponseModel() {
        return mPhotosResponseModel;
    }

    public void setPhotosResponseModel(PhotosResponseModel photosResponseModel) {
        this.mPhotosResponseModel = photosResponseModel;
    }
}
