package com.sambatech.player;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.libraries.mediaframework.exoplayerextensions.Video;
import com.sambatech.player.event.SambaApiListener;
import com.sambatech.player.model.SambaMedia;

import java.util.List;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SambaApi api = new SambaApi("token");

		api.requestMedia("34f07cf52fd85ccfc41a39bcf499e83b", "0632f26a442ba9ba3bb9067a45e239e2", new SambaApiListener() {
			@Override
			public void onMediaResponse(SambaMedia media) {
				SambaPlayer p = (SambaPlayer)findViewById(R.id.samba_player);
				p.setMedia(media);
				p.play();
			}
		});
	}
}
