package com.sambatech.player;

import com.sambatech.player.model.SambaMedia;

/**
 * Represents the player front-end API.
 *
 * @author Leandro Zanol - 28/12/15
 */
public interface SambaPlayer {

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

	/**
	 * Resumes media playback
	 */
	void play();

	/**
	 * Pauses media playback
	 */
	void pause();

	/**
	 * Stops media playback returning the video to it's beginning
	 */
	void stop();

	/**
	 * Moves the media to a specific position.
	 * @param position New position of the media in seconds
	 */
	void seek(float position);

	/**
	 * Sets fullscreen mode on and off
	 * @param flag true to enter in the fullscreen mode on and false to exit
	 */
	void setFullscreen(boolean flag);

	/**
	 * Indicates the fullscreen mode on or off
	 * @return Boolean
	 */
	boolean isFullscreen();

	/**
	 * Shows player controls
 	 */
	void show();

	/**
	 * Hides player controls
	 */
	void hide();

	/**
	 * Sets whether the player should go automatically on fullscreen or not.
	 * @param flag true to enable auto fullscreen mode and false to disable it
	 */
	void setAutoFullscreenMode(boolean flag);

	/**
	 * Gets the current time on the video.
	 * @return Float current time
	 */
	float getCurrentTime();

	/**
	 * Gets the total duration of the video
	 * @return Float total duration
	 */
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

	/**
	 * Changes the current output
	 * @param output SambaMedia.Output indicating the new output
	 */
	void changeOutput(SambaMedia.Output output);

	/**
	 * Destroys the player and it's events
	 */
	void destroy();
}
