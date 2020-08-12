package com.sambatech.sample.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.sambatech.player.cast.SambaCast;
import com.sambatech.player.model.SambaMediaRequest;
import com.sambatech.sample.R;
import com.sambatech.sample.adapters.MediasAdapter;
import com.sambatech.sample.model.EntitlementScheme;
import com.sambatech.sample.model.LiquidMedia;
import com.sambatech.sample.model.MediaInfo;
import com.sambatech.sample.rest.LiquidApi;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.Unbinder;
import de.greenrobot.event.EventBus;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Retrofit;

/**
 * The main activity to be shown when the app launches.
 */
public class MainActivity extends AppCompatActivity {

	@BindView(R.id.media_list) ListView list;
	@BindView(R.id.progressbar_view) LinearLayout loading;
	@BindString(R.string.media_list_api) String mediaListApi;

	private MediasAdapter mAdapter;
	private Unbinder unbinder;
	private SambaCast mSambaCast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		unbinder = ButterKnife.bind(this);
		requestMediaList();
		mSambaCast = new SambaCast(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbinder.unbind();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);

		CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu,
				R.id.media_route_menu_item);

		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSambaCast.notifyActivityResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSambaCast.notifyActivityPause();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.about:
				Intent about = new Intent(this, AboutActivity.class);
				startActivity(about);
				break;
			case R.id.offline:
				Intent offline = new Intent(this, OfflineActivity.class);
				startActivity(offline);
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
//		Call<ArrayList<LiquidMedia>> call = LiquidApi.
//				getApi(mediaListApi).
//				getMedias(16);

		loading.setVisibility(View.GONE);

