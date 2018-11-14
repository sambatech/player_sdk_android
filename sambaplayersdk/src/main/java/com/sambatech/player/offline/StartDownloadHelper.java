package com.sambatech.player.offline;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.exoplayer2.offline.DownloadAction;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.TrackKey;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider;
import com.google.android.exoplayer2.ui.TrackNameProvider;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.sambatech.player.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class StartDownloadHelper implements DownloadHelper.Callback, DialogInterface.OnClickListener {

    private static final String TAG = "StartDownloadHelper";
    private final DownloadHelper downloadHelper;
    private final String name;

    private final AlertDialog.Builder builder;
    private final View dialogView;
    private final List<TrackKey> trackKeys;
    private final ArrayAdapter<String> trackTitles;
    private final ListView representationList;
    private final TrackNameProvider trackNameProvider;
    private final Context context;

    public StartDownloadHelper(Context context, DownloadHelper downloadHelper, String name) {
        this.context = context;
        this.downloadHelper = downloadHelper;
        this.name = name;
        trackNameProvider = new DefaultTrackNameProvider(context.getResources());
        builder = new AlertDialog.Builder(context)
                        .setTitle(R.string.exo_download_description)
                        .setPositiveButton(android.R.string.ok, this)
                        .setNegativeButton(android.R.string.cancel, null);

        // Inflate with the builder's context to ensure the correct style is used.
        LayoutInflater dialogInflater = LayoutInflater.from(builder.getContext());
        dialogView = dialogInflater.inflate(R.layout.start_download_dialog, null);

        trackKeys = new ArrayList<>();
        trackTitles =
                new ArrayAdapter<>(
                        builder.getContext(), android.R.layout.simple_list_item_multiple_choice);
        representationList = dialogView.findViewById(R.id.representation_list);
        representationList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        representationList.setAdapter(trackTitles);
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
                    trackTitles.add(trackNameProvider.getTrackName(trackGroup.getFormat(k)));
                }
            }
        }
        if (!trackKeys.isEmpty()) {
            builder.setView(dialogView);
        }
        builder.create().show();
    }

    @Override
    public void onPrepareError(DownloadHelper helper, IOException e) {
        Toast.makeText(
                context.getApplicationContext(), "Failed to start download", Toast.LENGTH_LONG)
                .show();
        Log.e(TAG, "Failed to start download", e);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        ArrayList<TrackKey> selectedTrackKeys = new ArrayList<>();
        for (int i = 0; i < representationList.getChildCount(); i++) {
            if (representationList.isItemChecked(i)) {
                selectedTrackKeys.add(trackKeys.get(i));
            }
        }
        if (!selectedTrackKeys.isEmpty() || trackKeys.isEmpty()) {
            // We have selected keys, or we're dealing with single stream content.
            DownloadAction downloadAction =
                    downloadHelper.getDownloadAction(Util.getUtf8Bytes(name), selectedTrackKeys);
//            startDownload(downloadAction);
        }
    }
}