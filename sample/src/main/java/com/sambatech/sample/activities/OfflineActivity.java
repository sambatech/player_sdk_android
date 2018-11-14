package com.sambatech.sample.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.sambatech.player.SambaApi;
import com.sambatech.player.SambaPlayer;
import com.sambatech.player.event.SambaApiCallback;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaConfig;
import com.sambatech.player.model.SambaMediaRequest;
import com.sambatech.player.plugins.DrmRequest;
import com.sambatech.sample.R;
import com.sambatech.sample.adapters.MediasOfflineAdapter;
import com.sambatech.sample.model.MediaInfo;
import com.sambatech.sample.model.OnMediaClickListener;

import java.util.Arrays;
import java.util.List;

public class OfflineActivity extends AppCompatActivity implements OnMediaClickListener {


    private RecyclerView recyclerView;
    private SambaPlayer sambaPlayer;
    private MediasOfflineAdapter adapter;


    private SambaPlayerListener playerListener = new SambaPlayerListener() {
        @Override
        public void onLoad(SambaEvent e) {

        }

        @Override
        public void onPlay(SambaEvent e) {

        }

        @Override
        public void onPause(SambaEvent e) {

        }

        @Override
        public void onStop(SambaEvent e) {

        }

        @Override
        public void onFinish(SambaEvent e) {

        }

        @Override
        public void onFullscreen(SambaEvent e) {
            if (getActionBar() != null)
                getActionBar().hide();
        }

        @Override
        public void onFullscreenExit(SambaEvent e) {
            if (getActionBar() != null)
                getActionBar().show();
        }

        @Override
        public void onError(SambaEvent e) {

        }

        @Override
        public void onProgress(SambaEvent event) {

        }

        @Override
        public void onDestroy(SambaEvent event) {
            super.onDestroy(event);
        }

        @Override
        public void onCastConnect(SambaEvent event) {
            super.onCastConnect(event);
        }

        @Override
        public void onCastDisconnect(SambaEvent event) {
            super.onCastDisconnect(event);
        }

        @Override
        public void onCastPlay(SambaEvent event) {
            super.onCastPlay(event);
        }

        @Override
        public void onCastPause(SambaEvent event) {
            super.onCastPause(event);
        }

        @Override
        public void onCastFinish(SambaEvent event) {
            super.onCastFinish(event);
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);

        recyclerView = findViewById(R.id.mRecyclerView);
        sambaPlayer = findViewById(R.id.player);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));



        MediaInfo mediaInfo1 = new MediaInfo();
        mediaInfo1.setTitle("Media DRM 1");
        mediaInfo1.setProjectHash("f596f53018dc9150eee6661d891fb1d2");
        mediaInfo1.setId("1171319f6347a0a9c19b0278c0956eb6");
        mediaInfo1.setEnvironment(SambaMediaRequest.Environment.STAGING);
        mediaInfo1.setControlsEnabled(true);
        mediaInfo1.setDrmToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImY5NTRiMTIzLTI1YzctNDdmYy05MmRjLThkODY1OWVkNmYwMCJ9.eyJzdWIiOiJkYW1hc2lvLXVzZXIiLCJpc3MiOiJkaWVnby5kdWFydGVAc2FtYmF0ZWNoLmNvbS5iciIsImp0aSI6IklIRzlKZk1aUFpIS29MeHNvMFhveS1BZG83bThzWkNmNW5OVWdWeFhWSTg9IiwiZXhwIjoxNTQyMjExMDM0LCJpYXQiOjE1NDIyMDkyMzQsImFpZCI6ImRhbWFzaW8ifQ.I0F4wPRqQGF_I-2R0luyor6Abih9JTI60qnVVZBw6Vw");
        mediaInfo1.setAutoPlay(true);


        MediaInfo mediaInfo2 = new MediaInfo();
        mediaInfo2.setTitle("Media Playplus");
        mediaInfo2.setProjectHash("fad2b4a201ef2305d06cb817da1bd262");
        mediaInfo2.setId("ca60065f62e83445a4c5ae91abd3eacf");
        mediaInfo2.setEnvironment(SambaMediaRequest.Environment.PROD);
        mediaInfo2.setControlsEnabled(true);
        mediaInfo2.setAutoPlay(true);

        List<MediaInfo> mediaInfos = Arrays.asList(
                mediaInfo1,
                mediaInfo2
        );

        adapter = new MediasOfflineAdapter(mediaInfos, this);
        recyclerView.setAdapter(adapter);

        SambaEventBus.subscribe(playerListener);

    }

    @Override
    public void onMediaClicked(final MediaInfo mediaInfo, View ciew) {

        SambaApi api = new SambaApi(this, "");
        api.requestMedia(new SambaMediaRequest(mediaInfo.getProjectHash(), mediaInfo.getId()), new SambaApiCallback() {
            @Override
            public void onMediaResponse(SambaMedia media) {

                SambaMediaConfig mediaConfig = (SambaMediaConfig) media;

                if (mediaConfig.drmRequest != null) {
                    DrmRequest drmRequest = mediaConfig.drmRequest;

                    drmRequest.setToken(mediaInfo.getDrmToken());
                }

                sambaPlayer.setMedia(media);
                sambaPlayer.play();
            }
        });

    }

    @Override
    public void onDownloadButtonClicked(MediaInfo mediaInfo, View view) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sambaPlayer != null && sambaPlayer.hasStarted())
            sambaPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SambaEventBus.unsubscribe(playerListener);
    }
}
