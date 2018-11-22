package com.sambatech.player.offline.model;

import com.sambatech.player.model.SambaMedia;

public class SambaSubtitle {

    private String title;
    private SambaMedia.Caption caption;

    public SambaSubtitle(String title, SambaMedia.Caption caption) {
        this.title = title;
        this.caption = caption;
    }

    public String getTitle() {
        return title;
    }

    public SambaMedia.Caption getCaption() {
        return caption;
    }
}
