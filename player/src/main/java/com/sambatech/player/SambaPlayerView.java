package com.sambatech.player;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.libraries.mediaframework.exoplayerextensions.ExoplayerWrapper;
import com.google.android.libraries.mediaframework.exoplayerextensions.Video;
import com.google.android.libraries.mediaframework.layeredvideo.PlaybackControlLayer;
import com.google.android.libraries.mediaframework.layeredvideo.SimpleVideoPlayer;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaConfig;
import com.sambatech.player.plugins.ImaWrapper;
import com.sambatech.player.plugins.Tracking;

/**
 * Controller for SambaPlayer view.
 *
 * @author Leandro Zanol - 7/12/15
 */
public class SambaPlayerView extends FrameLayout implements SambaPlayer {

	private SimpleVideoPlayer player;
	private SambaMediaConfig media = new SambaMediaConfig();
	private SambaPlayerListener listener;
	private boolean isReady;
	private boolean hasStarted;

	public SambaPlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		applyAttributes(getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SambaPlayerView, 0, 0));

        /*if (!isInEditMode() && media.url != null)
            createPlayer();*/
	}

	/**	Player API **/

	public void setMedia(SambaMedia media) {
		if (media == null) {
			//SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.ERROR, ));
			throw new IllegalArgumentException("Media data is null.");
		}

		this.media = (SambaMediaConfig)media;

        //Creating player
        createPlayer();
	}

	public SambaMedia getMedia() {
		return media;
	}

	public void setListener(SambaPlayerListener listener) {
		this.listener = listener;
	}

	public void play() {
		player.play();
	}

	public void pause() {
		if (isReady)
			player.pause();
	}

	public void stop() {
		player.stop();
		SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.STOP));
	}

	public void seek(float position) {
		player.seek((int) (position * 1000f));
	}

	public void setFullscreen(boolean flag) {
		player.setFullscreen(flag);
	}

	public boolean isFullscreen() {
		return player.isFullscreen();
	}

	public void show() {
		player.show();
	}

	public void hide() {
		player.hide();
	}

	public float getCurrentTime() {
		return player.getCurrentPosition()/1000f;
	}

	public float getDuration() {
		return player.getDuration()/1000f;
	}

	public void destroy() {
		if (player == null)
			return;

		stop();
		player.release();
		SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.UNLOAD));
	}

	public View getView() {
		return this;
	}

	private void createPlayer() {
        if (media.url == null || media.url.isEmpty()) {
			Toast.makeText(getContext(), "The requested media has no URL!", Toast.LENGTH_SHORT).show();
			return;
		}

		destroy();

		Video.VideoType videoType = Video.VideoType.OTHER;

		switch (media.type.toLowerCase()) {
			/*case "progressive":
				videoType = Video.VideoType.MP4;
				break;*/
			case "hls":
				videoType = Video.VideoType.HLS;
				break;
			case "dash":
				videoType = Video.VideoType.DASH;
				break;
		}

        player = new SimpleVideoPlayer((Activity)getContext(), this,
                new Video(media.url, videoType),
                media.title, media.adUrl == null || media.adUrl.isEmpty());

		player.setSeekbarColor(media.themeColor);

		if (media.isLive)
			player.setControlsVisible(false);

		// Move the content player's surface layer to the background so that the ad player's surface
		// layer can be overlaid on top of it during ad playback.
		player.moveSurfaceToBackground();

		player.addActionButton(ContextCompat.getDrawable(getContext(), R.drawable.share), getContext().getString(R.string.share_facebook), new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getContext(), "Share Facebook", Toast.LENGTH_SHORT).show();
			}
		});

		player.addPlaybackListener(new ExoplayerWrapper.PlaybackListener() {
			@Override
			public void onStateChanged(boolean playWhenReady, int playbackState) {
				Log.i("player", "state: " + playWhenReady + " " + playbackState);

				switch (playbackState) {
					case ExoPlayer.STATE_READY:
						if (!playWhenReady)
							SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.PAUSE));
						break;
					case ExoPlayer.STATE_ENDED:
						pause();
						SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.FINISH));
						break;
				}
			}

			@Override
			public void onError(Exception e) {
				Log.i("player", "Error: " + media.url, e);
				destroy();
				SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.ERROR, e.getMessage()));
			}

			@Override
			public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
				Log.i("player", "size: " + width + ' ' + height + ' ' + unappliedRotationDegrees + ' ' + pixelWidthHeightRatio);
				SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.RESIZE, width, height, unappliedRotationDegrees, pixelWidthHeightRatio));
			}
		});

		player.setPlayCallback(new PlaybackControlLayer.PlayCallback() {
			@Override
			public void onPlay() {
				if (player.getPlaybackState() == ExoPlayer.STATE_ENDED)
					seek(0);

				if (!hasStarted) {
					hasStarted = true;
					SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.START));
				}

				SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.PLAY));
			}
		});

		player.setFullscreenCallback(new PlaybackControlLayer.FullscreenCallback() {
			@Override
			public void onGoToFullscreen() {
				SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.FULLSCREEN));
			}

			@Override
			public void onReturnFromFullscreen() {
				SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.FULLSCREEN_EXIT));
			}
		});

		// Plugins

		// TODO: desacoplar... (PluginsManager...onLoad: new plgs[i]()...onUnload: plgs[i].destroy())

		if (media.adUrl != null && !media.adUrl.isEmpty())
			new ImaWrapper((Activity)getContext(), this, media.adUrl);

		//new Tracking();

		isReady = true;

		SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.LOAD, this));
	}

	private void applyAttributes(TypedArray attrs) {
		media.url = attrs.getString(R.styleable.SambaPlayerView_url);
		media.title = attrs.getString(R.styleable.SambaPlayerView_title);
		attrs.recycle();
	}
}
