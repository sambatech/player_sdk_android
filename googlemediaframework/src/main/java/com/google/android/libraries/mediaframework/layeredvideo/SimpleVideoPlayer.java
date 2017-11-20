/**
 Copyright 2014 Google Inc. All rights reserved.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.google.android.libraries.mediaframework.layeredvideo;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.exoplayer.MediaFormat;
import com.google.android.libraries.mediaframework.exoplayerextensions.ExoplayerWrapper;
import com.google.android.libraries.mediaframework.exoplayerextensions.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * A video player which includes subtitle support and a customizable UI for playback control.
 *
 * <p>NOTE: If you want to get a video player up and running with minimal effort, just instantiate
 * this class and call play();
 */
public class SimpleVideoPlayer {

	/**
	 * The {@link Activity} that contains this video player.
	 */
	private final Activity activity;

	/**
	 * The underlying {@link LayerManager} which is used to assemble the player.
	 */
	private final LayerManager layerManager;

	/**
	 * The customizable view for playback control. It handles pause/play, fullscreen, seeking,
	 * title, and action buttons.
	 */
	private final PlaybackControlLayer playbackControlLayer;


	/**
	 * Implents Options Menu and its methods
	 */
	private final OptionsMenuLayer optionsMenuLayer;

	/**
	 * Displays subtitles at bottom center of video player.
	 */
	private final SubtitleLayer subtitleLayer;

	/**
	 * Renders the video.
	 */
	private final VideoSurfaceLayer videoSurfaceLayer;

	/**
	 * Set whether the video should play immediately.
	 */
	private boolean autoplay;

	/**
	 * Set whether media is audio only or not.
	 */
	private boolean audioOnly;

	/**
	 * Interceptable listener able to cancel both API and user actions.
	 */
	private PlaybackControlLayer.InterceptableListener interceptableListener;

	/**
	 * @param activity The activity that will contain the video player.
	 * @param container The {@link FrameLayout} which will contain the video player.
	 * @param video The video that should be played.
	 * @param videoTitle The title of the video (displayed on the left of the top chrome).
	 * @param autoplay Whether the video should start playing immediately.
	 */
	public SimpleVideoPlayer(Activity activity,
			 FrameLayout container,
			 Video video,
			 String videoTitle,
			 boolean autoplay,
	         boolean audioOnly) {
		this(activity, container, video, videoTitle, autoplay, audioOnly, 0, null);
	}

	/**
	 * @param activity The activity that will contain the video player.
	 * @param container The {@link FrameLayout} which will contain the video player.
	 * @param video The video that should be played.
	 * @param videoTitle The title of the video (displayed on the left of the top chrome).
	 * @param autoplay Whether the video should start playing immediately.
	 * @param fullscreenCallback The callback which gets triggered when the player enters or leaves
	 *                           fullscreen mode.
	 */
	public SimpleVideoPlayer(Activity activity,
			FrameLayout container,
			Video video,
			String videoTitle,
			boolean autoplay,
			boolean audioOnly,
			int startPostitionMs,
			PlaybackControlLayer.FullscreenCallback fullscreenCallback) {
		this.activity = activity;

		optionsMenuLayer = new OptionsMenuLayer();
		playbackControlLayer = new PlaybackControlLayer(videoTitle, fullscreenCallback, !audioOnly, optionsMenuLayer);
		subtitleLayer = new SubtitleLayer();
		videoSurfaceLayer = new VideoSurfaceLayer(autoplay);

		this.autoplay = autoplay;
		this.audioOnly = audioOnly;

		List<Layer> layers = new ArrayList<>();
		layers.add(videoSurfaceLayer);
		layers.add(playbackControlLayer);
		layers.add(subtitleLayer);
		layers.add(optionsMenuLayer);

		layerManager = new LayerManager(activity,
				container,
				video,
				layers);

		layerManager.getExoplayerWrapper().setTextListener(subtitleLayer);

		seek(startPostitionMs);
	}

