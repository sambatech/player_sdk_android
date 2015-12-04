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
				Log.i("asdf", media.title + " " + media.url + " " + media.type);

				//https://video-gru1-1.xx.fbcdn.net/hvideo-xpa1/v/t42.4659-2/12295945_947458022012335_503839406_n.mp4?oh=20884df6dada69db87202bcabe5609b5&oe=5661F047
				new ImaPlayer(MainActivity.this, (FrameLayout)findViewById(R.id.player_view),
						new Video(media.url, Video.VideoType.HLS),
						"Video Test!");
			}
		});
	}
}
