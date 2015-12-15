package com.sambatech.player.model;

/**
 * @author Leandro Zanol - 11/12/15
 */
public class SambaMediaRequest {

	public String projectId;
	public String mediaId;
	public String streamName;
	public String[] streamUrls;

	/**
	 * Represents a VOD media request.
	 *
	 * @param projectId - ID of the project (player hash)
	 * @param mediaId - ID of the media
	 */
	public SambaMediaRequest(String projectId, String mediaId) {
		this(projectId, mediaId, null, (String)null);
	}

	/**
	 * Represents a live request (with stream name).
	 *
	 * @param projectId - ID of the project (player hash)
	 * @param mediaId - ID of the media
	 * @param streamName - Name of the stream (live only)
	 */
	public SambaMediaRequest(String projectId, String mediaId, String streamName) {
		this(projectId, mediaId, streamName, (String)null);
	}

	/**
	 * Represents a direct live request (via URL).
	 *
	 * @param projectId - ID of the project (player hash)
	 * @param mediaId - ID of the media
	 * @param streamName - Name of the stream (live only)
	 * @param streamUrl - Alternative URL for live stream (`streamName` will be ignored)
	 */
	public SambaMediaRequest(String projectId, String mediaId, String streamName, String streamUrl) {
		this(projectId, mediaId, streamName, streamUrl != null ? new String[]{streamUrl} : null);
	}

	/**
	 * Represents a direct live request (via URL) with other backup URLs.
	 *
	 * @param projectId - ID of the project (player hash)
	 * @param mediaId - ID of the media
	 * @param streamName - Name of the stream (live only)
	 * @param streamUrls - Alternative URLs for live stream (`streamName` will be ignored)
	 */
	public SambaMediaRequest(String projectId, String mediaId, String streamName, String[] streamUrls) {
		this.projectId = projectId;
		this.mediaId = mediaId;
		this.streamName = streamName;
		this.streamUrls = streamUrls != null ? streamUrls : new String[]{};
	}

	@Override
	public String toString() {
		return String.format("projectId: %s, mediaId: %s, streamName: %s, streamUrls: %s", projectId, mediaId, streamName, streamUrls.length > 0 ? streamUrls[0] : null);
	}
}
