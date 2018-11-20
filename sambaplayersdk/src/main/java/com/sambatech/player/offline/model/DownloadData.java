package com.sambatech.player.offline.model;

public class DownloadData {

    private String mediaId;
    private String mediaTitle;
    private Double totalDownloadSizeInMB;


    public DownloadData(String mediaId, String mediaTitle, Double totalDownloadSizeInMB) {
        this.mediaId = mediaId;
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

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }
}
