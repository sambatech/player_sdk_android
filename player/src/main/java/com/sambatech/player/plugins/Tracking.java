package com.sambatech.player.plugins;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.sambatech.player.SambaPlayer;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.model.SambaMediaConfig;
import com.sambatech.player.utils.Helpers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Plugin responsible for sending different kinds of player media tracking data.
 *
 * @author Leandro Zanol - 28/12/15
 */
public class Tracking {

	private SambaPlayer player;
	private SambaMediaConfig media;
	private Sttm sttm;

	public Tracking() {
		SambaEventBus.subscribe(new SambaPlayerListener() {
			@Override
			public void onLoad(SambaEvent event) {
				player = (SambaPlayer) event.getData();
				media = (SambaMediaConfig) player.getMedia();

				init();
			}

			@Override
			public void onStart(SambaEvent event) {

			}

			@Override
			public void onPlay(SambaEvent event) {
				if (sttm != null)
					sttm.track("play");
			}

			@Override
			public void onPause(SambaEvent event) {
			}

			@Override
			public void onStop(SambaEvent event) {
			}

			@Override
			public void onProgress(SambaEvent event) {
				if (sttm != null)
					sttm.track("r00,r01,r02,r03,r04");
			}

			@Override
			public void onFinish(SambaEvent event) {

			}

			@Override
			public void onUnload(SambaEvent event) {
				SambaEventBus.unsubscribe(this);

				if (sttm != null)
					sttm.destroy();
			}
		});
	}

	private void init() {
		if (media.sttmUrl != null && sttm == null)
			sttm = new Sttm();
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

	private class Sttm extends TimerTask {

		private List<String> targets = new ArrayList<>();
		private Timer sttmTimer;

		public Sttm() {
			sttmTimer = new Timer();
			sttmTimer.scheduleAtFixedRate(this, 0, 5000);
		}

		@Override
		public void run() {
			new UrlTracker().execute(String.format("%s?sttmm=%s&sttmk=%s&sttms=%s&sttmu=123&sttmw=%s",
					media.sttmUrl, TextUtils.join(",", targets), media.sttmKey, media.sessionId,
					String.format("pid:%s/cat:%s/mid:%s", media.projectId, media.categoryId, media.hash)));

			targets.clear();
		}

		public void track(String target) {
			targets.add(target);
		}

		public void destroy() {
			if (sttmTimer != null) {
				sttmTimer.cancel();
				sttmTimer.purge();
				sttmTimer = null;
			}
		}
	}
}
