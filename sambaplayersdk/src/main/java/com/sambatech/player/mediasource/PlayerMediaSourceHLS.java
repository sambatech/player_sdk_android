package com.sambatech.player.mediasource;

import android.net.Uri;

import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.DefaultHlsPlaylistParserFactory;
import com.sambatech.player.offline.SambaDownloadManager;

/**
 * Created by luizbyrro on 28/11/2017.
 */

public class PlayerMediaSourceHLS extends PlayerMediaSource implements PlayerMediaSourceInterface {

    public PlayerMediaSourceHLS(PlayerInstanceDefault playerInstanceDefault, String url) {
        super(playerInstanceDefault);
        this.setUrl(url);
    }

    @Override
    public void setUrl(String url) {
        super.setUrl(url);
        Uri uri = Uri.parse(url);

        MediaSource mediaSource;

        if (SambaDownloadManager.getInstance().isConfigured()) {
            mediaSource = new HlsMediaSource.Factory(SambaDownloadManager.getInstance().buildDataSourceFactory())
                    .setPlaylistParserFactory(
                            new DefaultHlsPlaylistParserFactory(SambaDownloadManager.getInstance().getOfflineStreamKeys(uri)))
                    .createMediaSource(uri);
        } else {
            mediaSource = new HlsMediaSource.Factory(playerInstanceDefault.mediaDataSourceFactory).createMediaSource(uri);
        }

        setMediaSource(mediaSource);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
