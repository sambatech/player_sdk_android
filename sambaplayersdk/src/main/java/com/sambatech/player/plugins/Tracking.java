package com.sambatech.player.plugins;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.libraries.mediaframework.layeredvideo.SimpleVideoPlayer;
import com.sambatech.player.SambaPlayer;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.model.SambaMediaConfig;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

/**
 * Plugin responsible for sending different kinds of player media tracking data.
 *
 * @author Leandro Zanol - 28/12/15
 */
class Tracking implements Plugin {

	private SambaMediaConfig media;
	private Sttm sttm;

	private SambaPlayerListener playerListener = new SambaPlayerListener() {
		@Override
		public void onStart(SambaEvent event) {
			Log.i("sttm", "onstart");
			init();

			if (sttm != null)
				sttm.trackStart();
		}

		@Override
		public void onProgress(SambaEvent event) {
			if (sttm != null)
				sttm.trackProgress((float) event.getDataAll()[0], (float) event.getDataAll()[1]);
		}

		@Override
		public void onFinish(SambaEvent event) {
			if (sttm != null)
				sttm.trackComplete();
		}
	};

	public void onLoad(@NonNull SambaPlayer player) {
		Log.i("track", "load");
		media = (SambaMediaConfig)player.getMedia();

		if (media.projectHash != null && media.id != null)
			SambaEventBus.subscribe(playerListener);

		PluginManager.getInstance().notifyPluginLoaded(this);
	}

	public void onInternalPlayerCreated(@NonNull SimpleVideoPlayer internalPlayer) {}

	public void onDestroy() {
		Log.i("track", "destroy");
		SambaEventBus.unsubscribe(playerListener);

		if (sttm != null) {
			sttm.destroy();
			sttm = null;
		}
	}

	private void init() {
		if (media.sttmUrl != null && sttm == null)
			sttm = new Sttm();
	}

	private class UrlTracker extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			try {
				Log.i(getClass().getSimpleName(), params[0]);

				for (String url : params) {
					URL myURL = new URL(url);
					URLConnection conn = myURL.openConnection();
					conn.setRequestProperty("http.agent", "chrome");
					conn.setDoOutput(false);
					conn.setDoInput(true);
					conn.getInputStream();
				}
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
		private TreeSet<String> progresses = new TreeSet<>();
		private HashSet<Integer> trackedRetentions = new HashSet<>();

		Sttm() {
			sttmTimer = new Timer();
			sttmTimer.scheduleAtFixedRate(this, 0, 5000);
		}

		@Override
		public void run() {
			if (targets.size() == 0)
				return;

			// TODO: add version to STTM (BuildConfig.VERSION_NAME)
			new UrlTracker().execute(String.format("%s?sttmm=%s&sttmk=%s&sttms=%s&sttmu=123&sttmw=%s",
					media.sttmUrl, TextUtils.join(",", targets), media.sttmKey, media.sessionId,
					String.format("pid:%s/cat:%s/mid:%s", media.projectId, media.categoryId, media.id)));


			targets.clear();
		}

		void trackStart() {
			targets.add("play");
		}

		void trackComplete() {
			collectProgress();
			targets.add("complete");
		}

		void trackProgress(float time, float duration) {
			int p = (int)(100*time/duration);

			if (p > 99)
				p = 99;

			progresses.add(String.format(Locale.getDefault(), "p%02d", p));

			if (!trackedRetentions.contains(p))
				progresses.add(String.format(Locale.getDefault(), "r%02d", p));

			trackedRetentions.add(p);

			if (progresses.size() >= 5)
				collectProgress();
		}

		void destroy() {
			if (sttmTimer != null) {
				sttmTimer.cancel();
				sttmTimer.purge();
				sttmTimer = null;
			}
		}

		private void collectProgress() {
			if (progresses.size() == 0)
				return;

			targets.add(TextUtils.join(",", progresses));
			progresses.clear();
		}
	}
}
