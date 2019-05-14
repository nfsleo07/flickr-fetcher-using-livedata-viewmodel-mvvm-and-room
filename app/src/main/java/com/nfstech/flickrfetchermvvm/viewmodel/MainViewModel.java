package com.nfstech.flickrfetchermvvm.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nfstech.flickrfetchermvvm.common.DaoManager;
import com.nfstech.flickrfetchermvvm.common.DaoToUi;
import com.nfstech.flickrfetchermvvm.common.ResultCallbackListener;
import com.nfstech.flickrfetchermvvm.common.enums.ErrorEnum;
import com.nfstech.flickrfetchermvvm.dao.Photo;
import com.nfstech.flickrfetchermvvm.repo.MainRepo;
import com.nfstech.flickrfetchermvvm.responsemodel.FlickrRootResponseModel;
import com.nfstech.flickrfetchermvvm.view.model.PhotoListModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manpreet Anand on 1/4/19.
 */
public class MainViewModel extends AndroidViewModel {

    private static final String LOGGER_TAG = MainViewModel.class.getSimpleName();
    public static final int INCREMENT_COUNT = 50;
    private static final int MAX_PER_PAGE_COUNT = 500;
    private static final String DEFAULT_SEARCH_STRING = "teee"; //Default search string. It returns some really nice images ;)

    private int mCurrentPage = 1;
    private int mTotalPage = 0;
    private int mCurrentPerPageCount = 0;

    private int mStartIndex = 0;

    private MutableLiveData<List<PhotoListModel>> mPhotos;
    private MutableLiveData<Boolean> mShowLoading;
    private MutableLiveData<ErrorEnum> mError;
    private MainRepo mMainModel;

    private String mSearchString = null;
    private Observer<List<Photo>> mObserver;

    public MainViewModel(@NonNull Application application) {
        super(application);
        mMainModel = new MainRepo(application);
    }

    public MutableLiveData<List<PhotoListModel>> getPhotos() {
        if (mPhotos == null) {
            mPhotos = new MutableLiveData<>();
            Log.d(LOGGER_TAG, "Fetching photos with default tag");
            fetchPhotosWithDefaultTag();
        }

        return mPhotos;
    }

    private void setPhotos(List<PhotoListModel> photos) {
        if (photos != null) {
            List<PhotoListModel> currentPhotos = mPhotos.getValue();
            if (currentPhotos != null && !currentPhotos.isEmpty()) {
                currentPhotos.addAll(photos);
                mPhotos.postValue(currentPhotos);
            } else {
                mPhotos.postValue(photos);
            }
        } else {
            mPhotos.postValue(new ArrayList<PhotoListModel>());
        }
    }

    public LiveData<Boolean> getLoading() {
        if (mShowLoading == null) {
            mShowLoading = new MutableLiveData<>();
        }

        return mShowLoading;
    }

    private void setLoading(boolean isLoading) {
        mShowLoading.postValue(isLoading);
    }

    public MutableLiveData<ErrorEnum> getError() {
        if (mError == null) {
            mError = new MutableLiveData<>();
        }

        return mError;
    }

    private void setError(ErrorEnum error) {
        if (mError != null) {
            mError.postValue(error);
        }
    }

    private void fetchPhotosWithDefaultTag() {
        mSearchString = DEFAULT_SEARCH_STRING;
        fetchPhotos();
    }

    public void onSearchTapped(String searchString) {
        if (isSearchStringInValid(searchString)) {
            setError(ErrorEnum.INVALID_DATA);
        } else {
            mSearchString = searchString;
            resetPaginationFlags();
            setPhotos(null);
            DaoManager.getInstance(getApplication()).submitQuery(new Runnable() {
                @Override
                public void run() {
                    mMainModel.deleteAllPhotos();
                }
            });
            fetchPhotosFromWeb();
        }
    }

    private boolean isSearchStringInValid(String searchString) {
        return searchString == null || searchString.isEmpty();
    }

    private void resetPaginationFlags() {
        mCurrentPage = 1;
        mTotalPage = 0;
        mCurrentPerPageCount = 0;
        mStartIndex = 0;
    }

    /**
     * This method gets called when new photos from web are inserted in the DB.
     */
    private void setupListenerForLastAddedPhotos() {
        if (mObserver == null) {
            mObserver = new Observer<List<Photo>>() {
                @Override
                public void onChanged(@Nullable List<Photo> photos) {
                    if (photos != null && !photos.isEmpty()) {
                        List<PhotoListModel> photoListModels = new ArrayList<>();
                        for (Photo daoItem : photos) {
                            photoListModels.add(DaoToUi.toUi(daoItem));
                        }
                        Log.d(LOGGER_TAG, "Fetched rows from web " + photoListModels.size());
                        setPhotos(photoListModels);
                    }
                }
            };
            mMainModel.getLastAddedPhotos().observeForever(mObserver);
        }
    }

    public void fetchPhotos() {
        DaoManager.getInstance(getApplication()).submitQuery(
                new Runnable() {
                    @Override
                    public void run() {
                        //First try and fetch photos from DB
                        List<Photo> photos = mMainModel.getPhotosInRange(mStartIndex, INCREMENT_COUNT);
                        onDbPhotosReceived(photos);
                    }
                });
    }

    private void onDbPhotosReceived(List<Photo> daoPhotos) {
        if (daoPhotos == null || daoPhotos.isEmpty()) {
            //No photos received from DB, now fetch them from web
            fetchPhotosFromWeb();
        } else {
            List<PhotoListModel> photoListModels = new ArrayList<>();
            for (Photo daoItem : daoPhotos) {
                photoListModels.add(DaoToUi.toUi(daoItem));
            }
            Log.d(LOGGER_TAG, "Fetched rows from db " + photoListModels.size());
            setPhotos(photoListModels);
        }

        mStartIndex += INCREMENT_COUNT;
    }

    private void fetchPhotosFromWeb() {
        if (!isLoading()) {
            setupListenerForLastAddedPhotos();
            setLoading(true);
            if (mCurrentPerPageCount == MAX_PER_PAGE_COUNT) {
                mCurrentPerPageCount = INCREMENT_COUNT;
                mCurrentPage += 1;
            } else {
                mCurrentPerPageCount += INCREMENT_COUNT;
            }
            mMainModel.getPhotosFromWeb(mSearchString, new ResultCallbackListener<FlickrRootResponseModel, Throwable>() {
                @Override
                public void onSuccess(FlickrRootResponseModel response) {
                    setLoading(false);
                    mTotalPage = response.getPhotosResponseModel().getPages();
                }

                @Override
                public void onFailure(Throwable error) {
                    setLoading(false);
                    setError(ErrorEnum.UNABLE_TO_FETCH_DATA);
                }
            }, mCurrentPage, mCurrentPerPageCount);
        }
    }

    public boolean isLastPageFetched() {
        return mCurrentPage == mTotalPage;
    }

    public boolean isLoading() {
        return mShowLoading != null && (mShowLoading.getValue() != null ? mShowLoading.getValue() : false);
    }

    @Override
    protected void onCleared() {
        mMainModel.getLastAddedPhotos().removeObserver(mObserver);
    }
}
