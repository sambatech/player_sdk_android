package com.sambatech.player.offline;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.ProgressiveDownloadHelper;
import com.google.android.exoplayer2.offline.TrackKey;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.offline.DashDownloadHelper;
import com.google.android.exoplayer2.source.hls.offline.HlsDownloadHelper;
import com.google.android.exoplayer2.ui.TrackNameProvider;
import com.google.android.exoplayer2.upstream.DataSource;
import com.sambatech.player.model.SambaMediaConfig;
import com.sambatech.player.offline.listeners.SambaDownloadRequestListener;
import com.sambatech.player.offline.model.SambaDownloadRequest;
import com.sambatech.player.offline.model.SambaTrack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class StartDownloadHelper implements DownloadHelper.Callback {

    private static final String TAG = "StartDownloadHelper";
    private final DownloadHelper downloadHelper;
    private final String name;

    private final List<TrackKey> trackKeys;
    private final TrackNameProvider trackNameProvider;
    private final Context context;

    private final SambaDownloadRequest sambaDownloadRequest;
    private final SambaDownloadRequestListener requestListener;
    private final List<SambaTrack> sambaVideoTracks;
    private final List<SambaTrack> sambaAudioTracks;
    private final SambaMediaConfig sambaMediaConfig;

    public StartDownloadHelper(
            Context context,
            DataSource.Factory dataSourceFactory,
            SambaDownloadRequest sambaDownloadRequest,
            SambaDownloadRequestListener requestListener
    ) {
        this.sambaDownloadRequest = sambaDownloadRequest;
        this.requestListener = requestListener;
        this.sambaMediaConfig = (SambaMediaConfig) sambaDownloadRequest.getSambaMedia();
        this.context = context;
        this.downloadHelper = OfflineUtils.getDownloadHelper(Uri.parse(sambaMediaConfig.downloadUrl), sambaMediaConfig.type, dataSourceFactory);
        this.name = sambaMediaConfig.title;
        trackNameProvider = new SambaTrackNameProvider(context.getResources());
        trackKeys = new ArrayList<>();
        this.sambaVideoTracks = new ArrayList<>();
        this.sambaAudioTracks = new ArrayList<>();
    }

    public void start() {
        downloadHelper.prepare(this);
    }

    @Override
    public void onPrepared(DownloadHelper helper) {
        for (int i = 0; i < downloadHelper.getPeriodCount(); i++) {
            TrackGroupArray trackGroups = downloadHelper.getTrackGroups(i);
            for (int j = 0; j < trackGroups.length; j++) {
                TrackGroup trackGroup = trackGroups.get(j);
                for (int k = 0; k < trackGroup.length; k++) {
                    TrackKey trackKey = new TrackKey(i, j, k);
                    SambaTrack sambaTrack = new SambaTrack(
                            trackNameProvider.getTrackName(trackGroup.getFormat(k)),
                            OfflineUtils.getSizeInMB(trackGroup.getFormat(k).bitrate, (long) sambaMediaConfig.duration),
                            trackKey,
                            trackGroup.getFormat(k).width,
                            trackGroup.getFormat(k).height
                            );

                    if (OfflineUtils.inferPrimaryTrackType(trackGroup.getFormat(k)) == C.TRACK_TYPE_AUDIO) {
                        sambaTrack.setAudio(true);
                        sambaAudioTracks.add(sambaTrack);
                    } else {
                        sambaVideoTracks.add(sambaTrack);
                    }

                }
            }
        }

        sambaDownloadRequest.setSambaVideoTracks(sambaVideoTracks);
        sambaDownloadRequest.setSambaAudioTracks(sambaAudioTracks);
        sambaDownloadRequest.setDownloadHelper(downloadHelper);

        requestListener.onDownloadRequestPrepared(sambaDownloadRequest);
    }

    @Override
    public void onPrepareError(DownloadHelper helper, IOException e) {
        requestListener.onDownloadRequestFailed(new Error(e), "Error to start download");
    }

}