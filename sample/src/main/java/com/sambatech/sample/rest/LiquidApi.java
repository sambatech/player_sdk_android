package com.sambatech.sample.rest;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by tmiranda on 11/01/16.
 */
public class LiquidApi {

    static public ILiquidApi getApi(String endpoint) {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        OkHttpClient httpClient = new OkHttpClient();

        httpClient.interceptors().add(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build();

        ILiquidApi service = retrofit.create(ILiquidApi.class);

        return service;

    }

}
