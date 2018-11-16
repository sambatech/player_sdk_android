package com.sambatech.player.offline;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.offline.DownloadAction;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.ProgressiveDownloadHelper;
import com.google.android.exoplayer2.offline.TrackKey;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.offline.DashDownloadHelper;
import com.google.android.exoplayer2.source.hls.offline.HlsDownloadHelper;
import com.google.android.exoplayer2.source.smoothstreaming.offline.SsDownloadHelper;
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider;
import com.google.android.exoplayer2.ui.TrackNameProvider;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.sambatech.player.R;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaConfig;
import com.sambatech.player.offline.listeners.SambaDownloadRequestListener;
import com.sambatech.player.offline.model.SambaDownloadRequest;

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

    private final DataSource.Factory dataSourceFactory;
    private final SambaDownloadRequest sambaDownloadRequest;
    private final SambaDownloadRequestListener requestListener;

    public StartDownloadHelper(
            Context context,
            DataSource.Factory dataSourceFactory,
            SambaDownloadRequest sambaDownloadRequest,
            SambaDownloadRequestListener requestListener
    ) {
        this.sambaDownloadRequest = sambaDownloadRequest;
        this.requestListener = requestListener;
        SambaMediaConfig sambaMediaConfig = (SambaMediaConfig) sambaDownloadRequest.getSambaMedia();
        this.context = context;
        this.dataSourceFactory = dataSourceFactory;
        this.downloadHelper = getDownloadHelper(Uri.parse(sambaMediaConfig.url), sambaMediaConfig.type);
        this.name = sambaMediaConfig.title;
        trackNameProvider = new DefaultTrackNameProvider(context.getResources());
        trackKeys = new ArrayList<>();
    }

    public void prepare() {
        downloadHelper.prepare(this);
    }

    @Override
    public void onPrepared(DownloadHelper helper) {
        for (int i = 0; i < downloadHelper.getPeriodCount(); i++) {
            TrackGroupArray trackGroups = downloadHelper.getTrackGroups(i);
            for (int j = 0; j < trackGroups.length; j++) {
                TrackGroup trackGroup = trackGroups.get(j);
                for (int k = 0; k < trackGroup.length; k++) {
                    trackKeys.add(new TrackKey(i, j, k));
//                    trackTitles.add(trackNameProvider.getTrackName(trackGroup.getFormat(k)));
                }
            }
        }
        if (!trackKeys.isEmpty()) {
        }
    }

    @Override
    public void onPrepareError(DownloadHelper helper, IOException e) {
        requestListener.onDownloadRequestFailed(new Error(e), "Error to prepare download");
    }


    public void onClick(DialogInterface dialog, int which) {
//        ArrayList<TrackKey> selectedTrackKeys = new ArrayList<>();
//        for (int i = 0; i < representationList.getChildCount(); i++) {
//            if (representationList.isItemChecked(i)) {
//                selectedTrackKeys.add(trackKeys.get(i));
//            }
//        }
//        if (!selectedTrackKeys.isEmpty() || trackKeys.isEmpty()) {
//            // We have selected keys, or we're dealing with single stream content.
//            DownloadAction downloadAction =
//                    downloadHelper.getDownloadAction(Util.getUtf8Bytes(name), selectedTrackKeys);
////            startDownload(downloadAction);
//        }
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