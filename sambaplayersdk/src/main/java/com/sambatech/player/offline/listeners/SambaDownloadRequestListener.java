package com.sambatech.player.offline.listeners;

import com.sambatech.player.offline.model.SambaDownloadRequest;

public interface SambaDownloadRequestListener {
    void onDownloadRequestPrepared(SambaDownloadRequest sambaDownloadRequest);
    void onDownloadRequestFailed(Error error, String msg);
}
