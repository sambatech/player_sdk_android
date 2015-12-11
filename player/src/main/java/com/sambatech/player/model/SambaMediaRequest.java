package com.sambatech.player.model;

/**
 * @author Leandro Zanol - 11/12/15
 */
public class SambaMediaRequest {

	public String projectId;
	public String mediaId;
	public String streamName;
	public String streamUrl;

	public SambaMediaRequest(String projectId, String mediaId) {
		this(projectId, mediaId, null, null);
	}

	public SambaMediaRequest(String projectId, String mediaId, String streamName) {
		this(projectId, mediaId, streamName, null);
	}

	/**
	 * @param projectId - ID of the project (player hash)
	 * @param mediaId - ID of the media
	 * @param streamName - Name of the stream (live only)
	 * @param streamUrl - Alternative URL for live stream (`streamName` will be ignored)
	 */
	public SambaMediaRequest(String projectId, String mediaId, String streamName, String streamUrl) {
		this.projectId = projectId;
		this.mediaId = mediaId;
		this.streamName = streamName;
		this.streamUrl = streamUrl;
	}
}
