package com.sambatech.player.exception;

import com.sambatech.player.model.SambaMedia;

/**
 * @author Leandro Zanol - 11/10/16
 */
public class SambaPlayerException extends RuntimeException {

	public static final int INVALID_MEDIA = 0;
	public static final int ROOTED_DEVICE = 1;

	private final int id;
	private final SambaMedia media;

	public SambaPlayerException(int id) {
		this(id, null);
	}

	public SambaPlayerException(int id, SambaMedia media) {
		this.id = id;
		this.media = media;
	}

	public int getId() {
		return id;
	}

	@Override
	public String getMessage() {
		switch (id) {
			case ROOTED_DEVICE:
				return String.format("The media \"%s\" is not allowed to play on a rooted device.", media.title);
			case INVALID_MEDIA:
				return media == null ? "Media is null." : String.format("Invalid media format: %s", media);
			default:
				return super.getMessage();
		}
	}
}
