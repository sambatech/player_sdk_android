/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sambatech.player.offline;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.widget.Toast;

import com.google.android.exoplayer2.offline.ActionFile;
import com.google.android.exoplayer2.offline.DownloadAction;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadManager.TaskState;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.offline.TrackKey;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.sambatech.player.SambaApi;
import com.sambatech.player.event.SambaApiCallback;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaConfig;
import com.sambatech.player.model.SambaMediaRequest;
import com.sambatech.player.offline.listeners.LicenceDrmCallback;
import com.sambatech.player.offline.listeners.SambaDownloadListener;
import com.sambatech.player.offline.listeners.SambaDownloadRequestListener;
import com.sambatech.player.offline.model.DownloadData;
import com.sambatech.player.offline.model.SambaDownloadRequest;
import com.sambatech.player.offline.model.SambaTrack;

import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Tracks media that has been downloaded.
 *
 * <p>Tracked downloads are persisted using an {@link ActionFile}, however in a real application
 * it's expected that state will be stored directly in the application's media database, so that it
 * can be queried efficiently together with other information about the media.
 */
public class SambaDownloadTracker implements DownloadManager.Listener {


    private static final String TAG = "SambaDownloadTracker";

    private final Context context;
    private final DataSource.Factory dataSourceFactory;
    private final CopyOnWriteArraySet<SambaDownloadListener> listeners;
    private final HashMap<Uri, DownloadAction> trackedDownloadStates;
    private List<SambaMediaConfig> sambaMedias;
    private final ActionFile actionFile;
    private final Handler actionFileWriteHandler;

    public SambaDownloadTracker(
            Context context,
            DataSource.Factory dataSourceFactory,
            File actionFile,
            DownloadAction.Deserializer... deserializers) {
        this.context = context.getApplicationContext();
        this.dataSourceFactory = dataSourceFactory;
        this.actionFile = new ActionFile(actionFile);
        listeners = new CopyOnWriteArraySet<>();
        trackedDownloadStates = new HashMap<>();
        sambaMedias = OfflineUtils.getPersistedSambaMedias();
        HandlerThread actionFileWriteThread = new HandlerThread("SambaDownloadTracker");
        actionFileWriteThread.start();
        actionFileWriteHandler = new Handler(actionFileWriteThread.getLooper());

        loadTrackedActions(deserializers.length > 0 ? deserializers : DownloadAction.getDefaultDeserializers());
    }

    public void addListener(SambaDownloadListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SambaDownloadListener listener) {
        listeners.remove(listener);
    }

    public boolean isDownloaded(Uri uri) {
        return trackedDownloadStates.containsKey(uri);
    }

    public boolean isDownloaded(@NonNull SambaMedia sambaMedia) {
        SambaMediaConfig sambaMediaConfig = (SambaMediaConfig) sambaMedia;
        Uri uri = Uri.parse(sambaMediaConfig.downloadUrl);

        return trackedDownloadStates.containsKey(uri) && CollectionUtils.exists(sambaMedias, media -> media.id.equals(sambaMediaConfig.id));
    }

    @SuppressWarnings("unchecked")
    public List<StreamKey> getOfflineStreamKeys(Uri uri) {
        if (!trackedDownloadStates.containsKey(uri)) {
            return Collections.emptyList();
        }
        return trackedDownloadStates.get(uri).getKeys();
    }

    public void prepareDownload(@NonNull SambaDownloadRequest sambaDownloadRequest, @NonNull SambaDownloadRequestListener requestListener) {

        SambaApi api = new SambaApi(SambaDownloadManager.getInstance().getAppInstance().getApplicationContext(), "");
        api.requestMedia(new SambaMediaRequest(sambaDownloadRequest.getProjectHash(), sambaDownloadRequest.getMediaId()), new SambaApiCallback() {
            @Override
            public void onMediaResponse(SambaMedia media) {

                if (isDownloaded(media)) {
                    requestListener.onDownloadRequestFailed(new Error("Media already downloaded"), "Media already downloaded");
                } else {
                    SambaMediaConfig sambaMediaConfig = (SambaMediaConfig) media;
                    sambaDownloadRequest.setSambaMedia(sambaMediaConfig);

                    if (sambaMediaConfig.drmRequest != null) {

                        if (sambaDownloadRequest.getDrmToken() != null && !sambaDownloadRequest.getDrmToken().isEmpty()) {
                            sambaMediaConfig.drmRequest.setToken(sambaDownloadRequest.getDrmToken());
                        }

                        OfflineUtils.getLicenseDrm(sambaMediaConfig, new LicenceDrmCallback() {
                            @Override
                            public void onLicencePrepared(byte[] licencePayload) {
                                sambaMediaConfig.drmRequest.setDrmOfflinePayload(Base64.encodeToString(licencePayload, Base64.DEFAULT));
                                StartDownloadHelper startDownloadHelper = new StartDownloadHelper(context, dataSourceFactory, sambaDownloadRequest, requestListener);
                                startDownloadHelper.start();
                            }

                            @Override
                            public void onLicenceError(Error error) {
                                requestListener.onDownloadRequestFailed(error, "Error to request DRM licence");
                            }
                        });

                    } else {
                        StartDownloadHelper startDownloadHelper = new StartDownloadHelper(context, dataSourceFactory, sambaDownloadRequest, requestListener);
                        startDownloadHelper.start();
                    }
                }

            }

            @Override
            public void onMediaResponseError(Exception e, SambaMediaRequest request) {
                requestListener.onDownloadRequestFailed(new Error(e), "Error to request SambaMedia");
            }
        });

    }

