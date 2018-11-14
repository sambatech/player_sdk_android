package com.sambatech.player.offline.model;

public interface SambaDownloadListener {
    void onDownloadStateChanged(DownloadState downloadState);
}
