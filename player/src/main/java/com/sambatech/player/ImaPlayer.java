package com.sambatech.player;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.libraries.mediaframework.exoplayerextensions.ExoplayerWrapper;
import com.google.android.libraries.mediaframework.exoplayerextensions.Video;
import com.google.android.libraries.mediaframework.layeredvideo.PlaybackControlLayer;
import com.google.android.libraries.mediaframework.layeredvideo.SimpleVideoPlayer;
import com.google.android.libraries.mediaframework.layeredvideo.Util;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;

import java.util.ArrayList;
import java.util.List;

/**
 * The ImaPlayer is responsible for displaying both videos and ads. This is accomplished using two
 * video players. The content player displays the user's video. When an ad is requested, the ad
 * video player is overlaid on the content video player. When the ad is complete, the ad video
 * player is destroyed and the content video player is displayed again.
 */
public class ImaPlayer {

	private static String PLAYER_TYPE = "google/gmf-android";
	private static String PLAYER_VERSION = "0.2.0";

	/**
	 * The activity that is displaying this video player.
	 */
	private Activity activity;

	/**
	 * Url of the ad.
	 */
	private Uri adTagUrl;

	/**
	 * Plays the ad.
	 */
	private SimpleVideoPlayer adPlayer;

	/**
	 * The layout that contains the ad player.
	 */
	private FrameLayout adPlayerContainer;

	/**
	 * Used by the IMA SDK to overlay controls (i.e. skip ad) over the ad player.
	 */
	private FrameLayout adUiContainer;

	/**
	 * Responsible for requesting the ad and creating the
	 * {@link com.google.ads.interactivemedia.v3.api.AdsManager}
	 */
	private AdsLoader adsLoader;


	/**
	 * Responsible for containing listeners for processing the elements of the ad.
	 */
	private AdsManager adsManager;

	private AdListener adListener;

	/**
	 * These callbacks are notified when the video is played and when it ends. The IMA SDK uses this
	 * to poll for video progress and when to stop the ad.
	 */
	private List<VideoAdPlayer.VideoAdPlayerCallback> callbacks;

	/**
	 * Contains the content player and the ad frame layout.
	 */
	private FrameLayout container;

	/**
	 * Plays the content (i.e. the actual video).
	 */
	private SambaPlayer contentPlayer;

	/**
	 * The callback that is triggered when fullscreen mode is entered or closed.
	 */
	private PlaybackControlLayer.FullscreenCallback fullscreenCallback;

	/**
	 * Last recorded progress in ad playback. Occasionally the ad pauses when it needs to buffer (and
	 * progress stops), so it must be resumed. We detect this situation by noting if the difference
	 * "current progress" - "last recorded progress" = 0. If this occurs, then we can pause the
	 * video and replay it. This causes the ad to continue playback again.
	 */
	private VideoProgressUpdate oldVpu;

	/**
	 * This is the layout of the container before fullscreen mode has been entered.
	 * When we leave fullscreen mode, we restore the layout of the container to this layout.
	 */
	private ViewGroup.LayoutParams originalContainerLayoutParams;

	/**
	 * A flag to indicate whether the ads has been shown.
	 */
	private boolean adsShown;

