package com.sambatech.player.model;

/**
 * Data entity that represents a request media request to the server.
 *
 * @author Leandro Zanol 11/12/15
 */
public class SambaMediaRequest {

	public String projectHash;
	public String mediaHash;
	public String streamName;
	public String[] streamUrls;

	/**
	 * Represents a VOD media request.
	 *
	 * @param projectHash Hash code of the project
	 * @param mediaHash Hash code of the media
	 */
	public SambaMediaRequest(String projectHash, String mediaHash) {
		this(projectHash, mediaHash, null, (String)null);
	}

	/**
	 * Represents a live stream request (with stream name).
	 *
	 * @param projectHash Hash code of the project
	 * @param mediaHash Hash code of the media
	 * @param streamName Name of the stream (live only)
	 */
	public SambaMediaRequest(String projectHash, String mediaHash, String streamName) {
		this(projectHash, mediaHash, streamName, (String)null);
	}

	/**
	 * Represents a direct live stream request (via URL).
	 *
	 * @param projectHash Hash code of the project
	 * @param mediaHash Hash code of the media
	 * @param streamName Name of the stream (live only)
	 * @param streamUrl URL for stream (`streamName` will be ignored)
	 */
	public SambaMediaRequest(String projectHash, String mediaHash, String streamName, String streamUrl) {
		this(projectHash, mediaHash, streamName, streamUrl != null ? new String[]{streamUrl} : null);
	}

	/**
	 * Represents a direct live stream request (via URL) with other backup URLs.
	 *
	 * @param projectHash Hash code of the project
	 * @param mediaHash Hash code of the media
	 * @param streamName Name of the stream (live only)
	 * @param streamUrls Alternative URLs for stream (`streamName` will be ignored)
	 */
	public SambaMediaRequest(String projectHash, String mediaHash, String streamName, String[] streamUrls) {
		this.projectHash = projectHash;
		this.mediaHash = mediaHash;
		this.streamName = streamName;
		this.streamUrls = streamUrls != null ? streamUrls : new String[]{};
	}

	@Override
	public String toString() {
		return String.format("projectHash: %s, hash: %s, streamName: %s, streamUrls: %s", projectHash, mediaHash, streamName, streamUrls.length > 0 ? streamUrls[0] : null);
	}
}