//		call.enqueue(new Callback<ArrayList<LiquidMedia>>() {
//			@Override
//			public void onResponse(retrofit.Response<ArrayList<LiquidMedia>> response, Retrofit retrofit) {
//				if (response.code() == 200)
//					extractMediaList(response.body());
//				else Log.e(MainActivity.class.getSimpleName(), String.format("Invalid response code: %s", response.code()));
//
//				loading.setVisibility(View.GONE);
//			}
//
//			@Override
//			public void onFailure(Throwable t) {
//				Log.e(MainActivity.class.getSimpleName(), "Something went wrong during the media list request.", t);
//				loading.setVisibility(View.GONE);
//			}
//		});

		extractMediaList(new ArrayList<LiquidMedia>());
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

		MediaInfo mediaInfo = new MediaInfo();


		mediaInfo.setTitle("Media Playplus");
		mediaInfo.setProjectHash("fad2b4a201ef2305d06cb817da1bd262");
		mediaInfo.setId("ca60065f62e83445a4c5ae91abd3eacf");
		mediaInfo.setEnvironment(SambaMediaRequest.Environment.PROD);
		mediaInfo.setControlsEnabled(true);
		mediaInfo.setAutoPlay(true);

        MediaInfo mediaInfo2 = new MediaInfo();

        mediaInfo2.setTitle("Teste Audio lento");
        mediaInfo2.setProjectHash("4f25046e52b1b4643efd8a328b78fbf3");
        mediaInfo2.setId("bc6e1ec855f8f1142232f4282bfe5ed9");
        mediaInfo2.setEnvironment(SambaMediaRequest.Environment.PROD);
        mediaInfo2.setControlsEnabled(true);
        mediaInfo2.setAutoPlay(true);
        mediaInfo2.setQualifier("AUDIO");

        MediaInfo mediaInfo3 = new MediaInfo();

		mediaInfo3.setTitle("Teste Video Sem Bloqueio");
		mediaInfo3.setProjectHash("13a157b86aa27510d0c373e447dab6ea");
		mediaInfo3.setId("19310f3fcb5f515d1a44d5e709695c03");
		mediaInfo3.setEnvironment(SambaMediaRequest.Environment.PROD);
		mediaInfo3.setControlsEnabled(true);
		mediaInfo3.setAutoPlay(true);

		MediaInfo mediaInfo4 = new MediaInfo();

		mediaInfo4.setTitle("Teste Video Com Bloqueio de Dominio");
		mediaInfo4.setProjectHash("cf22fbd77a8df4023d80fd0d8d7e20e8");
		mediaInfo4.setId("8dca74ec0c18b63a206fb9d46658159f");
		mediaInfo4.setEnvironment(SambaMediaRequest.Environment.PROD);
		mediaInfo4.setControlsEnabled(true);
		mediaInfo4.setAutoPlay(true);


		MediaInfo mediaInfo5 = new MediaInfo();

		mediaInfo5.setTitle("Teste Video Playplus ESPN");
		mediaInfo5.setProjectHash("548fd94beda15ebe2fa22adf1839b60c");
		mediaInfo5.setId("64e477ca0dc1ecc035e02117b1d4514a");
		mediaInfo5.setEnvironment(SambaMediaRequest.Environment.PROD);
		mediaInfo5.setControlsEnabled(true);
		mediaInfo5.setAutoPlay(true);

		MediaInfo mediaInfo6 = new MediaInfo();

		mediaInfo6.setTitle("Teste Live Playplus ESPN");
		mediaInfo6.setProjectHash("548fd94beda15ebe2fa22adf1839b60c");
		mediaInfo6.setId("3958f83a366a90dbbd093f8907129171");
		mediaInfo6.setLiveChannelId("3958f83a366a90dbbd093f8907129171");
		mediaInfo6.setEnvironment(SambaMediaRequest.Environment.PROD);
		mediaInfo6.setControlsEnabled(true);
		mediaInfo6.setAutoPlay(true);

		MediaInfo mediaInfo7 = new MediaInfo();

		mediaInfo7.setTitle("Teste Media Fish TV Playplus");
		mediaInfo7.setProjectHash("e566111d7f36df2c1e504bd946fbaece");
		mediaInfo7.setId("f6283092dd18c52ebfc26d6710aec370");
		mediaInfo7.setEnvironment(SambaMediaRequest.Environment.PROD);
		mediaInfo7.setControlsEnabled(true);
		mediaInfo7.setAutoPlay(true);

		MediaInfo mediaInfo8 = new MediaInfo();

		mediaInfo8.setTitle("Teste Live Analytics");
		mediaInfo8.setProjectHash("964b56b4b184c2a29e3c2065a7a15038");
		mediaInfo8.setId("46fe05239a330e011ea2d0f36b1f0702");
		mediaInfo8.setEnvironment(SambaMediaRequest.Environment.PROD);
		mediaInfo8.setLiveChannelId("46fe05239a330e011ea2d0f36b1f0702");
		mediaInfo8.setControlsEnabled(true);
		mediaInfo8.setAutoPlay(true);

		MediaInfo mediaInfo9 = new MediaInfo();

		mediaInfo9.setTitle("Teste Media Ricardo Eletro");
		mediaInfo9.setProjectHash("31044efbcb9b4ac1df2190f3e902e25f");
		mediaInfo9.setId("517b82ca6ab11f280a62e636555d2481");
		mediaInfo9.setEnvironment(SambaMediaRequest.Environment.PROD);
		mediaInfo9.setControlsEnabled(true);
		mediaInfo9.setAutoPlay(true);

		MediaInfo mediaInfo10 = new MediaInfo();

		mediaInfo10.setTitle("Media DRM");
		mediaInfo10.setProjectHash("61a1c1faa23eb27de9110368e551de73");
		mediaInfo10.setId("2e323f019ccc61f5bfac0a3be28b8f70");
		mediaInfo10.setEnvironment(SambaMediaRequest.Environment.STAGING);
		mediaInfo10.setControlsEnabled(true);
		mediaInfo10.setAutoPlay(true);

		MediaInfo mediaInfo11 = new MediaInfo();

		mediaInfo11.setTitle("Media Legenda");
		mediaInfo11.setProjectHash("964b56b4b184c2a29e3c2065a7a15038");
		mediaInfo11.setId("b4c134b2a297d9eacfe7c7852fa86312");
		mediaInfo11.setEnvironment(SambaMediaRequest.Environment.PROD);
		mediaInfo11.setControlsEnabled(true);
		mediaInfo11.setAutoPlay(true);

		MediaInfo mediaInfo13 = new MediaInfo();

		mediaInfo13.setTitle("Live NTI");
		mediaInfo13.setProjectHash("7308d46a10eb5bef20e29516ca918f65");
		mediaInfo13.setId("4b0b02faa183575a0fc9cac5108fae8d");
		mediaInfo13.setEnvironment(SambaMediaRequest.Environment.PROD);
		mediaInfo13.setLiveChannelId("2a5e02638bd35f735f1319c90772c103");
		mediaInfo13.setControlsEnabled(true);
		mediaInfo13.setAutoPlay(true);

		MediaInfo mediaInfo14 = new MediaInfo();

		mediaInfo14.setTitle("Live Rede Minas");
		mediaInfo14.setProjectHash("13cd624949054179d0b60c84523d23b1");
		mediaInfo14.setId("ec4b5a296d97fa99bf990662f5b4f8e1");
		mediaInfo14.setEnvironment(SambaMediaRequest.Environment.PROD);
		mediaInfo14.setLiveChannelId("ec4b5a296d97fa99bf990662f5b4f8e1");
		mediaInfo14.setControlsEnabled(true);
		mediaInfo14.setAutoPlay(true);

		mediaInfoList.add(mediaInfo);
        mediaInfoList.add(mediaInfo2);
        mediaInfoList.add(mediaInfo3);
		mediaInfoList.add(mediaInfo4);
		mediaInfoList.add(mediaInfo5);
		mediaInfoList.add(mediaInfo6);
		mediaInfoList.add(mediaInfo7);
		mediaInfoList.add(mediaInfo8);
		mediaInfoList.add(mediaInfo9);
		mediaInfoList.add(mediaInfo10);
		mediaInfoList.add(mediaInfo11);
		mediaInfoList.add(mediaInfo13);
		mediaInfoList.add(mediaInfo14);

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
