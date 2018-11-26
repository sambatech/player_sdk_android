package com.sambatech.player.offline.model;

import com.google.android.exoplayer2.offline.DownloadManager;

public class ProgressMessageEvent {

    private DownloadManager.TaskState taskState;

    public ProgressMessageEvent(DownloadManager.TaskState taskState) {
        this.taskState = taskState;
    }

    public DownloadManager.TaskState getTaskState() {
        return taskState;
    }

    public void setTaskState(DownloadManager.TaskState taskState) {
        this.taskState = taskState;
    }
}
