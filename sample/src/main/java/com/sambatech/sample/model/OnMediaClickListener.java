package com.sambatech.sample.model;

import android.view.View;

public interface OnMediaClickListener {

    public void onMediaClicked(MediaInfo mediaInfo, View ciew);
    public void onDownloadButtonClicked(MediaInfo mediaInfo, View view);

}
