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

import android.animation.Animator;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer.util.PlayerControl;
import com.google.android.libraries.mediaframework.R;
import com.google.android.libraries.mediaframework.exoplayerextensions.PlayerControlCallback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * A {@link Layer} that creates a customizable view for controlling video playback.
 *
 * <p>The view consists of:
 *
 * <p> 1) a top chrome which contains a logo, title, and set of action buttons.
 *
 * <p> 2) a bottom chrome which contains a seek bar, fullscreen button, and text views indicating
 * the current time and total duration of the video.
 *
 * <p> 3) a translucent middle section which displays a pause/play button.
 *
 * <p>The view appears when the container containing the {@link PlaybackControlLayer} is tapped. It
 * automatically disappears after a given time.
 *
 * <p>The view can be customized by:
 *
 * <p> 1) Setting the color of the top chrome, bottom chrome, and background - this is called
 * the chrome tint color.
 *
 * <p> 2) Setting the color of the text - this is called the text color.
 *
 * <p> 3) Setting the color of the buttons and seek bar - this is called the control tint color.
 *
 * <p> 4) Setting the logo image displayed in the left of the top chrome.
 *
 * <p> 5) Setting the title of the video displayed in the left of the top chrome
 * (and to the right of the logo).
 *
 * <p> 6) Adding an action button by providing an image, a content description, and a click handler. If
 * there is enough room, the action buttons will be displayed on the right of the top chrome. If
 * there is NOT enough room, an overflow button will be displayed. When the overflow button is
 * clicked, a dialog box listing the content descriptions for the action buttons is displayed. The
 * action is then triggered by selecting it from the dialog box.
 *
 * <p>The view is defined in the layout file: res/layout/playback_control_layer.xml.
 */
public class PlaybackControlLayer implements Layer, PlayerControlCallback {

	/**
	 * In order to imbue the {@link PlaybackControlLayer} with the ability make the player fullscreen,
	 * a {@link PlaybackControlLayer.FullscreenCallback} must be assigned to it. The
	 * {@link PlaybackControlLayer.FullscreenCallback} implementation is responsible for
	 * hiding/showing the other views on the screen when the player enters/leaves fullscreen
	 * mode.
	 */
	public interface FullscreenCallback {

		/**
		 * When triggered, the activity should hide any additional views.
		 */
		public void onGoToFullscreen();

		/**
		 * When triggered, the activity should show any views that were hidden when the player
		 * went to fullscreen.
		 */
		public void onReturnFromFullscreen();
	}

	/**
	 * The {@link PlaybackControlLayer.PlayCallback} implementation will be called when the player
	 * plays the video (e.g. to request IMA ads) upon user taps on the play button.
	 */
	public interface PlayCallback {

		public void onPlay();
	}

	private interface Callback {
		void call();
	}

	/**
	 * Message handler which allows us to send delayed messages to the {@link PlaybackControlLayer}
	 * This is useful for fading out the view after a certain time.
	 */
	private static class MessageHandler extends Handler {
		/**
		 * A reference to the {@link PlaybackControlLayer} that we are handling messages for.
		 */
		private final WeakReference<PlaybackControlLayer> playbackControlLayer;

		/**
		 * @param playbackControlLayer The {@link PlaybackControlLayer} we should handle messages for.
		 */
		private MessageHandler(PlaybackControlLayer playbackControlLayer) {
			this.playbackControlLayer = new WeakReference<PlaybackControlLayer>(playbackControlLayer);
		}

		/**
		 * Receives either a {@link PlaybackControlLayer#FADE_OUT} message (which hides the playback
		 * control layer) or a {@link PlaybackControlLayer#SHOW_PROGRESS} message (which updates the
		 * seek bar to reflect the progress in the video).
		 * @param msg Either a {@link PlaybackControlLayer#FADE_OUT} or
		 * {@link PlaybackControlLayer#SHOW_PROGRESS} message.
		 */
		@Override
		public void handleMessage(Message msg) {
			PlaybackControlLayer layer = playbackControlLayer.get();
			if (layer == null || layer.getLayerManager().getControl() == null) {
				return;
			}
			int pos;
			switch (msg.what) {
				case FADE_OUT:
					layer.hide();
					break;
				case SHOW_PROGRESS:
					pos = layer.updateProgress();
					if (!layer.isSeekbarDragging
							&& layer.isVisible
							&& layer.getLayerManager().getControl().isPlaying()) {
						msg = obtainMessage(SHOW_PROGRESS);
						sendMessageDelayed(msg, 1000 - (pos % 1000));
					}
					break;
			}
		}
	}

	/**
	 * The chrome (the top chrome, bottom chrome, and background) is by default a slightly
	 * transparent black.
	 */
	public static final int DEFAULT_CHROME_COLOR = Color.argb(140, 0, 0, 0);

