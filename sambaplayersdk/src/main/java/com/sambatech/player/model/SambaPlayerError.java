package com.sambatech.player.model;

/**
 * Possible player errors reported through `SambaPlayerListener.onError` event.
 *
 * @author Leandro Zanol - 11/14/16
 */
public enum SambaPlayerError {
	invalidMedia,
	invalidUrl,
	emptyUrl,
	creatingPlayer,
	rootedDevice;

	@Override
	public String toString() {
		switch (this) {
			case invalidMedia:
				return "Invalid media format.";
			case invalidUrl:
				return "Invalid URL format.";
			case emptyUrl:
				return "Missing URL for the specified media.";
			case creatingPlayer:
				return "Error creating player.";
			case rootedDevice:
				return "Specified media cannot play on a rooted device.";
			default:
				return super.toString();
		}
	}
}
