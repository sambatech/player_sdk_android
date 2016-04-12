package com.sambatech.sample.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Arrays;
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

	//Simple map with player hashs and their pids
	private static final Map<Integer, String> phMap = new HashMap<Integer, String>() {{
		put(4421, "bc6a17435f3f389f37a514c171039b75");
		put(4460, "36098808ae444ca5de4acf231949e312");
	}};


	@Bind(R.id.media_list) ListView list;
	@Bind(R.id.progressbar_view) LinearLayout loading;

	//Samba api and Json Tag endpoints
	@BindString(R.string.sambaapi_endpoint) String api_endpoint;
	@BindString(R.string.mysjon_endpoint) String tag_endpoint;

	MediasAdapter mAdapter;
	Menu menu;

	Boolean adEnabled = false;

	//Array to store requested medias
	ArrayList<LiquidMedia> mediaList;
	//Array to store medias with ad tags
	ArrayList<LiquidMedia> adMediaList;

	@OnItemClick(R.id.media_list) public void mediaItemClick(int position) {
		if(loading.getVisibility() == View.VISIBLE) return;

		LiquidMedia media = (LiquidMedia) mAdapter.getItem(position);
		media.highlighted = position == 0;

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
		this.menu = menu;

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

		if(adEnabled) {
			MenuItem dbclick = (MenuItem) menu.findItem(R.id.withTag);
			dbclick.setIcon(R.drawable.ic_dbclick_disable);
			showMediasList(mediaList);
		}

		if(id == R.id.withTag && !adEnabled) {
			getTags("4xtfj");
			item.setIcon(R.drawable.ic_dbclick);
			adEnabled = true;
		}
		else if(id == R.id.withTag && adEnabled) {
			adEnabled = false;
		}else if (id == R.id.common) {
			this.mediaList.clear();
			callCommonList();
			adEnabled = false;
		}else if(id == R.id.about){
			Intent about = new Intent(this, AboutActivity.class);
			startActivity(about);
		}else if (id == R.id.live) {
			this.mediaList.clear();
			list.setAdapter(null);
			this.mediaList = populateLiveMedias();
			showMediasList(this.mediaList);
			adEnabled = false;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Make the request to the Samba Api ( see also http://dev.sambatech.com/documentation/sambavideos/index.html )
	 * @param token - Api token
	 * @param pid - Project ID
	 */
	private void makeMediasCall(final String token, final int pid) {
		Call<ArrayList<LiquidMedia>> call = LiquidApi.getApi(api_endpoint).getMedias(token, pid, true, "VIDEO,AUDIO");

		call.enqueue(new Callback<ArrayList<LiquidMedia>>() {
			@Override
			public void onResponse(retrofit.Response<ArrayList<LiquidMedia>> response, Retrofit retrofit) {
				if(response.code() == 200) {
					ArrayList<LiquidMedia> medias = response.body();
					medias = insertExternalData(medias, pid);
					mediaList.addAll(medias);
					showMediasList(mediaList);
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

	/**
	 * Request the DFP tags ( tags that you would put on the "ad_program" param of our browser player )
	 * @param jsonId - Id for the myjson.com service
	 */
	private void getTags(final String jsonId) {
		//Calling for our tags
		Call<ArrayList<LiquidMedia.AdTag>> tagCall = LiquidApi.getApi(tag_endpoint).getTags(jsonId);

		loading.setVisibility(View.VISIBLE);
		list.setAdapter(null);

		tagCall.enqueue(new Callback<ArrayList<LiquidMedia.AdTag>>() {
			@Override
			public void onResponse(retrofit.Response<ArrayList<LiquidMedia.AdTag>> response, Retrofit retrofit) {
				if (response.code() == 200) {
					ArrayList<LiquidMedia.AdTag> tags = (ArrayList<LiquidMedia.AdTag>) response.body();
					try {
						ArrayList<LiquidMedia> mediasModified = mediasWithTags(mediaList, tags);
						adMediaList = mediasModified;
						showMediasList(adMediaList);
					} catch (CloneNotSupportedException e) {
					}
				} else {
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

		if(mediaList != null ) {
			mediaList.clear();
		}else {
			mediaList = new ArrayList<>();
		}

		loading.setVisibility(View.VISIBLE);

		list.setAdapter(null);

		//Making the call to project 4421
		makeMediasCall("079cc2f1-4733-4c92-a7b5-7e1640698caa", 4421);

		//Making the call to project 4460
		makeMediasCall("079cc2f1-4733-4c92-a7b5-7e1640698caa", 4460);
	}

	/**
	 * Populates the MediaItem with the given medias
	 * @param medias - Array of objects representing the medias requested
	 */
	private void showMediasList(ArrayList<LiquidMedia> medias) {

		mAdapter = new MediasAdapter(this, medias);
		list.setAdapter(mAdapter);

		mAdapter.notifyDataSetChanged();

	}

	/**
	 * Inserts the corresponded player hashs in each media object given its project id
	 *
	 * @param medias - Array of objects representing the medias requested
	 * @param pid - Project ID
	 * @return
	 */
	private ArrayList<LiquidMedia> insertExternalData(ArrayList<LiquidMedia> medias, int pid) {
		if(medias != null) {

			for(LiquidMedia media : medias) {
				media.ph = phMap.get(pid);
			}

		}
		return medias;
	}

	/**
	 * Inserts ad tags for DFP Ads.
	 * @param medias - Array of objects representing the medias requested
	 * @param tags - List of DFP tags
	 * @return
	 * @throws CloneNotSupportedException
	 */
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

	/**
	 * Populate Live medias
	 * @return
	 */
	private ArrayList<LiquidMedia> populateLiveMedias() {

			ArrayList<LiquidMedia> medias = new ArrayList<>();

			LiquidMedia.Thumb thumb = new LiquidMedia.Thumb();
			thumb.url = "http://www.impactmobile.com/files/2012/09/icon64-broadcasts.png";
			ArrayList<LiquidMedia.Thumb> thumbs = new ArrayList<>(Arrays.asList(new LiquidMedia.Thumb[]{thumb}));

			LiquidMedia media = new LiquidMedia();
			media.ph = "bc6a17435f3f389f37a514c171039b75";
			media.streamUrl = "http://gbbrlive2.sambatech.com.br/liveevent/sbt3_8fcdc5f0f8df8d4de56b22a2c6660470/livestream/manifest.m3u8";
			media.title = "Live SBT (HLS)";
			media.description = "Transmissão ao vivo do SBT.";
			media.thumbs = thumbs;
			medias.add(media);

			media = new LiquidMedia();
			media.ph = "bc6a17435f3f389f37a514c171039b75";
			media.streamUrl = "http://vevoplaylist-live.hls.adaptive.level3.net/vevo/ch1/appleman.m3u8";
			media.title = "Live VEVO (HLS)";
			media.description = "Transmissão ao vivo do VEVO.";
			media.thumbs = thumbs;
			medias.add(media);

			media = new LiquidMedia();
			media.ph = "bc6a17435f3f389f37a514c171039b75";
			media.streamUrl = "http://itv08.digizuite.dk/tv2b/ngrp:ch1_all/playlist.m3u8";
			media.title = "Live Denmark channel (HLS)";
			media.description = "Transmissão ao vivo TV-DN.";
			media.thumbs = thumbs;
			medias.add(media);

			media = new LiquidMedia();
			media.ph = "bc6a17435f3f389f37a514c171039b75";
			media.streamUrl = "http://itv08.digizuite.dk/tv2b/ngrp:ch1_all/manifest.f4m";
			media.title = "Live Denmark channel (HDS: erro!)";
			media.description = "Transmissão ao vivo inválida.";
			media.thumbs = thumbs;
			medias.add(media);

			media = new LiquidMedia();
			media.ph = "bc6a17435f3f389f37a514c171039b75";
			media.streamUrl = "http://slrp.sambavideos.sambatech.com/liveevent/tvdiario_7a683b067e5eee5c8d45e1e1883f69b9/livestream/playlist.m3u8";
			media.title = "Tv Diário";
			media.description = "Transmissão ao vivo TV Diário";
			media.thumbs = thumbs;
			medias.add(media);

			return medias;
	}
}
