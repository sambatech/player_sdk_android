package com.sambatech.player.offline.model;

public class DownloadData {

    private String mediaTitle;
    private Double totalDownloadSizeInMB;


    public DownloadData(String mediaTitle, Double totalDownloadSizeInMB) {
        this.mediaTitle = mediaTitle;
        this.totalDownloadSizeInMB = totalDownloadSizeInMB;
    }

    public String getMediaTitle() {
        return mediaTitle;
    }

    public void setMediaTitle(String mediaTitle) {
        this.mediaTitle = mediaTitle;
    }

    public Double getTotalDownloadSizeInMB() {
        return totalDownloadSizeInMB;
    }

    public void setTotalDownloadSizeInMB(Double totalDownloadSizeInMB) {
        this.totalDownloadSizeInMB = totalDownloadSizeInMB;
    }
}
