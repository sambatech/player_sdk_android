package com.sambatech.player;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sambatech.player.event.SambaApiCallback;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaRequest;

public class MainActivity extends Activity {

	private SambaPlayer player;
	private ListView list;
	private TextView status;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		player = (SambaPlayer)findViewById(R.id.samba_player);
		list = (ListView)findViewById(R.id.media_list);
		status = (TextView)findViewById(R.id.status);

		initPlayer();
		initList();
		requestMediaList();
	}

	private void initPlayer() {
		//p.setListener(new SambaPlayerListener() {...});
		SambaEventBus.subscribe(new SambaPlayerListener() {
			@Override
			public void onLoad(SambaEvent e) {
				status.setText("Status: " + e.getType());
			}

			@Override
			public void onPlay(SambaEvent e) {
				status.setText("Status: " + e.getType());
			}

			@Override
			public void onPause(SambaEvent e) {
				status.setText("Status: " + e.getType());
			}

			@Override
			public void onStop(SambaEvent e) {
				status.setText("Status: " + e.getType());
			}

			@Override
			public void onFinish(SambaEvent e) {
				status.setText("Status: " + e.getType());
			}

			@Override
			public void onFullscreen(SambaEvent e) {
				status.setText("Status: " + e.getType());
			}

			@Override
			public void onFullscreenExit(SambaEvent e) {
				status.setText("Status: " + e.getType());
			}
		});
	}

	private void initList() {
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				loadMedia((SambaMedia) parent.getAdapter().getItem(position));
			}
		});
	}

	private void requestMediaList() {
		SambaApi api = new SambaApi(this, "token");

		api.requestMedia(new SambaMediaRequest[]{
				new SambaMediaRequest("2835573d6ea8b213efe1ff1ab3354da8", "593da65e3f9f4c866a0c4a9685414c7d"),
				new SambaMediaRequest("30183adb2092f87e5e6440f52b43662b", "a6f4795d02e6476618774561837b0cf7"),
				new SambaMediaRequest("34f07cf52fd85ccfc41a39bcf499e83b", "0632f26a442ba9ba3bb9067a45e239e2"),
				new SambaMediaRequest("2835573d6ea8b213efe1ff1ab3354da8", null, null, "http://vevoplaylist-live.hls.adaptive.level3.net/vevo/ch1/appleman.m3u8")
		}, new SambaApiCallback() {
			@Override
			public void onMediaResponse(SambaMedia media) {
				Log.i("req", media.title);
			}

			@Override
			public void onMediaListResponse(SambaMedia[] mediaList) {
				mediaList[1].title += " (Ad)";
				//mediaList[1].adUrl = "http://pubads.g.doubleclick.net/gampad/ads?sz=640x360&iu=/6062/iab_vast_samples/skippable&ciu_szs=300x250,728x90&impl=s&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=[referrer_url]&correlator=[timestamp]]";
				mediaList[1].adUrl = "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=sample_ct%3Dlinear&correlator=";
				mediaList[2].title += " (HLS)";
				mediaList[3].title = "Vevo (HLS Live)";

				status.setText("Loaded!");
				createListAdapter(mediaList);
				loadMedia(mediaList[0]);
			}

			@Override
			public void onMediaResponseError(Exception e) {
				Log.e("main", "Media load error", e);
			}
		});
	}

	private void createListAdapter(SambaMedia[] mediaList) {
		list.setAdapter(new ArrayAdapter<SambaMedia>(MainActivity.this, 0, mediaList) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null)
					convertView = LayoutInflater.from(getContext()).inflate(R.layout.media_list_item, parent, false);

				((TextView) convertView).setText(this.getItem(position).title);

				return convertView;
			}
		});
	}

	private void loadMedia(SambaMedia item) {
		player.setMedia(item);
		player.play();
	}
}
