package com.sambatech.player.event;

/**
 * Listener for player events.
 *
 * @author Leandro Zanol - 9/12/15
 */
public abstract class SambaPlayerListener {

	public void onLoad(SambaEvent event) {}

	public void onStart(SambaEvent event) {}

	public void onPlay(SambaEvent event) {}

	public void onPause(SambaEvent event) {}

	public void onStop(SambaEvent event) {}

	public void onProgress(SambaEvent event) {}

	public void onFullscreen(SambaEvent event) {}

	public void onFullscreenExit(SambaEvent event) {}

	public void onFinish(SambaEvent event) {}
}
