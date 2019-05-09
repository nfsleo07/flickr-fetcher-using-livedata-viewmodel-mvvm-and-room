package com.example.livedataroomlearning.common;

import android.support.annotation.Nullable;

import com.example.livedataroomlearning.dao.Photo;
import com.example.livedataroomlearning.view.model.PhotoListModel;

/**
 * Created by Manpreet Anand on 9/4/19.
 */
public class DaoToUi {

    @Nullable
    public static PhotoListModel toUi(Photo photo) {
        PhotoListModel photoListModel = null;
        if (photo != null) {
            photoListModel = new PhotoListModel();
            photoListModel.setUrl(photo.getPhotoUrl());
            photoListModel.setId(photo.getPhotoId());
        }

        return photoListModel;
    }
}
