package com.example.livedataroomlearning.api;

import com.example.livedataroomlearning.responsemodel.FlickrRootResponseModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Manpreet Anand on 27/3/19.
 */
public interface ApiInterface {

    @GET("services/rest")
    Call<FlickrRootResponseModel> getRandomPhotos(@Query("method") String method, @Query("api_key") String apiKey,
                                                  @Query("text") String text, @Query("page") int page,
                                                  @Query("per_page") int perPage, @Query("format") String format,
                                                  @Query("nojsoncallback") int noJsonCallback);
}
