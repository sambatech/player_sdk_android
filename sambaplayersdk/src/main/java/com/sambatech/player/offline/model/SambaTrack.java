package com.sambatech.player.offline.model;

import com.google.android.exoplayer2.offline.TrackKey;

public class SambaTrack {

    private String title;
    private long sizeInMB;
    private TrackKey trackKey;
    private boolean isAudio;


    public SambaTrack(String title, long sizeInMB) {
        this.title = title;
        this.sizeInMB = sizeInMB;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getSizeInMB() {
        return sizeInMB;
    }

    public void setSizeInMB(long sizeInMB) {
        this.sizeInMB = sizeInMB;
    }

    public TrackKey getTrackKey() {
        return trackKey;
    }

    public void setTrackKey(TrackKey trackKey) {
        this.trackKey = trackKey;
    }

    public boolean isAudio() {
        return isAudio;
    }

    public void setAudio(boolean audio) {
        isAudio = audio;
    }
}
