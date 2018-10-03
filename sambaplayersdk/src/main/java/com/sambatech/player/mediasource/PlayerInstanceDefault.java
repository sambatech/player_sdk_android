package com.sambatech.player.mediasource;

import android.content.Context;
import android.os.Handler;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;
import com.sambatech.player.model.SambaMediaConfig;

/**
 * Created by luizbyrro on 29/11/2017.
 */

public class PlayerInstanceDefault {

    Handler mainHandler;
    BandwidthMeter bandwidthMeter;
    AdaptiveTrackSelection.Factory adaptiveTrackSelectionFactory;
    DefaultTrackSelector trackSelector;
    private DefaultRenderersFactory renderersFactory;
    Context context;
    DataSource.Factory mediaDataSourceFactory;

    private HttpMediaDrmCallback drmCallback;
    private DefaultDrmSessionManager drmSessionManager;

    public PlayerInstanceDefault(Context context, SambaMediaConfig media) {

        boolean isDRM = media.drmRequest != null;

        this.context = context;
        this.mainHandler = new Handler();
        this.bandwidthMeter = new DefaultBandwidthMeter();
        this.adaptiveTrackSelectionFactory =  new AdaptiveTrackSelection.Factory(bandwidthMeter);
        this.trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);

        if (isDRM) {
            drmCallback = new HttpMediaDrmCallback(media.drmRequest.getLicenseUrl(), new DefaultHttpDataSourceFactory("user-agent"));
            try {
                drmSessionManager = new DefaultDrmSessionManager(C.WIDEVINE_UUID,
                        FrameworkMediaDrm.newInstance(C.WIDEVINE_UUID), drmCallback, null, mainHandler, null);
            } catch (UnsupportedDrmException e) {
                e.printStackTrace();
            }
            this.renderersFactory = new DefaultRenderersFactory(this.context, drmSessionManager);
        } else {
            this.renderersFactory = new DefaultRenderersFactory(this.context);
        }

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
        drmCallback = null;
        drmSessionManager = null;
    }
}
