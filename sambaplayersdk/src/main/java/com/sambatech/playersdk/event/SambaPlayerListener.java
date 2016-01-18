package com.sambatech.playersdk.event;

/**
 * Listener for player events.
 *
 * @author Leandro Zanol - 9/12/15
 */
public abstract class SambaPlayerListener {

	public enum EventType implements SambaEventType {
		LOAD,
		UNLOAD,
		START,
		PLAY,
		PAUSE,
		STOP,
		PROGRESS,
		FULLSCREEN,
		FULLSCREEN_EXIT,
		FINISH,
		RESIZE,
		CLICK,
		ERROR
	}

	public void onLoad(SambaEvent event) {}

	public void onUnload(SambaEvent event) {}

	public void onStart(SambaEvent event) {}

	public void onPlay(SambaEvent event) {}

	public void onPause(SambaEvent event) {}

	public void onStop(SambaEvent event) {}

	public void onProgress(SambaEvent event) {}

	public void onFullscreen(SambaEvent event) {}

	public void onFullscreenExit(SambaEvent event) {}

	public void onFinish(SambaEvent event) {}

	public void onResize(SambaEvent event) {}

	public void onClick(SambaEvent event) {}

	public void onError(SambaEvent event) {}

}
