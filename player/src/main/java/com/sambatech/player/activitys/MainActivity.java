package com.sambatech.player.activitys;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.sambatech.player.R;
import com.sambatech.player.adapters.MediasAdapter;
import com.sambatech.player.model.LiquidMedia;
import com.sambatech.player.rest.LiquidApi;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import retrofit.Call;
import retrofit.Response;

public class MainActivity extends Activity {

	@Bind(R.id.media_list) ListView list;
    @BindString(R.string.player_endpoint) String player_endpoint;

    //Medias Adapter
    MediasAdapter mAdapter;

    ArrayList<LiquidMedia> mediaList;

	@OnItemClick(R.id.media_list) public void mediaItemClick(int position) {
		LiquidMedia media = (LiquidMedia) mAdapter.getItem(position);
		Toast.makeText(this, "clicado no item " + media.title, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        Call<ArrayList<LiquidMedia>> call = LiquidApi.getApi().getMedias("ecae833f-979e-4856-af7b-ab335d2d0e61", 533);

        try {
            Response<ArrayList<LiquidMedia>> response = call.execute();
            ArrayList<LiquidMedia> medias = (ArrayList<LiquidMedia>) response.body();
            showMediasList(medias);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**Call<ArrayList<LiquidMedia>> call1 = LiquidApi.getApi().getMedias("ecae833f-979e-4856-af7b-ab335d2d0e61", 533);
        Call<ArrayList<LiquidMedia>> call2 = LiquidApi.getApi().getMedias("e7b11183-65d5-4a2c-a279-e9a2e933b897", 536);

        call1.enqueue(new Callback<ArrayList<LiquidMedia>>() {
            @Override
            public void onResponse(retrofit.Response<ArrayList<LiquidMedia>> response, Retrofit retrofit) {
                ArrayList<LiquidMedia> medias = (ArrayList<LiquidMedia>) response.body();
                showMediasList(medias);

            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("retrofit: ", "error");
            }
        });

        call2.enqueue(new Callback<ArrayList<LiquidMedia>>() {
            @Override
            public void onResponse(retrofit.Response<ArrayList<LiquidMedia>> response, Retrofit retrofit) {
                ArrayList<LiquidMedia> medias = (ArrayList<LiquidMedia>) response.body();
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("retrofit: ", "error");
            }
        });**/

	}


	private void showMediasList(ArrayList<LiquidMedia> medias) {

        if(mediaList == null) {
            this.mediaList = medias;
        }

        Log.e("retrofit:", this.mediaList.get(0).title);

        if(mAdapter == null) {
            mAdapter = new MediasAdapter(this, this.mediaList);
            list.setAdapter(mAdapter);
        }

        mAdapter.notifyDataSetChanged();

	}
}
