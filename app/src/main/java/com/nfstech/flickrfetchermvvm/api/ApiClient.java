package com.nfstech.flickrfetchermvvm.api;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Manpreet Anand on 27/3/19.
 */
public class ApiClient {
    private static Map<String, Retrofit> mUrlToClientMap = new HashMap<>();

    /**
     * Returns a {@link Retrofit} client based on the <code>rootPath</code> passed. This method also checks if a client already exists
     * for the given <code>rootPath</code>. If it already exists, the old instance is returned else a newly created client is returned.
     *
     * @param rootPath Root path of the URL to create a Retrofit client for
     * @return {@link Retrofit} client for the passed root path
     */
    public static Retrofit getClient(String rootPath) {
        Retrofit clientToReturn = mUrlToClientMap.get(rootPath);
        if (clientToReturn == null) {
            /*
                This interceptor is added if you are using the Stetho library. Please refer to
                https://github.com/facebook/stetho for more information.
            */
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addNetworkInterceptor(new StethoInterceptor())
                    .build();
            Retrofit client = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .baseUrl(rootPath)
                    .build();
            clientToReturn = client;
            mUrlToClientMap.put(rootPath, client);
        }

        return clientToReturn;
    }
}
