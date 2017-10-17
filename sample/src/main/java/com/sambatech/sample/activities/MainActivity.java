package com.sambatech.sample.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.sambatech.player.model.SambaMediaRequest;
import com.sambatech.sample.R;
import com.sambatech.sample.adapters.MediasAdapter;
import com.sambatech.sample.model.EntitlementScheme;
import com.sambatech.sample.model.LiquidMedia;
import com.sambatech.sample.model.MediaInfo;
import com.sambatech.sample.rest.LiquidApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import de.greenrobot.event.EventBus;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Retrofit;

/**
 * The main activity to be shown when the app launches.
 */
public class MainActivity extends Activity {

	@Bind(R.id.media_list) ListView list;
	@Bind(R.id.progressbar_view) LinearLayout loading;
	@BindString(R.string.media_list_api) String mediaListApi;

	private MediasAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		ButterKnife.bind(this);
		requestMediaList();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ButterKnife.unbind(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.about:
				Intent about = new Intent(this, AboutActivity.class);
				startActivity(about);
				break;
			default: break;
		}

		return super.onOptionsItemSelected(item);
	}

	@OnItemClick(R.id.media_list)
	void mediaItemClick(int position) {

		if (loading.getVisibility() == View.VISIBLE)
			return;

		final MediaInfo media = (MediaInfo) mAdapter.getItem(position);
		//media.setHighlighted(position == 0);

		EventBus.getDefault().postSticky(media);

		startActivity(new Intent(MainActivity.this, MediaItemActivity.class));
	}

	private void requestMediaList() {
		Call<ArrayList<LiquidMedia>> call = LiquidApi.
				getApi(mediaListApi).
				getMedias(16);

		loading.setVisibility(View.VISIBLE);

		call.enqueue(new Callback<ArrayList<LiquidMedia>>() {
			@Override
			public void onResponse(retrofit.Response<ArrayList<LiquidMedia>> response, Retrofit retrofit) {
				if (response.code() == 200)
					extractMediaList(response.body());
				else Log.e(MainActivity.class.getSimpleName(), String.format("Invalid response code: %s", response.code()));

				loading.setVisibility(View.GONE);
			}

			@Override
			public void onFailure(Throwable t) {
				Log.e(MainActivity.class.getSimpleName(), "Something went wrong during the media list request.", t);
				loading.setVisibility(View.GONE);
			}
		});
	}

	private void extractMediaList(ArrayList<LiquidMedia> mediaList) {
		final List<MediaInfo> mediaInfoList = new ArrayList<>();

		for (LiquidMedia m : mediaList) {
			final MediaInfo mediaInfo = new MediaInfo();

			mediaInfo.setId(m.getId());
			mediaInfo.setLiveChannelId(m.getLiveChannelId());
			mediaInfo.setProjectHash(m.getPh());
			mediaInfo.setTitle(m.getTitle());
			mediaInfo.setDescription(m.getDescription());
			mediaInfo.setQualifier(m.getQualifier());
			mediaInfo.setThumbnail(m.getThumbnail());
			mediaInfo.setType(m.getType());

			final LiquidMedia.Params params = m.getParams();

			if (params != null) {
				mediaInfo.setAdUrl(params.getAd_program());
				mediaInfo.setStreamUrl(params.getPrimaryLive());
				mediaInfo.setAutoPlay(params.getAutoStart());
				mediaInfo.setControlsEnabled(params.getEnableControls());

				if (params.getBackupLive() != null)
					mediaInfo.setBackupUrls(new String[]{params.getBackupLive()});

				if ("audiolive".equalsIgnoreCase(params.getType()))
					mediaInfo.setAudioLive(true);

				if (params.getTitle() != null && !params.getTitle().isEmpty())
					mediaInfo.setTitle(params.getTitle());

				if (params.getThumbnailURL() != null && !params.getThumbnailURL().isEmpty())
					mediaInfo.setThumbnail(params.getThumbnailURL());
			}

			switch (m.getEnv().toLowerCase()) {
				case "staging":
					mediaInfo.setEnvironment(SambaMediaRequest.Environment.STAGING);
					break;
				case "dev":
				case "web1-13000":
					mediaInfo.setEnvironment(SambaMediaRequest.Environment.DEV);
					break;
				case "prod":
				default:
					mediaInfo.setEnvironment(SambaMediaRequest.Environment.PROD);
			}

			if (m.isDrm())
				mediaInfo.setEntitlementScheme(new EntitlementScheme());

			mediaInfoList.add(mediaInfo);
		}

		showMediaList(mediaInfoList);
	}

	/**
	 * Populates the MediaItem with the given medias
	 * @param mediaList - Array of objects representing the medias requested
	 */
	private void showMediaList(List<MediaInfo> mediaList) {
		mAdapter = new MediasAdapter(this, mediaList);
		list.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
	}
}