	/**
	 * By default, there is no tint to the controls.
	 */
	public static final int DEFAULT_CONTROL_TINT_COLOR = Color.TRANSPARENT;

	/**
	 * By default, the text is white.
	 */
	public static final int DEFAULT_TEXT_COLOR = Color.WHITE;

	/**
	 * When the playback controls are shown, hide them after DEFAULT_TIMEOUT_MS milliseconds.
	 */
	private static final int DEFAULT_TIMEOUT_MS = 2000;

	/**
	 * When the controls are hidden, they fade out in FADE_OUT_DURATION_MS milliseconds.
	 */
	private static final int FADE_OUT_DURATION_MS = 200;

	/**
	 * Used by the {@link MessageHandler} to indicate that media controls should fade out.
	 */
	private static final int FADE_OUT = 1;

	/**
	 * Used by the {@link MessageHandler} to indicate that media controls should update progress bar.
	 */
	private static final int SHOW_PROGRESS = 2;

	/**
	 * List of image buttons which are displayed in the right side of the top chrome.
	 */
	private List<View> actionButtons;

	/**
	 * Whether the playback control layer is visible.
	 */
	private boolean isVisible;

	/**
	 * Whether the playback control layer is currently in the process of fading out.
	 */
	private boolean isFadingOut;

	/**
	 * Whether the user can drag the seek bar thumb to seek.
	 */
	private boolean canSeek;

	/**
	 * Player Control
	 *
	 */
	PlayerControl playerControl;

	/**
	 * <p> Derived from the Color class (ex. {@link Color#RED}), the chrome consists of three
	 * views, which are are tinted with the the chrome color.
	 *
	 * <p> The views are:
	 *
	 * <p> 1) The top chrome which contains the logo, title, and action buttons.
	 *
	 * <p> 2) The bottom chrome which contains the play/pause button, seek bar, and fullscreen
	 * buttons.
	 *
	 * <p> 3) The translucent middle section of the PlaybackControlLayer.
	 *
	 * <p> The chromeColor changes the color of each of these elements.
	 */
	private int chromeColor;

	/**
	 * Derived from the {@link Color} class (ex {@link Color#RED}), this is the color of the
	 * play/pause button, fullscreen button, seek bar, and action buttons.
	 */
	private int controlColor;

	/**
	 * Derived from the {@link Color} class (ex {@link Color#RED}), this is the color of the text
	 * views.
	 */
	private int textColor;

	/**
	 * Derived from the {@link Color} class (ex {@link Color#RED}), this is the color of the seekbar_progress
	 * track and thumb.
	 */
	private int seekbarColor;

	/**
	 * Displays the elapsed time into video.
	 */
	private TextView currentTime;

	/**
	 * Displays the duration of the video.
	 */
	private TextView endTime;

	/**
	 * Makes player  enter or leave fullscreen. This button is not displayed unless there is a
	 * {@link FullscreenCallback} associated with this object.
	 */
	private ImageButton fullscreenButton;

	/**
	 * Shows a menu for changing media quality.
	 */
	private ImageButton outputButton;

    /**
     * Shows a menu for changing caption.
     */
    private ImageButton captionButton;

	/**
	 * This callback is triggered when going to fullscreen and returning from fullscreen.
	 */
	private FullscreenCallback fullscreenCallback;

	private PlayCallback playCallback;
	/**
	 * The message handler which deals with displaying progress and fading out the media controls
	 * We use it so that we can make the view fade out after a timeout (by sending a delayed message).
	 */
	private Handler handler = new MessageHandler(this);

	/**
	 * Whether the player is currently in fullscreen mode.
	 */
	private boolean isFullscreen;

	/**
	 * Whether the seekbar_progress is currently being dragged.
	 */
	private boolean isSeekbarDragging;

	/**
	 * The {@link LayerManager} which is responsible for adding this layer to the container and
	 * displaying it on top of the video player.
	 */
	private LayerManager layerManager;

	/**
	 * The drawable that will be displayed in the {@link PlaybackControlLayer#logoImageView}.
	 */
	private Drawable logoDrawable;

	/**
	 * Displayed in the left of the top chrome - shows a logo. This is optional; if no image
	 * is provided, then no logo will be displayed.
	 */
	private ImageView logoImageView;

	/**
	 * This is the layout of the container before fullscreen mode has been entered.
	 * When we leave fullscreen mode, we restore the layout of the container to this layout.
	 */
	private ViewGroup.LayoutParams originalContainerLayoutParams;

	/**
	 * Contains the actions buttons (displayed in right of the top chrome).
	 */
	private LinearLayout actionButtonsContainer;

	/**
	 * Displays the play icon when the video is playing, or the pause icon when the video is playing.
	 */
	private ImageButton pausePlayLargeButton;

	/**
	 * Play/pause toggle button; causes the player to play or pause the media.
	 */
	private ImageButton pausePlayButton;

