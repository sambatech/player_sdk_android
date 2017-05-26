package com.sambatech.player;

import android.app.Activity;
import android.app.MediaRouteButton;
import android.content.Context;
import android.content.res.TypedArray;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.exoplayer.BehindLiveWindowException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.mediaframework.exoplayerextensions.ExoplayerWrapper;
import com.google.android.libraries.mediaframework.exoplayerextensions.UnsupportedDrmException;
import com.google.android.libraries.mediaframework.exoplayerextensions.Video;
import com.google.android.libraries.mediaframework.layeredvideo.PlaybackControlLayer;
import com.google.android.libraries.mediaframework.layeredvideo.SimpleVideoPlayer;
import com.sambatech.player.adapter.CaptionsAdapter;
import com.sambatech.player.adapter.OutputAdapter;
import com.sambatech.player.cast.CastDRM;
import com.sambatech.player.cast.CastObject;
import com.sambatech.player.cast.CastOptionsProvider;
import com.sambatech.player.cast.CastQuery;
import com.sambatech.player.cast.SambaCast;
import com.sambatech.player.event.SambaCastListener;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaConfig;
import com.sambatech.player.model.SambaPlayerError;
import com.sambatech.player.plugins.Captions;
import com.sambatech.player.plugins.PluginManager;
import com.sambatech.player.utils.Helpers;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents the player, responsible for managing media playback.
 *
 * @author Leandro Zanol - 7/12/15
 */
public class SambaPlayer extends FrameLayout {

