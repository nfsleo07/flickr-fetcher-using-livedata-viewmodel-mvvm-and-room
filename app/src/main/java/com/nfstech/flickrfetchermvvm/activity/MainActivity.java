package com.nfstech.flickrfetchermvvm.activity;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.nfstech.flickrfetchermvvm.R;
import com.nfstech.flickrfetchermvvm.adapter.PhotoGridAdapter;
import com.nfstech.flickrfetchermvvm.common.FlexLayoutScrollListener;
import com.nfstech.flickrfetchermvvm.common.enums.ErrorEnum;
import com.nfstech.flickrfetchermvvm.view.model.PhotoListModel;
import com.nfstech.flickrfetchermvvm.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manpreet Anand on 28/3/19.
 */
public class MainActivity extends AppCompatActivity {

    private static final String LOGGER_TAG = MainActivity.class.getSimpleName();
    private MainViewModel mMainViewModel;
    private PhotoGridAdapter mPhotoGridAdapter;
    private int mInitialPhotoRange = 0;
    private boolean mIgnoreFetching = false;
    private ProgressBar mProgressBar = null;
    private EditText mEtSearchString;

    private Runnable mResetFetchingFlag = new Runnable() {
        @Override
        public void run() {
            if (!isFinishing()) {
                mIgnoreFetching = false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        mProgressBar = findViewById(R.id.progress);
        Button searchButton = findViewById(R.id.btn_search);
        mEtSearchString = findViewById(R.id.et_search);
        final RecyclerView recyclerView = findViewById(R.id.rv_photo_grid);

        mEtSearchString.clearFocus();
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFocus();
                mMainViewModel.onSearchTapped(mEtSearchString.getText().toString());
            }
        });

        //Observer to listen to different Errors
        mMainViewModel.getError().observe(this, new Observer<ErrorEnum>() {
            @Override
            public void onChanged(@Nullable ErrorEnum errorEnum) {
                if (errorEnum != null) {
                    switch (errorEnum) {
                        case UNABLE_TO_FETCH_DATA:
                            Toast.makeText(MainActivity.this, errorEnum.getErrorResource(), Toast.LENGTH_SHORT).show();
                            break;
                        case INVALID_DATA:
                            mEtSearchString.setError(getString(errorEnum.getErrorResource()));
                            break;
                    }
                }
            }
        });

        //Flexbox setup to Photos grid
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setAlignItems(AlignItems.STRETCH);
        recyclerView.setLayoutManager(layoutManager);
        List<PhotoListModel> photos = new ArrayList<>();
        mPhotoGridAdapter = new PhotoGridAdapter(photos);
        recyclerView.setAdapter(mPhotoGridAdapter);

        //Observer for showing/hiding progress bar while fetching data from web
        mMainViewModel.getLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean showLoading) {
                mProgressBar.setVisibility(showLoading == Boolean.TRUE ? View.VISIBLE : View.INVISIBLE);
            }
        });

        //Observer for photos fetched via web or DB
        mMainViewModel.getPhotos().observe(this, new Observer<List<PhotoListModel>>() {
            @Override
            public void onChanged(@Nullable List<PhotoListModel> photoListModels) {
                if (photoListModels != null) {
                    if (photoListModels.isEmpty()) {
                        /*
                            Empty data set indicates clearing of exiting data set of the adapter i.e show a blank list
                            which will happen when a search button is tapped as we have to clear existing data before showing new data
                        */
                        mPhotoGridAdapter.setDataSource(new ArrayList<PhotoListModel>());
                        mPhotoGridAdapter.notifyDataSetChanged();
                        mInitialPhotoRange = 0;
                    } else {
                        /*
                            We receive a full data set of all the photos that have been fetched from the web or DB. We could have returned only
                            the most recently fetched data but we have to handle the case where the phone rotates and we have show all the existing
                            photos. This is the reason that the MainViewModel is keeping all the photos fetched.
                         */
                        List<PhotoListModel> subList = photoListModels.subList(mInitialPhotoRange, mInitialPhotoRange + (photoListModels.size() - mInitialPhotoRange));
                        mPhotoGridAdapter.addPhotos(subList);
                        mPhotoGridAdapter.notifyItemRangeChanged(mInitialPhotoRange, subList.size());
                        Log.d(LOGGER_TAG, "SubList Size:" + subList.size() + " start:" + mInitialPhotoRange + " total:" + mPhotoGridAdapter.getItemCount());
                        mInitialPhotoRange = mInitialPhotoRange + (photoListModels.size() - mInitialPhotoRange);
                    }
                }
            }
        });

        recyclerView.addOnScrollListener(new FlexLayoutScrollListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                if (!mIgnoreFetching) {
                    mIgnoreFetching = true;
                    new Handler(Looper.getMainLooper()).postDelayed(mResetFetchingFlag, 500);
                    Log.d(LOGGER_TAG, "Load more items called");
                    mMainViewModel.fetchPhotos();
                }
            }

            @Override
            public boolean isLastPage() {
                return mMainViewModel.isLastPageFetched();
            }

            @Override
            public boolean isLoading() {
                return mMainViewModel.isLoading();
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy != 0) {
                    clearFocus();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void clearFocus() {
        mEtSearchString.clearFocus();
        Window currentWindow = getWindow();
        if (currentWindow != null) {
            //Hide the keyboard on launch or scroll
            View currentFocus = currentWindow.getDecorView();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }
}
