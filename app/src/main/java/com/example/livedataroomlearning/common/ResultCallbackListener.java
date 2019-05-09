package com.example.livedataroomlearning.common;

/**
 * Common request/response listener.
 * <p>
 * Created by Manpreet Anand on 3/4/19.
 */
public interface ResultCallbackListener<R, E> {
    void onSuccess(R response);

    void onFailure(E error);
}
