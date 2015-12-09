package com.sambatech.player.event;

/**
 * Listener for player events.
 *
 * @author Leandro Zanol - 9/12/15
 */
public abstract class SambaPlayerListener {

	public void onLoad(SambaEvent event) {}

	//public void onStart() {}

	public void onPause(SambaEvent event) {}

	public void onPlay(SambaEvent event) {}

	//public void onProgress() {}

	//public void onFullscreen() {}

	//public void onFullscreenExit() {}

	public void onFinish(SambaEvent event) {}
}
