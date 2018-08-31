package com.sambatech.player.plugins;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.sambatech.player.R;
import com.sambatech.player.SambaPlayer;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.model.SambaMediaConfig;

import org.jose4j.base64url.internal.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

class TrackingLive implements Tracking {


    private static final String EVENT_LOAD = "lo";
    private static final String EVENT_PLAY = "pl";
    private static final String EVENT_PAUSE = "pa";
    private static final String EVENT_ONLINE = "on";
    private static final String EVENT_COMPLETE = "co";

    private static  final long TASK_EVENT_ON_TIME = 60000;

    private static final String ORIGIN_SDK_ANDROID = "player.sambatech.sdk.android";

    private SambaMediaConfig media;
    private SttmLive sttmLive;

    private Context context;

    private SambaPlayerListener playerListener = new SambaPlayerListener() {

        @Override
        public void onLoad(SambaEvent event) {
            super.onLoad(event);
            sttmLive.trackLoadEvent();
        }

        @Override
        public void onPlay(SambaEvent event) {
            super.onPlay(event);

            if (!sttmLive.isOnEventTaskRunning()) {
                sttmLive.trackPlayAndONEvent();
                sttmLive.startOnEventTask();
            }

        }

        @Override
        public void onPause(SambaEvent event) {
            super.onPause(event);
            sttmLive.cancelOnEventTask();
            sttmLive.trackPauseEvent();
        }

        @Override
        public void onError(SambaEvent event) {
            super.onError(event);
            sttmLive.cancelOnEventTask();
        }
    };


    public void onLoad(@NonNull SambaPlayer player) {
        Log.i("track", "load");

        this.context = player.getContext();
        this.media = (SambaMediaConfig) player.getMedia();

        init();

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
        if (media.sttmUrl != null && sttmLive == null) {
            sttmLive = new SttmLive();
        }

        if (media.projectHash != null && media.id != null) {
            SambaEventBus.subscribe(playerListener);
        }
    }

    private Sttm2 getSttm2() throws Exception {

        InputStream inputStream = null;
        Scanner scanner = null;

        try {

            URL myURL = new URL(getSttm2RequestUrl());
            HttpURLConnection conn = (HttpURLConnection) myURL.openConnection();
            conn.setRequestProperty("Content-Type", "text/plain");

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 299) {
                inputStream = conn.getInputStream();
                scanner = new Scanner(inputStream);
                return getSttm2ResponseFromScanner(scanner);
            }

        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();

                if (scanner != null)
                    scanner.close();
            } catch (IOException e) {
                Log.w(getClass().getSimpleName(), "Error closing server request", e);
            }
        }

        return null;
    }


    private Sttm2 getSttm2ResponseFromScanner(Scanner scanner) throws Exception {

        int delimiter = media.id != null ? Integer.parseInt(media.id.split("(?=\\d[a-zA-Z]*$)")[1].substring(0, 1)) : 0;

        Scanner scannerDelimited = scanner.useDelimiter("\\A");

        if (scannerDelimited.hasNext()) {
            String token = scannerDelimited.next();

            token = token.substring(delimiter, token.length() - delimiter).replaceAll("-", "+").replaceAll("_", "/");

            switch (token.length() % 4) {
                case 0:
                    break;
                case 2:
                    token += "==";
                    break;
                case 3:
                    token += "=";
                    break;
                default:
            }

            byte[] tokenBytes = Base64.decodeBase64(token);
            String jsonString = new String(tokenBytes);
            JSONObject json = new JSONObject(jsonString);

            if (json.has("key")) {
                Sttm2 response = new Sttm2();
                response.key = json.getString("key");

                if (json.has("url")) {
                    response.url = json.getString("url");
                } else {
                    response.url = media.sttm2Url;
                }

                return response;
            }
        }

        return null;
    }

    private String getSttm2RequestUrl() {

        String baseUrl = context.getString(R.string.player_endpoint_prod);

        return String.format("%s%s/jwt/%s", baseUrl, media.projectHash, media.id);
    }

    private class RequestTrackerLiveTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            try {
                Sttm2 sttm2 = getSttm2();

                String event = params[0];

                if (sttm2 != null && !TextUtils.isEmpty(sttm2.key) && !TextUtils.isEmpty(sttm2.url)) {
                    String sttmUrl = String.format("%s?event=%s&cid=%s&pid=%s&lid=%s&cat=%s&org=%s", sttm2.url, event, media.clientId, media.projectId, media.id, media.categoryId, ORIGIN_SDK_ANDROID);

                    URL url = new URL(sttmUrl);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Authorization", "Bearer " + sttm2.key);
                    conn.setRequestProperty("User-Agent", System.getProperty("http.agent"));

                    conn.getResponseCode();

                }

            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Failed to fetch URL", e);
            }

            return null;
        }
    }

    private class SttmLive {

        private Timer sttm2Timer;

        private boolean isEventTimerTaskRunning;


        public void startOnEventTask() {
            sttm2Timer = new Timer();
            sttm2Timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    trackOnEvent();
                }
            }, TASK_EVENT_ON_TIME, TASK_EVENT_ON_TIME);
            isEventTimerTaskRunning = true;
        }

        public boolean isOnEventTaskRunning() {
            return isEventTimerTaskRunning;
        }

        public void cancelOnEventTask() {
            if (sttm2Timer != null) {
                sttm2Timer.cancel();
                sttm2Timer.purge();
                sttm2Timer = null;
                isEventTimerTaskRunning = false;
            }
        }

        void trackPlayEvent() {
            sendEvents(EVENT_PLAY);
        }

        void trackPlayAndONEvent() {
            sendEvents(EVENT_ONLINE, EVENT_PLAY);
        }

        void trackPauseEvent() {
            sendEvents(EVENT_PAUSE);
        }

        void trackLoadEvent() {
            sendEvents(EVENT_LOAD);
        }

        void trackComplete() {
            sendEvents(EVENT_COMPLETE);
        }

        void trackOnEvent() {
            sendEvents(EVENT_ONLINE);
        }

        private void sendEvents(String... events) {

            String finalEvents = null;
            if (events.length > 1) {
                finalEvents = TextUtils.join(",", events);
            } else {
                finalEvents = events[0];
            }

            new RequestTrackerLiveTask().execute(finalEvents);
        }

        void destroy() {
            cancelOnEventTask();
        }

    }


    private class Sttm2 {
        public String key;
        public String url;
    }

}
