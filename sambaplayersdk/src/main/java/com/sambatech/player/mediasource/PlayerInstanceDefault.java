package com.sambatech.player.mediasource;

import android.content.Context;
import android.os.Handler;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;

/**
 * Created by luizbyrro on 29/11/2017.
 */

public class PlayerInstanceDefault {

    Handler mainHandler;
    BandwidthMeter bandwidthMeter;
    AdaptiveTrackSelection.Factory adaptiveTrackSelectionFactory;
    DefaultTrackSelector trackSelector;
    DefaultRenderersFactory renderersFactory;
    Context context;
    DataSource.Factory mediaDataSourceFactory;

    public PlayerInstanceDefault(Context context) {
        this.context = context;
        this.mainHandler = new Handler();
        this.bandwidthMeter = new DefaultBandwidthMeter();
        this.adaptiveTrackSelectionFactory =  new AdaptiveTrackSelection.Factory(bandwidthMeter);
        this.trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);
        this.renderersFactory = new DefaultRenderersFactory(this.context);
        this.mediaDataSourceFactory = new DefaultDataSourceFactory(this.context, Util.getUserAgent(this.context, "mediaPlayerSample"), (TransferListener<? super DataSource>) bandwidthMeter);
    }

    public SimpleExoPlayer createPlayerInstance() {
        return ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);
    }

    public void destroy() {
        mainHandler = null;
        bandwidthMeter = null;
        adaptiveTrackSelectionFactory = null;
        trackSelector = null;
        renderersFactory = null;
        mediaDataSourceFactory = null;
    }
}
