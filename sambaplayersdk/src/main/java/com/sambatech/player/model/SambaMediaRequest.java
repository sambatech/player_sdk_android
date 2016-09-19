package com.sambatech.player.model;

/**
 * Data entity that represents a request media request to the server.
 *
 * @author Leandro Zanol 11/12/15
 */
public class SambaMediaRequest {

	public String projectHash;
	public String mediaId;
	public String streamName;
	public String[] streamUrls;
	public Environment environment = Environment.PROD;

	/**
	 * Represents a VOD media request.
	 *
	 * @param projectHash Hash code of the project
	 * @param mediaId Hash code of the media
	 */
	public SambaMediaRequest(String projectHash, String mediaId) {
		this(projectHash, mediaId, null, (String)null);
	}

	/**
	 * Represents a live stream request (with stream name).
	 *
	 * @param projectHash Hash code of the project
	 * @param mediaId Hash code of the media
	 * @param streamName Name of the stream (live only)
	 */
	public SambaMediaRequest(String projectHash, String mediaId, String streamName) {
		this(projectHash, mediaId, streamName, (String)null);
	}

	/**
	 * Represents a direct live stream request (via URL).
	 *
	 * @param projectHash Hash code of the project
	 * @param mediaId Hash code of the media
	 * @param streamName Name of the stream (live only)
	 * @param streamUrl URL for stream (`streamName` will be ignored)
	 */
	public SambaMediaRequest(String projectHash, String mediaId, String streamName, String streamUrl) {
		this(projectHash, mediaId, streamName, streamUrl != null ? new String[]{streamUrl} : null);
	}

	/**
	 * Represents a direct live stream request (via URL) with other backup URLs.
	 *
	 * @param projectHash Hash code of the project
	 * @param mediaId Hash code of the media
	 * @param streamName Name of the stream (live only)
	 * @param streamUrls Alternative URLs for stream (`streamName` will be ignored)
	 */
	public SambaMediaRequest(String projectHash, String mediaId, String streamName, String[] streamUrls) {
		this.projectHash = projectHash;
		this.mediaId = mediaId;
		this.streamName = streamName;
		this.streamUrls = streamUrls != null ? streamUrls : new String[]{};
	}

	@Override
	public String toString() {
		return String.format("projectHash: %s, id: %s, streamName: %s, streamUrls: %s", projectHash, mediaId, streamName, streamUrls.length > 0 ? streamUrls[0] : null);
	}

	public enum Environment {
		LOCAL,
		TEST,
		PROD
	}
}
