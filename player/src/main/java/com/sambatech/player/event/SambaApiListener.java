package com.sambatech.player.event;

import com.sambatech.player.model.SambaMedia;

import java.util.List;

/**
 * Listeners representing server responses.
 *
 * @author zanol - 4/12/15
 */
public abstract class SambaApiListener {

	public void onMediaResponse(SambaMedia media) {}

	public void onMediaListResponse(List<SambaMedia> mediaList) {}
}
