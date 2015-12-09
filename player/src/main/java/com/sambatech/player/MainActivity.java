package com.sambatech.player;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.sambatech.player.event.SambaApiListener;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.model.SambaMedia;

import net.engio.mbassy.listener.Handler;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SambaApi api = new SambaApi(this, "token");

		api.requestMedia("34f07cf52fd85ccfc41a39bcf499e83b", "0632f26a442ba9ba3bb9067a45e239e2", new SambaApiListener() {
			@Override
			public void onMediaResponse(SambaMedia media) {
				SambaPlayer p = (SambaPlayer)findViewById(R.id.samba_player);

				SambaEventBus.getEventBus().subscribe(new SambaPlayerListener() {
					@Override
					@Handler(condition = "e.getType() == SambaEventType.LOAD")
					public void onLoad(SambaEvent e) {
						Log.i("evt", e.getData().toString());
					}

					@Override
					@Handler(condition = "e.getType() == SambaEventType.PLAY")
					public void onPlay(SambaEvent e) {
						Log.i("evt", e.getData().toString());
					}

					@Override
					@Handler(condition = "e.getType() == SambaEventType.PAUSED")
					public void onPause(SambaEvent event) {
						Log.i("evt", "Paused!");
					}

					@Override
					@Handler(condition = "e.getType() == SambaEventType.FINISHED")
					public void onFinish(SambaEvent event) {
						Log.i("evt", "Finished!");
					}
				});

				p.setMedia(media);
				p.play();
			}
		});
	}
}
