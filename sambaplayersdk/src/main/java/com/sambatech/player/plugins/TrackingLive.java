package com.sambatech.player.plugins;

import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
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

class TrackingLive implements Tracking {

    private SambaMediaConfig media;
    private SttmLive sttmLive;

    private SambaPlayerListener playerListener = new SambaPlayerListener() {
        @Override
        public void onStart(SambaEvent event) {
            Log.i("sttmLive", "onstart");
            init();

            if (sttmLive != null)
                sttmLive.trackStart();
        }

        @Override
        public void onProgress(SambaEvent event) {
            if (sttmLive != null)
                sttmLive.trackProgress((float) event.getDataAll()[0], (float) event.getDataAll()[1]);
        }

        @Override
        public void onFinish(SambaEvent event) {
            if (sttmLive != null)
                sttmLive.trackComplete();
        }
    };

    public void onLoad(@NonNull SambaPlayer player) {
        Log.i("track", "load");
        media = (SambaMediaConfig) player.getMedia();

        if (media.projectHash != null && media.id != null)
            SambaEventBus.subscribe(playerListener);

        PluginManager.getInstance().notifyPluginLoaded(this);
    }

    public void onInternalPlayerCreated(@NonNull SimpleExoPlayerView internalPlayer) {
    }

    public void onDestroy() {
        Log.i("track", "destroy");
        SambaEventBus.unsubscribe(playerListener);

        if (sttmLive != null) {
            sttmLive.destroy();
            sttmLive = null;
        }
    }

    private void init() {
        if (media.sttmUrl != null && sttmLive == null)
            sttmLive = new SttmLive();
    }

    private class UrlTrackerLiveTask extends AsyncTask<String, Void, Void> {

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
            } catch (IOException e) {
                Log.e(getClass().getSimpleName(), "Failed to fetch URL", e);
            }

            return null;
        }
    }

    private class SttmLive extends TimerTask {

        private List<String> targets = new ArrayList<>();
        private Timer sttmTimer;
        private TreeSet<String> progresses = new TreeSet<>();
        private HashSet<Integer> trackedRetentions = new HashSet<>();

        SttmLive() {
            sttmTimer = new Timer();
            sttmTimer.scheduleAtFixedRate(this, 0, 5000);
        }

        @Override
        public void run() {
            if (targets.size() == 0)
                return;

            // TODO: add version to STTM (BuildConfig.VERSION_NAME)
            new UrlTrackerLiveTask().execute(String.format("%s?sttmm=%s&sttmk=%s&sttms=%s&sttmu=123&sttmw=%s",
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
            int p = (int) (100 * time / duration);

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
