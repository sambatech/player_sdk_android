package com.sambatech.sample.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.sambatech.sample.R;
import com.sambatech.sample.adapters.MediasAdapter;
import com.sambatech.sample.model.LiquidMedia;
import com.sambatech.sample.rest.LiquidApi;

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
        put(4421, "bc6a17435f3f389f37a514c171039b75");
        put(4460, "36098808ae444ca5de4acf231949e312");
    }};

    //Adresses ( TODO: put them on an value.xml file )
    //private static String tag_endpoint = "http://198.101.153.219:3000/";
	private static String tag_endpoint = "https://api.myjson.com/bins/";
    private static String api_endpoint = "http://api.sambavideos.sambatech.com/v1/";

	@Bind(R.id.media_list) ListView list;
	@Bind(R.id.progressbar_view) LinearLayout loading;

    @BindString(R.string.player_endpoint) String player_endpoint;

    //Medias Adapter
    MediasAdapter mAdapter;

    ArrayList<LiquidMedia> mediaList;

	@OnItemClick(R.id.media_list) public void mediaItemClick(int position) {
		if(loading.getVisibility() == View.VISIBLE) return;

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

	    SearchView addTag = (SearchView) menu.findItem(R.id.addTag).getActionView();

	    addTag.setQueryHint("myjson id ( ex: 26dyf )");

	    addTag.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

		    @Override
		    public boolean onQueryTextSubmit(String query) {
			    getTags(query);
			    return false;
		    }

		    @Override
		    public boolean onQueryTextChange(String newText) {
			    return false;
		    }
	    });

	    // Clean magnifier
	    int searchCloseButtonId = addTag.getContext().getResources().getIdentifier("android:id/search_mag_icon", null, null);
	    ImageView magIcon = (ImageView) addTag.findViewById(searchCloseButtonId);
	    magIcon.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
	    magIcon.setVisibility(View.INVISIBLE);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.ad_program) {
            getTags("4xtfj");
        }else if (id == R.id.common) {
	        callCommonList();
        }else if(id == R.id.about){
			Intent about = new Intent(this, AboutActivity.class);
	        startActivity(about);
        }else if(id == R.id.addTag) {

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Make the request to the liquid api
     * @param token
     * @param pid
     */
    private void makeMediasCall(final String token, final int pid) {
        Call<ArrayList<LiquidMedia>> call = LiquidApi.getApi(api_endpoint).getMedias(token, pid, true, "VIDEO");

	    loading.setVisibility(View.VISIBLE);

        call.enqueue(new Callback<ArrayList<LiquidMedia>>() {
            @Override
            public void onResponse(retrofit.Response<ArrayList<LiquidMedia>> response, Retrofit retrofit) {
	            if(response.code() == 200) {
		            ArrayList<LiquidMedia> medias = response.body();
		            medias = insertExternalData(medias, pid);
		            showMediasList(medias);
	            }else {

	            }
	            loading.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Throwable t) {
	            makeMediasCall(token, pid);

            }
        });
    }

    private void getTags(final String jsonId) {
        //Calling for our tags
        Call<ArrayList<LiquidMedia.AdTag>> tagCall = LiquidApi.getApi(tag_endpoint).getTags(jsonId);

	    loading.setVisibility(View.VISIBLE);

        tagCall.enqueue(new Callback<ArrayList<LiquidMedia.AdTag>>() {
            @Override
            public void onResponse(retrofit.Response<ArrayList<LiquidMedia.AdTag>> response, Retrofit retrofit) {
	            if(response.code() == 200) {
		            ArrayList<LiquidMedia.AdTag> tags = (ArrayList<LiquidMedia.AdTag>) response.body();
		            try {
			            ArrayList<LiquidMedia> mediasModified = mediasWithTags(mediaList, tags);
			            mediaList.clear();
			            list.setAdapter(null);
			            showMediasList(mediasModified);
		            } catch (CloneNotSupportedException e) {
		            }
	            }else {
		            Toast.makeText(MainActivity.this, "Tags não achadas no id: " + jsonId, Toast.LENGTH_SHORT).show();
	            }
	            loading.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Throwable t) {
	            Toast.makeText(MainActivity.this, "erro requisição: " + jsonId, Toast.LENGTH_SHORT).show();
	            loading.setVisibility(View.GONE);
            }
        });
    }

	private void callCommonList() {

		if(mediaList != null ){
			mediaList.clear();
		}

		//Making the call to project 4421
		makeMediasCall("e88070d4-5b19-4a4f-a23f-6b9ca1bc5492", 4421);

		//Making the call to project 4460
		makeMediasCall("b7e616d0-39e2-4cde-a5f7-639257c1247f", 4460);
	}

    /**
     * Populates the MediaItem with the given medias
     * @param medias
     */
	private void showMediasList(ArrayList<LiquidMedia> medias) {
        if(mediaList == null) {
            this.mediaList = medias;
        }else {
            this.mediaList.addAll(medias);

	        // adding live streams

	        /*LiquidMedia.Thumb thumb = new LiquidMedia.Thumb();
	        thumb.url = "http://www.impactmobile.com/files/2012/09/icon64-broadcasts.png";
	        ArrayList<LiquidMedia.Thumb> thumbs = new ArrayList<>(Arrays.asList(new LiquidMedia.Thumb[]{ thumb }));

	        LiquidMedia media = new LiquidMedia();
	        media.ph = "bc6a17435f3f389f37a514c171039b75";
	        media.streamUrl = "http://gbbrlive2.sambatech.com.br/liveevent/sbt3_8fcdc5f0f8df8d4de56b22a2c6660470/livestream/manifest.f4m";
	        media.title = "Live SBT (HLS)";
	        media.description = "Transmissão ao vivo do SBT.";
	        media.thumbs = thumbs;
	        mediaList.add(media);
	        media = new LiquidMedia();
	        media.ph = "bc6a17435f3f389f37a514c171039b75";
	        media.streamUrl = "http://vevoplaylist-live.hls.adaptive.level3.net/vevo/ch1/appleman.m3u8";
	        media.title = "Live VEVO (HLS)";
	        media.description = "Transmissão ao vivo do VEVO.";
	        media.thumbs = thumbs;
	        mediaList.add(media);
	        media = new LiquidMedia();
	        media.ph = "bc6a17435f3f389f37a514c171039b75";
	        media.streamUrl = "http://itv08.digizuite.dk/tv2b/ngrp:ch1_all/playlist.m3u8";
	        media.title = "Live Denmark channel (HLS)";
	        media.description = "Transmissão ao vivo TV-DN.";
	        media.thumbs = thumbs;
	        mediaList.add(media);
	        media = new LiquidMedia();
	        media.ph = "bc6a17435f3f389f37a514c171039b75";
	        media.streamUrl = "http://itv08.digizuite.dk/tv2b/ngrp:ch1_all/manifest.f4m";
	        media.title = "Live Denmark channel (HDS: erro!)";
	        media.description = "Transmissão ao vivo inválida.";
	        media.thumbs = thumbs;
	        mediaList.add(media);*/
        }

        if(mAdapter == null) {
            mAdapter = new MediasAdapter(this, this.mediaList);
        }
		list.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();

	}

    private ArrayList<LiquidMedia> insertExternalData(ArrayList<LiquidMedia> medias, int pid) {
		if(medias != null) {

			for(LiquidMedia media : medias) {
				media.ph = phMap.get(pid);
			}

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
        return newMedias;
    }
}
