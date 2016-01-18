package com.sambatech.player.event;

import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaRequest;

/**
 * Listeners representing server responses.
 *
 * @author Leandro Zanol - 4/12/15
 */
public abstract class SambaApiCallback {

	public void onMediaResponse(SambaMedia media) {}

	public void onMediaListResponse(SambaMedia[] mediaList) {}

	public void onMediaResponseError(String msg, SambaMediaRequest request) {}
}
