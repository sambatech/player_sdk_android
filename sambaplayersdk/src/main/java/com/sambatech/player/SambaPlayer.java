package com.sambatech.player;

import android.app.Activity;
import android.app.MediaRouteButton;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
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

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.ext.ima.ImaAdsMediaSource;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ClippingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.text.CaptionStyleCompat;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectorResult;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;


import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.sambatech.player.adapter.CaptionsSheetAdapter;
import com.sambatech.player.adapter.OutputSheetAdapter;
import com.sambatech.player.cast.CastDRM;
import com.sambatech.player.cast.CastObject;
import com.sambatech.player.cast.CastOptionsProvider;
import com.sambatech.player.cast.CastQuery;
import com.sambatech.player.cast.SambaCast;
import com.sambatech.player.event.SambaCastListener;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.mediasource.PlayerInstanceDefault;
import com.sambatech.player.mediasource.PlayerMediaSourceDash;
import com.sambatech.player.mediasource.PlayerMediaSourceExtractor;
import com.sambatech.player.mediasource.PlayerMediaSourceHLS;
import com.sambatech.player.mediasource.PlayerMediaSourceInterface;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaConfig;
import com.sambatech.player.model.SambaPlayerError;
import com.sambatech.player.plugins.Captions;
import com.sambatech.player.plugins.PluginManager;
import com.sambatech.player.utils.Helpers;
import com.sambatech.player.utils.Orientation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import static android.graphics.Typeface.NORMAL;
import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static android.view.accessibility.CaptioningManager.CaptionStyle.EDGE_TYPE_NONE;
import static android.view.animation.Animation.ABSOLUTE;

/**
 * Represents the player, responsible for managing media playback.
 *
 * @author Leandro Zanol - 7/12/15
 */
public class SambaPlayer extends FrameLayout {

    private final Player.EventListener eventListener = new Player.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.i("SambaPlayer", "state: " + playWhenReady + " " + playbackState + "; playing: " + isPlaying());

            switch (playbackState) {
                case ExoPlayer.STATE_READY:
                    if (playWhenReady) {
                        if (!_hasStarted) {
                            _hasStarted = true;
                            _currentRetryIndex = 0;

                            //initOutputMenu();
                            destroyError();
                            SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.START));

                            // show controls
                            //player.show();

                            // initial position
                            if (!media.isLive && _initialTime > 0) {
                                seek(_initialTime);
                                _initialTime = 0;
                            }

                            // initial output
                            if (_initialOutput != -1) {
                                switchOutput(_initialOutput);
                                _initialOutput = -1;
                            }

                            // start in fullscreen
                            if (_initialFullscreen != null) {
                                //player.setFullscreen(_initialFullscreen);
                                _initialFullscreen = null;
                            }
                        }

