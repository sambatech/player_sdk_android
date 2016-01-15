package com.sambatech.player.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.sambatech.player.R;
import com.sambatech.player.adapters.MediasAdapter;
import com.sambatech.player.model.LiquidMedia;
import com.sambatech.player.rest.LiquidApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import de.greenrobot.event.EventBus;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Retrofit;

public class MainActivity extends Activity {

    //Simple map with player hashs and pids
    private static final Map<Integer, String> phMap = new HashMap<Integer, String>() {{
        put(533, "986e07f70986265468eae1377424d171");
        put(536, "ee9edb5cbca43b39708141df994b53d7");
    }};

    //Adresses ( TODO: put them on an value.xml file )
    //private static String tag_endpoint = "http://192.168.0.179:3000/";
    private static String tag_endpoint = "http://198.101.153.219:3000/";
    private static String api_endpoint = "http://198.101.153.219:8091/v1/";

	@Bind(R.id.media_list) ListView list;
    @BindString(R.string.player_endpoint) String player_endpoint;

    //Medias Adapter
    MediasAdapter mAdapter;

    ArrayList<LiquidMedia> mediaList;

	@OnItemClick(R.id.media_list) public void mediaItemClick(int position) {
		LiquidMedia media = (LiquidMedia) mAdapter.getItem(position);

        Intent intent = new Intent(MainActivity.this, MediaItemActivity.class);
        EventBus.getDefault().postSticky(media);
        startActivity(intent);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

		callCommonList();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.ad_program) {
            getTags();
        }else if (id == R.id.common) {
	        callCommonList();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Make the request to the liquid api
     * @param token
     * @param pid
     */
    private void makeMediasCall(String token, final int pid) {
        Call<ArrayList<LiquidMedia>> call = LiquidApi.getApi(api_endpoint).getMedias(token, pid);

        call.enqueue(new Callback<ArrayList<LiquidMedia>>() {
            @Override
            public void onResponse(retrofit.Response<ArrayList<LiquidMedia>> response, Retrofit retrofit) {
                ArrayList<LiquidMedia> medias = (ArrayList<LiquidMedia>) response.body();
                medias = insertExternalData(medias, pid);
                showMediasList(medias);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("retrofit: ", "error");
            }
        });
    }

    private void getTags() {
        //Calling for our tags
        Call<ArrayList<LiquidMedia.AdTag>> tagCall = LiquidApi.getApi(tag_endpoint).getTags();

        tagCall.enqueue(new Callback<ArrayList<LiquidMedia.AdTag>>() {
            @Override
            public void onResponse(retrofit.Response<ArrayList<LiquidMedia.AdTag>> response, Retrofit retrofit) {
                ArrayList<LiquidMedia.AdTag> tags = (ArrayList<LiquidMedia.AdTag>) response.body();
	            try {
		            ArrayList<LiquidMedia> mediasModified = mediasWithTags(mediaList, tags);
		            mediaList.clear();
		            list.setAdapter(null);
		            showMediasList(mediasModified);
	            }catch(CloneNotSupportedException e) {
					Log.e("tags", e.getMessage());
	            }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("retrofit: ", "error");
            }
        });
    }

	private void callCommonList() {

		if(mediaList != null ){
			mediaList.clear();
		}
		//Making the call to project 533
		makeMediasCall("ecae833f-979e-4856-af7b-ab335d2d0e61", 533);

		//Making the call to project 536
		makeMediasCall("e7b11183-65d5-4a2c-a279-e9a2e933b897", 536);
	}

    /**
     * Populates the MediaItem with the given medias
     * @param medias
     */
	private void showMediasList(ArrayList<LiquidMedia> medias) {
		Log.e("tags:", String.valueOf(medias.size()));
        if(mediaList == null) {
            this.mediaList = medias;
        }else {
            this.mediaList.addAll(medias);
        }

        if(mAdapter == null) {
            mAdapter = new MediasAdapter(this, this.mediaList);
        }
		list.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();

	}

    private ArrayList<LiquidMedia> insertExternalData(ArrayList<LiquidMedia> medias, int pid) {

        for(LiquidMedia media : medias) {
            media.ph = phMap.get(pid);
        }

        return medias;
    }

    private ArrayList<LiquidMedia> mediasWithTags(ArrayList<LiquidMedia> medias, ArrayList<LiquidMedia.AdTag> tags) throws CloneNotSupportedException{
        int mIndex = 0;
        ArrayList<LiquidMedia> newMedias = new ArrayList<LiquidMedia>();

        for(int i = 0; i < tags.size(); i++) {
            LiquidMedia m = new LiquidMedia();
            if(i < medias.size()) {
                m = (LiquidMedia) medias.get(i).clone();
            }else {
                m = (LiquidMedia) newMedias.get(mIndex).clone();
                mIndex = mIndex++;
            }
            m.adTag = new LiquidMedia.AdTag();
            m.adTag.name = tags.get(i).name;
            m.adTag.url = tags.get(i).url;
            m.description = tags.get(i).name;
	        newMedias.add(m);
        }
	    Log.e("tags:", String.valueOf(newMedias.size()));
        return newMedias;
    }
}
