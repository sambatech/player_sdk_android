package com.sambatech.player.mediasource;

import android.net.Uri;
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
        setMediaSource(new HlsMediaSource.Factory(SambaDownloadManager.getInstance().buildDataSourceFactory())
                .setPlaylistParserFactory(
                        new DefaultHlsPlaylistParserFactory(SambaDownloadManager.getInstance().getOfflineStreamKeys(uri)))
                .createMediaSource(uri));
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
