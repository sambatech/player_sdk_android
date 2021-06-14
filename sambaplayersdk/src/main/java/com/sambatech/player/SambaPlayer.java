package com.sambatech.player;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DrmSession;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.sambatech.player.cast.CastDRM;
import com.sambatech.player.cast.CastObject;
import com.sambatech.player.cast.CastOptionsProvider;
import com.sambatech.player.cast.CastPlayer;
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
import com.sambatech.player.plugins.PluginManager;
import com.sambatech.player.utils.CastLiveButtonListener;
import com.sambatech.player.utils.Helpers;
import com.sambatech.player.utils.Orientation;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static android.content.ContentValues.TAG;

/**
 * Represents the player, responsible for managing media playback.
 *
 * @author Leandro Zanol - 7/12/15
 */
public class SambaPlayer extends FrameLayout {

    private final Player.DefaultEventListener playerEventListener = new Player.DefaultEventListener() {

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Format video = null;
            Format legenda = null;
            TrackSelection videos = null;
            if (trackSelections.length > 0) videos = trackSelections.get(0);
            if (videos != null && videos.getSelectionReason() != C.SELECTION_REASON_INITIAL && videos.getSelectionReason() != C.SELECTION_REASON_TRICK_PLAY) { // == auto
                if (trackSelections.length > 0 && trackSelections.get(0) != null)
                    video = trackSelections.get(0).getSelectedFormat();
            }
            if (trackSelections.length > 2 && trackSelections.get(2) != null)
                legenda = trackSelections.get(2).getSelectedFormat();
            simplePlayerView.setupMenu(playerMediaSourceInterface, video, legenda, _abrEnabled);


        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.i("SambaPlayer", "state: " + playWhenReady + " " + playbackState + "; playing: " + isPlaying() + "; playingAd: " + player.isPlayingAd());
            switch (playbackState) {
                case Player.STATE_READY:
                    if (playWhenReady) {


                        if (!_hasStarted) {
                            _hasStarted = true;
                            _currentRetryIndex = 0;

                            destroyError();
                            SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.START));

                            // initial position
                            if (!media.isLive && _initialTime > 0) {
                                seek(_initialTime);
                                _initialTime = 0;
                            }

                            // start in fullscreen
                            if (_initialFullscreen != null) {
                                simplePlayerView.setFullscreen(_initialFullscreen);
                                _initialFullscreen = null;
                            }
                        }
                        dispatchPlay();
                    } else {
                        dispatchPause();
                    }
                    simplePlayerView.updatePlayPause(playWhenReady ? PlayPauseState.Playing : PlayPauseState.Pause);
                    adjustCurrentOutputs();

