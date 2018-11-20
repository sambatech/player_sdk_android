package com.sambatech.player.offline.model;

import com.sambatech.player.model.SambaMediaConfig;

public class DownloadData {

    private String mediaId;
    private String mediaTitle;
    private Double totalDownloadSizeInMB;
    private SambaMediaConfig sambaMedia;


    public DownloadData(String mediaId, String mediaTitle, Double totalDownloadSizeInMB, SambaMediaConfig sambaMedia) {
        this.mediaId = mediaId;
        this.mediaTitle = mediaTitle;
        this.totalDownloadSizeInMB = totalDownloadSizeInMB;
        this.sambaMedia = sambaMedia;
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

    public SambaMediaConfig getSambaMedia() {
        return sambaMedia;
    }

    public void setSambaMedia(SambaMediaConfig sambaMedia) {
        this.sambaMedia = sambaMedia;
    }
}
