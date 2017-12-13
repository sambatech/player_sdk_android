package com.sambatech.player.mediasource;

import android.net.Uri;

import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;

/**
 * Created by luizbyrro on 28/11/2017.
 */

public class PlayerMediaSourceExtractor extends PlayerMediaSource implements PlayerMediaSourceInterface {

    private ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

    public PlayerMediaSourceExtractor(PlayerInstanceDefault playerInstanceDefault, String url) {
        super(playerInstanceDefault);
        this.extractorsFactory = new DefaultExtractorsFactory();
        this.setUrl(url);
    }

    @Override
    public void setUrl(String url) {
        super.setUrl(url);
        setMediaSource(new ExtractorMediaSource(Uri.parse(url),this.playerInstanceDefault.mediaDataSourceFactory, extractorsFactory, this.playerInstanceDefault.mainHandler, null));
    }

    @Override
    public void destroy() {
        super.destroy();
        extractorsFactory = null;
    }
}