                    break;
                case Player.STATE_ENDED:
                    if (!playWhenReady || player.isPlayingAd())
                        break;
                    pause();
                    player.seekTo(0);
                    Log.d(TAG, "onPlayerStateChanged: " + player.isPlayingAd());
                    stopProgressTimer();
                    SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.FINISH));
                    _hasFinished = true;
                    simplePlayerView.updatePlayPause(PlayPauseState.Pause);

                    break;
                case Player.STATE_BUFFERING:
                    simplePlayerView.updatePlayPause(PlayPauseState.Loading);
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
        public void onPlayerError(final ExoPlaybackException e) {
            final Exception error = (Exception) e.getCause();
            Log.d("SambaPlayer", "Error: " + media, error);

            String msg = "Você está offline! Verifique sua conexão.";
            SambaPlayerError.Severity severity = SambaPlayerError.Severity.recoverable;
            final boolean isBehindLiveWindowException = error instanceof BehindLiveWindowException;


            for (StackTraceElement element : e.getCause().getStackTrace()) {
                if (element.toString().contains("MediaCodecRenderer.feedInputBuffer") || element.toString().contains("native_dequeueOutputBuffer")) {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            destroyInternal();
                            create(false);
                        }
                    }, 1000);
                    return;
                }
            }

            if (_initialTime == 0f)
                _initialTime = getCurrentTime();


            _currentOutputIndex = playerMediaSourceInterface.getCurrentOutputTrackIndex(player.getCurrentTrackSelections(), _abrEnabled);
            _currentCaptionIndex = playerMediaSourceInterface.getCurrentCaptionTrackIndex(player.getCurrentTrackSelections());

            if (_currentCaptionIndex >= 0) _forceCaptionIndexTo = _currentCaptionIndex;
            if (_currentOutputIndex >= 0) _forceOutputIndexTo = _currentOutputIndex;

            _initialFullscreen = simplePlayerView.isFullscreen();

            destroyInternal();

            // unauthorized DRM content
            if (error instanceof DrmSession.DrmSessionException || error.getCause() instanceof UnsupportedDrmException) {
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
                                                "Conectando...", SambaPlayerError.Severity.info, error));
                                        return;
                                    }

                                    dispatchError(SambaPlayerError.unknown.setValues(SambaPlayerError.unknown.getCode(),
                                            "Ocorreu um erro! Por favor, tente mais tarde...",
                                            SambaPlayerError.Severity.critical, error));
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
                } catch (IOException e1) {
                    msg = "Ocorreu um erro! Por favor, tente novamente.";
                    severity = SambaPlayerError.Severity.recoverable;
                }
            }
            // no network connection
            else if (_currentRetryIndex++ < media.retriesTotal) {
                final AtomicInteger secs = new AtomicInteger(8);

                stopErrorTimer();

                errorTimer = new Timer();
                errorTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (secs.get() == 0) {
                                    stopErrorTimer();
                                    create(false);
                                }

                                dispatchError(SambaPlayerError.unknown.setValues(SambaPlayerError.unknown.getCode(),
                                        secs.get() > 0 ? String.format("Reconectando em %ss", secs) : "Conectando...",
                                        SambaPlayerError.Severity.info, error, R.drawable.sambaplayer_ic_nosignal));

                                secs.decrementAndGet();
                            }
                        });
                    }
                }, 0, 1000);
                return;
            }

            dispatchError(SambaPlayerError.unknown.setValues(SambaPlayerError.unknown.getCode(),
                    msg, severity, error));
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            adjustCurrentOutputs(); //Pode ser o fim do primero AD
        }

        private void adjustCurrentOutputs() {
            if (!player.isPlayingAd() && playerMediaSourceInterface != null) {
                if (_forceOutputIndexTo >= 0) {
                    playerMediaSourceInterface.forceOutuputTrackTo(_forceOutputIndexTo, _abrEnabled);
                    _forceOutputIndexTo = -1;
                }
                if (_forceCaptionIndexTo >= 0) {
                    playerMediaSourceInterface.forceCaptionTrackTo(_forceCaptionIndexTo);
                    _forceCaptionIndexTo = -1;
                }
            }
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

    private final SambaCastListener castListener = new SambaCastListener() {

        RemoteMediaClient remoteMediaClient;

        @Override
        public void onConnected(final CastSession castSession) {

            if (player == null) {
                return;
            }

            stopProgressTimer();
            player.setPlayWhenReady(false);

            SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.CAST_CONNECT));

            final RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
            if (remoteMediaClient == null) return;

            castPlayer.setRemoteMediaClient(remoteMediaClient);
            castPlayer.setIsLive(media.isLive);

            // converting SambaMedia to MediaInfo
            MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
            movieMetadata.putString(MediaMetadata.KEY_TITLE, media.title);
            movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, media.title);

            CastQuery qs = new CastQuery(true, CastOptionsProvider.environment.toString(),
                    CastOptionsProvider.appId, (long) getCurrentTime(), getCaption());

            CastObject castObject = new CastObject(media.title, media.id,
                    (long) media.duration, media.themeColorHex,
                    media.projectHash, qs, "", CastOptionsProvider.playerUrl);

            if (media.isLive) {
                castObject.setLive(media.id);
            } else {
                castObject.setLive(null);
            }


            if (media.drmRequest != null) {
                CastDRM castDRM = new CastDRM(media.drmRequest.getLicenseParam("SessionId"),
                        media.drmRequest.getLicenseParam("Ticket"));
                if (media.drmRequest.getProvider() != null && media.drmRequest.getProvider().equals("SAMBA_DRM") && media.drmRequest.getToken() != null) {
                    castDRM.setToken(media.drmRequest.getToken());
                }

                castObject.setDrm(castDRM);
            }


            MediaInfo mediaInfo = new MediaInfo.Builder(castObject.toString())
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType("video/mp4")
                    .setMetadata(movieMetadata)
                    .build();

            MediaQueueItem[] mediaQueueItems = new MediaQueueItem[1];
            mediaQueueItems[0] = new MediaQueueItem.Builder(mediaInfo).build();


            String tempMediaCasting = SambaCast.currentMediaCastingId(getContext());


            if (tempMediaCasting == null || !tempMediaCasting.equals(media.id)) {
                PendingResult<RemoteMediaClient.MediaChannelResult> result = castPlayer.loadItems(mediaQueueItems, 0, 0, Player.REPEAT_MODE_OFF);
                result.setResultCallback(new ResultCallback<RemoteMediaClient.MediaChannelResult>() {
                    @Override
                    public void onResult(@NonNull RemoteMediaClient.MediaChannelResult mediaChannelResult) {
                        SambaCast.setCurrentMediaCastingId(getContext(), media.id);
                        castPlayer.setPlayWhenReady(true);
                    }
                }, 5, TimeUnit.SECONDS);
            } else {
                castPlayer.resumeItems(mediaQueueItems, 0, Player.REPEAT_MODE_OFF);
                castPlayer.syncInternalState();
            }


            sambaCast.registerDeviceForProgress(true);
            castPlayer.setMessageListener(castSession);

            simplePlayerView.showCast(media.isLive, new CastLiveButtonListener() {
                @Override
                public void onLiveButtonClicked(View view) {
                    SambaCast.cleanCacheDatas(getContext());
                    castPlayer.setPlayWhenReady(false);
                    castPlayer.updateInternalState();
                    castListener.onConnected();
                }
            });

            setFullscreen(false);


            this.remoteMediaClient = remoteMediaClient;
        }

        @Override
        public void onConnected() {
            onConnected(sambaCast.getCastSession());
        }

        @Override
        public void onDisconnected() {
            SambaCast.cleanCacheDatas(getContext());
            SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.CAST_DISCONNECT));
            long lastPosition = castPlayer.getContentPosition();

            if (simplePlayerView != null) {
                simplePlayerView.setupCastButton(false);
            }

            if (player != null) {
                player.seekTo(lastPosition);
            }

            if (simplePlayerView != null) {
                simplePlayerView.hideCast();
            }

            play();
            startProgressTimer();

        }
    };

    //private SimpleExoPlayer player;
    private View errorScreen;
    private @NonNull
    SambaMediaConfig media = new SambaMediaConfig();
    private Timer progressTimer;
    private boolean _hasStarted;
    private boolean _hasFinished;
    private OrientationEventListener orientationEventListener;
    private SambaCast sambaCast;
    private boolean _autoFsMode;
    private boolean _enableControls = true;
    private boolean _disabled;

    private float _initialTime = 0f;
    private Boolean _initialFullscreen = null;
    private Timer errorTimer;
    private List<String> controlsHidden = new ArrayList<>();
    private boolean _abrEnabled = true;
    private int _forceOutputIndexTo = -1;
    private int _forceCaptionIndexTo = -1;

    private int _currentBackupIndex;
    private int _currentRetryIndex;

    private int _currentOutputIndex = -1;
    private int _currentCaptionIndex = -1;


    private SambaSimplePlayerView simplePlayerView;
    private SimpleExoPlayer player;
    private PlayerInstanceDefault playerInstanceDefault;
    private PlayerMediaSourceInterface playerMediaSourceInterface;
    //private boolean wasPlaying;

    CastPlayer castPlayer;

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
        this._abrEnabled = abrEnabled;
        this._forceOutputIndexTo = outputIndex == -1 && !abrEnabled ? 0 : outputIndex;
        this._forceCaptionIndexTo = 0;

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
        } else {
            if (_forceOutputIndexTo >= 0)
                playerMediaSourceInterface.forceOutuputTrackTo(_forceOutputIndexTo, _abrEnabled);
            _forceOutputIndexTo = -1;
        }

        if (sambaCast != null && sambaCast.isCasting() && castPlayer != null) {
            castPlayer.setPlayWhenReady(true);
            stopProgressTimer();
            player.setPlayWhenReady(false);
        } else {
            player.setPlayWhenReady(true);
            SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.PLAY));
        }
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
        if (player == null) return;
        if (sambaCast != null && sambaCast.isCasting() && castPlayer != null) {
            castPlayer.setPlayWhenReady(false);
        } else {
            player.setPlayWhenReady(false);
            SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.PAUSE));
        }
    }

    /**
     * Stops media playback returning the video to it's beginning.
     */
    public void stop() {
        if (player == null) return;
        if (sambaCast != null && sambaCast.isCasting()) {
            //sambaCast.stopCasting();
        } else {
            player.stop();
        }
        SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.STOP));
    }

    /**
     * Moves the media to a specific position.
     *
     * @param position New position of the media in seconds
     */
    public void seek(float position) {
        if (player == null) return;
        if (sambaCast != null && sambaCast.isCasting()) {
            sambaCast.seekTo((int) (position * 1000));
        } else {
            player.seekTo(Math.round(position * 1000f));
        }
    }

    /**
     * Enables or disables controls.
     *
     * @param flag true to show or hide the controls
     */
    public void setControlsVisibility(boolean flag) {
        _enableControls = flag;
        if (player == null && simplePlayerView == null) return;
        simplePlayerView.setEnableControls(flag);
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

        simplePlayerView.setControlsVisible(false, controls);
    }

    /**
     * Sets fullscreen mode on and off.
     *
     * @param flag true to enter in the fullscreen mode on and false to exit
     */
    public void setFullscreen(boolean flag) {
        if (player == null || simplePlayerView == null) return;
        simplePlayerView.setFullscreen(flag);
    }

    /**
     * Indicates the fullscreen mode on or off.
     *
     * @return boolean Whether fullscreen mode is on of off.
     */
    public boolean isFullscreen() {
        return simplePlayerView != null && simplePlayerView.isFullscreen();
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

        if (sambaCast != null && sambaCast.isCasting() && castPlayer != null) {
            return castPlayer.getCurrentPosition() / 1000f;
        }

        return player != null ? player.getCurrentPosition() / 1000f : 0f;
    }

    /**
     * Gets the total duration of the video.
     *
     * @return Float total duration
     */
    public float getDuration() {

        if (sambaCast != null && sambaCast.isCasting() && castPlayer != null) {
            return castPlayer.getDuration() / 1000f;
        }

        return player != null ? player.getDuration() / 1000f : media.duration;
    }

    /**
     * Indicates whether media is being reproduced.
     *
     * @return The state of media playback
     */
    public boolean isPlaying() {
        return player != null && player.getPlayWhenReady() && (player.getPlaybackState() == Player.STATE_READY || player.getPlaybackState() == Player.STATE_BUFFERING);
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
     * Must be called after START event has been dispatched.
     *
     * @param index The index in the outputs array.
     */

    public void switchOutput(int index) {
        if (player == null || simplePlayerView == null || playerMediaSourceInterface == null)
            return;
        playerMediaSourceInterface.forceOutuputTrackTo(index, _abrEnabled);
    }

    /**
     * Retrieves the selected output index.
     *
     * @return The selected output index
     */
    public int getCurrentOutputIndex() {
        if (player == null || player.getCurrentTrackSelections() == null || simplePlayerView == null || playerMediaSourceInterface == null)
            return C.INDEX_UNSET;
        return playerMediaSourceInterface.getCurrentOutputTrackIndex(player.getCurrentTrackSelections(), _abrEnabled);
    }

    /**
     * Changes the current caption.
     *
     * @param index The index in the captions array
     */
    public void changeCaption(int index) {
        if (player == null || simplePlayerView == null || playerMediaSourceInterface == null)
            return;
        playerMediaSourceInterface.forceCaptionTrackTo(index);
    }

    public String getCaption() {
        if (player == null || simplePlayerView == null || playerMediaSourceInterface == null)
            return "";
        Format caption = null;
        if (player.getCurrentTrackSelections().length > 2 && player.getCurrentTrackSelections().get(2) != null)
            caption = player.getCurrentTrackSelections().get(2).getSelectedFormat();
        return String.format("[%s,ffcc00,42]", caption != null && caption.language != null ? caption.language : "");
    }

    public int getCurrentCaptionIndex() {
        if (player == null || player.getCurrentTrackSelections() == null || simplePlayerView == null || playerMediaSourceInterface == null)
            return C.INDEX_UNSET;
        return playerMediaSourceInterface.getCurrentCaptionTrackIndex(player.getCurrentTrackSelections());
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
        SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.DESTROY));

        if (error != null)
            showError(error);
        else destroyError();
    }

    /* End Player API */

    private void applyAttributes(TypedArray attrs) {
        try {
            setAutoFullscreenMode(attrs.getBoolean(R.styleable.SambaPlayer_autoFullscreenMode, false));
            setControlsVisibility(attrs.getBoolean(R.styleable.SambaPlayer_enableControls, true));
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

        playerInstanceDefault = new PlayerInstanceDefault(getContext(), media);
        simplePlayerView = new SambaSimplePlayerView(getContext(), this);

        player = playerInstanceDefault.createPlayerInstance();

        simplePlayerView.setPlayer(player);
        simplePlayerView.setVideoTitle(media.title);
        simplePlayerView.configureSubTitle(media.captionsConfig);
        simplePlayerView.configView(!media.isAudioOnly, media.isLive, media.isDvr, sambaCast != null && !sambaCast.isCastButtonOut());
        simplePlayerView.setEnableControls(_enableControls);

        if (media.url.toLowerCase().endsWith(".mp3"))
            media.type = "progressive";

        String url = media.isOffline ? media.downloadUrl : media.url;
        switch (media.type.toLowerCase()) {
            case "hls":
                if(media.clientId == 3170) {
                    playerMediaSourceInterface = new PlayerMediaSourceHLS(playerInstanceDefault, url, true, media.isLive);
                } else {
                    playerMediaSourceInterface = new PlayerMediaSourceHLS(playerInstanceDefault, url, false, media.isLive);
                }
                break;
            case "dash":
                playerMediaSourceInterface = new PlayerMediaSourceDash(playerInstanceDefault, url);
                break;
            default:
                playerMediaSourceInterface = new PlayerMediaSourceExtractor(playerInstanceDefault, url);
                break;
        }

        player.addListener(playerEventListener);

        player.setPlayWhenReady(true);
        if ((media.captions != null && !media.captions.isEmpty()) && (!media.isOffline || media.isSubtitlesOffline)) {
            playerMediaSourceInterface.addSubtitles(media.captions);
        }

        if (media.adUrl != null) {
            playerMediaSourceInterface.addAds(media.adUrl, simplePlayerView.getPlayerView().getOverlayFrameLayout());
        }

        player.prepare(playerMediaSourceInterface.getMediaSource());
        player.setRepeatMode(Player.REPEAT_MODE_OFF);

        simplePlayerView.setThemeColor(media.themeColor);

        if (media.isAudioOnly) {
            if (media.thumbAudioURL != null && !media.thumbAudioURL.isEmpty()) {
                simplePlayerView.setBackgroundImageThumb(media.thumbAudioURL);
            } else {
                simplePlayerView.setBackgroundColor(0xFF434343);
            }
            simplePlayerView.setChromeColor(0x00000000);
        } else {
            simplePlayerView.setFullscreenCallback(fullscreenListener);
        }

        if (!controlsHidden.isEmpty())
            setHideControls(controlsHidden.toArray(new String[0]));

        // Fullscreen
        createOrientationEventListener();

        // video-only
        if (!media.isAudioOnly) {

            //if (!_enableControls)
            //player.disableControls();

            PluginManager.getInstance().onInternalPlayerCreated(simplePlayerView.getPlayerView());

            if (notify)
                SambaEventBus.post(new SambaEvent(SambaPlayerListener.EventType.LOAD, this));
        }

        if (media.isAudioOnly) {
            if (sambaCast != null && sambaCast.isCasting()) {
                sambaCast.setEventListener(null);
                sambaCast.stopCasting();
            } else {
                SambaCast.cleanCacheDatas(getContext());
            }
        } else {
            setupCast();

            simplePlayerView.createCastPlayer(castPlayer, media.themeColor, media.captions);

            if (sambaCast != null && sambaCast.isCasting()) {
                castListener.onConnected(sambaCast.getCastSession());
            } else {
                SambaCast.cleanCacheDatas(getContext());
            }
        }
    }

    private void createOrientationEventListener() {
        orientationEventListener = new OrientationEventListener(getContext()) {

            private final Orientation orientation = new Orientation();
            private int lastRotatedTo = 0;

            {
                enable();
            }

            @Override
            public void onOrientationChanged(int newValue) {
                if (!_autoFsMode || player == null || simplePlayerView == null) return;
                int newOrientation = orientation.getMeasuredOrientation(newValue);
                if (newOrientation == lastRotatedTo || newOrientation == Orientation.INVALID)
                    return;
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
    }

    private void destroyInternal() {
        stopProgressTimer();
        stopErrorTimer();
        stop();
        setFullscreen(false);
        if (orientationEventListener != null) {
            orientationEventListener.disable();
            orientationEventListener = null;
        }

        if (sambaCast != null) {
            //player.setInterceptableListener(null);
            sambaCast.setEventListener(null);
        }

        if (simplePlayerView != null) {
            simplePlayerView.setFullscreenCallback(null);
            simplePlayerView.destroyInternal();
            simplePlayerView = null;
        }

        if (playerInstanceDefault != null) {
            playerInstanceDefault.destroy();
            playerInstanceDefault = null;
        }

        if (playerMediaSourceInterface != null) {
            playerMediaSourceInterface.destroy();
            playerMediaSourceInterface = null;
        }

        if (player != null) {
            player.removeListener(playerEventListener);
            player.release();
            player = null;
        }

        _hasStarted = false;
        _hasFinished = false;
        _disabled = false;
    }

    private void showError(@NonNull SambaPlayerError error) {
        if (errorScreen == null)
            errorScreen = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.error_screen, this, false);

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

        if (media.isAudioOnly)
            // shows retry button when recoverable error
            if (error.getSeverity() == SambaPlayerError.Severity.recoverable)
                textView.setVisibility(GONE);
            else textView.setCompoundDrawables(null, null, null, null);
            // set custom image
        else if (error.getDrawableRes() > 0)
            textView.setCompoundDrawablesWithIntrinsicBounds(0, error.getDrawableRes(), 0, 0);
            // default error image
        else
            textView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.sambaplayer_error_icon, 0, 0);

        if (errorScreen.getParent() == null)
            addView(errorScreen);
    }

    private void destroyError() {
        stopErrorTimer();

        if (errorScreen == null)
            return;

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
                showError(error);
                break;
        }
    }

    private void setupCast() {
        if (sambaCast == null || media.isAudioOnly) return;
        sambaCast.setEventListener(castListener);
        castPlayer = new CastPlayer(getContext(), sambaCast);
    }
}
