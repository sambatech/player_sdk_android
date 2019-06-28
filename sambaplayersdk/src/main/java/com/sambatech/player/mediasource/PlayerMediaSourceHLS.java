package com.sambatech.player.mediasource;

import android.net.Uri;

import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.DefaultHlsPlaylistParserFactory;
import com.sambatech.player.offline.SambaDownloadManager;
import com.peer5.sdk.Peer5Sdk;

public class PlayerMediaSourceHLS extends PlayerMediaSource implements PlayerMediaSourceInterface {

    public PlayerMediaSourceHLS(PlayerInstanceDefault playerInstanceDefault, String url, Boolean enablePeer5) {
        super(playerInstanceDefault);
        this.setEnablePeer5(enablePeer5);
        this.setUrl(url);
    }

    @Override
    public void setUrl(String url) {
        super.setUrl(url);
        Uri uri = Uri.parse(url);
        MediaSource mediaSource;

        if(this.getEnablePeer5()) {
            String peer5Url = Peer5Sdk.getPeer5StreamUrl(url);
            Uri peer5Uri = Uri.parse(peer5Url);

            uri = peer5Uri;
        }

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
