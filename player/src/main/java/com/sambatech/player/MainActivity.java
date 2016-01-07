package com.sambatech.player;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

	@Override
	protected void onPause() {
		super.onPause();

		if (player != null)
			player.pause();
	}

	private void initPlayer() {
		//p.setListener(new SambaPlayerListener() {...});
		SambaEventBus.subscribe(new SambaPlayerListener() {
			@Override
			public void onLoad(SambaEvent e) {
				status.setText(String.format("Status: %s", e.getType()));
			}

			@Override
			public void onPlay(SambaEvent e) {
				status.setText(String.format("Status: %s", e.getType()));
			}

			@Override
			public void onPause(SambaEvent e) {
				status.setText(String.format("Status: %s", e.getType()));
			}

			@Override
			public void onStop(SambaEvent e) {
				status.setText(String.format("Status: %s", e.getType()));
			}

			@Override
			public void onFinish(SambaEvent e) {
				status.setText(String.format("Status: %s", e.getType()));
			}

			@Override
			public void onFullscreen(SambaEvent e) {
				status.setText(String.format("Status: %s", e.getType()));
			}

			@Override
			public void onFullscreenExit(SambaEvent e) {
				status.setText(String.format("Status: %s", e.getType()));
			}

			@Override
			public void onError(SambaEvent e) {
				status.setText(String.format("Status: %s", e.getType()));
				Toast.makeText(MainActivity.this, (String)e.getData(), Toast.LENGTH_SHORT).show();
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

		api.requestMedia(new SambaMediaRequest[] {
				new SambaMediaRequest("986e07f70986265468eae1377424d171", "a7dd940fb617b7af746da3ed42c019e5"),
				new SambaMediaRequest("dc6d5bfa19c79d8f7903db43024bea3e", "ac1309d58f045e11375d9190dd055699"),
				new SambaMediaRequest("986e07f70986265468eae1377424d171", "e44c1f5a5ac1d914b0a58a1cd9835003"),
				new SambaMediaRequest("986e07f70986265468eae1377424d171", "01c6fa6b407d68f7ccfa771695378e0d"),
				new SambaMediaRequest("986e07f70986265468eae1377424d171", "9d821dfd8c457b5c20d5929167ce98a8"),
				new SambaMediaRequest("986e07f70986265468eae1377424d171", "d9bf79fe12d9ed24baeac28fd4ae9a21"),
				new SambaMediaRequest("986e07f70986265468eae1377424d171", "171aff4964aa674a715a6928f744c66f"),
				new SambaMediaRequest("2835573d6ea8b213efe1ff1ab3354da8", null, null, "http://vevoplaylist-live.hls.adaptive.level3.net/vevo/ch1/appleman.m3u8"),
				new SambaMediaRequest("2835573d6ea8b213efe1ff1ab3354da8", null, null, "http://itv08.digizuite.dk/tv2b/ngrp:ch1_all/playlist.m3u8"),
				new SambaMediaRequest("986e07f70986265468eae1377424d171", "ea328a17cd7b8291b10e3efacd3dc181"),
				new SambaMediaRequest("986e07f70986265468eae1377424d171", "c01f14609a8c6e0b97f0e71ce62d5419"),
				new SambaMediaRequest("986e07f70986265468eae1377424d171", "12341234123412341234123412341234")
		}, new SambaApiCallback() {
			@Override
			public void onMediaResponse(SambaMedia media) {
				status.setText(String.format("Loading...%s", media != null ? media.title : ""));
			}

			@Override
			public void onMediaListResponse(SambaMedia[] mediaList) {
				for (SambaMedia m : mediaList) {
					if (m.title.isEmpty())
						m.title = "Sem título";

					m.title += " (" + m.type.toUpperCase() + (m.isLive ? " Live" : "") + ")";
				}

				//http://test.d.sambavideos.sambatech.com/account/100209/50/2014-10-06/video/9ba974f571a8bf28db3d48636a04baa1/30DIFERENCAS.2.mp4
				mediaList[2].title += ": preroll";
				mediaList[2].adUrl = "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=sample_ct%3Dredirecterror&correlator=";
				mediaList[3].title += ": Pre+mid+post+bumpers";
				mediaList[3].adUrl = "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=%2F3510761%2FadRulesSampleTags&ciu_szs=160x600%2C300x250%2C728x90&cust_params=adrule%3Dpremidpostpodandbumpers&impl=s&gdfp_req=1&env=vp&ad_rule=1&vid=12345&cmsid=3601&output=xml_vast2&unviewed_position_start=1&url=%5Breferrer_url%5D&correlator=%5Btimestamp%5D";
				mediaList[4].title += ": postroll";
				mediaList[4].adUrl = "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=%2F3510761%2FadRulesSampleTags&ciu_szs=160x600%2C300x250%2C728x90&cust_params=adrule%3Dpostrollonly&impl=s&gdfp_req=1&env=vp&ad_rule=1&vid=12345&cmsid=3601&output=xml_vast2&unviewed_position_start=1&url=%5Breferrer_url%5D&correlator=%5Btimestamp%5D";
				mediaList[5].title += ": skippable";
				mediaList[5].adUrl = "http://pubads.g.doubleclick.net/gampad/ads?sz=640x360&iu=/6062/iab_vast_samples/skippable&ciu_szs=300x250,728x90&impl=s&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=%5Breferrer_url%5D&correlator=%5Btimestamp%5D";
				mediaList[8].title += ": Ad";
				mediaList[8].adUrl = "http://pubads.g.doubleclick.net/gampad/ads?sz=640x360&iu=/6062/iab_vast_samples/skippable&ciu_szs=300x250,728x90&impl=s&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=%5Breferrer_url%5D&correlator=%5Btimestamp%5D";

				status.setText("Loaded!");
				createListAdapter(mediaList);
				loadMedia(mediaList[0]);
			}

			@Override
			public void onMediaResponseError(String msg, SambaMediaRequest request) {
				Toast.makeText(MainActivity.this, msg + ": " + request.mediaId, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void createListAdapter(SambaMedia[] mediaList) {
		list.setAdapter(new ArrayAdapter<SambaMedia>(MainActivity.this, 0, mediaList) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null)
					convertView = LayoutInflater.from(getContext()).inflate(R.layout.media_list_item, parent, false);

				SambaMedia media = this.getItem(position);

				((ImageView)convertView.findViewById(R.id.image)).setImageDrawable(media.thumb);
				((TextView)convertView.findViewById(R.id.text)).setText(media.title);

				return convertView;
			}
		});
	}

	private void loadMedia(SambaMedia media) {
		player.setMedia(media);
		player.play();
	}
}
