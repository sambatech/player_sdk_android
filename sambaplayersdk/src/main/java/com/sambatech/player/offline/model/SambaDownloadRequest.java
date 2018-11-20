package com.sambatech.player.offline.model;

import android.support.annotation.NonNull;

import com.google.android.exoplayer2.offline.DownloadHelper;
import com.sambatech.player.model.SambaMedia;

import java.util.List;

public class SambaDownloadRequest {

    private String mediaId;
    private String projectHash;
    private String drmToken;
    private Double totalDownloadSize;

    private SambaMedia sambaMedia;

    private List<SambaTrack> sambaVideoTracks;
    private List<SambaTrack> sambaAudioTracks;

    private List<SambaTrack> sambaTracksForDownload;

    private DownloadHelper downloadHelper;


    public SambaDownloadRequest(@NonNull String projectHash,@NonNull  String mediaId) {
        this.mediaId = mediaId;
        this.projectHash = projectHash;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getProjectHash() {
        return projectHash;
    }

    public void setProjectHash(String projectHash) {
        this.projectHash = projectHash;
    }

    public String getDrmToken() {
        return drmToken;
    }

    public void setDrmToken(String drmToken) {
        this.drmToken = drmToken;
    }

    public List<SambaTrack> getSambaVideoTracks() {
        return sambaVideoTracks;
    }

    public void setSambaVideoTracks(List<SambaTrack> sambaVideoTracks) {
        this.sambaVideoTracks = sambaVideoTracks;
    }

    public SambaMedia getSambaMedia() {
        return sambaMedia;
    }

    public void setSambaMedia(SambaMedia sambaMedia) {
        this.sambaMedia = sambaMedia;
    }

    public List<SambaTrack> getSambaAudioTracks() {
        return sambaAudioTracks;
    }

    public void setSambaAudioTracks(List<SambaTrack> sambaAudioTracks) {
        this.sambaAudioTracks = sambaAudioTracks;
    }

    public List<SambaTrack> getSambaTracksForDownload() {
        return sambaTracksForDownload;
    }

    public void setSambaTracksForDownload(List<SambaTrack> sambaTracksForDownload) {
        this.sambaTracksForDownload = sambaTracksForDownload;
    }

    public DownloadHelper getDownloadHelper() {
        return downloadHelper;
    }

    public void setDownloadHelper(DownloadHelper downloadHelper) {
        this.downloadHelper = downloadHelper;
    }

    public Double getTotalDownloadSize() {
        return totalDownloadSize;
    }

    public void setTotalDownloadSize(Double totalDownloadSize) {
        this.totalDownloadSize = totalDownloadSize;
    }
}
