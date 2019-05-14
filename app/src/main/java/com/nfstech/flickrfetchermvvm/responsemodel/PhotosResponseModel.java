package com.nfstech.flickrfetchermvvm.responsemodel;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Manpreet Anand on 27/3/19.
 */
public class PhotosResponseModel {
    @SerializedName("page")
    private Integer page;
    @SerializedName("pages")
    private Integer pages;
    @SerializedName("perpage")
    private Integer perpage;
    @SerializedName("total")
    private String total;
    @SerializedName("photo")
    private List<PhotoResponseModel> mPhotoResponseModel = null;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public Integer getPerpage() {
        return perpage;
    }

    public void setPerpage(Integer perpage) {
        this.perpage = perpage;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public List<PhotoResponseModel> getPhotoResponseModel() {
        return mPhotoResponseModel;
    }

    public void setPhotoResponseModel(List<PhotoResponseModel> photoResponseModel) {
        this.mPhotoResponseModel = photoResponseModel;
    }
}