	/**
	 * Creates an image based button to put in the top right of the video player.
	 *
	 * @param icon The image of the action (ex. trash can).
	 * @param contentDescription The text description this action. This is used in case the
	 *                           action buttons do not fit in the video player. If so, an overflow
	 *                           button will appear and, when clicked, it will display a list of the
	 *                           content descriptions for each action.
	 * @param onClickListener The handler for when the action is triggered.
	 */
	public void addActionButton(Drawable icon,
								String contentDescription,
								View.OnClickListener onClickListener) {
		playbackControlLayer.addActionButton(activity, icon, contentDescription, onClickListener);
	}

	/**
	 * Creates a button to put in the top right of the video player.
	 *
	 * @param button The button to be put.
	 */
	public void addActionButton(View button) {
		playbackControlLayer.addActionButton(activity, button);
	}

	/**
	 * Set a listener which reacts to state changes, video size changes, and errors.
	 * @param listener Listens to playback events.
	 */
	public void addPlaybackListener(ExoplayerWrapper.PlaybackListener listener) {
		layerManager.getExoplayerWrapper().addListener(listener);
	}

	public void removePlaybackListener(ExoplayerWrapper.PlaybackListener listener) {
		layerManager.getExoplayerWrapper().removeListener(listener);
	}

	public int getPlaybackState() {
		return layerManager.getExoplayerWrapper().getPlaybackState();
	}

	public void setSelectedTrack(int index) {
		layerManager.getExoplayerWrapper().setSelectedTrack(ExoplayerWrapper.TYPE_VIDEO, index);
	}

	public int getSelectedTrack() {
		return layerManager.getExoplayerWrapper().getSelectedTrack(ExoplayerWrapper.TYPE_VIDEO);
	}

	public MediaFormat[] getTrackFormats(int type) {
		return layerManager.getExoplayerWrapper().getTrackFormats(type);
	}

	public int getTrackCount(int type) {
		return layerManager.getExoplayerWrapper().getTrackCount(type);
	}

	/**
	 * Hides the seek bar thumb and prevents the user from seeking to different time points in the
	 * video.
	 */
	public void disableSeeking() {
		playbackControlLayer.disableSeeking();
	}

	/**
	 * Makes the seek bar thumb visible and allows the user to seek to different time points in the
	 * video.
	 */
	public void enableSeeking() {
		playbackControlLayer.enableSeeking();
	}

	/**
	 * Returns the current playback position in milliseconds.
	 */
	public int getCurrentPosition() {
		return layerManager.getControl().getCurrentPosition();
	}

	/**
	 * Returns the duration of the track in milliseconds or
	 * {@link com.google.android.exoplayer.ExoPlayer#UNKNOWN_TIME} if the duration is unknown.
	 */
	public int getDuration() {
		return layerManager.getControl().getDuration();
	}

	/**
	 * Fades the playback control layer out and then removes it from the {@link LayerManager}'s
	 * container.
	 */
	public void hide() {
		videoSurfaceLayer.hide(); // TODO: testar!
		playbackControlLayer.hide();
		subtitleLayer.setVisibility(View.GONE);
	}

	public void setControlsVisible(boolean state, String... controls) {
		playbackControlLayer.setControlsVisible(state, controls);
	}

	/**
	 * Hides the top chrome (which displays the logo, title, and action buttons).
	 */
	public void hideTopChrome() {
		playbackControlLayer.hideTopChrome();
	}

	/**
	 * Returns whether the player is currently in fullscreen mode.
	 */
	public boolean isFullscreen() {
		return playbackControlLayer.isFullscreen();
	}

	/**
	 * Set the controls to false via parameter disableControls ( only shows play/pause )
	 */
	public void disableControls() {
		playbackControlLayer.hideTopChrome();
		playbackControlLayer.hideBottomChrome();
		playbackControlLayer.hideMainControls();
	}

	/**
	 * Set the controls to true via parameter enableControls
	 */
	public void enableControls() {
		playbackControlLayer.showTopChrome();
		playbackControlLayer.showBottomChrome();
	}

	public void setAutoHide(boolean state) {
		playbackControlLayer.setAutoHide(state);
	}

	/**
	 * Make the player enter or leave fullscreen mode.
	 * @param shouldBeFullscreen If true, the player is put into fullscreen mode. If false, the player
	 *                           leaves fullscreen mode.
	 * @param isReverseLandscape Whether orientation is reverse landscape.
	 */
	public void setFullscreen(boolean shouldBeFullscreen, boolean isReverseLandscape) {
		playbackControlLayer.setFullscreen(shouldBeFullscreen, isReverseLandscape);
	}

