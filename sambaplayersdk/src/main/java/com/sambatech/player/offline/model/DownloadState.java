package com.sambatech.player.offline.model;

import com.sambatech.player.model.SambaMedia;

public class DownloadState {

    public float downloadPercentage;
    public DownloadData downloadData;
    public State state;


    public DownloadState(float downloadPercentage, DownloadData downloadData, State state) {
        this.downloadPercentage = downloadPercentage;
        this.downloadData = downloadData;
        this.state = state;
    }

    public enum State {
        WAITING,
        COMPLETED,
        CANCELED,
        IN_PROGRESS,
        FAILED
    }

}
