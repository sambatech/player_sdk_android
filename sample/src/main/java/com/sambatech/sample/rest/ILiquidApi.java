package com.sambatech.sample.rest;

import com.sambatech.sample.model.LiquidMedia;

import java.util.ArrayList;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;


/**
 * @author Thiago Miranda - 11/01/16
 */
public interface ILiquidApi {

    @Headers({
            "Content-type: application/json"
    })

    @GET("medias")
    Call<ArrayList<LiquidMedia>> getMedias(@Query("cat_id") int categoryId);

}