	/**
	 * Notifies callbacks when the ad finishes.
	 */
	private final ExoplayerWrapper.PlaybackListener adPlaybackListener
			= new ExoplayerWrapper.PlaybackListener() {

		/**
		 * We don't respond to errors.
		 * @param e The error.
		 */
		@Override
		public void onError(Exception e) {

		}

		/**
		 * We notify all callbacks when the ad ends.
		 * @param playWhenReady Whether the video should play as soon as it is loaded.
		 * @param playbackState The state of the Exoplayer instance.
		 */
		@Override
		public void onStateChanged(boolean playWhenReady, int playbackState) {
			if (playbackState == ExoPlayer.STATE_ENDED) {
				for (VideoAdPlayer.VideoAdPlayerCallback callback : callbacks) {
					callback.onEnded();
				}
			}
		}

		@Override
		public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
			// No need to respond to size changes here.
		}
	};

	/**
	 * Listener for the content player
	 */
	private final ExoplayerWrapper.PlaybackListener contentPlaybackListener
			= new ExoplayerWrapper.PlaybackListener() {

		/**
		 * We don't respond to errors.
		 * @param e The error.
		 */
		@Override
		public void onError(Exception e) {

		}

		/**
		 * We notify the adLoader when the content has ended so it knows to play postroll ads.
		 * @param playWhenReady Whether the video should play as soon as it is loaded.
		 * @param playbackState The state of the Exoplayer instance.
		 */
		@Override
		public void onStateChanged(boolean playWhenReady, int playbackState) {
			if (playbackState == ExoPlayer.STATE_ENDED) {
				adsLoader.contentComplete();
			}
		}

		/**
		 * We don't respond to size changes.
		 * @param width The new width of the player.
		 * @param height The new height of the player.
		 * @param unappliedRotationDegrees The new rotation angle of the player thats not applied.
		 */
		@Override
		public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

		}
	};


	/**
	 * Sets up ads manager, responds to ad errors, and handles ad state changes.
	 */
	private class AdListener implements AdErrorEvent.AdErrorListener,
			AdsLoader.AdsLoadedListener, AdEvent.AdEventListener {
		@Override
		public void onAdError(AdErrorEvent adErrorEvent) {
			// If there is an error in ad playback, log the error and resume the content.
			Log.d(this.getClass().getSimpleName(), adErrorEvent.getError().getMessage());

			// Display a toast message indicating the error.
			// You should remove this line of code for your production app.
			Toast.makeText(activity, adErrorEvent.getError().getMessage(), Toast.LENGTH_SHORT).show();
			resumeContent();
		}

		@Override
		public void onAdEvent(AdEvent event) {
			switch (event.getType()) {
				case LOADED:
					adsManager.start();
					break;
				case CONTENT_PAUSE_REQUESTED:
					pauseContent();
					break;
				case CONTENT_RESUME_REQUESTED:
					resumeContent();
					break;
				default:
					break;
			}
		}

		@Override
		public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
			adsManager = adsManagerLoadedEvent.getAdsManager();
			adsManager.addAdErrorListener(this);
			adsManager.addAdEventListener(this);
			adsManager.init();
		}
	}

	/**
	 * Handles loading, playing, retrieving progress, pausing, resuming, and stopping ad.
	 */
	private final VideoAdPlayer videoAdPlayer = new VideoAdPlayer() {
		@Override
		public void playAd() {
			hideContentPlayer();
		}

		@Override
		public void loadAd(String mediaUri) {
			adTagUrl = Uri.parse(mediaUri);
			createAdPlayer();
		}

		@Override
		public void stopAd() {
			destroyAdPlayer();
			showContentPlayer();
		}

		@Override
		public void pauseAd() {
			if (adPlayer != null){
				adPlayer.pause();
			}
		}

		@Override
		public void resumeAd() {
			if(adPlayer != null) {
				adPlayer.play();
			}
		}

		@Override
		public void addCallback(VideoAdPlayerCallback videoAdPlayerCallback) {
			callbacks.add(videoAdPlayerCallback);
		}

		@Override
		public void removeCallback(VideoAdPlayerCallback videoAdPlayerCallback) {
			callbacks.remove(videoAdPlayerCallback);
		}

		/**
		 * Reports progress in ad player or content player (whichever is currently playing).
		 *
		 * NOTE: When the ad is buffering, the video is paused. However, when the buffering is
		 * complete, the ad is resumed. So, as a workaround, we will attempt to resume the ad, by
		 * calling the start method, whenever we detect that the ad is buffering. If the ad is done
		 * buffering, the start method will resume playback. If the ad has not finished buffering,
		 * then the start method will be ignored.
		 */
		@Override
		public VideoProgressUpdate getAdProgress() {
			VideoProgressUpdate vpu;

			if (adPlayer == null && contentPlayer == null) {
				// If neither player is available, indicate that the time is not ready.
				vpu = VideoProgressUpdate.VIDEO_TIME_NOT_READY;
			} else if (adPlayer != null) {
				// If an ad is playing, report the progress of the ad player.
				vpu = new VideoProgressUpdate(adPlayer.getCurrentPosition(),
						adPlayer.getDuration());
			} else {
				// If the cotntent is playing, report the progress of the content player.
				vpu = new VideoProgressUpdate((long)(contentPlayer.getCurrentTime() * 1000),
						(long)(contentPlayer.getDuration() * 1000));
			}

			if (oldVpu == null) {
				oldVpu = vpu;
			} else if ((!vpu.equals(VideoProgressUpdate.VIDEO_TIME_NOT_READY))
					&& vpu.getCurrentTime() == oldVpu.getCurrentTime()) {
				// TODO(hsubrama): Find better method for detecting ad pause and resuming ad playback.
				// Resume the ad player if it has paused due to buffering.
				if (adPlayer != null && adPlayer.shouldBePlaying()) {
					adPlayer.pause();
					adPlayer.play();
				}
			}

			oldVpu = vpu;
			return vpu;
		}
	};

	/**
	 * @param activity The activity that will contain the video player.
	 * @param player Video content player.
	 * @param sdkSettings The settings that should be used to configure the IMA SDK.
	 * @param adTagUrl The URL containing the VAST document of the ad.
	 */
	public ImaPlayer(Activity activity,
					 SambaPlayer player,
					 ImaSdkSettings sdkSettings,
					 String adTagUrl) {
		this.activity = activity;
		this.contentPlayer = player;
		this.container = (FrameLayout)player.getView();

		if (adTagUrl != null) {
			this.adTagUrl = Uri.parse(adTagUrl);
		}

		sdkSettings.setPlayerType(PLAYER_TYPE);
		sdkSettings.setPlayerVersion(PLAYER_VERSION);
		adsLoader = ImaSdkFactory.getInstance().createAdsLoader(activity, sdkSettings);
		adListener = new AdListener();
		adsLoader.addAdErrorListener(adListener);
		adsLoader.addAdsLoadedListener(adListener);

		callbacks = new ArrayList<VideoAdPlayer.VideoAdPlayerCallback>();

		// Create the ad adDisplayContainer UI which will be used by the IMA SDK to overlay ad controls.
		adUiContainer = new FrameLayout(activity);
		container.addView(adUiContainer);
		adUiContainer.setLayoutParams(Util.getLayoutParamsBasedOnParent(
				adUiContainer,
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		this.originalContainerLayoutParams = container.getLayoutParams();

		// Listeners

		SambaEventBus.subscribe(new SambaPlayerListener() {
			@Override
			public void onPlay(SambaEvent e) {
				Log.i("ima", e.getType() + " " + e.getData());
				handlePlay();
			}

			@Override
			public void onFullscreen(SambaEvent e) {
				if (adPlayer == null)
					container.setLayoutParams(Util.getLayoutParamsBasedOnParent(
							container,
							ViewGroup.LayoutParams.MATCH_PARENT,
							ViewGroup.LayoutParams.MATCH_PARENT
					));
			}

			@Override
			public void onFullscreenExit(SambaEvent e) {
				if (adPlayer == null)
					container.setLayoutParams(originalContainerLayoutParams);
			}

			@Override
			public void onPause(SambaEvent e) {
				Log.i("ima", e.getType() + " " + e.getData());
			}

			@Override
			public void onFinish(SambaEvent e) {
				Log.i("ima", e.getType() + " " + e.getData());
			}
		});
	}

	/**
	 * @param activity The activity that will contain the video player.
	 * @param player Video content player.
	 * @param adTagUrl The URL containing the VAST document of the ad.
	 */
	public ImaPlayer(Activity activity,
					 SambaPlayer player,
					 String adTagUrl) {
		this(activity,
				player,
				ImaSdkFactory.getInstance().createImaSdkSettings(),
				adTagUrl);
	}

	/**
	 * Pause video playback.
	 */
	public void pause() {
		if (adPlayer != null) {
			adPlayer.pause();
		}
		contentPlayer.pause();
	}

	/**
	 * Resume video playback.
	 */
	public void play() {
		if (adTagUrl != null) {
			requestAd();
		} else {
			contentPlayer.play();
		}
	}

	/**
	 * When you are finished using this {@link ImaPlayer}, make sure to call this method.
	 */
	public void release() {
		if (adPlayer != null) {
			adPlayer.release();
			adPlayer = null;
		}
		if (adsManager != null) {
			adsManager.destroy();
			adsManager = null;
		}
		adsLoader.contentComplete();
		//contentPlayer.release(); TODO: tirar
		adsLoader.removeAdsLoadedListener(adListener);
	}

	/**
	 * Create a {@link SimpleVideoPlayer} to play an ad and display it.
	 */
	private void createAdPlayer(){
		// Kill any existing ad player.
		destroyAdPlayer();

		// Add the ad frame layout to the adDisplayContainer that contains all the content player.
		adPlayerContainer = new FrameLayout(activity);
		container.addView(adPlayerContainer);
		adPlayerContainer.setLayoutParams(Util.getLayoutParamsBasedOnParent(
				adPlayerContainer,
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT
		));

		// Ensure tha the ad ui adDisplayContainer is the topmost view.
		container.removeView(adUiContainer);
		container.addView(adUiContainer);


		Video adVideo = new Video(adTagUrl.toString(), Video.VideoType.MP4);
		adPlayer = new SimpleVideoPlayer(activity,
				adPlayerContainer,
				adVideo,
				"",
				true,
				0,
				fullscreenCallback);

		adPlayer.addPlaybackListener(adPlaybackListener);

		// Move the ad player's surface layer to the foreground so that it is overlaid on the content
		// player's surface layer (which is in the background).
		adPlayer.moveSurfaceToForeground();
		adPlayer.play();
		adPlayer.disableSeeking();
		adPlayer.setSeekbarColor(Color.YELLOW);
		adPlayer.hideTopChrome();
		adPlayer.setFullscreen(contentPlayer.isFullscreen());

		// Notify the callbacks that the ad has begun playing.
		for (VideoAdPlayer.VideoAdPlayerCallback callback : callbacks) {
			callback.onPlay();
		}
	}

	/**
	 * Destroy the {@link SimpleVideoPlayer} responsible for playing the ad and remove it.
	 */
	private void destroyAdPlayer(){
		if(adPlayerContainer != null){
			container.removeView(adPlayerContainer);
		}
		if (adUiContainer != null) {
			container.removeView(adUiContainer);
		}
		if(adPlayer != null){
			contentPlayer.setFullscreen(adPlayer.isFullscreen());
			adPlayer.release();
		}
		adPlayerContainer = null;
		adPlayer = null;
	}

	/**
	 * Pause and hide the content player.
	 */
	private void hideContentPlayer(){
		contentPlayer.pause();
		contentPlayer.hide();
	}

	/**
	 * Show the content player and start playing again.
	 */
	private void showContentPlayer(){
		contentPlayer.show();
		contentPlayer.play();
	}

	/**
	 * Pause the content player and notify the ad callbacks that the content has paused.
	 */
	private void pauseContent(){
		hideContentPlayer();
		for (VideoAdPlayer.VideoAdPlayerCallback callback : callbacks) {
			callback.onPause();
		}
	}

	/**
	 * Resume the content and notify the ad callbacks that the content has resumed.
	 */
	private void resumeContent(){
		destroyAdPlayer();
		showContentPlayer();
		for (VideoAdPlayer.VideoAdPlayerCallback callback : callbacks) {
			callback.onResume();
		}
	}

	/**
	 * Create an ads request which will request the VAST document with the given ad tag URL.
	 * @param tagUrl URL pointing to a VAST document of an ad.
	 * @return a request for the VAST document.
	 */
	private AdsRequest buildAdsRequest(String tagUrl) {
		AdDisplayContainer adDisplayContainer = ImaSdkFactory.getInstance().createAdDisplayContainer();
		adDisplayContainer.setPlayer(videoAdPlayer);
		adDisplayContainer.setAdContainer(adUiContainer);
		AdsRequest request = ImaSdkFactory.getInstance().createAdsRequest();
		request.setAdTagUrl(tagUrl);

		request.setAdDisplayContainer(adDisplayContainer);
		return request;
	}

	/**
	 * Make the ads loader request an ad with the ad tag URL which this {@link ImaPlayer} was
	 * created with
	 */
	private void requestAd() {
		adsLoader.requestAds(buildAdsRequest(adTagUrl.toString()));
	}

	/**
	 * handle play callback, to request IMA ads
	 */
	private void handlePlay() {
		if (!adsShown && adTagUrl != null) {
			Log.i("player", contentPlayer+"");
			contentPlayer.pause();
			requestAd();
			adsShown = true;
		}
	}
}
