package com.sambatech.player;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.libraries.mediaframework.exoplayerextensions.ExoplayerWrapper;
import com.google.android.libraries.mediaframework.exoplayerextensions.Video;
import com.google.android.libraries.mediaframework.layeredvideo.PlaybackControlLayer;
import com.google.android.libraries.mediaframework.layeredvideo.SimpleVideoPlayer;
import com.sambatech.player.adapter.OutputAdapter;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaConfig;
import com.sambatech.player.plugins.PluginsManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Controller for SambaPlayer view.
 *
 * @author Leandro Zanol - 7/12/15
 */
public class SambaPlayerController implements SambaPlayer {

	private SimpleVideoPlayer player;
	private SambaMediaConfig media = new SambaMediaConfig();
	private Timer progressTimer;
	private boolean _hasStarted;
	private boolean _hasFinished;
	private FrameLayout container;
	private ListView outputMenuList;
	private OrientationEventListener orientationEventListener;

	private static final SambaPlayerController instance = new SambaPlayerController();

	public static SambaPlayerController getInstance() {
		return instance;
	}

	private final ExoplayerWrapper.PlaybackListener playbackListener = new ExoplayerWrapper.PlaybackListener() {
		@Override
		public void onStateChanged(boolean playWhenReady, int playbackState) {
			Log.i("player", "state: " + playWhenReady + " " + playbackState);

			switch (playbackState) {
				case ExoPlayer.STATE_READY:
					if (playWhenReady) {
                        if (!_hasStarted) {
                            _hasStarted = true;
                            SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.START));
                        }

                        SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.PLAY));
                        startProgressTimer();
                    }
                    else {
						stopProgressTimer();
						SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.PAUSE));
					}
					break;
				case ExoPlayer.STATE_ENDED:
					if (!playWhenReady)
						break;

					stopProgressTimer();
					pause();
					seek(0);
					SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.FINISH));
					_hasFinished = true;
					break;
			}
		}

		@Override
		public void onError(Exception e) {
			Log.i("player", "Error: " + media, e);
			SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.ERROR, e.getMessage()));
		}

		@Override
		public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
			SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.RESIZE, width, height, unappliedRotationDegrees, pixelWidthHeightRatio));
		}
	};

	private final PlaybackControlLayer.PlayCallback playListener = new PlaybackControlLayer.PlayCallback() {
		@Override
		public void onPlay() {
			if (player.getPlaybackState() == ExoPlayer.STATE_ENDED)
				seek(0);
		}
	};

	private final PlaybackControlLayer.FullscreenCallback fullscreenListener = new PlaybackControlLayer.FullscreenCallback() {
		@Override
		public void onGoToFullscreen() {
			SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.FULLSCREEN));
		}

		@Override
		public void onReturnFromFullscreen() {
			SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.FULLSCREEN_EXIT));
		}
	};

	private final AdapterView.OnItemClickListener menuItemListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			changeOutput((SambaMedia.Output)parent.getItemAtPosition(position));
		}
	};

	private SambaPlayerController() {
		PluginsManager.getInstance().initialize();
	}

	public void init(FrameLayout container) {
		this.container = container;
	}

	/**	Player API **/

	public void setMedia(SambaMedia media) {
		if (media == null)
			throw new IllegalArgumentException("Media data is null");

		this.media = (SambaMediaConfig)media;
		destroy();
	}

	public SambaMedia getMedia() {
		return media;
	}

	public void play() {
		if (player != null)
			player.play();
		else create();
	}

	public void pause() {
		if (_hasStarted)
			player.pause();
	}

	public void stop() {
		player.stop();
		SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.STOP));
	}

	public void seek(float position) {
		player.seek(Math.round(position * 1000f));
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

	public boolean hasStarted() {
		return _hasStarted;
	}

	public boolean hasFinished() {
		return _hasFinished;
	}

	public void destroy() {
		destroyInternal();
		SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.UNLOAD));
	}

	public View getView() {
		return container;
	}

	public void changeOutput(SambaMedia.Output output) {
		int currentPosition = player.getCurrentPosition();
		for(SambaMedia.Output o : media.outputs) {
			o.current = o.label.equals(output.label);
		}
		media.url = output.url;
		destroyInternal();
		createInternal();
		player.seek(currentPosition);
	}

	/**	End Player API **/

	private void create() {
		create(true);
	}

	private void createInternal() {
		create(false);
	}

	private void create(boolean notify) {
		if (player != null) {
			Log.e("player", "Player already created!");
			return;
		}

        if (media.url == null || media.url.isEmpty()) {
			Log.e("player", "Media data is null!");
	        return;
		}

		Video.VideoType videoType = Video.VideoType.OTHER;

		switch (media.type.toLowerCase()) {
			case "hls":
				videoType = Video.VideoType.HLS;
				break;
			case "dash":
				videoType = Video.VideoType.DASH;
				break;
		}

		// no autoplay if there's ad because ImaWrapper controls the player through events
        player = new SimpleVideoPlayer((Activity)container.getContext(), container,
                new Video(media.url, videoType),
                media.title, media.adUrl == null || media.adUrl.isEmpty());

		player.setSeekbarColor(media.themeColor);

		// Move the content player's surface layer to the background so that the ad player's surface
		// layer can be overlaid on top of it during ad playback.
		player.moveSurfaceToBackground();

		//Live treatment
		if(media.isLive) {
			((Activity) container.getContext()).findViewById(R.id.time_container).setVisibility(View.INVISIBLE);

			player.setControlsVisible(false);
			player.addActionButton(ContextCompat.getDrawable(container.getContext(), R.drawable.ic_live),
					container.getContext().getString(R.string.live), null);
		}

		/**player.addActionButton(ContextCompat.getDrawable(container.getContext(), R.drawable.share),
		        container.getContext().getString(R.string.share_facebook), new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(container.getContext(), "Share Facebook", Toast.LENGTH_SHORT).show();
			}
		});**/

		player.addPlaybackListener(playbackListener);
		player.setPlayCallback(playListener);
		player.setFullscreenCallback(fullscreenListener);

		// TODO: add flag "autoFullscreen"
		// Fullscreen
		orientationEventListener = new OrientationEventListener(container.getContext()) {

			{ enable(); }

			@Override
			public void onOrientationChanged(int orientation) {
				if(player == null)
					return;

				if(orientation <= 15 && orientation >= 0) {
					if(player.isFullscreen()) {
						player.setFullscreen(false);
					}
					SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.PORTRAIT));
				}else if((orientation >= 80 && orientation <= 100 ) || (orientation >= 260 && orientation <= 290)){
					if(!player.isFullscreen()) {
						player.setFullscreen(true);
					}
					SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.LANDSCAPE));
				}
			}
		};

		if (notify) {
			PluginsManager.getInstance().onLoad(this);
			SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.LOAD, this));
		}

		// Output Menu
		if (media.outputs != null && media.outputs.size() > 1) {
			View outputMenu = ((Activity) container.getContext()).getLayoutInflater().inflate(R.layout.output_menu_layout, null);
			OutputAdapter outputAdapter = new OutputAdapter(container.getContext(), media.outputs);
			outputMenuList = (ListView)outputMenu.findViewById(R.id.output_menu_list);
			outputMenuList.setAdapter(outputAdapter);
			outputMenuList.setOnItemClickListener(menuItemListener);
			outputAdapter.notifyDataSetChanged();
			player.setOutputMenu(outputMenu);
		}

		// TODO reunir em um "pos create"?
		//Show controls
		player.show();
	}

	private void destroyInternal() {
		if (player == null)
			return;

		PluginsManager.getInstance().onDestroy();
		stopProgressTimer();
		stop();

		if (outputMenuList != null)
			outputMenuList.setOnItemClickListener(null);

		orientationEventListener.disable();
		player.setPlayCallback(null);
		player.setFullscreenCallback(null);
		player.release();

		outputMenuList = null;
		orientationEventListener = null;
		player = null;
		_hasStarted = false;
		_hasFinished = false;
	}

    private void startProgressTimer() {
		if (progressTimer != null)
			return;

		progressTimer = new Timer();
		progressTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.PROGRESS, getCurrentTime(), getDuration()));
			}
		}, 0, 250);
	}

	private void stopProgressTimer() {
		if (progressTimer == null)
			return;

		progressTimer.cancel();
		progressTimer.purge();
		progressTimer = null;
	}

	/*private void applyAttributes(TypedArray attrs) {
		media.url = attrs.getString(R.styleable.SambaPlayerView_url);
		media.title = attrs.getString(R.styleable.SambaPlayerView_title);
		attrs.recycle();
	}*/
}
