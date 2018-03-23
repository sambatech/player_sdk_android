package com.sambatech.player.mediasource;

import android.net.Uri;

import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.dash.DashChunkSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;

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
        setMediaSource(new DashMediaSource.Factory(dashChunkSourceFactory,
                playerInstanceDefault.mediaDataSourceFactory)
                .createMediaSource(Uri.parse(url),
                        playerInstanceDefault.mainHandler,
                        null));
    }

    @Override
    public void destroy() {
        super.destroy();
        dashChunkSourceFactory = null;
    }

}
