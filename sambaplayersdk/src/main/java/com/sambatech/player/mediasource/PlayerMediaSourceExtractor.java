package com.sambatech.player.mediasource;

import android.net.Uri;

import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;

/**
 * Created by luizbyrro on 28/11/2017.
 */

public class PlayerMediaSourceExtractor extends PlayerMediaSource implements PlayerMediaSourceInterface {

    public PlayerMediaSourceExtractor(PlayerInstanceDefault playerInstanceDefault, String url) {
        super(playerInstanceDefault);
        this.setUrl(url);
    }

    @Override
    public void setUrl(String url) {
        super.setUrl(url);
        setMediaSource(new ExtractorMediaSource.Factory(this.playerInstanceDefault.mediaDataSourceFactory).createMediaSource(Uri.parse(url), this.playerInstanceDefault.mainHandler, null));
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