	private final ExoplayerWrapper.PlaybackListener playbackListener =
			new ExoplayerWrapper.PlaybackListener() {

		@Override
		public void onStateChanged(boolean playWhenReady, int playbackState) {
			Log.i("SambaPlayer", "state: " + playWhenReady + " " + playbackState + "; playing: " + isPlaying());

			switch (playbackState) {
				case ExoPlayer.STATE_READY:
					if (playWhenReady) {
                        if (!_hasStarted) {
                            _hasStarted = true;
	                        _currentRetryIndex = 0;

	                        initOutputMenu();
	                        destroyError();
                            SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.START));

	                        //Show controls
							player.show();

	                        if (!media.isLive && _initialTime > 0) {
		                        seek(_initialTime);
		                        _initialTime = 0;
	                        }

	                        if (_initialOutput != -1) {
		                        switchOutput(_initialOutput);
		                        _initialOutput = -1;
	                        }

	                        if(_initialFullscreen != null) {
								player.setFullscreen(_initialFullscreen);
								_initialFullscreen = null;
							}
                        }

                        dispatchPlay();
                    }
                    else dispatchPause();

					player.hideLoading();
					break;
				case ExoPlayer.STATE_ENDED:
					if (!playWhenReady)
						break;

					stopProgressTimer();
					pause();
					seek(0);
					SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.FINISH));
					_hasFinished = true;
					player.hideLoading();
					break;
				case ExoPlayer.STATE_BUFFERING:
					player.showLoading();
					break;
			}
		}

		@Override
		public void onError(final Exception e) {
			Log.d("SambaPlayer", "Error: " + media, e);

			String msg = "Você está offline! Verifique sua conexão.";
			SambaPlayerError.Severity severity = SambaPlayerError.Severity.recoverable;
			final boolean isBehindLiveWindowException = e.getCause() instanceof BehindLiveWindowException;

			if (_initialTime == 0f)
				_initialTime = getCurrentTime();

			if (isBehindLiveWindowException)
				_initialOutput = player.getTrackCount(ExoplayerWrapper.TYPE_VIDEO) - 1;

			_initialFullscreen = player.isFullscreen();

			destroyInternal();

			// unauthorized DRM content
			if (e.getCause() instanceof UnsupportedDrmException) {
				msg = String.format("Você não tem permissão para %s", media.isAudioOnly ? "ouvir este áudio." : "assistir este vídeo.");
				severity = SambaPlayerError.Severity.critical;
			}
			// possible network or streaming instability (misalignment, holes, etc.), try to recover
			else if (isBehindLiveWindowException) {
				msg = "Instabilidade na rede ou no envio de dados.";
				severity = SambaPlayerError.Severity.minor;
				create(false);
			}
			// URL not found
			else if (Helpers.isNetworkAvailable(getContext())) {
				msg = "Conectando...";
				severity = SambaPlayerError.Severity.info;

				try {
					final HttpURLConnection con = (HttpURLConnection) new URL(String.format("%s://www.google.com",
                            media.request.protocol)).openConnection();

					con.setConnectTimeout(1000);
                    con.setReadTimeout(1000);

					Helpers.requestUrl(con, new Helpers.RequestCallback() {
						@Override
						public void onSuccess(String response) {
							// check whether it can fallback (changes error criticity) or fail otherwise
                            ((Activity) getContext()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (_currentBackupIndex < media.backupUrls.length) {
                                        media.url = media.backupUrls[_currentBackupIndex++];

                                        create(false);
                                        dispatchError(SambaPlayerError.unknown.setValues(SambaPlayerError.unknown.getCode(),
		                                        "Conectando...", SambaPlayerError.Severity.info, e));
	                                    return;
                                    }

                                    dispatchError(SambaPlayerError.unknown.setValues(SambaPlayerError.unknown.getCode(),
		                                    "Ocorreu um erro! Por favor, tente mais tarde...",
	                                        SambaPlayerError.Severity.critical, e));
                                }
                            });
						}

						@Override
						public void onError(Exception e, String response) {
							dispatchError(SambaPlayerError.unknown.setValues(SambaPlayerError.unknown.getCode(),
									"Você está offline! Verifique sua conexão.",
									SambaPlayerError.Severity.recoverable, e));
						}
					});
				}
				catch (IOException e1) {
                    msg = "Ocorreu um erro! Por favor, tente novamente.";
		            severity = SambaPlayerError.Severity.recoverable;
				}
			}
			// no network connection
			else if (_currentRetryIndex++ < media.retriesTotal) {
				final AtomicInteger secs = new AtomicInteger(8);
				final Timer timer = new Timer();

				timer.scheduleAtFixedRate(new TimerTask() {
					@Override
					public void run() {
						((Activity) getContext()).runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (secs.get() == 0) {
									timer.cancel();
									timer.purge();
									create(false);
								}

								dispatchError(SambaPlayerError.unknown.setValues(SambaPlayerError.unknown.getCode(),
										secs.get() > 0 ? String.format("Reconectando em %ss", secs) : "Conectando...",
										SambaPlayerError.Severity.info, e, R.drawable.ic_nosignal_disable));

								secs.decrementAndGet();
							}
						});
					}
				}, 0, 1000);
				return;
			}

			dispatchError(SambaPlayerError.unknown.setValues(SambaPlayerError.unknown.getCode(),
					msg, severity, e));
		}

		@Override
		public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
			//Log.i("SambaPlayer", unappliedRotationDegrees+" "+width + " " + height);
			SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.RESIZE, width, height,
					unappliedRotationDegrees, pixelWidthHeightRatio));
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
			_wasAutoFS = false;
			SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.FULLSCREEN_EXIT));
		}
	};

	private final Runnable progressDispatcher = new Runnable() {
		@Override
		public void run() {
			if (player == null) return;

			SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.PROGRESS, getCurrentTime(), getDuration()));
		}
	};

	private final SambaCastListener castListener = new SambaCastListener() {

		RemoteMediaClient castPlayer;
		
		private int lastPosition = 0;

		private final PlaybackControlLayer.InterceptableListener interceptableListener = new PlaybackControlLayer.InterceptableListener() {
			@Override
			public boolean onPlay() {
				dispatchPlay();
				sambaCast.playCast();
				return false;
			}

			@Override
			public boolean onPause() {
				dispatchPause();
				sambaCast.pauseCast();
				return false;
			}

			@Override
			public boolean onSeek(int position) {
				sambaCast.seekTo(position);
				return false;
			}

			@Override
			public int getCurrentTime() {
				return castPlayer != null ? (int)castPlayer.getApproximateStreamPosition() : 0;
			}

			@Override
			public int getDuration() {
				return castPlayer != null ? (int)castPlayer.getStreamDuration() : 0;
			}
		};

		@Override
		public void onConnected(final CastSession castSession) {

			stopProgressTimer();
			pause();

			final RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
			if (remoteMediaClient == null) return;

			// enabling hook for API and user actions
			player.setInterceptableListener(interceptableListener);
			player.setAutoHide(false);
			player.setControlsVisible(false, "outputMenu", "captionMenu");

			// converting SambaMedia to MediaInfo
			MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
			movieMetadata.putString(MediaMetadata.KEY_TITLE, media.title);
			movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, media.title);

			CastQuery qs = new CastQuery(true, CastOptionsProvider.environment.toString(),
					CastOptionsProvider.appId, (int)getCurrentTime(), getCaption());

			CastObject castObject = new CastObject(media.title, media.id,
					(int) getDuration(),media.themeColorHex,
					media.projectHash, qs, "", CastOptionsProvider.playerUrl);

			if (media.drmRequest != null)
				castObject.setDrm(new CastDRM(media.drmRequest.getLicenseParam("SessionId"),
						media.drmRequest.getLicenseParam("Ticket")));

			MediaInfo mediaInfo = new MediaInfo.Builder(castObject.toString())
					.setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
					.setContentType("video/mp4")
					.setMetadata(movieMetadata)
					.build();

			remoteMediaClient.load(mediaInfo, false, 0).setResultCallback(new ResultCallbacks<RemoteMediaClient.MediaChannelResult>() {
				@Override
				public void onSuccess(@NonNull RemoteMediaClient.MediaChannelResult mediaChannelResult) {
					Log.d("load", mediaChannelResult.getStatus().toString());
				}

				@Override
				public void onFailure(@NonNull Status status) {
					Log.d("load", status.toString());
				}
			});

			sambaCast.registerDeviceForProgress(true);

			lastPosition = 0;

			Cast.MessageReceivedCallback messageReceived = new Cast.MessageReceivedCallback() {
				@Override
				public void onMessageReceived(CastDevice castDevice, String namespace, String message)  {
					Log.i("Message Received", castDevice.toString() + namespace + message);

					try {
						JSONObject jsonObject = new JSONObject(message);

						if (jsonObject.has("progress") && jsonObject.has("duration")) {
							float progress = jsonObject.getInt("progress");
							float duration = jsonObject.getInt("duration");

							lastPosition = (int)progress;

							if (player != null)
								player.setCurrentTime(progress, duration);
						}
						else if (jsonObject.has("type")) {
							jsonObject = new JSONObject(message);
							String type = jsonObject.getString("type");

							if (type.equalsIgnoreCase("finish"))
								sambaCast.stopCasting();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			};

			try {
				castSession.setMessageReceivedCallbacks(CastOptionsProvider.CUSTOM_NAMESPACE,messageReceived);
			} catch (IOException e) {
				e.printStackTrace();
			}

			this.castPlayer = remoteMediaClient;
			player.updatePlayPauseButton(true);
		}

		@Override
		public void onDisconnected() {
			player.setControlsVisible(true,
					outputMenu != null ? "outputMenu" : null,
					captionMenu != null ? "captionMenu" : null);
			player.setAutoHide(true);
			player.seek(lastPosition*1000);
			lastPosition = 0;
			// disabling hook for API and user actions
			player.setInterceptableListener(null);
			play();
			startProgressTimer();
		}
	};

	private SimpleVideoPlayer player;
	private View errorScreen;
	private @NonNull SambaMediaConfig media = new SambaMediaConfig();
	private Timer progressTimer;
	private boolean _hasStarted;
	private boolean _hasFinished;
	private OrientationEventListener orientationEventListener;
	private View outputMenu;
	private View captionMenu;
	private SambaCast sambaCast;
	private boolean _autoFsMode;
	private boolean _enableControls;
	private boolean _wasAutoFS;
	private boolean _disabled;
	private int _currentBackupIndex;
	private int _currentRetryIndex;
	private float _initialTime = 0f;
	private int _initialOutput = -1;
	private Boolean _initialFullscreen = null;
	//private boolean wasPlaying;

	public SambaPlayer(Context context, AttributeSet attrs) {
		super(context, attrs);
		applyAttributes(getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SambaPlayer, 0, 0));
	}

	/**
	 * Defines/overwrites current media.
	 * @param media The media to be played.
	 */
	public void setMedia(@NonNull SambaMedia media) {
		SambaMediaConfig m = new SambaMediaConfig(media);

		this.media = m;
		_initialTime = m.initialTime;

		if (m.blockIfRooted && Helpers.isDeviceRooted()) {
			_disabled = true;
			dispatchError(SambaPlayerError.rootedDevice);
			return;
		}

		destroy();

		// TODO: create thumbnail or create audio player

		PluginManager.getInstance().onLoad(this);
	}

	/**
	 * Retrieves the current media in use.
	 *
	 * Always returns a non null media data after LOAD event has been dispatched,
	 * but before its dispatch null checks must be made.
	 *
	 * @return Media data
	 */
	public SambaMedia getMedia() {
		return media;
	}

	/**
	 * Resumes media playback.
	 */
	public void play() {
		// in case of forbidden rooted device
		if (_disabled) return;

		// defer play if plugins not loaded yet
		if (!PluginManager.getInstance().isLoaded()) {
			PluginManager.getInstance().setPendingPlay(true);
			return;
		}

		// create internal player if it doesn't exist
		if (player == null) {
			create();
			return;
		}

		player.play();
	}

	/**
	 * Pauses media playback.
	 */
	public void pause() {
		if (player == null || !_hasStarted) return;

		player.pause();
	}

	/**
	 * Stops media playback returning the video to it's beginning.
	 */
	public void stop() {
		if (player == null) return;

		player.stop();
		SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.STOP));
	}

	/**
	 * Moves the media to a specific position.
	 * @param position New position of the media in seconds
	 */
	public void seek(float position) {
		if (player == null) return;

		player.seek(Math.round(position * 1000f));
	}

	/**
	 * Enables or disables controls.
	 * @param flag true to show or hide the controls
	 */
	public void setEnableControls(boolean flag) {
		if(media.isAudioOnly) return;

		if(player != null) {
			if(flag)
				player.enableControls();
			else
				player.disableControls();
		}
		else _enableControls = flag;
	}

	/**
	 * Sets fullscreen mode on and off.
	 * @param flag true to enter in the fullscreen mode on and false to exit
	 */
	public void setFullscreen(boolean flag) {
		if (player == null) return;

		player.setFullscreen(flag);
	}

	/**
	 * Indicates the fullscreen mode on or off.
	 * @return boolean Whether fullscreen mode is on of off.
	 */
	public boolean isFullscreen() {
		return player != null && player.isFullscreen();
	}

	/**
	 * Shows player controls.
	 */
	public void show() {
		if (player == null) return;

		player.show();
	}

	/**
	 * Hides player controls.
	 */
	public void hide() {
		if (player == null) return;

		player.hide();
	}

	/**
	 * Sets whether the player should go automatically on fullscreen or not.
	 * @param flag true to enable auto fullscreen mode and false to disable it
	 */
	public void setAutoFullscreenMode(boolean flag) {
		_autoFsMode = flag;
	}

	/**
	 * Gets the current time on the video.
	 * @return Float Current time
	 */
	public float getCurrentTime() {
		return player != null ? player.getCurrentPosition()/1000f : 0f;
	}

	/**
	 * Gets the total duration of the video.
	 * @return Float total duration
	 */
	public float getDuration() {
		return player != null ? player.getDuration()/1000f : media.duration;
	}

	/**
	 * Indicates whether media is being reproduced.
	 * @return The state of media playback
	 */
	public boolean isPlaying() {
		return player != null && player.shouldBePlaying();
	}

	/**
	 * Indicates whether media has already started playing.
	 * @return True if media has started playing
	 */
	public boolean hasStarted() {
		return _hasStarted;
	}

	/**
	 * Indicates whether media has finished at least once.
	 * Does not imply playing it thoroughly without seeking.
	 * @return True once media hits the end
	 */
	public boolean hasFinished() {
		return _hasFinished;
	}

	/**
	 * Changes the current output.
	 * @param output SambaMedia.Output indicating the new output
	 */
	public void changeOutput(@NonNull SambaMedia.Output output) {
		if (output.url == null || output.url.isEmpty()) {
			//dispatchError(SambaPlayerError.emptyUrl);
			Log.e("SambaPlayer", "URL not found for output \"" + output.label + "\".");
			return;
		}

		final int currentPosition = player.getCurrentPosition();
		//final boolean wasPlaying = isPlaying();

		for (SambaMedia.Output o : media.outputs)
			o.isDefault = o.label.equals(output.label);

		media.url = output.url;

		destroyInternal();
		create(false);
		player.seek(currentPosition);
	}

	/**
	 * Changes the current output.
	 * Must be called after START event has been dispatched.
	 * @param index The index in the outputs array.
	 */

	public void switchOutput(int index) {
		if (player == null || outputMenu == null)
			return;

		outputMenu.setTag(index);
		player.setSelectedTrack(index);
	}

	/**
	 * Retrieves the selected output index.
	 * @return The selected output index
	 */
	public int getCurrentOutputIndex() {
		return outputMenu != null && outputMenu.getTag() != null ?
				(int)outputMenu.getTag() : media.defaultOutputIndex;
	}

	/**
	 * Changes the current caption.
	 * @param index The index in the captions array
	 */
	public void changeCaption(int index) {
		Captions plugin = (Captions) PluginManager.getInstance().getPlugin(Captions.class);

		if (plugin == null) return;

		plugin.changeCaption(index);
	}

	public String getCaption() {
		if (captionMenu == null) return null;

		CaptionsAdapter adapter = (CaptionsAdapter) ((ListView) captionMenu.findViewById(R.id.menu_list)).getAdapter();
		SambaMedia.Caption caption = (SambaMedia.Caption) adapter.getItem(adapter.currentIndex);

		return String.format("[%s,ffcc00,42]", caption.language);
	}

	/**
	 * If set, Chromecast support will be enabled inside player view.
	 * @param sambaCast The SambaCast instance
	 */
	public void setSambaCast(@NonNull SambaCast sambaCast) {
		this.sambaCast = sambaCast;
		setupCast();
	}

	/**
	 * Destroys the player and it's events.
	 */
	public void destroy() {
		destroy(null);
	}

	/**
	 * Destroys the player and it's events and shows an error screen.
	 * @param error Error type to show
	 */
	public void destroy(SambaPlayerError error) {
		PluginManager.getInstance().onDestroy();
		destroyInternal();
		SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.UNLOAD));

		if (error != null)
			showError(error);
		else destroyError();
	}

	/* End Player API */

	private void applyAttributes(TypedArray attrs) {
		try {
			setAutoFullscreenMode(attrs.getBoolean(R.styleable.SambaPlayer_autoFullscreenMode, false));
			setEnableControls(attrs.getBoolean(R.styleable.SambaPlayer_enableControls, true));
		}
		finally {
			attrs.recycle();
		}
	}

	private void create() {
		create(true, true);
	}

	private void create(boolean notify) {
		create(notify, true);
	}

	private void create(boolean notify, boolean isAutoPlay) {
		if (player != null) {
			Log.i("SambaPlayer", "Player already created!");
			return;
		}

		if (media.url == null || media.url.isEmpty()) {
			dispatchError(SambaPlayerError.emptyUrl);
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

		// no autoplay if there's ad because ImaWrapper takes control of the player
		player = new SimpleVideoPlayer((Activity)getContext(), this,
				new Video(media.url, videoType, media.drmRequest), media.title,
				!notify && isAutoPlay || isAutoPlay && (sambaCast == null || !sambaCast.isCasting())
                        && (media.adUrl == null || media.adUrl.isEmpty()),
				media.isAudioOnly);

		player.setThemeColor(media.themeColor);

		// Move the content player's surface layer to the background so that the ad player's surface
		// layer can be overlaid on top of it during ad playback.
		player.moveSurfaceToBackground();

		//Live treatment
		if (media.isLive) {
			((Activity)getContext()).findViewById(R.id.time_container).setVisibility(INVISIBLE);

			player.setControlsVisible(false, "seekbar");
			player.addActionButton(ContextCompat.getDrawable(getContext(), R.drawable.ic_live),
					getContext().getString(R.string.live), null);
		}

		player.addPlaybackListener(playbackListener);
		player.setPlayCallback(playListener);

		if (media.isAudioOnly) {
			player.setControlsVisible(true, "play");
			player.setControlsVisible(false, "fullscreen", "playLarge", "topChrome");
			//playbackControlLayer.swapControls("time", "seekbar");
			player.setBackgroundColor(0xFF434343);
			player.setChromeColor(0x00000000);
		}
		else player.setFullscreenCallback(fullscreenListener);

		// Fullscreen
		orientationEventListener = new OrientationEventListener(getContext()) {

			{ enable(); }

			@Override
			public void onOrientationChanged(int orientation) {
				if (Settings.System.getInt(getContext().getContentResolver(),
						Settings.System.ACCELEROMETER_ROTATION, 0) == 0 || !_autoFsMode || player == null)
					return;

				if (orientation <= 15 && orientation >= 0) {
					if(_wasAutoFS && player.isFullscreen()) {
						player.setFullscreen(false);
					}

					SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.PORTRAIT));
				}
				else {
					final boolean isReverseLandscape = orientation >= 80 && orientation <= 100;

					if (orientation >= 260 && orientation <= 290 || isReverseLandscape) {
						if(!player.isFullscreen()) {
							_wasAutoFS = true;
							player.setFullscreen(true, isReverseLandscape);
						}

						SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.LANDSCAPE));
					}
				}
			}
		};

		// video-only
		if (!media.isAudioOnly) {
			initCaptionMenu();

			if (!_enableControls)
				player.disableControls();

			PluginManager.getInstance().onInternalPlayerCreated(player);

			if (notify)
				SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.LOAD, this));
		}

		/*player.addActionButton(ContextCompat.getDrawableRes(getContext(), R.drawable.share),
		        getContext().getString(R.string.share_facebook), new OnClickListener() {
			@Override
			public void onClick(View v) {}
		});*/

		setupCast();

		if (sambaCast != null && sambaCast.isCasting())
			castListener.onConnected(sambaCast.getCastSession());
	}

	private View initDialog(@StringRes int titleRes, ListAdapter adapter,
	                        AdapterView.OnItemClickListener itemListener,
	                        OnClickListener cancelListener) {
		View dialog = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.menu_layout, null);

		TextView cancelButton = (TextView) dialog.findViewById(R.id.menu_cancel_button);
		TextView title = (TextView) dialog.findViewById(R.id.menu_label);
		title.setText(getContext().getString(titleRes));

		cancelButton.setOnClickListener(cancelListener);

		ListView menuList = (ListView) dialog.findViewById(R.id.menu_list);

		menuList.setAdapter(adapter);
		menuList.setOnItemClickListener(itemListener);
		menuList.deferNotifyDataSetChanged();

		return dialog;
	}

	private void initOutputMenu() {
		if (player == null)
			return;

		final MediaFormat[] tracks = player.getTrackFormats(ExoplayerWrapper.TYPE_VIDEO);

		if (media.isAudioOnly || tracks.length <= 1)
			return;

		outputMenu = initDialog(R.string.output, new OutputAdapter(getContext(),
						tracks, this),
				new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						player.closeOutputMenu();
						switchOutput(position);
					}
				}, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						player.closeOutputMenu();
					}
				});

		player.setOutputMenu(outputMenu);
	}

	private void initCaptionMenu() {
		if (media.isAudioOnly || media.captions == null ||
				media.captions.size() == 0 || player == null)
			return;

		captionMenu = initDialog(R.string.captions, new CaptionsAdapter(getContext(), media.captions),
				new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						player.closeCaptionMenu();
						changeCaption(position);
					}
				}, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						player.closeCaptionMenu();
					}
				});

		player.setCaptionMenu(captionMenu);
	}

	private void destroyInternal() {
		if (player == null)
			return;

		stopProgressTimer();
		stop();
		player.setFullscreen(false);

		if (outputMenu != null) {
			((ListView)outputMenu.findViewById(R.id.menu_list)).setOnItemClickListener(null);
			outputMenu.findViewById(R.id.menu_cancel_button).setOnClickListener(null);
		}

        if (captionMenu != null) {
            ((ListView) captionMenu.findViewById(R.id.menu_list)).setOnItemClickListener(null);
            captionMenu.findViewById(R.id.menu_cancel_button).setOnClickListener(null);
        }

		orientationEventListener.disable();
		player.removePlaybackListener(playbackListener);
		player.setPlayCallback(null);
		player.setFullscreenCallback(null);

		if (sambaCast != null) {
			player.setInterceptableListener(null);
			sambaCast.setEventListener(null);
		}

        player.setControlsVisible(false);
		player.release();

		outputMenu = null;
		captionMenu = null;
		orientationEventListener = null;
		player = null;
		_hasStarted = false;
		_hasFinished = false;
		_disabled = false;
	}

	private void showError(@NonNull SambaPlayerError error) {
		if (errorScreen == null)
			errorScreen = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.error_screen, this, false);

		TextView textView = (TextView) errorScreen.findViewById(R.id.error_message);
		textView.setText(error.toString());

		ImageButton retryButton = (ImageButton) errorScreen.findViewById(R.id.retry_button);
		retryButton.setVisibility(error.getSeverity() == SambaPlayerError.Severity.recoverable ? VISIBLE : GONE);
		retryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				destroyInternal();
				create(false);
			}
		});

		// removes images if audio player
		if (media.isAudioOnly)
			// shows retry button when recoverable error
			if (error.getSeverity() == SambaPlayerError.Severity.recoverable)
				textView.setVisibility(GONE);
			else textView.setCompoundDrawables(null, null, null, null);
		// set custom image
		else if (error.getDrawableRes() > 0)
			textView.setCompoundDrawablesWithIntrinsicBounds(0, error.getDrawableRes(), 0, 0);
		// default error image
		else textView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.error_icon, 0, 0);

		if (errorScreen.getParent() == null)
			addView(errorScreen);
	}

	private void destroyError() {
		if (errorScreen == null) return;
		errorScreen.findViewById(R.id.retry_button).setOnClickListener(null);
		removeView(errorScreen);
		errorScreen = null;
	}

    private void startProgressTimer() {
		if (progressTimer != null)
			return;

		progressTimer = new Timer();
		progressTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				((Activity)getContext()).runOnUiThread(progressDispatcher);
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

	private void dispatchPlay() {
		SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.PLAY));
		startProgressTimer();
	}

	private void dispatchPause() {
		stopProgressTimer();
		SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.PAUSE));
	}

	private void dispatchError(@NonNull SambaPlayerError error) {
		// give user the chance to customize error message before showing it (in case of critical)
		SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.ERROR, error));

		switch (error.getSeverity()) {
			case critical:
				destroy(error);
				break;

			case info:
			case recoverable:
				showError(error);
				break;
		}
	}

	private void setupCast() {
		// if Chromecast support is enabled
		if (sambaCast == null || media.isLive || media.isAudioOnly) return;

		sambaCast.setEventListener(castListener);

		if (player != null) {
			MediaRouteButton button = sambaCast.getButton();
			ViewGroup parent = (ViewGroup)button.getParent();

			if (parent != null)
				parent.removeView(button);

			player.addActionButton(button);
		}
	}
}
