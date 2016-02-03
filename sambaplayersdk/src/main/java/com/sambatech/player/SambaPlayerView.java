package com.sambatech.player;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Controller for SambaPlayer view.
 *
 * @author Leandro Zanol - 7/12/15
 */
public class SambaPlayerView extends FrameLayout implements SambaPlayer {

	private SimpleVideoPlayer player;
	private SambaMediaConfig media = new SambaMediaConfig();
	private Timer progressTimer;
	private boolean _hasStarted;
	private boolean _hasFinished;
	private OutputAdapter oAdapter;
	private ListView oList;
	private FrameLayout outputContainer;

	private ExoplayerWrapper.PlaybackListener playbackListener = new ExoplayerWrapper.PlaybackListener() {
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
			destroy();
			SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.ERROR, e.getMessage()));
		}

		@Override
		public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
			SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.RESIZE, width, height, unappliedRotationDegrees, pixelWidthHeightRatio));
		}
	};

	private PlaybackControlLayer.PlayCallback playListener = new PlaybackControlLayer.PlayCallback() {
		@Override
		public void onPlay() {
			if (player.getPlaybackState() == ExoPlayer.STATE_ENDED)
				seek(0);
		}
	};

	private PlaybackControlLayer.FullscreenCallback fullscreenListener = new PlaybackControlLayer.FullscreenCallback() {
		@Override
		public void onGoToFullscreen() {
			SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.FULLSCREEN));
		}

		@Override
		public void onReturnFromFullscreen() {
			SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.FULLSCREEN_EXIT));
		}
	};

	public SambaPlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		/*applyAttributes(getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SambaPlayerView, 0, 0));

        if (!isInEditMode() && media.url != null)
            createPlayer();*/

		PluginsManager.getInstance().initialize();
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
			doAction("play", null, null);
		else {
			createPlayer();


			//TODO reunir em um "pos create"?
			//Show controls
			player.show();
		};
	}

	public void pause() {
		if (_hasStarted)
			doAction("pause", null, null);
	}

	public void stop() {
		doAction("stop", null, null);
		SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.STOP));
	}

	public void seek(float position) {
		doAction("seek", null, position * 1000f);
	}

	public void setFullscreen(boolean flag) {
		doAction("fullscreen", flag, null);
	}

	public boolean isFullscreen() {
		return (getAction("isFullscreen") != 0.0f);
	}

	public void show() {

		doAction("visibility", true, null);
	}

	public void hide() {
		doAction("visibility", false, null);
	}

	public float getCurrentTime() {
		return getAction("getCurrentTime");
	}

	public float getDuration() {
		return getAction("getDuration");
	}

	public boolean hasStarted() {
		return _hasStarted;
	}

	public boolean hasFinished() {
		return _hasFinished;
	}

	public void destroy() {
		doAction("destroy", null, null);
	}

	public View getView() {
		return this;
	}

	private void createPlayer() {
		if (player != null) {
			Log.e("player", "Player already created!");
			return;
		}

        if (media.url == null || media.url.isEmpty()) {
			//throw new InvalidParameterException("Media data is null");
			Log.e("player", "Media data is null!");
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

		player.addPlaybackListener(playbackListener);
		player.setPlayCallback(playListener);
		player.setFullscreenCallback(fullscreenListener);

        PluginsManager.getInstance().onLoad(this);
		SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.LOAD, this));

		//Fullscreen
		OrientationEventListener orientationEventListener = new OrientationEventListener(this.getContext()) {
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
		orientationEventListener.enable();

		//OutputList
		oList = (ListView) this.findViewById(R.id.output_menu_list);
		showOutputList(media.outputs);
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

	private void doAction(String action, Boolean enable, Float time) {
		Log.e("player:", action);
		try{throw new InvalidParameterException("Media data is null");}catch(Exception e){Log.i("asdf", "blah!", e);}
		if(player == null) return;

		switch (action) {
			case "play":
				player.play();
				break;
			case "pause":
				player.pause();
				break;
			case "fullscreen":
				player.setFullscreen(enable);
				break;

			case "stop":
				player.stop();
				break;

			case "seek":
				player.seek(Math.round(time));
				break;

			case "destroy":
				PluginsManager.getInstance().onDestroy();
				stopProgressTimer();
				stop();
				player.setPlayCallback(null);
				player.setFullscreenCallback(null);
				player.release();

				player = null;
				_hasStarted = false;
				_hasFinished = false;

				SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.UNLOAD));
				break;

			case "visibility":
				Log.e("player:", String.valueOf(enable));

				if(enable) {
					player.show();
				}else {
					player.hide();
				}
				break;
			default:
				break;
		}

	}

	private Float getAction(String action) {
		if(player == null) return 0.0f;

		Float f = 0f;
		switch (action) {
			case "getCurrentTime":
				f = player.getCurrentPosition()/1000f;
				break;
			case "getDuration":
				f = player.getDuration()/1000f;
				break;
			case "isFullscreen":
				Boolean fs = player.isFullscreen();
				f = fs?1.0f:0.0f;
				break;
		}
		return f;
	}

	private void showOutputList(ArrayList<SambaMedia.Outputs> outputs) {
		oAdapter = new OutputAdapter(this.getContext(), outputs, this);
		oList.setAdapter(oAdapter);

		oAdapter.notifyDataSetChanged();
	}

	public void changeOutput(String url) {
		int currentPosition = player.getCurrentPosition();
		media.url = url;
		destroy();
		createPlayer();
		player.seek(currentPosition);
	}

	/*private void applyAttributes(TypedArray attrs) {
		media.url = attrs.getString(R.styleable.SambaPlayerView_url);
		media.title = attrs.getString(R.styleable.SambaPlayerView_title);
		attrs.recycle();
	}*/
}
