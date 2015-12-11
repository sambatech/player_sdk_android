package com.sambatech.player.event;

import com.sambatech.player.model.SambaMedia;

/**
 * Listeners representing server responses.
 *
 * @author zanol - 4/12/15
 */
public abstract class SambaApiCallback {

	public void onMediaResponse(SambaMedia media) {}

	public void onMediaListResponse(SambaMedia[] mediaList) {}
}
