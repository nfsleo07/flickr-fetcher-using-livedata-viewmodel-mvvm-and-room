package com.nfstech.flickrfetchermvvm.repo;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.nfstech.flickrfetchermvvm.api.ApiClient;
import com.nfstech.flickrfetchermvvm.api.ApiInterface;
import com.nfstech.flickrfetchermvvm.common.DaoManager;
import com.nfstech.flickrfetchermvvm.common.ResponseToDao;
import com.nfstech.flickrfetchermvvm.common.ResultCallbackListener;
import com.nfstech.flickrfetchermvvm.dao.Photo;
import com.nfstech.flickrfetchermvvm.dao.PhotoDao;
import com.nfstech.flickrfetchermvvm.responsemodel.FlickrRootResponseModel;
import com.nfstech.flickrfetchermvvm.responsemodel.PhotoResponseModel;
import com.nfstech.flickrfetchermvvm.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Manpreet Anand on 1/4/19.
 */
public class MainRepo {

    private static final String LOGGER_TAG = MainRepo.class.getSimpleName();
    private static final String ROOT_PATH = "https://api.flickr.com/";
    private static final String API_KEY = "ADD_API_TOKEN_HERE";
    private static final String FORMAT = "json";
    private PhotoDao mPhotoDao;
    private LiveData<List<Photo>> mLastAddedPhotos = null;
    private final Context mContext;

    public MainRepo(Context context) {
        this.mContext = context;
        mPhotoDao = DaoManager.getInstance(context).getAppDatabase().photoDao();
    }

    public void getPhotosFromWeb(String tag, final ResultCallbackListener<FlickrRootResponseModel, Throwable> listener,
                                 int page, int perPage) {
        Retrofit client = ApiClient.getClient(ROOT_PATH);
        ApiInterface apiInterface = client.create(ApiInterface.class);
        Call<FlickrRootResponseModel> randomPhotos = apiInterface.getRandomPhotos("flickr.photos.search", API_KEY, tag
                , page, perPage, FORMAT, 1);

        Log.d(LOGGER_TAG, "Enqueueing fetch request");
        randomPhotos.enqueue(new Callback<FlickrRootResponseModel>() {
            @Override
            public void onResponse(@NonNull Call<FlickrRootResponseModel> call, @NonNull Response<FlickrRootResponseModel> response) {
                if (response.isSuccessful()) {
                    insertDataInDb(response.body());
                    listener.onSuccess(response.body());
                } else {
                    listener.onFailure(new Throwable("Unable to load data"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<FlickrRootResponseModel> call, @NonNull Throwable throwable) {
                listener.onFailure(throwable);
            }
        });
    }

    private void insertDataInDb(FlickrRootResponseModel body) {
        if (body != null && body.getPhotosResponseModel().getPhotoResponseModel() != null) {
            final List<Photo> daoPhotos = new ArrayList<>();
            List<PhotoResponseModel> photoResponseModel = body.getPhotosResponseModel().getPhotoResponseModel();

            /*
                Since Flickr provides no means to fetch photos from a min_range to max_range, we have to add logic so as to add
                only the newly fetched photos and not the existing ones. So for eg while paginating, we want to fetch the next 50
                photos and we already have 100 photos downloaded, we will create a request to fetch 150 photos. This will contain
                previously fetched 100 photos and 50 new photos. Inorder to handle this case, we have take a sublist of last 50 photos
                and then insert them in the database.
                Math.min is applied to handle the case where we receive less than 50 photos.
             */
            int from = photoResponseModel.size() - Math.min(photoResponseModel.size(), MainViewModel.INCREMENT_COUNT);
            int to = from + Math.min(photoResponseModel.size(), MainViewModel.INCREMENT_COUNT);
            for (PhotoResponseModel item : photoResponseModel.subList(from, to)) {
                daoPhotos.add(ResponseToDao.toDao(item));
            }

            Log.d(LOGGER_TAG, "Inserting records in db:" + daoPhotos.size() + " from: " + from + " to: " + to);
            DaoManager.getInstance(mContext).submitQuery(new Runnable() {
                @Override
                public void run() {
                    mPhotoDao.insertPhoto(daoPhotos);
                }
            });
        }
    }

    public List<Photo> getPhotosInRange(int startIndex, int rangeCount) {
        return mPhotoDao.getPhotosInRange(startIndex, rangeCount);
    }

    public LiveData<List<Photo>> getLastAddedPhotos() {
        if (mLastAddedPhotos == null) {
            mLastAddedPhotos = mPhotoDao.getLastAddedPhotos();
        }

        return mLastAddedPhotos;
    }

    public void deleteAllPhotos() {
        mPhotoDao.deleteAllPhotos();
    }
}