	/**
	 * Displays a track and a thumb which can be used to seek to different time points in the video.
	 */
	private SeekBar seekBar;

	/**
	 * Whether the play button has been pressed and the video should be playing.
	 * We include this variable because the video may pause when buffering must occur. Although
	 * the video will usually resume automatically when the buffering is complete, there are instances
	 * (i.e. ad playback), where it will not resume automatically. So, if we detect that the video is
	 * paused after buffering and should be playing, we can resume it programmatically.
	 */
	private boolean shouldBePlaying;

	/**
	 * Encodes the HH:MM:SS or MM:SS time format.
	 */
	private StringBuilder timeFormat;

	/**
	 * Formats times to HH:MM:SS or MM:SS form.
	 */
	private Formatter timeFormatter;

	/**
	 * Contains the logo, video title, and other actions button. It can be tinted with a color for
	 * branding.
	 */
	private LinearLayout topChrome;

	/**
	 * This is the root view which contains all other views that make up the playback control layer.
	 * It can be tinted by setting the chrome color.
	 */
	private FrameLayout playbackControlRootView;

	/**
	 * Contains the seek bar, current time, end time, and fullscreen button. The background can
	 * be tinted with a color for branding.
	 */
	private LinearLayout bottomChrome;

	/**
	 * The title displayed in the {@link PlaybackControlLayer#videoTitleView}.
	 */
	private String videoTitle;

	/**
	 * Video title displayed in the left of the top chrome.
	 */
	private TextView videoTitleView;

	/**
	 * The view created by this {@link PlaybackControlLayer}
	 */
	private FrameLayout view;

	/**
	 * The output menu.
	 */
	private Dialog outputMenu;

    /**
     * The caption menu
     */
    private Dialog captionMenu;

    /**
	 * Indicates playback last state before the output menu has open.
	 */
	private boolean menuWasPlaying;

	/**
	 * Whether it should auto hide controls or not.
	 */
	private boolean autoHide = true;

	private List<Callback> preInitCallbacks = new ArrayList<>();

	private HashMap<String, View> controlsMap;

	private View _captionMenuView;

	public PlaybackControlLayer(String videoTitle) {
		this(videoTitle, null, true);
	}

	public PlaybackControlLayer(String videoTitle, FullscreenCallback fullscreenCallback, boolean autoHide) {
		this.videoTitle = videoTitle;
		this.canSeek = true;
		this.fullscreenCallback = fullscreenCallback;
		this.shouldBePlaying = false;
		this.autoHide = autoHide;
		actionButtons = new ArrayList<>();
	}

	/**
	 * Creates an image based button to put in the set of action buttons at the right of the top chrome.
	 * @param activity The activity that contains the video player.
	 * @param icon The image of the action (ex. trash can).
	 * @param contentDescription The text description this action. This is used in case the
	 *                           action buttons do not fit in the video player. If so, an overflow
	 *                           button will appear and, when clicked, it will display a list of the
	 *                           content descriptions for each action.
	 * @param onClickListener The handler for when the action is triggered.
	 */
	public void addActionButton(Activity activity,
								Drawable icon,
								String contentDescription,
								View.OnClickListener onClickListener) {
		ImageButton button = new ImageButton(activity);

		button.setContentDescription(contentDescription);
		button.setImageDrawable(icon);
		button.setOnClickListener(onClickListener);

		addActionButton(activity, button);
	}

	/**
	 * Creates a button to put in the set of action buttons at the right of the top chrome.
	 * @param activity The activity that contains the video player.
	 * @param button The button to be put.
	 */
	public void addActionButton(Activity activity, View button) {
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT
		);

		int margin = activity.getResources().getDisplayMetrics().densityDpi * 5;
		layoutParams.setMargins(margin, 0, margin, 0);

		button.setBackgroundColor(Color.TRANSPARENT);
		button.setLayoutParams(layoutParams);

		isFullscreen = false;

		actionButtons.add(button);

