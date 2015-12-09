package com.sambatech.player.event;

/**
 * Listener for player events.
 *
 * @author Leandro Zanol - 9/12/15
 */
public abstract class SambaPlayerListener {

	public void onLoad() {}

	//public void onStart() {}

	public void onPause() {}

	public void onPlay() {}

	//public void onProgress() {}

	//public void onFullscreen() {}

	//public void onFullscreenExit() {}

	public void onFinish() {}
}
