package com.sambatech.player.model;

import android.support.annotation.NonNull;

/**
 * Data entity that represents a request media request to the server.
 *
 * @author Leandro Zanol 11/12/15
 */
public class SambaMediaRequest {

	public String projectHash;
	public String mediaId;
	public String liveChannelId;
	public String streamName;
	public String streamUrl;
	public @NonNull String[] backupUrls = new String[]{};
	public @NonNull Environment environment = Environment.PROD;
	public @NonNull Protocol protocol = Protocol.HTTPS;

	/**
	 * Represents a VOD media request.
	 *
	 * @param projectHash Hash code of the project
	 * @param mediaId Hash code of the media
	 */
	public SambaMediaRequest(String projectHash, String mediaId) {
		this(projectHash, mediaId, false);
	}

	/**
	 * Represents a live stream request.
	 *
	 * @param projectHash Hash code of the project
	 * @param mediaIdOrLiveChannelId The media ID or live channel ID (depending on the flag)
	 * @param isLive Set this flag to true when using a live channel ID
	 */
	public SambaMediaRequest(String projectHash, String mediaIdOrLiveChannelId, boolean isLive) {
		this.projectHash = projectHash;

		if (isLive)
			this.liveChannelId = mediaIdOrLiveChannelId;
		else this.mediaId = mediaIdOrLiveChannelId;
	}

	/**
	 * Represents a live stream request (by stream name).
	 *
	 * @param projectHash Hash code of the project
	 * @param mediaId Hash code of the media
	 * @param streamName Name of the stream (live only)
	 */
	public SambaMediaRequest(String projectHash, String mediaId, String streamName) {
		this(projectHash, mediaId, streamName, null);
	}

	/**
	 * Represents a direct live stream request (by URL).
	 *
	 * @param projectHash Hash code of the project
	 * @param mediaId Hash code of the media
	 * @param streamName Name of the stream (live only)
	 * @param streamUrl URL for stream (`streamName` will be ignored)
	 */
	public SambaMediaRequest(String projectHash, String mediaId, String streamName, String streamUrl) {
		this(projectHash, mediaId, streamName, streamUrl, null);
	}

	/**
	 * Represents a direct live stream request (by URL) with other backup URLs.
	 *
	 * @param projectHash Hash code of the project
	 * @param mediaId Hash code of the media
	 * @param streamName Name of the stream (live only)
	 * @param streamUrl URL for stream (`streamName` will be ignored)
	 * @param backupUrls URL list for fallback purposes
	 */
	public SambaMediaRequest(String projectHash, String mediaId, String streamName, String streamUrl, String[] backupUrls) {
		this.projectHash = projectHash;
		this.mediaId = mediaId;
		this.streamName = streamName;
		this.streamUrl = streamUrl;
		this.backupUrls = backupUrls != null ? backupUrls : new String[]{};
	}

	@Override
	public String toString() {
		return String.format("projectHash: %s, id: %s, streamName: %s, streamUrls: %s, backupUrls (count): %s", projectHash, mediaId, streamName, streamUrl, backupUrls.length);
	}

	public enum Environment {
		LOCAL("localhost-8080"),
		DEV("web4-7091"),
		STAGING("staging"),
		PROD("prod");

		private final String value;

		Environment(String value) {
			this.value = value;
		}

		public static Environment stringToEnvironment(String string) {
			try {
				return valueOf(string);
			} catch (Exception ex) {
				// For error cases
				return PROD;
			}
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum Protocol {
		HTTP,
		HTTPS
	}
}
