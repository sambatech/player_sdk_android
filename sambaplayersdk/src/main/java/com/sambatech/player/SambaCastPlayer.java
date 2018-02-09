package com.sambatech.player;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.framework.CastSession;
import com.sambatech.player.cast.CastOptionsProvider;
import com.sambatech.player.cast.SambaCast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by luizbyrro on 08/02/2018.
 */

public class SambaCastPlayer implements Player {

    SambaCast sambaCast;

    int lastPosition = 0;

    long currentProgress = 0;
    long currentDuration = 0;

    EventListener eventListener;

    // Internal state.
    private TrackGroupArray currentTrackGroups;
    private TrackSelectionArray currentTrackSelection;
    private int playbackState;
    private int repeatMode;
    private int currentWindowIndex;
    private boolean playWhenReady;
    private long lastReportedPositionMs;
    private int pendingSeekCount;
    private int pendingSeekWindowIndex;
    private long pendingSeekPositionMs;
    private boolean waitingForInitialTimeline;

    Cast.MessageReceivedCallback messageReceived = new Cast.MessageReceivedCallback() {
        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace, String message)  {
            Log.i("Message Received", castDevice.toString() + namespace + message);

            try {
                JSONObject jsonObject = new JSONObject(message);

                if (jsonObject.has("progress") && jsonObject.has("duration")) {
                    currentProgress = jsonObject.getInt("progress");
                    currentDuration = jsonObject.getInt("duration");

                    eventListener.onPlayerStateChanged(true, Player.STATE_READY );

                }
                else if (jsonObject.has("type")) {
                    jsonObject = new JSONObject(message);
                    String type = jsonObject.getString("type");

                    if (type.equalsIgnoreCase("finish"))
                        sambaCast.stopCasting();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public SambaCastPlayer(@NonNull SambaCast sambaCast) {
        this.sambaCast = sambaCast;
    }

    public void setMessageListener(CastSession castSession) {
        try {
            castSession.setMessageReceivedCallbacks(CastOptionsProvider.CUSTOM_NAMESPACE,messageReceived);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addListener(EventListener listener) {
        eventListener = listener;
    }

    @Override
    public void removeListener(EventListener listener) {

    }

    @Override
    public int getPlaybackState() {
        return 0;
    }

    @Override
    public void setPlayWhenReady(boolean playWhenReady) {
        if(playWhenReady){
            sambaCast.playCast();
        } else {
            sambaCast.pauseCast();
        }
    }

    @Override
    public boolean getPlayWhenReady() {
        return false;
    }

    @Override
    public void setRepeatMode(int repeatMode) {

    }

    @Override
    public int getRepeatMode() {
        return 0;
    }

    @Override
    public void setShuffleModeEnabled(boolean shuffleModeEnabled) {

    }

    @Override
    public boolean getShuffleModeEnabled() {
        return false;
    }

    @Override
    public boolean isLoading() {
        return false;
    }

    @Override
    public void seekToDefaultPosition() {

    }

    @Override
    public void seekToDefaultPosition(int windowIndex) {

    }

    @Override
    public void seekTo(long positionMs) {
        sambaCast.seekTo((int) positionMs);
    }

    @Override
    public void seekTo(int windowIndex, long positionMs) {

    }

    @Override
    public void setPlaybackParameters(@Nullable PlaybackParameters playbackParameters) {

    }

    @Override
    public PlaybackParameters getPlaybackParameters() {
        return null;
    }

    @Override
    public void stop() {
        sambaCast.stopCasting();
    }

    @Override
    public void release() {

    }

    @Override
    public int getRendererCount() {
        return 0;
    }

    @Override
    public int getRendererType(int index) {
        return 0;
    }

    @Override
    public TrackGroupArray getCurrentTrackGroups() {
        return null;
    }

    @Override
    public TrackSelectionArray getCurrentTrackSelections() {
        return null;
    }

    @Nullable
    @Override
    public Object getCurrentManifest() {
        return null;
    }

    @Override
    public Timeline getCurrentTimeline() {
        return new Timeline() {
            @Override
            public int getWindowCount() {
                return 0;
            }

            @Override
            public Window getWindow(int windowIndex, Window window, boolean setIds, long defaultPositionProjectionUs) {
                return null;
            }

            @Override
            public int getPeriodCount() {
                return 0;
            }

            @Override
            public Period getPeriod(int periodIndex, Period period, boolean setIds) {
                return null;
            }

            @Override
            public int getIndexOfPeriod(Object uid) {
                return 0;
            }
        };
    }

    @Override
    public int getCurrentPeriodIndex() {
        return 0;
    }

    @Override
    public int getCurrentWindowIndex() {
        return 0;
    }

    @Override
    public int getNextWindowIndex() {
        return 0;
    }

    @Override
    public int getPreviousWindowIndex() {
        return 0;
    }

    @Override
    public long getDuration() {
        return currentDuration;
    }

    @Override
    public long getCurrentPosition() {
        return getContentPosition();
    }

    @Override
    public long getBufferedPosition() {
        return 0;
    }

    @Override
    public int getBufferedPercentage() {
        return 0;
    }

    @Override
    public boolean isCurrentWindowDynamic() {
        return false;
    }

    @Override
    public boolean isCurrentWindowSeekable() {
        return false;
    }

    @Override
    public boolean isPlayingAd() {
        return false;
    }

    @Override
    public int getCurrentAdGroupIndex() {
        return 0;
    }

    @Override
    public int getCurrentAdIndexInAdGroup() {
        return 0;
    }

    @Override
    public long getContentPosition() {
        return 0;
    }
}
