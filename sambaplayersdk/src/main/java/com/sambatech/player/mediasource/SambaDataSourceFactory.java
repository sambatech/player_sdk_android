package com.sambatech.player.mediasource;

import android.content.Context;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;

public class SambaDataSourceFactory implements DataSource.Factory {

    private final TransferListener bandwidthMeter;
    private DefaultHttpDataSourceFactory defaultHttpDataSourceFactory;
    private Context context;

    public SambaDataSourceFactory(Context context, String agent, TransferListener bandwidthMeter) {
        this.context = context;
        this.bandwidthMeter = bandwidthMeter;
        this.defaultHttpDataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(context, "mediaPlayerSample"), bandwidthMeter);
    }

    @Override
    public DataSource createDataSource() {

        DefaultHttpDataSource defaultHttpDataSource = (DefaultHttpDataSource) defaultHttpDataSourceFactory.createDataSource();
        defaultHttpDataSource.setRequestProperty("teste-header", "value-test");

        return new DefaultDataSource(context, bandwidthMeter, defaultHttpDataSource);

    }
}
