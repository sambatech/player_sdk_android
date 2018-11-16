package com.sambatech.player.offline.model;

import android.support.annotation.NonNull;

import com.sambatech.player.model.SambaMedia;

import java.util.List;

public class SambaDownloadRequest {

    private String mediaId;
    private String projectHash;
    private String drmToken;

    private SambaMedia sambaMedia;

    private List<SambaTrack> sambaVideoTracks;
    private List<SambaTrack> sambaAudioTracks;


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
}