	/**
	 * Make the player enter or leave fullscreen mode.
	 * @param shouldBeFullscreen If true, the player is put into fullscreen mode. If false, the player
	 *                           leaves fullscreen mode.
	 */
	public void setFullscreen(boolean shouldBeFullscreen) {
		playbackControlLayer.setFullscreen(shouldBeFullscreen);
	}

	/**
	 * When mutliple surface layers are used (ex. in the case of ad playback), one layer must be
	 * overlaid on top of another. This method sends this player's surface layer to the background
	 * so that other surface layers can be overlaid on top of it.
	 */
	public void moveSurfaceToBackground() {
		videoSurfaceLayer.moveSurfaceToBackground();
	}

	/**
	 * When mutliple surface layers are used (ex. in the case of ad playback), one layer must be
	 * overlaid on top of another. This method sends this player's surface layer to the foreground
	 * so that it is overlaid on top of all layers which are in the background.
	 */
	public void moveSurfaceToForeground() {
		videoSurfaceLayer.moveSurfaceToForeground();
	}

    /**
     * Getting the subtitleLayer externaly
     */
    public SubtitleLayer getSubtitleLayer() {
    	return subtitleLayer;
    }

    /**
	 * Pause video playback.
	 */
	public void pause() {
		if (interceptableListener != null && !interceptableListener.onPause()) {
			playbackControlLayer.updatePlayPauseButton(false);
			return;
		}

		// Set the autoplay for the video surface layer in case the surface hasn't been created yet.
		// This way, when the surface is created, it won't start playing.
		videoSurfaceLayer.setAutoplay(false);

		layerManager.getControl().pause();
	}

	/**
	 * Resume video playback.
	 */
	public void play() {
		if (interceptableListener != null && !interceptableListener.onPlay()) {
			playbackControlLayer.updatePlayPauseButton(true);
			return;
		}

		// Set the autoplay for the video surface layer in case the surface hasn't been created yet.
		// This way, when the surface is created, it will automatically start playing.
		videoSurfaceLayer.setAutoplay(autoplay);

		layerManager.getControl().start();
	}

	/**
	 * Moves media to a specific position.
	 * @param posMs The position in MS
	 */
	public void seek(int posMs) {
		if (posMs < 0)
			return;

		layerManager.getExoplayerWrapper().seekTo(posMs);
	}

	public void stop() {
		if (layerManager.getExoplayerWrapper() != null)
			layerManager.getExoplayerWrapper().stop();
	}

	/**
	 * Sets the color of the top chrome, bottom chrome, and background.
	 * @param color a color derived from the @{link Color} class
	 *              (ex. {@link android.graphics.Color#RED}).
	 */
	public void setChromeColor(int color) {
		playbackControlLayer.setChromeColor(color);
	}

	public void setBackgroundColor(int color) {
		playbackControlLayer.setBackgroundColor(color);
	}

	/**
	 * Set the callback which will be called when the player enters and leaves fullscreen mode.
	 * @param fullscreenCallback The callback should hide other views in the activity when the player
	 *                           enters fullscreen mode and show other views when the player leaves
	 *                           fullscreen mode.
	 */
	public void setFullscreenCallback(PlaybackControlLayer.FullscreenCallback fullscreenCallback) {
		playbackControlLayer.setFullscreenCallback(fullscreenCallback);
	}

	/**
	 * Set the callback which will be called when the player plays video.
	 * @param playCallback The callback.
	 */
	public void setPlayCallback(PlaybackControlLayer.PlayCallback playCallback) {
		playbackControlLayer.setPlayCallback(playCallback);
	}

	/**
	 * Set the logo with appears in the left of the top chrome.
	 * @param logo The drawable which will be the logo.
	 */
	public void setLogoImage(Drawable logo) {
		playbackControlLayer.setLogoImageView(logo);
	}

