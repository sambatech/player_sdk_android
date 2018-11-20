package com.sambatech.sample.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.sambatech.player.SambaApi;
import com.sambatech.player.SambaPlayer;
import com.sambatech.player.event.SambaApiCallback;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaConfig;
import com.sambatech.player.model.SambaMediaRequest;
import com.sambatech.player.offline.SambaDownloadManager;
import com.sambatech.player.offline.listeners.SambaDownloadListener;
import com.sambatech.player.offline.listeners.SambaDownloadRequestListener;
import com.sambatech.player.offline.model.DownloadData;
import com.sambatech.player.offline.model.DownloadState;
import com.sambatech.player.offline.model.SambaDownloadRequest;
import com.sambatech.player.offline.model.SambaTrack;
import com.sambatech.player.plugins.DrmRequest;
import com.sambatech.sample.R;
import com.sambatech.sample.adapters.MediasOfflineAdapter;
import com.sambatech.sample.model.MediaInfo;
import com.sambatech.sample.model.OnMediaClickListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OfflineActivity extends AppCompatActivity implements OnMediaClickListener, DialogInterface.OnClickListener, SambaDownloadListener {


    private RecyclerView recyclerView;
    private SambaPlayer sambaPlayer;
    private MediasOfflineAdapter adapter;
    private ListView tracksDialogList;
    private ArrayAdapter<SambaTrack> tracksAdapter;
    private SambaDownloadRequest actualDownloadRequest;
    private List<MediaInfo> mediaInfos;


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
        mediaInfo1.setDrmToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImY5NTRiMTIzLTI1YzctNDdmYy05MmRjLThkODY1OWVkNmYwMCJ9.eyJzdWIiOiJkYW1hc2lvLXVzZXIiLCJpc3MiOiJkaWVnby5kdWFydGVAc2FtYmF0ZWNoLmNvbS5iciIsImp0aSI6IklIRzlKZk1aUFpIS29MeHNvMFhveS1BZG83bThzWkNmNW5OVWdWeFhWSTg9IiwiZXhwIjoxNTQyODAzODYzLCJpYXQiOjE1NDI3MTc0NjMsImFpZCI6ImRhbWFzaW8ifQ.MsHAcCyS-PSWoovDVS2K4OVC3Z6mK-wbxzZF5J7XW_w");
        mediaInfo1.setAutoPlay(true);


        MediaInfo mediaInfo2 = new MediaInfo();
        mediaInfo2.setTitle("Media Playplus");
        mediaInfo2.setProjectHash("fad2b4a201ef2305d06cb817da1bd262");
        mediaInfo2.setId("ca60065f62e83445a4c5ae91abd3eacf");
        mediaInfo2.setEnvironment(SambaMediaRequest.Environment.PROD);
        mediaInfo2.setControlsEnabled(true);
        mediaInfo2.setAutoPlay(true);

        MediaInfo mediaInfo3 = new MediaInfo();

        mediaInfo3.setTitle("Media Legenda");
        mediaInfo3.setProjectHash("964b56b4b184c2a29e3c2065a7a15038");
        mediaInfo3.setId("b4c134b2a297d9eacfe7c7852fa86312");
        mediaInfo3.setEnvironment(SambaMediaRequest.Environment.PROD);
        mediaInfo3.setControlsEnabled(true);
        mediaInfo3.setAutoPlay(true);

        mediaInfos = Arrays.asList(
                mediaInfo1,
                mediaInfo2,
                mediaInfo3
        );

        adapter = new MediasOfflineAdapter(mediaInfos, this);
        recyclerView.setAdapter(adapter);

        SambaEventBus.subscribe(playerListener);
        SambaDownloadManager.getInstance().addDownloadListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.offline_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cancelAll:
                SambaDownloadManager.getInstance().cancelAllDownloads();
                break;
            case R.id.stopAll:
                if (item.getTitle().equals(getString(R.string.pausar_todos_downloads))) {
                    SambaDownloadManager.getInstance().stopAllDownloads();
                    item.setTitle(getString(R.string.reiniciar_todos_downloads));
                } else {
                    SambaDownloadManager.getInstance().startStoppedDownloads();
                    item.setTitle(getString(R.string.pausar_todos_downloads));
                }
        }

        return super.onOptionsItemSelected(item);
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


        if (SambaDownloadManager.getInstance().isDownloading(mediaInfo.getId())) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Pergunta")
                    .setMessage("Deseja cancelar o download de: \n\n" + mediaInfo.getTitle())
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        SambaDownloadManager.getInstance().cancelDownload(mediaInfo.getId());
                    })
                    .setNegativeButton(android.R.string.cancel, null);

            builder.create().show();

        } else if (SambaDownloadManager.getInstance().isDownloaded(mediaInfo.getId())) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Pergunta")
                    .setMessage("Deseja apagar o download de: \n\n" + mediaInfo.getTitle())
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        SambaDownloadManager.getInstance().deleteDownload(mediaInfo.getId());
                    })
                    .setNegativeButton(android.R.string.cancel, null);

            builder.create().show();

        } else {
            SambaDownloadRequest sambaDownloadRequest = new SambaDownloadRequest(mediaInfo.getProjectHash(), mediaInfo.getId());

            if (mediaInfo.getDrmToken() != null && !mediaInfo.getDrmToken().isEmpty()) {
                sambaDownloadRequest.setDrmToken(mediaInfo.getDrmToken());
            }

            SambaDownloadManager.getInstance().prepareDownload(sambaDownloadRequest, new SambaDownloadRequestListener() {
                @Override
                public void onDownloadRequestPrepared(SambaDownloadRequest sambaDownloadRequest) {
                    buildDialog(sambaDownloadRequest);
                }

                @Override
                public void onDownloadRequestFailed(Error error, String msg) {

                }
            });
        }

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
        SambaDownloadManager.getInstance().removeDownloadListener(this);

    }

    private void buildDialog(SambaDownloadRequest sambaDownloadRequest) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.exo_download_description)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, null);

        actualDownloadRequest = sambaDownloadRequest;

        LayoutInflater dialogInflater = LayoutInflater.from(builder.getContext());
        View dialogView = dialogInflater.inflate(R.layout.start_download_dialog, null);

        tracksAdapter = new ArrayAdapter<>(
                builder.getContext(), android.R.layout.simple_list_item_multiple_choice);
        tracksDialogList = dialogView.findViewById(R.id.representation_list);
        tracksDialogList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        tracksAdapter.addAll(sambaDownloadRequest.getSambaVideoTracks());
        tracksAdapter.addAll(sambaDownloadRequest.getSambaAudioTracks());

        tracksDialogList.setAdapter(tracksAdapter);

        if ((sambaDownloadRequest.getSambaVideoTracks().size() + sambaDownloadRequest.getSambaAudioTracks().size()) > 0) {
            builder.setView(dialogView);
        }

        builder.create().show();

    }


    @Override
    public void onClick(DialogInterface dialogInterface, int i) {

        List<SambaTrack> selectedTracks = new ArrayList<>();
        for (int j = 0; j < tracksDialogList.getChildCount(); j++) {
            if (tracksDialogList.isItemChecked(j)) {
                selectedTracks.add((SambaTrack) tracksAdapter.getItem(j));
            }
        }

        actualDownloadRequest.setSambaTracksForDownload(selectedTracks);
        SambaDownloadManager.getInstance().performDownload(actualDownloadRequest);

    }

    @Override
    public void onDownloadStateChanged(DownloadState downloadState) {

        DownloadData downloadData = downloadState.downloadData;
        MediaInfo mediaInfo = null;
        int position = 0;
        for (int i = 0; i < mediaInfos.size(); i++) {
            if (mediaInfos.get(i).getId().equals(downloadData.getMediaId())) {
                mediaInfo = mediaInfos.get(i);
                position = i;
                break;
            }
        }

        if (mediaInfo != null) {
            mediaInfo.setDownloadState(downloadState);
        }

        adapter.notifyItemChanged(position);

    }
}
