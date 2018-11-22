package com.sambatech.player.mediasource;

import android.content.Context;
import android.os.Handler;
import android.util.Base64;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
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
import com.sambatech.player.offline.SambaDownloadManager;

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

    private DefaultDrmSessionManager drmSessionManager;
    private FrameworkMediaDrm mediaDrm;

    public PlayerInstanceDefault(Context context, SambaMediaConfig media) {

        boolean isDRM = media.drmRequest != null;

        this.context = context;
        this.mainHandler = new Handler();
        this.bandwidthMeter = new DefaultBandwidthMeter();
        this.adaptiveTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        this.trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);

        if (isDRM) {
            try {
                if (media.isOffline) {
                    drmSessionManager = buildOfflineDrmSessionManager(media);
                } else {
                    drmSessionManager = buildOnlineDrmSessionManager(media);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.renderersFactory = new DefaultRenderersFactory(this.context);

//        this.mediaDataSourceFactory = new SambaDataSourceFactory(this.context, Util.getUserAgent(this.context, "mediaPlayerSample"), (TransferListener<? super DataSource>) bandwidthMeter);
        this.mediaDataSourceFactory = new DefaultDataSourceFactory(this.context, Util.getUserAgent(this.context, "mediaPlayerSample"), (TransferListener) bandwidthMeter);
    }

    private DefaultDrmSessionManager buildOnlineDrmSessionManager(SambaMediaConfig media) throws UnsupportedDrmException {

        String userAgent = SambaDownloadManager.getInstance().isConfigured() ? SambaDownloadManager.getInstance().getUserAgent() : Util.getUserAgent(context.getApplicationContext(), "SambaPlayer");

        return new DefaultDrmSessionManager<>(
                C.WIDEVINE_UUID,
                FrameworkMediaDrm.newInstance(C.WIDEVINE_UUID),
                new HttpMediaDrmCallback(media.drmRequest.getLicenseUrl(), new DefaultHttpDataSourceFactory(userAgent)),
                null
        );
    }

    private DefaultDrmSessionManager buildOfflineDrmSessionManager(SambaMediaConfig media) throws UnsupportedDrmException {

        DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;

        String offlineAssetKeyIdStr = media.drmRequest.getDrmOfflinePayload();

        mediaDrm = FrameworkMediaDrm.newInstance(C.WIDEVINE_UUID);

        if (offlineAssetKeyIdStr != null && !offlineAssetKeyIdStr.isEmpty()) {
            drmSessionManager = new DefaultDrmSessionManager<>(
                    C.WIDEVINE_UUID,
                    mediaDrm,
                    new HttpMediaDrmCallback(media.drmRequest.getLicenseUrl(),
                            new DefaultHttpDataSourceFactory(SambaDownloadManager.getInstance().getUserAgent())),
                    null
            );

            byte[] offlineAssetKeyId = Base64.decode(offlineAssetKeyIdStr, Base64.DEFAULT);

            drmSessionManager.setMode(DefaultDrmSessionManager.MODE_QUERY, offlineAssetKeyId);

        }

        return drmSessionManager;
    }

    public SimpleExoPlayer createPlayerInstance() {
        return ExoPlayerFactory.newSimpleInstance(this.context, renderersFactory, trackSelector, drmSessionManager);
    }

    public void destroy() {
        mainHandler = null;
        bandwidthMeter = null;
        adaptiveTrackSelectionFactory = null;
        trackSelector = null;
        renderersFactory = null;
        mediaDataSourceFactory = null;
        if (mediaDrm != null) {
            mediaDrm.release();
        }

        drmSessionManager = null;

    }
}
