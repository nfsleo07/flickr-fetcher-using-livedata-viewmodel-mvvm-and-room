package com.nfstech.flickrfetchermvvm.common;

import android.app.Application;

/**
 * Created by Manpreet Anand on 5/4/19.
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //Uncomment below line if you want to use Stetho (https://github.com/facebook/stetho)
        //Stetho.initializeWithDefaults(this);
    }
}
