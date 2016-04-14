package com.sambatech.player;

import android.util.Log;
import android.view.View;

import com.sambatech.player.model.SambaMedia;

/**
 * Disabled version of controller layer for SambaPlayer.
 *
 * @author Leandro Zanol - 22/1/16
 */
public class SambaPlayerControllerNull implements SambaPlayer {

	private static final SambaPlayerControllerNull instance = new SambaPlayerControllerNull();

	public static SambaPlayerControllerNull getInstance() {
		return instance;
	}

	private SambaPlayerControllerNull() {}

	@Override
	public void setMedia(SambaMedia media) {
		logMessage();
	}

	@Override
	public SambaMedia getMedia() {
		logMessage();
		return null;
	}

	@Override
	public void play() {
		logMessage();
	}

	@Override
	public void pause() {
		logMessage();
	}

	@Override
	public void stop() {
		logMessage();
	}

	@Override
	public void seek(float position) {
		logMessage();
	}

	@Override
	public void setFullscreen(boolean flag) {
		logMessage();
	}

	@Override
	public boolean isFullscreen() {
		logMessage();
		return false;
	}

	@Override
	public void show() {
		logMessage();
	}

	@Override
	public void hide() {
		logMessage();
	}

	@Override
	public void setAutoFullscreenMode(boolean flag) {
		logMessage();
	}

	@Override
	public float getCurrentTime() {
		logMessage();
		return 0;
	}

	@Override
	public float getDuration() {
		logMessage();
		return 0;
	}

	@Override
	public boolean hasFinished() {
		logMessage();
		return false;
	}

	@Override
	public boolean hasStarted() {
		logMessage();
		return false;
	}

	@Override
	public void changeOutput(SambaMedia.Output output) {
		logMessage();
	}

	@Override
	public void destroy() {
		logMessage();
	}

	private void logMessage() {
		Log.i("SambaPlayer", "No action will be done (call setMedia() to renable player).");
	}
}
