package com.sambatech.player.event;

import com.sambatech.player.model.SambaMedia;

import java.util.List;

/**
 * Created by zanol on 12/4/15.
 *
 * Carries listeners representing server responses.
 */
public abstract class SambaApiListener {

	public void onMediaResponse(SambaMedia media) {}

	public void onMediaListResponse(List<SambaMedia> mediaList) {}
}
