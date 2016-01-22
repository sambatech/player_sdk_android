package com.sambatech.player;

import com.sambatech.player.model.SambaMedia;

/**
 * Represents the player business logic.
 *
 * @author Leandro Zanol - 28/12/15
 */
public interface SambaPlayerBase {

	/**
	 * Defines/overwrites current media.
	 * @param media The media to be played.
	 */
	void setMedia(SambaMedia media);

	/**
	 * Retrieves the current media data in use.
	 *
	 * Always returns a non null media data after LOAD event has been dispatched,
	 * but before its dispatch null checks must be made.
	 *
	 * @return Media data
	 */
	SambaMedia getMedia();

	void play();

	void pause();

	void stop();

	/**
	 * Moves the media to a specific position.
	 * @param position New position of the media in seconds
	 */
	void seek(float position);

	void setFullscreen(boolean flag);

	boolean isFullscreen();

	void show();

	void hide();

	float getCurrentTime();

	float getDuration();

	/**
	 * Indicates whether media has finished at least once.
	 * Does not imply playing it thoroughly without seeking.
	 *
	 * @return True once media hits the end.
	 */
	boolean hasFinished();

	/**
	 * Indicates whether media has already started playing.
	 *
	 * @return True if media has started playing.
	 */
	boolean hasStarted();

	void destroy();
}