		if (playbackControlRootView != null) {
			updateActionButtons();
			updateColors();
		}
	}

	@Override
	public FrameLayout createView(LayerManager layerManager) {
		this.layerManager = layerManager;

		LayoutInflater inflater = layerManager.getActivity().getLayoutInflater();

		view = (FrameLayout) inflater.inflate(R.layout.playback_control_layer, null);
		setupView();

		originalContainerLayoutParams = layerManager
				.getContainer()
				.getLayoutParams();

		layerManager.getControl().addCallback(this);

		textColor = DEFAULT_TEXT_COLOR;
		chromeColor = DEFAULT_CHROME_COLOR;
		controlColor = DEFAULT_CONTROL_TINT_COLOR;
		// Since the seek bar doens't use image assets, we can't use TRANSPARENT as the default tint
		// because that would make it invisible, so instead we use the default text tint (White).
		seekbarColor = DEFAULT_TEXT_COLOR;

		if (logoDrawable != null) {
			logoImageView.setImageDrawable(logoDrawable);
		}

		getLayerManager().getContainer().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!autoHide)
					return;

				if (isVisible) {
					hide();
				} else {
					show();
				}
			}
		});

		// Make the view hidden initially. It will be made visible again in the show(timeout) method.
		playbackControlRootView.setVisibility(View.INVISIBLE);
		playerControl = getLayerManager().getControl();

		return view;
	}

	/**
	 * Hides the seek bar thumb and prevents the user from seeking to different time points in the
	 * video.
	 */
	public void disableSeeking() {
		this.canSeek = false;
		if (playbackControlRootView != null) {
			updateColors();
		}
	}

	/**
	 * Fullscreen mode will rotate to landscape mode, hide the action bar, hide the navigation bar,
	 * hide the system tray, and make the video player take up the full size of the display.
	 * The developer who is using this function must ensure the following:
	 *
	 * <p>1) Inside the android manifest, the activity that uses the video player has the attribute
	 * android:configChanges="orientation".
	 *
	 * <p>2) Other views in the activity (or fragment) are
	 * hidden (or made visible) when this method is called.
	 */
	public void doToggleFullscreen() {

		// If there is no callback for handling fullscreen, don't do anything.
		if (fullscreenCallback == null) {
			return;
		}
		PlayerControl playerControl = getLayerManager().getControl();
		if (playerControl == null) {
			return;
		}

		Activity activity = getLayerManager().getActivity();
		FrameLayout container = getLayerManager().getContainer();

		if (isFullscreen) {
			fullscreenCallback.onReturnFromFullscreen();
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

			// Make the status bar and navigation bar visible again.
			activity.getWindow().getDecorView().setSystemUiVisibility(0);

			container.setLayoutParams(originalContainerLayoutParams);

			fullscreenButton.setImageResource(R.drawable.fullscreen);

			isFullscreen = false;
		} else {
			fullscreenCallback.onGoToFullscreen();
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

			activity.getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);

			// Whenever the status bar and navigation bar appear, we want the playback controls to
			// appear as well.
			activity.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
				new View.OnSystemUiVisibilityChangeListener() {
					@Override
					public void onSystemUiVisibilityChange(int i) {
						// By doing a logical AND, we check if the fullscreen option is triggered (i.e. the
						// status bar is hidden). If the result of the logical AND is 0, that means that the
						// fullscreen flag is NOT triggered. This means that the status bar is showing. If
						// this is the case, then we show the playback controls as well (by calling show()).
						if ((i & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
							show();
						}
					}
				}
			);

			container.setLayoutParams(Util.getLayoutParamsBasedOnParent(container,
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT));

			fullscreenButton.setImageResource(R.drawable.fullscreen_exit);

			isFullscreen = true;
		}
	}

	/**
	 * Makes the seek bar thumb visible and allows the user to seek to different time points in the
	 * video.
	 */
	public void enableSeeking() {
		this.canSeek = true;
		if (playbackControlRootView != null) {
			updateColors();
		}
	}

	/**
	 * Returns the {@link LayerManager} which is responsible for displaying this layer's view.
	 */
	public LayerManager getLayerManager() {
		return layerManager;
	}

	/**
	 * Fades the playback control layer out and then removes it from the {@link LayerManager}'s
	 * container.
	 */
	public void hide() {
		hide(false);
	}

	/**
	 * Fades the playback control layer out and then removes it from the {@link LayerManager}'s
	 * container.
	 * @param ignoreState Should it ignore player state and hide anyway?
	 */
	public void hide(boolean ignoreState) {
		if (isFadingOut || !ignoreState && !getLayerManager().getControl().isPlaying()) {
			return;
		}
		final FrameLayout container = getLayerManager().getContainer();
		if (container == null) {
			return;
		}

		if (isVisible) {
			isFadingOut = true;
			playbackControlRootView.animate()
					.alpha(0.0f)
					.setDuration(FADE_OUT_DURATION_MS)
					.setListener(new Animator.AnimatorListener() {
						@Override
						public void onAnimationStart(Animator animation) {
						}

						@Override
						public void onAnimationEnd(Animator animation) {
							isFadingOut = false;
							playbackControlRootView.setVisibility(View.INVISIBLE);
							container.removeView(view);

							// Make sure that the status bar and navigation bar are hidden when the playback
							// controls are hidden.
							if (isFullscreen) {
								getLayerManager().getActivity().getWindow().getDecorView().setSystemUiVisibility(
										View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
							}
							handler.removeMessages(SHOW_PROGRESS);
							isVisible = false;
						}

						@Override
						public void onAnimationCancel(Animator animation) {
						}

						@Override
						public void onAnimationRepeat(Animator animation) {
						}
					});
		}
	}

	/**
	 * Add the playback control layer back to the container.
	 * The playback controls disappear after timeout milliseconds.
	 * @param timeout Hide the view after timeout milliseconds. If timeout == 0, then the playback
	 *                controls will not disappear unless their container is tapped again.
	 */
	public void show(int timeout) {
		if (!autoHide)
			timeout = 0;

		if (!isVisible && getLayerManager().getContainer() != null) {
			playbackControlRootView.setAlpha(1.0f);
			// Make the view visible.
			playbackControlRootView.setVisibility(View.VISIBLE);

			updateProgress();

			// Add the view to the container again.
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT,
					Gravity.CENTER
			);
			getLayerManager().getContainer().removeView(view);
			getLayerManager().getContainer().addView(view, layoutParams);
			setupView();
			isVisible = true;
		}
		updatePlayPauseButton();

		handler.sendEmptyMessage(SHOW_PROGRESS);

		Message msg = handler.obtainMessage(FADE_OUT);
		handler.removeMessages(FADE_OUT);
		if (timeout > 0) {
			handler.sendMessageDelayed(msg, timeout);
		}
	}

	/**
	 * Add the playback control layer back to the container. It will disappear when the user taps
	 * the screen.
	 */
	public void show() {
		show(DEFAULT_TIMEOUT_MS);
	}

	public void setAutoHide(boolean state) {
		autoHide = state;

		if (state)
			show();
	}

	/**
	 * Hides the top chrome (which displays the logo, title, and action buttons).
	 */
	public void hideTopChrome() {
		topChrome.setVisibility(View.GONE);
	}

	/**
	 * Hides the bottom chrome ( play, seekbar, output, fullscreen )
	 */
	public void hideBottomChrome() {
		bottomChrome.setVisibility(View.GONE);
	}

	/**
	 * Hides the bottom chrome ( play, seekbar, output, fullscreen )
	 */
	public void hideMainControls() {
		pausePlayLargeButton.setVisibility(View.GONE);
	}

	/**
	 * Shows the top chrome (which displays the logo, title, and action buttons).
	 */
	public void showTopChrome() {
		topChrome.setVisibility(View.VISIBLE);
		updateActionButtons();
		updateColors();
	}

	/**
	 * Shows the bottom chrome
	 */
	public void showBottomChrome() {
		bottomChrome.setVisibility(View.VISIBLE);
		updateActionButtons();
		updateColors();
	}

	/**
	 * Returns whether the player is currently in fullscreen mode.
	 */
	public boolean isFullscreen() {
		return isFullscreen;
	}

	/**
	 * Make the player enter or leave fullscreen mode.
	 * @param shouldBeFullscreen If true, the player is put into fullscreen mode. If false, the player
	 *                           leaves fullscreen mode.
	 */
	public void setFullscreen(boolean shouldBeFullscreen) {
		if (shouldBeFullscreen != isFullscreen) {
			doToggleFullscreen();
		}
	}

	@Override
	public void onLayerDisplayed(LayerManager layerManager) {}

	/**
	 * Updates the play/pause button to the play icon.
	 */
	@Override
	public void onPause() {
		updatePlayPauseButton();
	}

	/**
	 * Updates the play/pause button to the pause icon.
	 */
	@Override
	public void onPlay() {
		updatePlayPauseButton();
		if (playCallback != null) {
			playCallback.onPlay();
		}
	}

	/**
	 * Sets the color of the top chrome, bottom chrome, and background.
	 * @param color a color derived from the @{link Color} class (ex. {@link Color#RED}).
	 */
	public void setChromeColor(int color) {
		chromeColor = color;
		bottomChrome.setBackgroundColor(color);
		topChrome.setBackgroundColor(color);

		/*if (playbackControlRootView != null) {
			updateColors();
		}*/
	}

	/**
	 * Sets the color of the buttons and seek bar.
	 * @param color a color derived from the @{link Color} class (ex. {@link Color#RED}).
	 */
	public void setControlColor(int color) {
		this.controlColor = color;
		if (playbackControlRootView != null) {
			updateColors();
			updateActionButtons();
		}
	}

	/**
	 * Sets the color of the seekbar_progress.
	 * @param color a color derived from the @{link Color} class (ex. {@link Color#RED}).
	 */
	public void setSeekbarColor(int color) {
		this.seekbarColor = color;
		if (playbackControlRootView != null) {
			updateColors();
		}
	}

	/**
	 * Sets the color of the text views
	 * @param color a color derived from the @{link Color} class (ex. {@link Color#RED}).
	 */
	public void setTextColor(int color) {
		this.textColor = color;
		if (playbackControlRootView != null) {
			updateColors();
		}
	}

	public void setBackgroundColor(int color) {
		playbackControlRootView.setBackgroundColor(color);
	}

	/**
	 * Set the callback which will be called when the player enters and leaves fullscreen mode.
	 * @param fullscreenCallback The callback should hide other views in the activity when the player
	 *                           enters fullscreen mode and show other views when the player leaves
	 *                           fullscreen mode.
	 */
	public void setFullscreenCallback(FullscreenCallback fullscreenCallback) {
		this.fullscreenCallback = fullscreenCallback;
		if (fullscreenButton != null && fullscreenCallback != null) {
			fullscreenButton.setVisibility(View.VISIBLE);
		} else if (fullscreenButton != null && fullscreenCallback == null) {
			fullscreenButton.setVisibility(View.GONE);
		}
	}

	/**
	 * Set the logo with appears in the left of the top chrome.
	 * @param logo The drawable which will be the logo.
	 */
	public void setLogoImageView(Drawable logo) {
		logoDrawable = logo;
		if (logoImageView != null) {
			logoImageView.setImageDrawable(logo);
		}
	}

	/**
	 * Play or pause the player.
	 * @param shouldPlay If true, then the player starts playing. If false, the player pauses.
	 */
	public void setPlayPause(boolean shouldPlay) {
		if (playerControl == null) {
			return;
		}

		if (shouldPlay) {
			playerControl.start();
		} else {
			playerControl.pause();
		}

		updatePlayPauseButton();
	}

	/**
	 * Set the title of the video in the left of the top chrome (to the right of the logo).
	 * @param title The video title. If it is too long, it will be ellipsized.
	 */
	public void setVideoTitle(String title) {
		videoTitle = title;
		if (videoTitleView != null) {
			videoTitleView.setText(title);
		}
	}

	// TODO it might not be here
	/**
	 * Sets the adapter for the output menu.
	 * @param view The view for the output menu.
	 */
	public void setOutputMenu(View view) {
		outputMenu = new Dialog(getLayerManager().getActivity());
		outputMenu.requestWindowFeature(Window.FEATURE_NO_TITLE);
		outputMenu.setContentView(view);
		outputMenu.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		outputMenu.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (menuWasPlaying)
					playerControl.start();
			}
		});

		outputButton.setVisibility(View.VISIBLE);
	}

	/**
	 * Sets the adapter for the caption menu
	 * @param view The view for the caption menu
	 */
	public void setCaptionMenu(View view) {
        captionMenu = new Dialog(getLayerManager().getActivity());
        captionMenu.requestWindowFeature(Window.FEATURE_NO_TITLE);
        captionMenu.setContentView(_captionMenuView = view);
        captionMenu.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        captionMenu.setOnDismissListener(new DialogInterface.OnDismissListener(){
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(menuWasPlaying)
                    playerControl.start();
            }
        });

        captionButton.setVisibility(View.VISIBLE);
    }

	public View getCaptionMenu() {
		return _captionMenuView;
	}

	// TODO it might not be here
	/**
	 * Closes the output menu.
	 */
	public void closeOutputMenu() {
		outputMenu.dismiss();
	}

    /**
     * Closes the caption menu.
     */
    public void closeCaptionMenu() {
        captionMenu.dismiss();
	    hide(true);
    }

	/**
	 * Enables/Disables the specified controls.
	 * @param state Whether to enable or disable the listed controls
	 * @param names Controls names: "play", "playLarge", "fullscreen", "outputMenu", "seekbar", "time"
	 */
	public void setControlsVisible(final boolean state, final String ... names) {
		if (controlsMap == null) {
			preInitCallbacks.add(new Callback() {
				public void call() {
					setControlsVisible(state, names);
				}
			});
			return;
		}

		int visibility = state ? View.VISIBLE : View.GONE;

		// specific
		if (names.length > 0) {
			for (String name : names)
				if (controlsMap.containsKey(name))
					controlsMap.get(name).setVisibility(state || !name.equals("seekbar") ? visibility : View.INVISIBLE);
		}
		// all
		else for (Map.Entry<String, View> pair : controlsMap.entrySet())
			pair.getValue().setVisibility(state || !pair.getKey().equals("seekbar") ? visibility : View.INVISIBLE);
	}

	/*public void swapControls(final String name1, final String name2) {
		if (controlsMap == null) {
			preInitCallbacks.add(new Callback() {
				public void call() {
					swapControls(name1, name2);
				}
			});
			return;
		}

		if (controlsMap.containsKey(name1) && controlsMap.containsKey(name2)) {
			View control1 = (View)controlsMap.get(name1);
			View control2 = (View)controlsMap.get(name2);
			int index1 = view.indexOfChild(control1);

			view.removeView(control1);
			view.addView(control1, view.indexOfChild(control2));
			view.removeView(control2);
			view.addView(control2, index1);
		}
	}*/

	/**
	 * Perform binding to UI, setup of event handlers and initialization of values.
	 */
	private void setupView() {
		// Bind fields to UI elements.
		pausePlayLargeButton = (ImageButton) view.findViewById(R.id.pauseLarge);
		pausePlayButton = (ImageButton) view.findViewById(R.id.pause);
		fullscreenButton = (ImageButton) view.findViewById((R.id.fullscreen));
		outputButton = (ImageButton) view.findViewById(R.id.output_button);
        captionButton = (ImageButton) view.findViewById(R.id.caption_button);
		seekBar = (SeekBar) view.findViewById(R.id.mediacontroller_progress);
		videoTitleView = (TextView) view.findViewById(R.id.video_title);
		endTime = (TextView) view.findViewById(R.id.time_duration);
		currentTime = (TextView) view.findViewById(R.id.time_current);
		logoImageView = (ImageView) view.findViewById(R.id.logo_image);
		playbackControlRootView = (FrameLayout) view.findViewById(R.id.middle_section);
		topChrome = (LinearLayout) view.findViewById(R.id.top_chrome);
		bottomChrome = (LinearLayout) view.findViewById(R.id.bottom_chrome);
		actionButtonsContainer = (LinearLayout) view.findViewById(R.id.actions_container);

		outputButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (outputMenu == null)
					return;

				menuWasPlaying = playerControl.isPlaying();
				playerControl.pause();
				outputMenu.show();
			}
		});

		if (outputMenu == null) {
			// hidden by default
			outputButton.setVisibility(View.GONE);
		}

        //Caption
        captionButton.setOnClickListener(new View.OnClickListener() {
           @Override
            public void onClick(View v) {
               if(captionMenu == null) return;

               menuWasPlaying = playerControl.isPlaying();
               playerControl.pause();
               captionMenu.show();
           }
        });

        if (captionMenu == null) {
           captionButton.setVisibility(View.GONE);
        }

		// The play button should toggle play/pause when the play/pause button is clicked.
		pausePlayLargeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				togglePlayPause();
				show(DEFAULT_TIMEOUT_MS);
			}
		});

		pausePlayButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				togglePlayPause();
				show(DEFAULT_TIMEOUT_MS);
			}
		});

		if (fullscreenCallback == null) {
			fullscreenButton.setVisibility(View.GONE);
		}
		// Go into fullscreen when the fullscreen button is clicked.
		fullscreenButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				doToggleFullscreen();
				show(DEFAULT_TIMEOUT_MS);
				updateActionButtons();
				updateColors();
			}
		});

		seekBar.setMax(1000);

		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromuser) {
				if (!fromuser || !canSeek) {
					// Ignore programmatic changes to seek bar position.
					// Ignore changes to seek bar position is seeking is not enabled.
					return;
				}

				PlayerControl playerControl = getLayerManager().getControl();
				long duration = playerControl.getDuration();
				long newposition = (duration * progress) / 1000L;
				playerControl.seekTo((int) newposition);
				if (currentTime != null) {
					currentTime.setText(stringForTime((int) newposition));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				show(0);
				isSeekbarDragging = true;
				handler.removeMessages(SHOW_PROGRESS);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				isSeekbarDragging = false;
				updateProgress();
				updatePlayPauseButton();
				show(DEFAULT_TIMEOUT_MS);

				handler.sendEmptyMessage(SHOW_PROGRESS);
			}
		});

		videoTitleView.setText(videoTitle);

		timeFormat = new StringBuilder();
		timeFormatter = new Formatter(timeFormat, Locale.getDefault());

		controlsMap = new HashMap<>();
		controlsMap.put("playLarge", pausePlayLargeButton);
		controlsMap.put("play", pausePlayButton);
		controlsMap.put("fullscreen", fullscreenButton);
		controlsMap.put("outputMenu", outputButton);
		controlsMap.put("seekbar", seekBar);
		controlsMap.put("topChrome", topChrome);
		controlsMap.put("bottomChrome", bottomChrome);
		controlsMap.put("time", view.findViewById(R.id.time_container));

		if (preInitCallbacks.size() > 0) {
			for (Callback callback : preInitCallbacks)
				callback.call();

			preInitCallbacks.clear();
		}
	}

	/**
	 * Returns whether the player should be playing (based on whether the user has
	 * tapped pause or play). This can be used by other classes to look at the playback control
	 * layer's play/pause state and force the player to play or pause accordingly.
	 */
	public boolean shouldBePlaying() {
		return shouldBePlaying;
	}

	/**
	 * Format the milliseconds to HH:MM:SS or MM:SS format.
	 */
	public String stringForTime(int timeMs) {
		int totalSeconds = timeMs / 1000;

		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 3600;

		timeFormat.setLength(0);
		if (hours > 0) {
			return timeFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
		} else {
			return timeFormatter.format("%02d:%02d", minutes, seconds).toString();
		}
	}

	/**
	 * If the player is paused, play it and if the player is playing, pause it.
	 */
	public void togglePlayPause() {
		this.shouldBePlaying = !getLayerManager().getControl().isPlaying();
		setPlayPause(shouldBePlaying);
	}

	/**
	 * The action buttons are displayed in the top right of the video player. If the player is in
	 * portrait mode, then display an overflow button which displays a dialog window containing the
	 * possible actions. If the player is in landscape, then display the images for the actions in the
	 * top right of the video player.
	 */
	public void updateActionButtons() {
		actionButtonsContainer.removeAllViews();

		//if (isFullscreen) {
			for (View button : actionButtons) {
				actionButtonsContainer.addView(button);
			}
		/*} else {
			ImageButton overflowButton = new ImageButton(getLayerManager().getActivity());
			overflowButton.setContentDescription(getLayerManager()
					.getActivity()
					.getString(R.string.overflow));
			overflowButton.setImageDrawable(getLayerManager()
					.getActivity()
					.getResources()
					.getDrawable(R.drawable.ic_action_overflow));

			AlertDialog.Builder builder = new AlertDialog.Builder(getLayerManager().getActivity());
			builder.setTitle(getLayerManager().getActivity().getString(R.string.select_an_action));
			final CharSequence[] actions = new CharSequence[actionButtons.size()];
			for (int i = 0; i < actionButtons.size(); i++) {
				actions[i] = actionButtons.get(i).getContentDescription();
			}
			builder.setItems(actions, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					actionButtons.get(i).performClick();
				}
			});

			final AlertDialog alertDialog = builder.create();

			overflowButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					alertDialog.show();
				}
			});

			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT
			);
			int margin = 5 * getLayerManager()
					.getActivity()
					.getResources()
					.getDisplayMetrics()
					.densityDpi;
			layoutParams.setMargins(margin, 0, margin, 0);

			overflowButton.setBackgroundColor(Color.TRANSPARENT);
			overflowButton.setLayoutParams(layoutParams);
			overflowButton.setColorFilter(controlColor);
			actionButtonsContainer.addView(overflowButton);
		}*/
	}

	/**
	 * Ensure that the chrome, control, and text colors displayed on the screen are correct.
	 */
	public void updateColors() {
		/*currentTime.setTextColor(textColor);
		endTime.setTextColor(textColor);
		videoTitleView.setTextColor(textColor);

		fullscreenButton.setColorFilter(controlColor);
		pausePlayLargeButton.setColorFilter(controlColor);*/
		((LayerDrawable)seekBar.getProgressDrawable()).findDrawableByLayerId(android.R.id.progress).setColorFilter(seekbarColor, PorterDuff.Mode.ADD);
		//seekBar.getThumb().setColorFilter(seekbarColor, PorterDuff.Mode.SRC_ATOP);

		// Hide the thumb drawable if the SeekBar is disabled
		if (canSeek) {
			seekBar.getThumb().mutate().setAlpha(255);
		} else {
			seekBar.getThumb().mutate().setAlpha(0);
		}

		/*for (ImageButton imageButton : actionButtons) {
			imageButton.setColorFilter(controlColor);
		}

		topChrome.setBackgroundColor(chromeColor);
		bottomChrome.setBackgroundColor(chromeColor);*/
	}

	/**
	 * Change the icon of the play/pause button to indicate play or pause based on the state of the
	 * video player.
	 */
	public void updatePlayPauseButton() {
		PlayerControl playerControl = getLayerManager().getControl();
		if (view == null || pausePlayLargeButton == null || playerControl == null) {
			return;
		}

		if (playerControl.isPlaying()) {
			pausePlayLargeButton.setImageResource(R.drawable.ic_action_pause_large);
			pausePlayButton.setImageResource(R.drawable.ic_action_pause);
		} else {
			pausePlayLargeButton.setImageResource(R.drawable.ic_action_play_large);
			pausePlayButton.setImageResource(R.drawable.ic_action_play);
		}
	}

	/**
	 * Adjust the position of the action bar to reflect the progress of the video.
	 */
	public int updateProgress() {
		PlayerControl playerControl = getLayerManager().getControl();
		if (playerControl == null || isSeekbarDragging) {
			return 0;
		}

		int position = playerControl.getCurrentPosition();
		int duration = playerControl.getDuration();

		if (seekBar != null) {
			if (duration > 0) {
				long pos = 1000L * position / duration;
				seekBar.setProgress((int) pos);
			}

			int percent = playerControl.getBufferPercentage();
			seekBar.setSecondaryProgress(percent * 10);
		}

		if (endTime != null) {
			endTime.setText(stringForTime(duration));
		}
		if (currentTime != null) {
			currentTime.setText(stringForTime(position));
		}

		return position;
	}

	/**
	 * Set play callback
	 */
	public void setPlayCallback(PlayCallback playCallback) {
		this.playCallback = playCallback;
	}
}