    public void performDownload(@NonNull SambaDownloadRequest sambaDownloadRequest) {

        if (isDownloaded(sambaDownloadRequest.getSambaMedia())) {
            Toast.makeText(context, "Media já baixada", Toast.LENGTH_SHORT).show();
        } else {
            if (isValidRequest(sambaDownloadRequest)) {
                startDownload(sambaDownloadRequest);
            }
        }

    }

    private boolean isValidRequest(SambaDownloadRequest sambaDownloadRequest) {

        return sambaDownloadRequest.getDownloadHelper() != null
                && sambaDownloadRequest.getSambaMedia() != null
                && sambaDownloadRequest.getSambaTracksForDownload() != null
                && !sambaDownloadRequest.getSambaTracksForDownload().isEmpty();

    }

    private List<SambaTrack> buildFinalTracks(SambaDownloadRequest sambaDownloadRequest) {

        List<SambaTrack> selectedTracks = sambaDownloadRequest.getSambaTracksForDownload();
        List<SambaTrack> audioTracks = sambaDownloadRequest.getSambaAudioTracks();

        if (selectedTracks != null && !selectedTracks.isEmpty()) {
            if (audioTracks != null && !audioTracks.isEmpty()) {

                for (SambaTrack audioTrack : audioTracks) {
                    if (!selectedTracks.contains(audioTrack)) {
                        selectedTracks.add(audioTrack);
                    }
                }
            }

        } else {
            selectedTracks = new ArrayList<>();
        }

        return selectedTracks;
    }

    // DownloadManager.Listener

    @Override
    public void onInitialized(DownloadManager downloadManager) {
        // Do nothing.
    }

    @Override
    public void onTaskStateChanged(DownloadManager downloadManager, TaskState taskState) {
        DownloadAction action = taskState.action;
        Uri uri = action.uri;
        if ((action.isRemoveAction && taskState.state == TaskState.STATE_COMPLETED)
                || (!action.isRemoveAction && taskState.state == TaskState.STATE_FAILED)) {
            // A download has been removed, or has failed. Stop tracking it.
            if (trackedDownloadStates.remove(uri) != null) {
                handleTrackedDownloadStatesChanged();
            }

            SambaMediaConfig sambaMediaConfig = CollectionUtils.find(sambaMedias, media -> media.downloadUrl.equals(uri.toString()));

            if (sambaMediaConfig != null && sambaMedias.remove(sambaMediaConfig)) {
                OfflineUtils.persistSambaMedias(sambaMedias);
            }

        }


//        for (SambaDownloadListener listener : listeners) {
//            listener.onDownloadStateChanged();
//        }
    }

    @Override
    public void onIdle(DownloadManager downloadManager) {
        // Do nothing.
    }

    // Internal methods

    private void loadTrackedActions(DownloadAction.Deserializer[] deserializers) {
        try {
            DownloadAction[] allActions = actionFile.load(deserializers);
            for (DownloadAction action : allActions) {
                trackedDownloadStates.put(action.uri, action);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to load tracked actions", e);
        }
    }

    private void handleTrackedDownloadStatesChanged() {
        final DownloadAction[] actions = trackedDownloadStates.values().toArray(new DownloadAction[0]);
        actionFileWriteHandler.post(
                () -> {
                    try {
                        actionFile.store(actions);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to store tracked actions", e);
                    }
                });
    }

    private void startDownload(SambaDownloadRequest sambaDownloadRequest) {

        List<SambaTrack> finalTracks =  buildFinalTracks(sambaDownloadRequest);
        List<TrackKey> trackKeys = new ArrayList<>(CollectionUtils.collect(finalTracks, input -> input.getTrackKey()));

        Double totalDownloadSize = buildDownloadSize(finalTracks);

        byte[] downloadData = buildDownloadData(sambaDownloadRequest.getSambaMedia().title, totalDownloadSize);

        DownloadAction downloadAction = sambaDownloadRequest.getDownloadHelper().getDownloadAction(downloadData, trackKeys);

        if (trackedDownloadStates.containsKey(downloadAction.uri)) {
            return;
        }

        trackedDownloadStates.put(downloadAction.uri, downloadAction);
        sambaMedias.add((SambaMediaConfig) sambaDownloadRequest.getSambaMedia());
        OfflineUtils.persistSambaMedias(sambaMedias);

        handleTrackedDownloadStatesChanged();
        startServiceWithAction(downloadAction);
    }

    private Double buildDownloadSize(List<SambaTrack> finalTracks) {

        Double totalSize = 0D;

        for (SambaTrack finalTrack : finalTracks) {
            totalSize += finalTrack.getSizeInMB();
        }

        return totalSize;
    }

    private byte[] buildDownloadData(String mediaTitle, Double totalDownload) {

        DownloadData downloadData = new DownloadData(mediaTitle, totalDownload);

        String json = new Gson().toJson(downloadData, DownloadData.class);

        return Util.getUtf8Bytes(json);
    }

    private void startServiceWithAction(DownloadAction action) {
        DownloadService.startWithAction(context, SambaDownloadService.class, action, false);
    }


}
