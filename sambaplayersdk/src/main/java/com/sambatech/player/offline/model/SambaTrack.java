package com.sambatech.player.offline.model;

import com.google.android.exoplayer2.offline.TrackKey;

public class SambaTrack {

    private String title;
    private Double sizeInMB;
    private TrackKey trackKey;
    private int width;
    private int height;
    private boolean isAudio;



    public SambaTrack(String title, Double sizeInMB, TrackKey trackKey, int width, int height) {
        this.title = title;
        this.sizeInMB = sizeInMB;
        this.trackKey = trackKey;
        this.width = width;
        this.height = height;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getSizeInMB() {
        return sizeInMB;
    }

    public void setSizeInMB(Double sizeInMB) {
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


    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return String.format("%s - %.2f MB", title, sizeInMB);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SambaTrack)) return false;

        SambaTrack that = (SambaTrack) o;

        if (width != that.width) return false;
        if (height != that.height) return false;
        if (isAudio != that.isAudio) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (sizeInMB != null ? !sizeInMB.equals(that.sizeInMB) : that.sizeInMB != null)
            return false;

        if (trackKey != null) {
            if (that.trackKey != null) {
                 return trackKey.groupIndex == that.trackKey.groupIndex
                         && trackKey.periodIndex == that.trackKey.periodIndex
                         && trackKey.trackIndex == that.trackKey.trackIndex;
            } else {
                return false;
            }
        } else {
            return that.trackKey == null;
        }

    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (sizeInMB != null ? sizeInMB.hashCode() : 0);
        result = 31 * result + (trackKey != null ? trackKey.hashCode() : 0);
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + (isAudio ? 1 : 0);
        return result;
    }
}