                        dispatchPlay();
                    } else dispatchPause();

                    //player.hideLoading();
                    break;
                case ExoPlayer.STATE_ENDED:
                    if (!playWhenReady)
                        break;

                    stopProgressTimer();
                    pause();
                    seek(0);
                    SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.FINISH));
                    _hasFinished = true;
                    //player.hideLoading();
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    //player.showLoading();
                    stopErrorTimer();

                    // buffering timeout
                    final AtomicInteger secs = new AtomicInteger(20);

                    errorTimer = new Timer();
                    errorTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            ((Activity) getContext()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // on buffer timeout disable ABR (sets to lower)
                                    if (secs.get() == 0) {
                                        stopErrorTimer();
                                        _initialOutput = 0;
                                    }

                                    secs.decrementAndGet();
                                }
                            });
                        }
                    }, 0, 1000);
                    break;
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {

        }

        @Override
        public void onPositionDiscontinuity() {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }
    };

    private final SambaSimplePlayerView.FullscreenCallback fullscreenListener = new SambaSimplePlayerView.FullscreenCallback() {
        @Override
        public void onGoToFullscreen() {
            SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.FULLSCREEN));
        }

        @Override
        public void onReturnFromFullscreen() {
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

/*	private final SambaCastListener castListener = new SambaCastListener() {

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
			player.setControlsVisible(false, Controls.MENU);

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
			player.setAutoHide(true);
			player.seek(lastPosition*1000);
			lastPosition = 0;
			// disabling hook for API and user actions
			player.setInterceptableListener(null);
			play();
			startProgressTimer();
		}
	};*/

    //private SimpleExoPlayer player;
    private View errorScreen;
    private @NonNull
    SambaMediaConfig media = new SambaMediaConfig();
    private Timer progressTimer;
    private boolean _hasStarted;
    private boolean _hasFinished;
    private OrientationEventListener orientationEventListener;
    private View outputSheetView;
    private View captionSheetView;
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
    private Timer errorTimer;
    private int _outputOffset;
    private List<String> controlsHidden = new ArrayList<>();


    private SambaSimplePlayerView simplePlayerView;
    private SimpleExoPlayer player;
    private PlayerInstanceDefault playerInstanceDefault;
    private ImaAdsLoader imaAdsLoader;

    private PlayerMediaSourceInterface playerMediaSourceInterface;
    //private boolean wasPlaying;

    public SambaPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyAttributes(getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SambaPlayer, 0, 0));
    }

    /**
     * Defines/overwrites current media.
     *
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
     * <p>
     * Always returns a non null media data after LOAD event has been dispatched,
     * but before its dispatch null checks must be made.
     *
     * @return Media data
     */
    public @NonNull
    SambaMedia getMedia() {
        return media;
    }

    /**
     * Resumes media playback
     *
     * @param outputIndex start index
     */
    public void play(boolean abrEnabled, int outputIndex) {
        _outputOffset = abrEnabled ? 0 : 1;
        _initialOutput = outputIndex;

        // in case of forbidden rooted device or error state
        if (_disabled || errorScreen != null) return;

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

        player.setPlayWhenReady(true);
    }

    /**
     * Resumes media playback.
     */
    public void play() {
        play(true, -1);
    }

    /**
     * Pauses media playback.
     */
    public void pause() {
        if (player == null || !_hasStarted) return;

        player.setPlayWhenReady(false);
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
     *
     * @param position New position of the media in seconds
     */
    public void seek(float position) {
        if (player == null) return;

        player.seekTo(Math.round(position * 1000f));
    }

    /**
     * Enables or disables controls.
     *
     * @param flag true to show or hide the controls
     */
    public void setEnableControls(boolean flag) {
        if (media.isAudioOnly) return;

        if (player != null) {
            simplePlayerView.setEnableControls(flag);
        } else _enableControls = flag;
    }

    /**
     * Hides the player controls.
     *
     * @param controls List of the controls to be affected (constants from class <code>SambaPlayer.Controls</code>)
     */
    public void setHideControls(@NonNull final String... controls) {
        if (controls.length == 0)
            return;

        controlsHidden = Arrays.asList(controls);

        if (player == null)
            return;

        //player.setControlsVisible(false, controls);
    }

    /**
     * Sets fullscreen mode on and off.
     *
     * @param flag true to enter in the fullscreen mode on and false to exit
     */
    public void setFullscreen(boolean flag) {
        if (player == null) return;

        //player.getPlaybackParameters();
    }

    /**
     * Indicates the fullscreen mode on or off.
     *
     * @return boolean Whether fullscreen mode is on of off.
     */
    public boolean isFullscreen() {
        return player != null /*&& player.isFullscreen()*/;
    }

    /**
     * Shows player controls.
     */
    public void show() {
        if (player == null) return;

        simplePlayerView.show();
    }

    /**
     * Hides player controls.
     */
    public void hide() {
        if (player == null) return;

        simplePlayerView.hide();
    }

    /**
     * Sets whether the player should go automatically on fullscreen or not.
     *
     * @param flag true to enable auto fullscreen mode and false to disable it
     */
    public void setAutoFullscreenMode(boolean flag) {
        _autoFsMode = flag;
    }

    /**
     * Gets the current time on the video.
     *
     * @return Float Current time
     */
    public float getCurrentTime() {
        return player != null ? player.getCurrentPosition() / 1000f : 0f;
    }

    /**
     * Gets the total duration of the video.
     *
     * @return Float total duration
     */
    public float getDuration() {
        return player != null ? player.getDuration() / 1000f : media.duration;
    }

    /**
     * Indicates whether media is being reproduced.
     *
     * @return The state of media playback
     */
    public boolean isPlaying() {
        return player != null && player.getPlayWhenReady();
    }

    /**
     * Indicates whether media has already started playing.
     *
     * @return True if media has started playing
     */
    public boolean hasStarted() {
        return _hasStarted;
    }

    /**
     * Indicates whether media has finished at least once.
     * Does not imply playing it thoroughly without seeking.
     *
     * @return True once media hits the end
     */
    public boolean hasFinished() {
        return _hasFinished;
    }

    /**
     * Changes the current output.
     *
     * @param output SambaMedia.Output indicating the new output
     */
    public void changeOutput(@NonNull SambaMedia.Output output) {
        if (output.url == null || output.url.isEmpty()) {
            //dispatchError(SambaPlayerError.emptyUrl);
            Log.e("SambaPlayer", "URL not found for output \"" + output.label + "\".");
            return;
        }

        final long currentPosition = player.getCurrentPosition();
        //final boolean wasPlaying = isPlaying();

        for (SambaMedia.Output o : media.outputs)
            o.isDefault = o.label.equals(output.label);

        media.url = output.url;

        destroyInternal();
        create(false);
        player.seekTo(currentPosition);
    }

    /**
     * Changes the current output.
     * Must be called after START event has been dispatched.
     *
     * @param index The index in the outputs array.
     */

    public void switchOutput(int index) {
        if (player == null || outputSheetView == null)
            return;

        outputSheetView.setTag(index + _outputOffset);
        //player.setSelectedTrack(index + _outputOffset);
    }

    /**
     * Retrieves the selected output index.
     *
     * @return The selected output index
     */
    public int getCurrentOutputIndex() {
        return (outputSheetView != null && outputSheetView.getTag() != null ?
                (int) outputSheetView.getTag() : media.defaultOutputIndex) - _outputOffset;
    }

    /**
     * Changes the current caption.
     *
     * @param index The index in the captions array
     */
    public void changeCaption(int index) {
        Captions plugin = (Captions) PluginManager.getInstance().getPlugin(Captions.class);

        if (plugin == null) return;

        plugin.changeCaption(index);
    }

    public String getCaption() {
        if (captionSheetView == null) return null;
        CaptionsSheetAdapter adapter = (CaptionsSheetAdapter) ((ListView) captionSheetView.findViewById(R.id.sheet_list)).getAdapter();
        SambaMedia.Caption caption = (SambaMedia.Caption) adapter.getItem(adapter.currentIndex);
        return String.format("[%s,ffcc00,42]", caption.language);
    }

    /**
     * If set, Chromecast support will be enabled inside player view.
     *
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
     *
     * @param error Error type to show
     */
    public void destroy(SambaPlayerError error) {
        PluginManager.getInstance().onDestroy();
        destroyInternal();
        SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.UNLOAD));

        //if (error != null)
        //	showError(error);
        //else destroyError();
    }

	/* End Player API */

    private void applyAttributes(TypedArray attrs) {
        try {
            setAutoFullscreenMode(attrs.getBoolean(R.styleable.SambaPlayer_autoFullscreenMode, false));
            setEnableControls(attrs.getBoolean(R.styleable.SambaPlayer_enableControls, true));
        } finally {
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


        // 1. Create a default TrackSelector

        playerInstanceDefault = new PlayerInstanceDefault(getContext());
        simplePlayerView = new SambaSimplePlayerView(getContext(), this);
        player = playerInstanceDefault.createPlayerInstance();
        simplePlayerView.setPlayer(player);
        simplePlayerView.setVideoTitle(media.title);
        simplePlayerView.configureSubTitle(media.captionsConfig);
        simplePlayerView.configView(!media.isAudioOnly, media.isLive);


        switch (media.type.toLowerCase()) {
            case "hls":
                playerMediaSourceInterface = new PlayerMediaSourceHLS(playerInstanceDefault, media.url);
                break;
            case "dash":
                playerMediaSourceInterface = new PlayerMediaSourceDash(playerInstanceDefault, media.url);
                break;
            default:
                playerMediaSourceInterface = new PlayerMediaSourceExtractor(playerInstanceDefault, media.url);
                break;
        }

        //playerMediaSourceInterface = new PlayerMediaSourceDash(playerInstanceDefault, "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd");
        //playerMediaSourceInterface = new PlayerMediaSourceExtractor(playerInstanceDefault, "https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-lavf-56.36.100-aac-avc-main-1280x720.mkv");

        player.addListener(new Player.EventListener() {

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                Format video = null;
                Format legenda = null;
                TrackSelection videos = null;
                if (trackSelections.length > 0 ) videos = trackSelections.get(0);
                if (videos != null && videos.getSelectionReason() != C.SELECTION_REASON_INITIAL) { //SELECTION_REASON_INITIAL == auto,
                    if (trackSelections.length > 0 && trackSelections.get(0) != null) video = trackSelections.get(0).getSelectedFormat();
                }
                if (trackSelections.length > 2 && trackSelections.get(2) != null)  legenda = trackSelections.get(2).getSelectedFormat();
                simplePlayerView.setupMenu(playerMediaSourceInterface, video, legenda);
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                simplePlayerView.setLoading(playbackState == Player.STATE_BUFFERING);
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity() {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }
        });

        player.setPlayWhenReady(true);
        playerMediaSourceInterface.addSubtitles(media.captions);
        //media.adUrl = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpostoptimizedpod&cmsid=496&vid=short_onecue&correlator=";
        if (media.adUrl != null ) {
            playerMediaSourceInterface.addAds(media.adUrl, simplePlayerView.getPlayerView().getOverlayFrameLayout());
        }
        player.prepare(playerMediaSourceInterface.getMediaSource());
        _hasStarted = true;


//		//Live treatment
//		if (media.isLive) {
//			((Activity)getContext()).findViewById(R.id.time_container).setVisibility(INVISIBLE);
//
//			player.setControlsVisible(false, Controls.SEEKBAR);
//			player.addActionButton(ContextCompat.getDrawable(getContext(), R.drawable.ic_live),
//					getContext().getString(R.string.live), null);
//		}

//		player.addPlaybackListener(playbackListener);
//		player.setPlayCallback(playListener);

		if (media.isAudioOnly) {
//			player.setControlsVisible(true, Controls.PLAY);
//			player.setControlsVisible(false, Controls.FULLSCREEN, Controls.PLAY_LARGE, Controls.TOP_CHROME);
//			player.setBackgroundColor(0xFF434343);
//			player.setChromeColor(0x00000000);
		}
		else simplePlayerView.setFullscreenCallback(fullscreenListener);

        if (!controlsHidden.isEmpty())
            setHideControls(controlsHidden.toArray(new String[0]));

        // Fullscreen
        orientationEventListener = new OrientationEventListener(getContext()) {

            private final Orientation orientation = new Orientation();
            private int lastRotatedTo = 0;

            {
                enable();
            }

            @Override
            public void onOrientationChanged(int newValue) {
                if (!_autoFsMode || player == null) return;
                int newOrientation = orientation.getMeasuredOrientation(newValue);
                if (newOrientation == lastRotatedTo || newOrientation == Orientation.INVALID) return;
                lastRotatedTo = newOrientation;
                switch (lastRotatedTo) {
                    case Orientation.PORTRAIT:
                        simplePlayerView.setFullscreen(false);
                        SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.PORTRAIT));
                        break;
                    case Orientation.REVERSE_LANDSCAPE:
                        simplePlayerView.setFullscreen(true, true);
                        SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.LANDSCAPE));
                        break;
                    case Orientation.LANDSCAPE:
                        simplePlayerView.setFullscreen(true, false);
                        SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.LANDSCAPE));
                        break;
                    default:
                        break;
                }
            }
        };

        // video-only
        if (!media.isAudioOnly) {

            //if (!_enableControls)
            //player.disableControls();

            //PluginManager.getInstance().onInternalPlayerCreated(player);

            if (notify)
                SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.LOAD, this));
        }

		/*player.addActionButton(ContextCompat.getDrawableRes(getContext(), R.drawable.share),
                getContext().getString(R.string.share_facebook), new OnClickListener() {
			@Override
			public void onClick(View v) {}
		});*/

        setupCast();

