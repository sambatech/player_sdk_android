package com.sambatech.player.offline.listeners;

import com.sambatech.player.offline.model.DownloadState;

public interface SambaDownloadListener {
    void onDownloadStateChanged(DownloadState downloadState);
}
