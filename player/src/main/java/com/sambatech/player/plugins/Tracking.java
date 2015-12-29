package com.sambatech.player.plugins;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.sambatech.player.SambaPlayer;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.model.SambaMediaConfig;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Plugin responsible for sending different kinds of player media metrics.
 *
 * @author Leandro Zanol - 28/12/15
 */
public class Tracking {

	//private static final int TOTAL_TRIES = 3;

	static { new Tracking(); }

	private SambaPlayer player;
	private SambaMediaConfig media;

	public Tracking() {
		SambaEventBus.subscribe(new SambaPlayerListener() {
			@Override
			public void onLoad(SambaEvent event) {
				player = (SambaPlayer)event.getData();
				media = (SambaMediaConfig) player.getMedia();

				track(media.sttmUrl);
			}

			@Override
			public void onStart(SambaEvent event) {}

			@Override
			public void onPlay(SambaEvent event) {}

			@Override
			public void onPause(SambaEvent event) {}

			@Override
			public void onStop(SambaEvent event) {}

			@Override
			public void onProgress(SambaEvent event) {}

			@Override
			public void onFinish(SambaEvent event) {}
		});
	}

	private void track(String url) {
		new UrlTracker().execute(url);
	}

	private class UrlTracker extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			try {
				for (String url : params)
					new URL(url).openConnection().connect();
			}
			catch (IOException e) {
				Log.e(getClass().getSimpleName(), "Failed to fetch URL", e);
			}

			return null;
		}
	}
}
