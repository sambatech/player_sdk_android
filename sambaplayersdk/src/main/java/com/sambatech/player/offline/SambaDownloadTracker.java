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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Base64;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.drm.DrmInitData;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.OfflineLicenseHelper;
import com.google.android.exoplayer2.offline.ActionFile;
import com.google.android.exoplayer2.offline.DownloadAction;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadManager.TaskState;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.offline.ProgressiveDownloadHelper;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.offline.TrackKey;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashUtil;
import com.google.android.exoplayer2.source.dash.manifest.DashManifest;
import com.google.android.exoplayer2.source.dash.offline.DashDownloadHelper;
import com.google.android.exoplayer2.source.hls.offline.HlsDownloadHelper;
import com.google.android.exoplayer2.source.smoothstreaming.offline.SsDownloadHelper;
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider;
import com.google.android.exoplayer2.ui.TrackNameProvider;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.sambatech.player.R;
import com.sambatech.player.offline.model.SambaDownloadListener;

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
        HandlerThread actionFileWriteThread = new HandlerThread("SambaDownloadTracker");
        actionFileWriteThread.start();
        actionFileWriteHandler = new Handler(actionFileWriteThread.getLooper());
        loadTrackedActions(
                deserializers.length > 0 ? deserializers : DownloadAction.getDefaultDeserializers());
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

    @SuppressWarnings("unchecked")
    public List<StreamKey> getOfflineStreamKeys(Uri uri) {
        if (!trackedDownloadStates.containsKey(uri)) {
            return Collections.emptyList();
        }
        return trackedDownloadStates.get(uri).getKeys();
    }

    public void requestDownload(Context context, String name, Uri uri, String extension) {
        if (isDownloaded(uri)) {
            DownloadAction removeAction =
                    getDownloadHelper(uri, extension).getRemoveAction(Util.getUtf8Bytes(name));
            startServiceWithAction(removeAction);
            SambaDownloadManager.getInstance().getSharedPreferences().edit().clear().apply();
        } else {
            StartDownloadHelper helper = new StartDownloadHelper(context, getDownloadHelper(uri, extension), name);
            helper.prepare();

            @SuppressLint("StaticFieldLeak") AsyncTask<Uri, String, String> task = new AsyncTask<Uri, String, String>() {
                @Override
                protected String doInBackground(Uri... uris) {

                    try {
                        Uri uri = uris[0];

                        String licenseUrl = "https://samba-drm.live.ott.irdeto.com/licenseServer/widevine/v1/record/license?ls_session=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjdlN2Q5YmE4LWUzNzctNGFkMi1iZGIzLTdmOWRiOWYzZGI2ZiJ9.eyJzdWIiOiJyZWNvcmQtdXNlciIsImlzcyI6ImRpZWdvLmR1YXJ0ZUBzYW1iYXRlY2guY29tLmJyIiwianRpIjoiSUhHOUpmTVpQWkhLb0x4c28wWG95LUFkbzdtOHNaQ2Y1bk5VZ1Z4WFZJOD0iLCJleHAiOjE1NDI2NTY3NjEsImlhdCI6MTU0MjA1MTk2MSwiYWlkIjoicmVjb3JkIn0.w_0gr1zTOuD7dx7vEMOUbPXzqSxXuyULmHj09PJM46M&SubContentType=Default&CrmId=record&AccountId=record&ContentId=5fdb1a23aeb42f71f5734dce028cc458";


                        if (Util.SDK_INT < 18) {
                            return null;
                        }

                        CustomDrmCallback customDrmCallback = new CustomDrmCallback(
                                SambaDownloadManager.getInstance().buildHttpDataSourceFactory(),
                                licenseUrl
                        );


                        String offlineAssetKeyIdStr = SambaDownloadManager.getInstance().
                                getSharedPreferences().getString(SambaDownloadManager.KEY_OFFLINE_OFFSET_ID, SambaDownloadManager.EMPTY);

                        byte[] offlineAssetKeyId = Base64.decode(offlineAssetKeyIdStr, Base64.DEFAULT);

                        DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory("EXO-TEST");
                        OfflineLicenseHelper<FrameworkMediaCrypto> offlineLicenseHelper = OfflineLicenseHelper.newWidevineInstance(licenseUrl, httpDataSourceFactory);

//                        OfflineLicenseHelper<FrameworkMediaCrypto> offlineLicenseHelper =
//                                new OfflineLicenseHelper<>(C.WIDEVINE_UUID, mediaDrm, mediaDrmCallback, null);

                        Pair<Long, Long> remainingSecPair = null;
                        try {
                            remainingSecPair = offlineLicenseHelper.getLicenseDurationRemainingSec(offlineAssetKeyId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

//        Log.e(TAG, " License remaining Play time : " + remainingSecPair.first + ", Purchase time : " + remainingSecPair.second);
                        if (SambaDownloadManager.EMPTY.equals(offlineAssetKeyIdStr) || remainingSecPair == null || (remainingSecPair.first == 0 || remainingSecPair.second == 0)) {
                            //            String path = getIntent().getStringExtra(EXTRA_OFFLINE_URI);
                            //            File file = getUriForManifest(path);
                            //            Uri uri = Uri.fromFile(file);
                            //            InputStream is = new FileInputStream(file);
                            //            Log.e(TAG, "will start download now");


                            DataSource dataSource = httpDataSourceFactory.createDataSource();

                            DashManifest dashManifest = DashUtil.loadManifest(dataSource, uri); //movie url
                            DrmInitData drmInitData = DashUtil.loadDrmInitData(dataSource, dashManifest.getPeriod(0));
                            offlineAssetKeyId = offlineLicenseHelper.downloadLicense(drmInitData);
                            Pair<Long, Long> p = offlineLicenseHelper.getLicenseDurationRemainingSec(offlineAssetKeyId);
                            android.util.Log.e(TAG, "download done : " + p.toString());

                            SharedPreferences sharedPreferences = SambaDownloadManager.getInstance().getSharedPreferences();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(SambaDownloadManager.KEY_OFFLINE_OFFSET_ID,
                                    Base64.encodeToString(offlineAssetKeyId, Base64.DEFAULT));
                            editor.commit();

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return "";
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                }
            };


            task.execute(uri);


        }
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

    private void startDownload(DownloadAction action) {
        if (trackedDownloadStates.containsKey(action.uri)) {
            // This content is already being downloaded. Do nothing.
            return;
        }
        trackedDownloadStates.put(action.uri, action);
        handleTrackedDownloadStatesChanged();
        startServiceWithAction(action);
    }

    private void startServiceWithAction(DownloadAction action) {
        DownloadService.startWithAction(context, SambaDownloadService.class, action, false);
    }

    private DownloadHelper getDownloadHelper(Uri uri, String extension) {
        int type = Util.inferContentType(uri, extension);
        switch (type) {
            case C.TYPE_DASH:
                return new DashDownloadHelper(uri, dataSourceFactory);
            case C.TYPE_SS:
                return new SsDownloadHelper(uri, dataSourceFactory);
            case C.TYPE_HLS:
                return new HlsDownloadHelper(uri, dataSourceFactory);
            case C.TYPE_OTHER:
                return new ProgressiveDownloadHelper(uri);
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }


}
