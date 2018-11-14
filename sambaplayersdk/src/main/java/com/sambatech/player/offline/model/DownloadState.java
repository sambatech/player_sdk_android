package com.sambatech.player.offline.model;

import com.sambatech.player.model.SambaMedia;

public class DownloadState {

    public static int STARTED = 0;
    public static int COMPLETED = 1;
    public static int CANCELED = 2;
    public static int FAILED = 3;

    private SambaMedia sambaMedia;
    private float downloadPercentage;


    public DownloadState(SambaMedia sambaMedia, float downloadPercentage) {
        this.sambaMedia = sambaMedia;
        this.downloadPercentage = downloadPercentage;
    }

    public SambaMedia getSambaMedia() {
        return sambaMedia;
    }

    public void setSambaMedia(SambaMedia sambaMedia) {
        this.sambaMedia = sambaMedia;
    }

    public float getDownloadPercentage() {
        return downloadPercentage;
    }

    public void setDownloadPercentage(float downloadPercentage) {
        this.downloadPercentage = downloadPercentage;
    }
}
