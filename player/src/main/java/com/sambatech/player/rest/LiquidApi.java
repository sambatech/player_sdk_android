package com.sambatech.player.rest;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by tmiranda on 11/01/16.
 */
public class LiquidApi {

    static String test_endpoint = "http://192.168.0.179:3000/medias/";
    static String api_endpoint = "http://198.101.153.219:8091/v1/";
    static String api_key = "ecae833f-979e-4856-af7b-ab335d2d0e61"; //progressive //pid=
    //static String api_key = "8a3f2dac516c17e0c115fc82acda2f19"; //hls

    static public ILiquidApi getApi() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient httpClient = new OkHttpClient();

        httpClient.interceptors().add(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(api_endpoint)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build();

        ILiquidApi service = retrofit.create(ILiquidApi.class);

        return service;

    }

}