	/**
	 * Sets the color of the buttons and seek bar.
	 * @param color a color derived from the @{link Color} class
	 *              (ex. {@link android.graphics.Color#RED}).
	 */
	public void setPlaybackControlColor(int color) {
		playbackControlLayer.setControlColor(color);
	}

	/**
	 * Sets the color of the seekbar.
	 * @param color a color derived from the @{link Color} class
	 *              (ex. {@link android.graphics.Color#RED}).
	 */
	public void setThemeColor(int color) {
		playbackControlLayer.setThemeColor(color);
	}

	/**
	 * Sets the color of the text views
	 * @param color a color derived from the @{link Color} class
	 *              (ex. {@link android.graphics.Color#RED}).
	 */
	public void setTextColor(int color) {
		playbackControlLayer.setTextColor(color);
	}

	/**
	 * Set the title of the video in the left of the top chrome (to the right of the logo).
	 * @param title The video title. If it is too long, it will be ellipsized.
	 */
	public void setVideoTitle (String title) {
		playbackControlLayer.setVideoTitle(title);
	}

	/**
	 * Returns whether the player should be playing (based on whether the user has
	 * tapped pause or play). This can be used by other classes to look at the playback control
	 * layer's play/pause state and force the player to play or pause accordingly.
	 */
	public boolean shouldBePlaying() {
		return playbackControlLayer.shouldBePlaying();
	}

	/**
	 * Add the playback control layer back to the container. It will disappear when the user taps
	 * the screen.
	 */
	public void show() {
		videoSurfaceLayer.show(); // TODO: testar!
		playbackControlLayer.show();
		subtitleLayer.setVisibility(View.VISIBLE);
	}

	/**
	 * Shows the top chrome (which displays the logo, title, and action buttons).
	 */
	public void showTopChrome() {
		playbackControlLayer.showTopChrome();
	}

	/**
	 * Sets the adapter for the output menu.
	 * @param view The view for the output menu.
	 */
	public void setOutputMenu(View view) {
		playbackControlLayer.setOutputMenu(view);
	}

	/**
	 * Closes output menu.
	 */
	public void closeOutputMenu() {
		playbackControlLayer.closeOutputMenu();
	}

    /** Sets the adapter for the caption menu.
     * @param view The view for the caption menu
     */
    public void setCaptionMenu(View view) {
	    playbackControlLayer.setCaptionMenu(view);
    }

	/** Gets the caption menu view.
	 * @return The caption menu view
	 */
	public View getCaptionMenu() {
		return playbackControlLayer.getCaptionMenu();
	}

    /**
     * Closes caption menu.
     */
    public void closeCaptionMenu() {
        playbackControlLayer.closeCaptionMenu();
    }

    /**
	 * When you are finished using this {@link SimpleVideoPlayer}, make sure to call this method.
	 */
	public void release() {
		videoSurfaceLayer.release();
		layerManager.release();
	}

	/**
	 * Set a listener to respond to media format changes, bandwidth samples, load events, and dropped
	 * frames.
	 * @param listener Listens to media format changes, bandwidth samples, load events, and dropped
	 *                 frames.
	 */
	public void setInfoListener(ExoplayerWrapper.InfoListener listener) {
		layerManager.getExoplayerWrapper().setInfoListener(listener);
	}

	/**
	 * Sets a interceptable listener able to cancel both API and user actions.
	 * @param interceptableListener The listener instance
	 */
	public void setInterceptableListener(PlaybackControlLayer.InterceptableListener interceptableListener) {
		playbackControlLayer.setInterceptableListener(this.interceptableListener = interceptableListener);
	}

	public void setCurrentTime(float currentTime, float duration){
		playbackControlLayer.setCurrentTime(currentTime,duration);
	}

	public void updatePlayPauseButton(boolean shouldBePlaying){
		playbackControlLayer.updatePlayPauseButton(shouldBePlaying);
	}

	/**
	 * Progress
	 */

	public void showLoading() {
		if (!audioOnly)
			playbackControlLayer.hideMainControls();

		playbackControlLayer.setLoadingProgress(View.VISIBLE);
	}

	public void hideLoading() {
		if (!audioOnly)
			playbackControlLayer.showMainControls();

		playbackControlLayer.setLoadingProgress(View.GONE);
	}
}
