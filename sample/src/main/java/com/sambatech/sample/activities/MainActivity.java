package com.sambatech.sample.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.libraries.mediaframework.exoplayerextensions.DrmRequest;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaConfig;
import com.sambatech.player.model.SambaMediaRequest;
import com.sambatech.sample.R;
import com.sambatech.sample.adapters.MediasAdapter;
import com.sambatech.sample.model.LiquidMedia;
import com.sambatech.sample.rest.LiquidApi;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
	@BindString(R.string.access_token) String access_token;

	MediasAdapter mAdapter;
	Menu menu;

	Boolean adEnabled = false;

	//Array to store requested medias
	ArrayList<LiquidMedia> mediaList;
	//Array to store medias with ad tags
	ArrayList<LiquidMedia> adMediaList;
	//Controls loading
	private Boolean loadingFlag;
	private boolean _autoPlay = true;
	private Drawable _autoPlayIcon;
	private static final int _autoPlayColor = 0xff99ccff;
	private static final int _autoPlayColorDisabled = 0xff999999;

	@OnItemClick(R.id.media_list) public void mediaItemClick(int position) {

		if(loading.getVisibility() == View.VISIBLE) return;

		LiquidMedia media = (LiquidMedia) mAdapter.getItem(position);
		media.highlighted = position == 0;

		EventBus.getDefault().postSticky(media);

		Intent intent = new Intent(MainActivity.this, MediaItemActivity.class);
		intent.putExtra("autoPlay", _autoPlay);
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
	protected void onDestroy() {
		super.onDestroy();
		ButterKnife.unbind(this);
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

		_autoPlayIcon = DrawableCompat.wrap(menu.findItem(R.id.autoPlay).getIcon()).mutate();

		if (_autoPlay)
			DrawableCompat.setTint(_autoPlayIcon, _autoPlayColor);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if(adEnabled) {
			MenuItem dbclick = menu.findItem(R.id.withTag);
			dbclick.setIcon(R.drawable.ic_dbclick_disable);
			showMediasList(mediaList);
		}

		switch (id) {
			case R.id.withTag:
				if (!adEnabled) {
					getTags("4xtfj");
					item.setIcon(R.drawable.ic_dbclick);
				}

				adEnabled = !adEnabled;
				break;

			case R.id.common:
				this.mediaList.clear();
				callCommonList();
				adEnabled = false;
				break;

			case R.id.about:
				Intent about = new Intent(this, AboutActivity.class);
				startActivity(about);
				break;

			case R.id.live:
				this.mediaList.clear();
				list.setAdapter(null);
				this.mediaList = populateLiveMedias();
				showMediasList(this.mediaList);
				adEnabled = false;
				break;

			case R.id.autoPlay:
				if (_autoPlay)
					DrawableCompat.setTint(_autoPlayIcon, _autoPlayColorDisabled);
				else DrawableCompat.setTint(_autoPlayIcon, _autoPlayColor);

				_autoPlay = !_autoPlay;
				break;
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
		loadingFlag = true;
		call.enqueue(new Callback<ArrayList<LiquidMedia>>() {
			@Override
			public void onResponse(retrofit.Response<ArrayList<LiquidMedia>> response, Retrofit retrofit) {
				if (response.code() == 200) {
					ArrayList<LiquidMedia> medias = response.body();
					medias = insertExternalData(medias, pid);
					mediaList.addAll(medias);
					showMediasList(mediaList);
					loadingFlag = false;
				}

				loading.setVisibility(View.GONE);
			}

			@Override
			public void onFailure(Throwable t) {
				loadingFlag = false;
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
					ArrayList<LiquidMedia.AdTag> tags = response.body();
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

		LiquidMedia.Thumb thumb = new LiquidMedia.Thumb();
		thumb.url = "http://67.media.tumblr.com/avatar_b5715f76628b_128.png";
		ArrayList<LiquidMedia.Thumb> thumbs = new ArrayList<>(Arrays.asList(new LiquidMedia.Thumb[]{thumb}));

		LiquidMedia m = new LiquidMedia();
		m.url = "http://wowza-hel-loadbala-tjihmv7zwnlz-966664345.us-east-1.elb.amazonaws.com/vod/_definst_/amlst:stg;cid,pid,mid;/monitoring/;video_240p.mp4,200704,320,240;/manifest_mpm4sav_mvtime.mpd";
		m.title = "Dash sample 1";
		m.qualifier = "VIDEO";
		m.type = "dash";
		m.thumbs = thumbs;
		mediaList.add(m);

		m = new LiquidMedia();
		m.url = "http://wowza-hel-loadbala-tjihmv7zwnlz-966664345.us-east-1.elb.amazonaws.com/vod/_definst_/amlst:stg;219,4421,d3ff15216acdbd9633689ca83998a8d3;/account/219/183/2016-01-08/video/;b7336c505c989d8f31ce51fefd27950b/Keep_On_Pushin__from_Ricki_Bedenbaugh_plus_240p.mp4,295936,426,240,5966d00bd7ef5044abafab8ac7221a7a/Keep_On_Pushin__from_Ricki_Bedenbaugh_plus_360p.mp4,948224,640,360,c6f7ec9b3d65325efb6a51108b0295fe/Keep_On_Pushin__from_Ricki_Bedenbaugh_plus_480p.mp4,1418240,854,480,e8a8e4581ceff50d50d01ec16e4dc4be/Keep_On_Pushin__from_Ricki_Bedenbaugh_plus_720p.mp4,2273280,1280,720,a7f09317a4681381486d2f7895281d34/Keep_On_Pushin__from_Ricki_Bedenbaugh_plus_1080p.mp4,3813376,1920,1080,;70000,120000/manifest.mpd";
		m.title = "Dash sample 2";
		m.qualifier = "VIDEO";
		m.type = "dash";
		m.thumbs = thumbs;
		mediaList.add(m);

		// AXINOM
		m = new LiquidMedia();
		m.url = "https://media.axprod.net/TestVectors/v6-MultiDRM/Manifest_1080p.mpd";
		m.drm = new LiquidMedia.Drm("https://drm-quick-start.azurewebsites.net/api/authorization/Axinom%20demo%20video", new LiquidMedia.DrmCallback() {
			public void call(SambaMediaConfig media, String response) {
				HashMap<String, String> requestProperties = new HashMap<>();
				requestProperties.put("X-AxDRM-Message", response.substring(1, response.length() - 1));
				media.drmRequest = new DrmRequest("https://drm-widevine-licensing.axtest.net/AcquireLicense", requestProperties);
			}
		});
		m.title = "Dash DRM (Axinom)";
		m.qualifier = "VIDEO";
		m.type = "dash";
		m.thumbs = thumbs;
		mediaList.add(m);

		// IRDETO
		//b00772b75e3677dba5a59e09598b7a0d be4a12397143caf9ec41c9acb98728bf
		m = new LiquidMedia();
		m.ph = "b00772b75e3677dba5a59e09598b7a0d";
		m.id = "4a48d2ea922217a3d91771f2acf56fdf";
		m.environment = SambaMediaRequest.Environment.TEST;
		HashMap<String, String> headers = new HashMap<>();
		headers.put("MAN-user-id", "app@sambatech.com");
		headers.put("MAN-user-password", "c5kU6DCTmomi9fU");
		m.drm = new LiquidMedia.Drm("http://sambatech.stage.ott.irdeto.com/services/CreateSession?CrmId=sambatech&UserId=smbUserTest", headers, new LiquidMedia.DrmCallback() {
			public void call(SambaMediaConfig media, String response) {
				if (media.drmRequest == null)// || media.drmRequest.requestProperties == null)
					return;

				try {
					Document parse = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(response.getBytes()));
					NamedNodeMap attributes = parse.getElementsByTagName("Session").item(0).getAttributes();
					//media.drmRequest.requestProperties.put("MANRoamingSession", attributes.getNamedItem("MANRoamingSession").getTextContent());
					//media.drmRequest.requestProperties.put("SessionId", attributes.getNamedItem("SessionId").getTextContent());
					//media.drmRequest.requestProperties.put("Ticket", attributes.getNamedItem("Ticket").getTextContent());
					media.drmRequest.postData += "&SessionId=" + attributes.getNamedItem("SessionId").getTextContent();
					media.drmRequest.postData += "&Ticket=" + attributes.getNamedItem("Ticket").getTextContent();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		m.title = "Dash DRM (Irdeto)";
		m.qualifier = "VIDEO";
		m.type = "dash";
		m.thumbs = thumbs;
		mediaList.add(m);

		m = new LiquidMedia();
		m.ph = "36098808ae444ca5de4acf231949e312";
		m.id = "0e060023c14f0427108e775dbf10d7b6";
		m.qualifier = "VIDEO";
		m.title = "SBT";
		mediaList.add(m);

		/*m = new LiquidMedia();
		m.ph = "f9b073c78f3812cf96690a9c2989c781";
		m.id = "8c9f8d1511d7805aab0cc6f23ea3c899";
		m.qualifier = "VIDEO";
		m.title = "Univer";
		mediaList.add(m);*/

		/*m = new LiquidMedia();
		m.ph = "be4a12397143caf9ec41c9acb98728bf";
		m.id = "698b61bf17f1529ab8bc483cbb69265e";
		m.title = "Dash API 1";
		m.thumbs = thumbs;
		m.environment = SambaMediaRequest.Environment.TEST;
		mediaList.add(m);

		m = new LiquidMedia();
		m.ph = "be4a12397143caf9ec41c9acb98728bf";
		m.id = "59b37af90f043e44e95723a93d067d1d";
		m.title = "Dash API 2";
		m.thumbs = thumbs;
		m.environment = SambaMediaRequest.Environment.TEST;
		mediaList.add(m);

		m = new LiquidMedia();
		m.ph = "be4a12397143caf9ec41c9acb98728bf";
		m.id = "7e9371fa2f65c49d9bf9996ddf12a1ac";
		m.title = "Dash API 3";
		m.thumbs = thumbs;
		m.environment = SambaMediaRequest.Environment.TEST;
		mediaList.add(m);

		m = new LiquidMedia();
		m.ph = "be4a12397143caf9ec41c9acb98728bf";
		m.id = "853a0a228ca4d31851369c4ae26e155c";
		m.title = "Dash API 4";
		m.thumbs = thumbs;
		m.environment = SambaMediaRequest.Environment.TEST;
		mediaList.add(m);

		m = new LiquidMedia();
		m.ph = "be4a12397143caf9ec41c9acb98728bf";
		m.id = "f52e3f6a6c317db20b369ddc42d37c61";
		m.title = "Dash API 5";
		m.thumbs = thumbs;
		m.environment = SambaMediaRequest.Environment.TEST;
		mediaList.add(m);*/

		loading.setVisibility(View.VISIBLE);
		list.setAdapter(null);

		//Making the call to project 4421
		makeMediasCall(access_token, 4421);

		//Making the call to project 4460
		makeMediasCall(access_token, 4460);
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
			//LiquidMedia m = (LiquidMedia) (i < medias.size() ? medias.get(i).clone() : newMedias.get(mIndex++).clone());
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
