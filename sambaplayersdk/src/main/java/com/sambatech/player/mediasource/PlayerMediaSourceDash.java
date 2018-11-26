package com.sambatech.player.mediasource;

import android.net.Uri;

import com.google.android.exoplayer2.offline.FilteringManifestParser;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashChunkSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser;
import com.sambatech.player.offline.SambaDownloadManager;

/**
 * Created by luizbyrro on 28/11/2017.
 */

public class PlayerMediaSourceDash extends PlayerMediaSource implements PlayerMediaSourceInterface {

    private DashChunkSource.Factory dashChunkSourceFactory;

    public PlayerMediaSourceDash(PlayerInstanceDefault playerInstanceDefault, String url) {
        super(playerInstanceDefault);
        dashChunkSourceFactory = new DefaultDashChunkSource.Factory(playerInstanceDefault.mediaDataSourceFactory);
        setUrl(url);
    }

    @Override
    public void setUrl(String url) {
        super.setUrl(url);
        Uri uri = Uri.parse(url);

        MediaSource mediaSource;

        if (SambaDownloadManager.getInstance().isConfigured()) {
            mediaSource = new DashMediaSource.Factory(SambaDownloadManager.getInstance().buildDataSourceFactory())
                    .setManifestParser(
                            new FilteringManifestParser<>(new DashManifestParser(), SambaDownloadManager.getInstance().getOfflineStreamKeys(uri)))
                    .createMediaSource(uri);
        } else {
            mediaSource = new DashMediaSource.Factory(dashChunkSourceFactory,
                    playerInstanceDefault.mediaDataSourceFactory)
                    .createMediaSource(Uri.parse(url));
        }

        setMediaSource(mediaSource);
    }

    @Override
    public void destroy() {
        super.destroy();
        dashChunkSourceFactory = null;
    }

}
