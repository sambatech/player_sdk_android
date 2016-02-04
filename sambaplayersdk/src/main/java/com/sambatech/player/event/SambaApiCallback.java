package com.sambatech.player.event;

import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaRequest;

/**
 * Listeners representing server responses.
 *
 * @author Leandro Zanol - 4/12/15
 */
public abstract class SambaApiCallback {

	/**
	 * Fired up after a unique success response from the server.
	 * @param media SambaMedia
	 */
	public void onMediaResponse(SambaMedia media) {}

	/**
	 * Fired up after a set of success responses from the server.
	 * @param mediaList SambaMedia
	 */
	public void onMediaListResponse(SambaMedia[] mediaList) {}

	/**
	 *
	 * Fired up after an error response from the server.
	 * @param msg Error message
	 * @param request Original request
	 */
	public void onMediaResponseError(String msg, SambaMediaRequest request) {}
}
