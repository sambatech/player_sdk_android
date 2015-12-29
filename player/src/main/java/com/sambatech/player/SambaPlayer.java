package com.sambatech.player;

import android.view.View;

import com.sambatech.player.model.SambaMedia;

/**
 * @author Leandro Zanol - 28/12/15
 */
public interface SambaPlayer {

	/**
	 * Defines/overwrites current media.
	 *
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

	void setFullscreen(boolean flag);

	boolean isFullscreen();

	void show();

	void hide();

	void destroy();

	float getCurrentTime();

	float getDuration();

	/**
	 * Returns the view layer associated with the player.
	 *
	 * @return Android View layer
	 */
	View getView();

	/**
	 * Tells if the player is ready to play the media.
	 *
	 * @return Ready to play
	 */
	boolean isReady();

	// TODO: seek(float secs)
}
