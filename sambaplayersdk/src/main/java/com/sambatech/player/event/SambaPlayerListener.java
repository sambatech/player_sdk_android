package com.sambatech.player.event;

/**
 * Listener for player events.
 *
 * @author Leandro Zanol - 9/12/15
 */
public abstract class SambaPlayerListener {

	/**
	 * Events type
	 */
	public enum EventType implements SambaEventType {
		LOAD,
		DESTROY,
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
		ERROR,
		PORTRAIT,
		LANDSCAPE,
		CAST_CONNECT,
		CAST_DISCONNECT,
		CAST_PLAY,
		CAST_PAUSE,
		CAST_FINISH
	}

	/**
	 * OnLoad player fired when the player is initialized.
	 * @param event LOAD EventType.event
	 */
	public void onLoad(SambaEvent event) {}

	/**
	 * onDestroy player fired when the player is destroyed.
	 * @param event DESTROY EventType.event
	 */
	public void onDestroy(SambaEvent event) {}

	/**
	 * OnStart player fired when the media starts to play for the first time.
	 * @param event START EventType.event
	 */
	public void onStart(SambaEvent event) {}

	/**
	 * OnPlay player fired when the player is resumed ( it's fired after the start event ).
	 * @param event PLAY EventType.event
	 */
	public void onPlay(SambaEvent event) {}

	/**
	 * OnPause player fired when the user pauses the player.
	 * @param event PAUSE EventType.event
	 */
	public void onPause(SambaEvent event) {}

	/**
	 * OnStop player fired when the player is stopped.
	 * @param event STOP EventType.event
	 */
	public void onStop(SambaEvent event) {}

	/**
	 * OnProgress player fired as the player is watched.
	 * @param event PROGRESS EventType.event
	 */
	public void onProgress(SambaEvent event) {}

	/**
	 * OnFullscreen player fired when the player enters the fullscreen mode.
	 * @param event FULLSCREEN EventType.event
	 */
	public void onFullscreen(SambaEvent event) {}

	/**
	 * OnFullscreenExit player fired when the player exit the fullscreen mode.
	 * @param event FULLSCREEN_EXIT EventType.event
	 */
	public void onFullscreenExit(SambaEvent event) {}

	/**
	 * OnFinish player fired when the player is finished.
	 * @param event FINISH EventType.event
	 */
	public void onFinish(SambaEvent event) {}

	/**
	 * OnResize player fired when the player is resized.
	 * @param event RESIZE EventType.event
	 */
	public void onResize(SambaEvent event) {}

	/**
	 * OnLoad OnClick fired whenever the player view is clicked
	 * @param event CLICK EventType.event
	 */
	public void onClick(SambaEvent event) {}

	/**
	 * OnError player fired when the player has an error
	 * @param event ERROR EventType.event
	 */
	public void onError(SambaEvent event) {}

	/**
	 * OnPortrait player fired when the user puts the cellphone in portrait mode
	 * @param event PORTRAIT EventType.event
	 */
	public void onPortrait(SambaEvent event) {}

	/**
	 * OnLandscape player fired when the user puts the cellphone in landscape mode
	 * @param event LANDSCAPE EventType.event
	 */
	public void onLandscape(SambaEvent event) {}

	public void onCastConnect(SambaEvent event) {}

	public void onCastDisconnect(SambaEvent event) {}

	public void onCastPlay(SambaEvent event) {}

	public void onCastPause(SambaEvent event) {}

	public void onCastFinish(SambaEvent event) {}

}