//		if (sambaCast != null && sambaCast.isCasting())
//			castListener.onConnected(sambaCast.getCastSession());
    }

    private void destroyInternal() {
        stopProgressTimer();
        stopErrorTimer();

        if (player == null)
            return;

        stop();
        //player.setFullscreen(false);

        if (outputSheetView != null) {
            ((ListView) outputSheetView.findViewById(R.id.sheet_list)).setOnItemClickListener(null);
        }

        if (captionSheetView != null) {
            ((ListView) captionSheetView.findViewById(R.id.sheet_list)).setOnItemClickListener(null);
        }

        orientationEventListener.disable();
        //player.removePlaybackListener(playbackListener);
        //player.setPlayCallback(null);
        //player.setFullscreenCallback(null);

        if (sambaCast != null) {
            //player.setInterceptableListener(null);
            sambaCast.setEventListener(null);
        }

        // player.setControlsVisible(false);
        player.release();

        outputSheetView = null;
        captionSheetView = null;
        orientationEventListener = null;
        player = null;
        _hasStarted = false;
        _hasFinished = false;
        _disabled = false;
    }

	/*private void showError(@NonNull SambaPlayerError error) {
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
	}*/

    private void destroyError() {
        stopErrorTimer();

        if (errorScreen == null)
            return;

        //errorScreen.findViewById(R.id.retry_button).setOnClickListener(null);
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
                ((Activity) getContext()).runOnUiThread(progressDispatcher);
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

    private void stopErrorTimer() {
        if (errorTimer == null) return;

        errorTimer.cancel();
        errorTimer.purge();
        errorTimer = null;
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
                //showError(error);
                break;
        }
    }

    private void setupCast() {
        // if Chromecast support is enabled
        if (sambaCast == null || media.isLive || media.isAudioOnly) return;

        //sambaCast.setEventListener(castListener);

        if (player != null) {
            MediaRouteButton button = sambaCast.getButton();
            ViewGroup parent = (ViewGroup) button.getParent();

            if (parent != null)
                parent.removeView(button);

            //player.addActionButton(button);
        }
    }
}
