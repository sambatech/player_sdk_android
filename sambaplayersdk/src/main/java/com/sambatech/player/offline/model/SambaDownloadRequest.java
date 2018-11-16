package com.sambatech.player.offline.model;

import android.support.annotation.NonNull;

import com.sambatech.player.model.SambaMedia;

import java.util.List;

public class SambaDownloadRequest {

    private String mediaId;
    private String projectHash;
    private String drmToken;

    private List<SambaTrack> sambaTracks;
    private SambaMedia sambaMedia;
    private List<SambaTrack> audioTracks;


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

    public List<SambaTrack> getSambaTracks() {
        return sambaTracks;
    }

    public void setSambaTracks(List<SambaTrack> sambaTracks) {
        this.sambaTracks = sambaTracks;
    }

    public SambaMedia getSambaMedia() {
        return sambaMedia;
    }

    public void setSambaMedia(SambaMedia sambaMedia) {
        this.sambaMedia = sambaMedia;
    }

    public List<SambaTrack> getAudioTracks() {
        return audioTracks;
    }

    public void setAudioTracks(List<SambaTrack> audioTracks) {
        this.audioTracks = audioTracks;
    }
}
