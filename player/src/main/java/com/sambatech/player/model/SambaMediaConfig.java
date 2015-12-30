package com.sambatech.player.model;

import com.sambatech.player.utils.Helpers;

/**
 * Internal extension of the media entity for player/plugins config purposes.
 *
 * @author Leandro Zanol - 2/12/15
 */
public class SambaMediaConfig extends SambaMedia {

	public String hash;
	public String projectHash;
	public int projectId;
	public int categoryId;
	public String sessionId = Helpers.getSessionId();
	public int themeColor = 0xFF72BE44;
	public String sttmUrl;
	public String sttmKey;
}
