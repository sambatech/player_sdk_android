package com.sambatech.player.mediasource;

import android.net.Uri;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;

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
        setMediaSource(new HlsMediaSource(Uri.parse(url), playerInstanceDefault.mediaDataSourceFactory, playerInstanceDefault.mainHandler, null));
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
