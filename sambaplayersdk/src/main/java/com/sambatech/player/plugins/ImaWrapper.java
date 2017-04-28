package com.sambatech.player.plugins;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.google.ads.interactivemedia.v3.api.player.ContentProgressProvider;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.libraries.mediaframework.exoplayerextensions.ExoplayerWrapper;
import com.google.android.libraries.mediaframework.exoplayerextensions.Video;
import com.google.android.libraries.mediaframework.layeredvideo.PlaybackControlLayer;
import com.google.android.libraries.mediaframework.layeredvideo.SimpleVideoPlayer;
import com.google.android.libraries.mediaframework.layeredvideo.Util;
import com.sambatech.player.R;
import com.sambatech.player.SambaPlayer;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * The ImaWrapper is responsible for displaying both videos and ads. This is accomplished using two
 * video players. The content player displays the user's video. When an ad is requested, the ad
 * video player is overlaid on the content video player. When the ad is complete, the ad video
 * player is destroyed and the content video player is displayed again.
 */
public class ImaWrapper implements Plugin {

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

	private ContentProgressProvider contentProgressProvider;

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
			Log.i("ima", "ad player error: " + e.getMessage());
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
	 * Sets up ads manager, responds to ad errors, and handles ad state changes.
	 */
	private class AdListener implements AdErrorEvent.AdErrorListener,
			AdsLoader.AdsLoadedListener, AdEvent.AdEventListener {

		private ImageView img;
		private FrameLayout.LayoutParams layoutParams;

		public AdListener() {
			// TODO: decouple dependency
			img = new ImageView(activity);
			img.setImageResource(R.drawable.play);
		}

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
			Log.i("ima", event.getType() + "");
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
				case CLICKED:
					layoutParams = new FrameLayout.LayoutParams(adUiContainer.getLayoutParams());
					layoutParams.width = 150;
					layoutParams.height = 150;
					layoutParams.gravity = Gravity.CENTER;
					adUiContainer.addView(img, layoutParams);
					break;
				case TAPPED:
					if (adPlayer != null) {
						adUiContainer.removeView(img);
						adPlayer.play();
					}
					break;
				case ALL_ADS_COMPLETED:
					onDestroy();
					break;
				case STARTED:
					if(adPlayer != null) {
						adPlayer.hideLoading();
					}
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
			adPlayer.play();

			// Notify the callbacks that the ad has begun playing.
			for (VideoAdPlayer.VideoAdPlayerCallback callback : callbacks)
				callback.onPlay();
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
			if (adPlayer != null) {
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
			VideoProgressUpdate vpu = adPlayer != null && adPlayer.getDuration() > 0 ?
					new VideoProgressUpdate(adPlayer.getCurrentPosition(), adPlayer.getDuration()) :
					VideoProgressUpdate.VIDEO_TIME_NOT_READY;

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

	private SambaPlayerListener playerListener = new SambaPlayerListener() {
		@Override
		public void onLoad(SambaEvent event) {
			if (!adsShown && ImaWrapper.this.adTagUrl != null) {
				requestAd();
				adsShown = true;
			}
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
		public void onFinish(SambaEvent event) {
			if (adsLoader != null)
				adsLoader.contentComplete();
		}
	};

	public void onLoad(@NonNull SambaPlayer player) {
		Log.i("ima", "load");
		SambaMedia media = player.getMedia();

		if (media.adUrl == null || media.adUrl.isEmpty()) {
			PluginManager.getInstance().notifyPluginLoaded(this);
			return;
		}

		adTagUrl = Uri.parse(media.adUrl);
		contentPlayer = player;
		container = player;
		activity = (Activity)container.getContext();

		ImaSdkSettings sdkSettings = ImaSdkFactory.getInstance().createImaSdkSettings();
		sdkSettings.setPlayerType(PLAYER_TYPE);
		sdkSettings.setPlayerVersion(PLAYER_VERSION);

		adsLoader = ImaSdkFactory.getInstance().createAdsLoader(activity, sdkSettings);
		adListener = new AdListener();
		adsLoader.addAdErrorListener(adListener);
		adsLoader.addAdsLoadedListener(adListener);
		callbacks = new ArrayList<>();

		// Create the ad adDisplayContainer UI which will be used by the IMA SDK to overlay ad controls.
		adUiContainer = new FrameLayout(activity);
		container.addView(adUiContainer);
		adUiContainer.setLayoutParams(Util.getLayoutParamsBasedOnParent(
				adUiContainer,
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		originalContainerLayoutParams = container.getLayoutParams();
		contentProgressProvider = new ContentProgressProvider() {
			@Override
			public VideoProgressUpdate getContentProgress() {
				return contentPlayer != null && contentPlayer.getDuration() > 0 ?
						new VideoProgressUpdate((long)(contentPlayer.getCurrentTime()*1000), (long)contentPlayer.getDuration()) :
						VideoProgressUpdate.VIDEO_TIME_NOT_READY;
			}
		};

		SambaEventBus.subscribe(playerListener);
		PluginManager.getInstance().notifyPluginLoaded(this);
	}

	public void onInternalPlayerCreated(@NonNull SimpleVideoPlayer internalPlayer) {}

	public void onDestroy() {
		Log.i("ima:", String.valueOf(adsLoader == null));
		if (adsLoader == null)
			return;

		//Main player reference destruction
		contentPlayer = null;

		SambaEventBus.unsubscribe(playerListener);
		destroyAdPlayer();
		release();
	}

	/**
	 * Pause video playback.
	 */
	public void pause() {
		if (adPlayer != null)
			adPlayer.pause();
	}

	/**
	 * Resume video playback.
	 */
	public void play() {
		if (adTagUrl != null)
			requestAd();
	}

	/**
	 * When you are finished using this {@link ImaWrapper}, make sure to call this method.
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

		if (adsLoader != null) {
			adsLoader.removeAdsLoadedListener(adListener);
			adsLoader.removeAdErrorListener(adListener);
			adsLoader = null;
		}
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
				false,
				0,
				fullscreenCallback);

		adPlayer.addPlaybackListener(adPlaybackListener);

		// Move the ad player's surface layer to the foreground so that it is overlaid on the content
		// player's surface layer (which is in the background).
		adPlayer.moveSurfaceToForeground();
		adPlayer.disableSeeking();

		SambaMediaConfig m = (SambaMediaConfig)contentPlayer.getMedia();

		adPlayer.setThemeColor(m != null ? m.themeColor : Color.YELLOW);
		adPlayer.hideTopChrome();
		adPlayer.setFullscreen(contentPlayer.isFullscreen());
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
			//contentPlayer.setFullscreen(adPlayer.isFullscreen());
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
	private void showContentPlayer() {
		if(contentPlayer == null)
			return;

		contentPlayer.show();

		if (!contentPlayer.hasFinished())
			contentPlayer.play();
	}

	/**
	 * Pause the content player and notify the ad callbacks that the content has paused.
	 */
	private void pauseContent() {
		hideContentPlayer();
		for (VideoAdPlayer.VideoAdPlayerCallback callback : callbacks) {
			callback.onPause();
		}
	}

	/**
	 * Resume the content and notify the ad callbacks that the content has resumed.
	 */
	private void resumeContent() {
		if(contentPlayer == null) return;

		if (contentPlayer.hasFinished())
			return;

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
		request.setContentProgressProvider(contentProgressProvider);
		request.setAdDisplayContainer(adDisplayContainer);

		return request;
	}

	/**
	 * Make the ads loader request an ad with the ad tag URL which this {@link ImaWrapper} was
	 * created with
	 */
	private void requestAd() {
		adsLoader.requestAds(buildAdsRequest(adTagUrl.toString()));
	}
}
