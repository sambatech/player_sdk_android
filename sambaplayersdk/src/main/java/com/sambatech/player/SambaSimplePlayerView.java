package com.sambatech.player;

import android.app.Activity;
import android.app.MediaRouteButton;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.CaptionStyleCompat;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.gms.cast.framework.CastSession;
import com.sambatech.player.adapter.CaptionsSheetAdapter;
import com.sambatech.player.adapter.OutputSheetAdapter;
import com.sambatech.player.adapter.SpeedSheetAdapter;
import com.sambatech.player.cast.CastOptionsMenu;
import com.sambatech.player.cast.CastPlayer;
import com.sambatech.player.event.SambaCastListener;
import com.sambatech.player.mediasource.PlayerMediaSourceInterface;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.utils.Controls;
import com.sambatech.player.utils.OptionsMenuLayer;
import com.sambatech.player.utils.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static android.graphics.Typeface.NORMAL;
import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static android.view.accessibility.CaptioningManager.CaptionStyle.EDGE_TYPE_NONE;

/**
 * Created by luizbyrro on 30/11/2017.
 */

public class  SambaSimplePlayerView implements View.OnClickListener {

    private Context context;
    private FrameLayout playerContainer;
    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;
    private OptionsMenuLayer optionsMenuLayer;
    private FrameLayout loadingView;

    //bottom buttons e  progresso
    private LinearLayout progressControls;
    private ImageButton fullscreenButton;

    //barras de controle
    private LinearLayout bottomBar;
    private LinearLayout controlsView;
    private LinearLayout topBar;

    //topbar buttons e texto
    private ImageButton optionsMenuButton;
    private ImageButton liveButton;
    private FrameLayout castButton;
    private TextView videoTitle;

    private boolean isFullscreen = false;
    private boolean isReverseLandscape = false;

    private boolean isLive = false;
    private boolean isVideo = false;
    private boolean hasCast = false;

    private View outputSheetView;
    private View captionSheetView;
    private View speedSheetView;

    private CustomTimeBar customTimeBar;
    private ProgressBar progressBar;

    //small buttons bottom bar
    private LinearLayout smallPlayPauseContainer;
    private ImageButton playSmallButton;
    private ImageButton pauseSmallButton;
    private ProgressBar smallProgressBar;

    //hide Controls
    private HashMap<String, View> controlsMap;
    private LinkedHashSet<View> hiddenViews;
    private boolean hasMenu = false;

    // cast controls
    private PlaybackControlView castControlView;
    private CastPlayer sambaCastPlayer;
    private CastOptionsMenu castOptionsMenu;

    /**
     * The output menu modalsheet.
     */
    private BottomSheetDialog outputSheetDialog;

    /**
     * Captions menu modalsheet
     */
    private BottomSheetDialog captionsSheetDialog;

    /**
     * Speed menu modalsheet
     */
    private BottomSheetDialog speedSheetDialog;

    /**
     * Indicates playback last state before the output menu has open.
     */
    private boolean menuWasPlaying;

    /**
     * Play or pause for another buttons.
     */
    private View.OnClickListener playPauseClickLisner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int i = v.getId();
            if (i == R.id.small_play) {
                player.setPlayWhenReady(true);
            } else if (i == R.id.small_pause) {
                player.setPlayWhenReady(false);
            }

        }
    };

    public SambaSimplePlayerView(Context context, FrameLayout playerContainer) {
        this.context = context;
        this.playerContainer = playerContainer;
        playerView = (SimpleExoPlayerView) SimpleExoPlayerView.inflate(context, R.layout.custom_simple_exo_player_view, null);
        bindMethods();
        createMenuView();
        this.originalContainerLayoutParams = this.playerContainer.getLayoutParams();
        this.playerContainer.addView(playerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.playerContainer.setBackgroundColor(Color.BLACK);
        playerView.setBackgroundColor(Color.BLACK);
        initControlsMap();
    }

    public void bindMethods() {
        videoTitle = (TextView) playerView.findViewById(R.id.video_title_text);
        optionsMenuButton = (ImageButton) playerView.findViewById(R.id.topbar_menu_button);
        liveButton = (ImageButton) playerView.findViewById(R.id.topbar_live_button);
        castButton = (FrameLayout) playerView.findViewById(R.id.topbar_cast_button);
        fullscreenButton = (ImageButton) playerView.findViewById(R.id.fullscreen_button);
        loadingView = (FrameLayout) playerView.findViewById(R.id.exo_progress_view);
        controlsView = (LinearLayout) playerView.findViewById(R.id.exo_control_bar);
        topBar = (LinearLayout) playerView.findViewById(R.id.exo_top_bar);
        bottomBar = (LinearLayout) playerView.findViewById(R.id.exo_bottom_bar);
        progressControls = (LinearLayout) playerView.findViewById(R.id.exo_progress_controls);
        customTimeBar = (CustomTimeBar) playerView.findViewById(R.id.exo_progress);
        progressBar = (ProgressBar) playerView.findViewById(R.id.progress_bar);
        playSmallButton = (ImageButton) playerView.findViewById(R.id.small_play);
        pauseSmallButton = (ImageButton) playerView.findViewById(R.id.small_pause);
        smallPlayPauseContainer = (LinearLayout) playerView.findViewById(R.id.play_pause_container);
        smallProgressBar = (ProgressBar) playerView.findViewById(R.id.small_progress);
        fullscreenButton.setOnClickListener(this);
        optionsMenuButton.setOnClickListener(this);
        playerView.findViewById(R.id.cast_image_container).setOnClickListener(this);
        playSmallButton.setOnClickListener(playPauseClickLisner);
        pauseSmallButton.setOnClickListener(playPauseClickLisner);
    }

    public void createMenuView() {
        View menuPlaceholder = playerView.findViewById(R.id.exo_menu_placeholder);
        ViewGroup parent = ((ViewGroup) menuPlaceholder.getParent());
        this.optionsMenuLayer = new OptionsMenuLayer(context, parent);
        parent.addView(optionsMenuLayer, menuPlaceholder.getLayoutParams());
        optionsMenuLayer.setCallback(optionsMenuCallBack);
    }

    public void initControlsMap() {
        controlsMap = new HashMap<>();
        hiddenViews = new LinkedHashSet<>();
        controlsMap.put(Controls.PLAY_LARGE, controlsView);
        controlsMap.put(Controls.PLAY, smallPlayPauseContainer);
        controlsMap.put(Controls.FULLSCREEN, fullscreenButton);
        controlsMap.put(Controls.MENU, optionsMenuButton);
        controlsMap.put(Controls.SEEKBAR, playerView.findViewById(R.id.exo_progress));
        controlsMap.put(Controls.TOP_CHROME, topBar);
        controlsMap.put(Controls.BOTTOM_CHROME, bottomBar);
        controlsMap.put(Controls.TIME, playerView.findViewById(R.id.time_components));
    }

    public void configView(boolean isVideo, boolean isLive, boolean hasCast) {
        this.isLive = isLive;
        this.isVideo = isVideo;
        if (isVideo) {
            playerView.setControllerHideOnTouch(true);
            playerView.setControllerShowTimeoutMs(2 * 1000);
            topBar.setVisibility(View.VISIBLE);
            smallPlayPauseContainer.setVisibility(View.GONE);
            optionsMenuButton.setVisibility(this.hasMenu ? View.VISIBLE : View.GONE);
            if (isLive) {
                castButton.setVisibility(View.GONE);
                progressControls.setVisibility(View.INVISIBLE);
                liveButton.setVisibility(View.VISIBLE);
            } else {
                castButton.setVisibility(hasCast ? View.VISIBLE : View.GONE);
                progressControls.setVisibility(View.VISIBLE);
                liveButton.setVisibility(View.GONE);
            }
        } else {
            playerView.setControllerHideOnTouch(false);
            playerView.setControllerShowTimeoutMs(-1);
            topBar.setVisibility(View.GONE);
            fullscreenButton.setVisibility(View.GONE);
            smallPlayPauseContainer.setVisibility(View.VISIBLE);
            controlsView.setVisibility(View.GONE);
            optionsMenuButton.setVisibility(View.GONE);
            if (isLive) {
                progressControls.setVisibility(View.INVISIBLE);
            } else {
                progressControls.setVisibility(View.VISIBLE);
            }
            castButton.setVisibility(View.GONE);
        }
        postConfigUi();
    }

    private OptionsMenuLayer.OptionsMenuCallback optionsMenuCallBack = new OptionsMenuLayer.OptionsMenuCallback(


    ) {
        @Override
        public void onTouchHD() {
            if (outputSheetDialog == null) return;
            outputSheetDialog.show();
        }

        @Override
        public void onTouchCaptions() {
            if (captionsSheetDialog == null) return;
            captionsSheetDialog.show();
        }

        @Override
        public void onTouchSpeed() {
            if (speedSheetDialog == null) return;
            speedSheetDialog.show();
        }

        @Override
        public void onMenuDismiss() {
            if (menuWasPlaying) {
                player.setPlayWhenReady(true);
                playerView.hideController();
            } else {
                playerView.showController();
            }
            if (isFullscreen) {
                ((Activity) context).getWindow().getDecorView().setSystemUiVisibility(mFullScreenFlags);
            }
        }
    };

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.topbar_menu_button) {
            optionsMenuLayer.showMenu();
            menuWasPlaying = player.isPlayingAd() || (player.getPlayWhenReady() && (player.getPlaybackState() == Player.STATE_READY || player.getPlaybackState() == Player.STATE_BUFFERING));
            player.setPlayWhenReady(false);
            playerView.hideController();
        } else if (viewId == R.id.cast_image_container) {
            playerView.findViewById(R.id.cast_dummy_button).performClick();
        } else if (viewId == R.id.topbar_live_button) {

        } else if (viewId == R.id.fullscreen_button) {
            doToggleFullscreen();
        }
    }

    public void setPlayer(@NonNull SimpleExoPlayer simpleExoPlayer) {
        this.player = simpleExoPlayer;
        playerView.setPlayer(player);
    }

    public void setEnableControls(boolean flag) {
        playerView.setUseController(flag);
    }

    public void setVideoTitle(String title) {
        videoTitle.setText(title);
    }

    public void configureSubTitle(SambaMedia.CaptionsConfig captionsConfig) {
        Typeface typeface = Typeface.create((Typeface) null, NORMAL);
        CaptionStyleCompat captionStyleCompat = new CaptionStyleCompat(captionsConfig.color, 0, 0, EDGE_TYPE_NONE, 0, typeface);
        playerView.getSubtitleView().setStyle(captionStyleCompat);
        playerView.getSubtitleView().setFixedTextSize(COMPLEX_UNIT_SP, captionsConfig.size);
    }

    public void setupMenu(PlayerMediaSourceInterface playerMediaSource, Format selectedVideo, Format selectedSubtitle, boolean isAbrEnabled) {
        if (!isVideo) {
            outputSheetView = null;
            outputSheetDialog = null;
            captionSheetView = null;
            captionsSheetDialog = null;
            speedSheetDialog = null;
            speedSheetView = null;
            optionsMenuButton.setVisibility(View.GONE);
            return;
        }
        if (playerMediaSource.getVideoOutputsTracks() != null && playerMediaSource.getVideoOutputsTracks().length > 1) {
            initOutputMenu(playerMediaSource, selectedVideo, isAbrEnabled);
        } else {
            outputSheetView = null;
            outputSheetDialog = null;
        }
        if (playerMediaSource.getSubtitles() != null && playerMediaSource.getSubtitles().length > 1) {
            initCaptionMenu(playerMediaSource, selectedSubtitle);
        } else {
            captionSheetView = null;
            captionsSheetDialog = null;
        }
        if (!isLive) {
            initSpeedMenu();
        } else {
            speedSheetDialog = null;
            speedSheetView = null;
        }
        this.hasMenu = captionsSheetDialog != null || outputSheetDialog != null || speedSheetDialog != null;
        optionsMenuButton.setVisibility(this.hasMenu && !hiddenViews.contains(optionsMenuButton)? View.VISIBLE : View.GONE);
        optionsMenuLayer.setCaptionsButtonVisibility(captionsSheetDialog != null);
        optionsMenuLayer.setHdButtonVisibility(outputSheetDialog != null);
        optionsMenuLayer.setSpeedButtonVisibility(speedSheetDialog != null);
    }

    private void initCaptionMenu(final PlayerMediaSourceInterface playerMediaSource, Format currentCaption) {
        final TrackGroupArray captions = playerMediaSource.getSubtitles();
        captionSheetView = ((Activity) context).getLayoutInflater().inflate(R.layout.action_sheet, null);
        TextView title = (TextView) captionSheetView.findViewById(R.id.action_sheet_title);
        title.setText(context.getString(R.string.captions));
        final ListView menuList = (ListView) captionSheetView.findViewById(R.id.sheet_list);
        final CaptionsSheetAdapter adapter = new CaptionsSheetAdapter(context, captions);
        menuList.setAdapter(adapter);
        if (currentCaption != null) {
            for (int i = 0; i < captions.length; i++) {
                if (captions.get(i).getFormat(0) == currentCaption) {
                    adapter.currentIndex = i;
                }
            }
        } else {
            adapter.currentIndex = 0;
        }
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                closeCaptionMenu();
                TrackGroup format = captions.get(position);
                playerMediaSource.setSubtitle(format);
                menuList.smoothScrollToPosition(0);
                adapter.currentIndex = position;
            }
        });
        menuList.deferNotifyDataSetChanged();
        captionsSheetDialog = setupMenuDialog(captionSheetView);
    }

    private void initOutputMenu(final PlayerMediaSourceInterface playerMediaSource, Format currentOutput, boolean isAbrEnabled) {
        final TrackGroup outputs = playerMediaSource.getVideoOutputsTracks();
        outputSheetView = ((Activity) context).getLayoutInflater().inflate(R.layout.action_sheet, null);
        TextView title = (TextView) outputSheetView.findViewById(R.id.action_sheet_title);
        title.setText(context.getString(R.string.output));
        final ListView menuList = (ListView) outputSheetView.findViewById(R.id.sheet_list);
        final OutputSheetAdapter adapter = new OutputSheetAdapter(context, outputs, isAbrEnabled);
        menuList.setAdapter(adapter);
        if (currentOutput != null) {
            adapter.currentIndex = outputs.indexOf(currentOutput) + (isAbrEnabled ? 1 : 0);
        } else {
            adapter.currentIndex = 0;
        }
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                closeOutputMenu();
                Format format = (Format) adapter.getItem(position);
                playerMediaSource.setVideoOutputTrack(format);
                menuList.smoothScrollToPosition(0);
                adapter.currentIndex = position;
            }
        });
        menuList.deferNotifyDataSetChanged();
        outputSheetDialog = setupMenuDialog(outputSheetView);
    }

    private void initSpeedMenu() {
        speedSheetView = ((Activity) context).getLayoutInflater().inflate(R.layout.action_sheet, null);
        TextView title = (TextView) speedSheetView.findViewById(R.id.action_sheet_title);
        title.setText(context.getString(R.string.speed));
        final ListView menuList = (ListView) speedSheetView.findViewById(R.id.sheet_list);
        final float[] speeds = new float[]{0.25f, 0.5f, 1.0f, 1.5f, 2.0f};
        final float[] audioPitch = new float[]{0.25f, 0.5f, 1.0f, 1.5f, 2.0f};
        final SpeedSheetAdapter adapter = new SpeedSheetAdapter(context, speeds);
        menuList.setAdapter(adapter);
        for (int i = 0; i < speeds.length; i++) {
            if (player.getPlaybackParameters().speed == speeds[i]) {
                adapter.currentIndex = i;
            }
        }
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                closeSpeedMenu();
                player.setPlaybackParameters(new PlaybackParameters(speeds[position], audioPitch[2]));
                menuList.smoothScrollToPosition(0);
                adapter.currentIndex = position;

            }
        });
        menuList.deferNotifyDataSetChanged();
        speedSheetDialog = setupMenuDialog(speedSheetView);
    }


    /**
     * Sets the adapter for all menus
     *
     * @param view The view for the caption menu
     */
    public BottomSheetDialog setupMenuDialog(View view) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (optionsMenuLayer != null) optionsMenuLayer.hideMenu();
                if (menuWasPlaying) {
                    player.setPlayWhenReady(true);
                    playerView.hideController();
                } else {
                    playerView.showController();
                }
                if (isFullscreen) {
                    ((Activity) context).getWindow().getDecorView().setSystemUiVisibility(mFullScreenFlags);
                }
            }
        });
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(((View) view.getParent()));
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setPeekHeight(Integer.MAX_VALUE);
        return bottomSheetDialog;
    }

    /**
     * Closes the output menu.
     */
    public void closeOutputMenu() {
        outputSheetDialog.dismiss();
        if (optionsMenuLayer != null) optionsMenuLayer.hideMenu();
    }

    /**
     * Closes the caption menu.
     */
    public void closeCaptionMenu() {
        captionsSheetDialog.dismiss();
        if (optionsMenuLayer != null) optionsMenuLayer.hideMenu();
    }

    /**
     * Closes the speed menu.
     */
    public void closeSpeedMenu() {
        speedSheetDialog.dismiss();
        if (optionsMenuLayer != null) optionsMenuLayer.hideMenu();
    }

    public SimpleExoPlayerView getPlayerView() {
        return this.playerView;
    }


    private final int mFullScreenFlags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;


    /**
     * This is the layout of the container before fullscreen mode has been entered.
     * When we leave fullscreen mode, we restore the layout of the container to this layout.
     */
    private ViewGroup.LayoutParams originalContainerLayoutParams;

    private FullscreenCallback fullscreenCallback;

    public void setFullscreenCallback(FullscreenCallback fullscreenCallback) {
        this.fullscreenCallback = fullscreenCallback;
    }

    public void doToggleFullscreen() {
        setFullscreen(!isFullscreen);
    }


    public void setFullscreen(boolean newValue) {
        setFullscreen(newValue, false);
    }

    /**
     * Fullscreen mode will rotate to landscape mode, hide the action bar, hide the navigation bar,
     * hide the system tray, and make the video player take up the full size of the display.
     * The developer who is using this function must ensure the following:
     * <p>
     * <p>1) Inside the android manifest, the activity that uses the video player has the attribute
     * android:configChanges="orientation".
     * <p>
     * <p>2) Other views in the activity (or fragment) are
     * hidden (or made visible) when this method is called.
     *
     * @param isReverseLandscape Whether orientation is reverse landscape.
     */
    public void setFullscreen(boolean newValue, boolean isReverseLandscape) {
        if (this.isFullscreen == newValue && this.isReverseLandscape == isReverseLandscape) return;
        if (fullscreenCallback == null) return;
        final Activity activity = (Activity) context;
        if (newValue == false) {
            fullscreenCallback.onReturnFromFullscreen();
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            // Make the status bar and navigation bar visible again.
            activity.getWindow().getDecorView().setSystemUiVisibility(0);

            playerContainer.setLayoutParams(originalContainerLayoutParams);
            fullscreenButton.setImageResource(R.drawable.fullscreen);
            this.isFullscreen = newValue;
            this.isReverseLandscape = isReverseLandscape;
        } else {
            fullscreenCallback.onGoToFullscreen();
            activity.setRequestedOrientation(isReverseLandscape ?
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE :
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            activity.getWindow().getDecorView().setSystemUiVisibility(mFullScreenFlags);

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
                                if (!optionsMenuLayer.isVisible()) playerView.showController();
                            }
                            if (isFullscreen)
                                activity.getWindow().getDecorView().setSystemUiVisibility(mFullScreenFlags);
                        }
                    }
            );
            playerContainer.setLayoutParams(Util.getLayoutParamsBasedOnParent(playerContainer,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            fullscreenButton.setImageResource(R.drawable.fullscreen_exit);
            this.isFullscreen = newValue;
            this.isReverseLandscape = isReverseLandscape;
        }
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }

    public interface FullscreenCallback {

        /**
         * When triggered, the activity should hide any additional views.
         */
        void onGoToFullscreen();

        /**
         * When triggered, the activity should show any views that were hidden when the player
         * went to fullscreen.
         */
        void onReturnFromFullscreen();
    }

    public void destroyInternal() {
        destroyDialogs();
        setEnableControls(false);
        optionsMenuLayer.setCallback(null);
        fullscreenCallback = null;
        originalContainerLayoutParams = null;
        if (optionsMenuLayer != null)
            ((ViewGroup) optionsMenuLayer.getParent()).removeView(optionsMenuLayer);
        if (playerView != null)
            ((ViewGroup) playerView.getParent()).removeView(playerView);
        optionsMenuLayer = null;
        playerView = null;
        destroyCastPlayer();
    }

    private void destroyDialogs() {
        if (outputSheetView != null) {
            ((ListView) outputSheetView.findViewById(R.id.sheet_list)).setOnItemClickListener(null);
        }
        if (captionSheetView != null) {
            ((ListView) captionSheetView.findViewById(R.id.sheet_list)).setOnItemClickListener(null);
        }
        if (speedSheetView != null) {
            ((ListView) speedSheetView.findViewById(R.id.sheet_list)).setOnItemClickListener(null);
        }
        if (outputSheetDialog != null) outputSheetDialog.cancel();
        if (captionsSheetDialog != null) captionsSheetDialog.cancel();
        if (speedSheetDialog != null) speedSheetDialog.cancel();
        outputSheetView = null;
        captionSheetView = null;
        speedSheetView = null;
        outputSheetDialog = null;
        captionsSheetDialog = null;
        speedSheetDialog = null;
    }

    public void setThemeColor(int themeColor) {
        customTimeBar.setDefaultBarColor(themeColor);
        progressBar.getIndeterminateDrawable().mutate().setColorFilter(themeColor, PorterDuff.Mode.MULTIPLY);
        smallProgressBar.getIndeterminateDrawable().mutate().setColorFilter(themeColor, PorterDuff.Mode.MULTIPLY);
    }

    public void updatePlayPause(PlayPauseState playPauseState) {
        playSmallButton.setVisibility(playPauseState == PlayPauseState.Loading ? View.GONE : playPauseState == PlayPauseState.Pause ? View.VISIBLE : View.GONE);
        pauseSmallButton.setVisibility(playPauseState == PlayPauseState.Loading ? View.GONE : playPauseState == PlayPauseState.Playing ? View.VISIBLE : View.GONE);
        smallProgressBar.setVisibility(playPauseState == PlayPauseState.Loading ? View.VISIBLE : View.GONE);

        loadingView.setVisibility(playPauseState == PlayPauseState.Loading && isVideo ? View.VISIBLE : View.GONE);
        if(!hiddenViews.contains(controlsView))
            controlsView.setVisibility(playPauseState == PlayPauseState.Loading || !isVideo ? View.GONE : View.VISIBLE);
    }

    /**
     * Enables/Disables the specified controls.
     *
     * @param state    Whether to enable or disable the listed controls
     * @param controls Names from class <code>Controls</code>
     */
    public void setControlsVisible(final boolean state, final String... controls) {
        if(state) {
            if (controls == null || controls.length == 0) {
                for (Map.Entry<String, View> pair : controlsMap.entrySet()) {
                    hiddenViews.remove(pair.getValue());
                }
            } else {
                for (String control : controls) {
                    View view = controlsMap.get(control);
                    hiddenViews.remove(view);
                }
            }
            configView(this.isVideo, this.isVideo, this.hasCast);
        } else {
            if (controls == null || controls.length == 0) {
                for (Map.Entry<String, View> pair : controlsMap.entrySet()) {
                    hiddenViews.add(pair.getValue());
                }
            } else {
                for (String control : controls) {
                    View view = controlsMap.get(control);
                    hiddenViews.add(view);
                }
            }
            postConfigUi();
        }
    }

    private void postConfigUi(){
        for (View view : hiddenViews){
            boolean isSeekBar = view.equals(controlsMap.get(Controls.SEEKBAR));
            int visibility = isSeekBar ? View.INVISIBLE : View.GONE;
            view.setVisibility(visibility);
        }
    }

    public void setChromeColor(int color) {
        topBar.setBackgroundColor(color);
        bottomBar.setBackgroundColor(color);

		/*if (playbackControlRootView != null) {
			updateColors();
		}*/
    }

    public void setBackgroundColor(int color) {
        playerView.findViewById(R.id.exo_playback_layout).setBackgroundColor(color);
    }

    public void createCastPlayer(@NonNull final CastPlayer castPlayer, int themeColor, final ArrayList<SambaMedia.Caption> captions){
        this.sambaCastPlayer = castPlayer;
        if(castControlView == null) {
            castControlView = new PlaybackControlView(context);
            ((CustomTimeBar) castControlView.findViewById(R.id.exo_progress)).setDefaultBarColor(themeColor);
            this.playerContainer.addView(castControlView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        castControlView.setPlayer(sambaCastPlayer);
        castControlView.setShowTimeoutMs(-1);
        castControlView.findViewById(R.id.play_pause_container).setVisibility(View.GONE);
        castControlView.findViewById(R.id.topbar_live_button).setVisibility(View.GONE);
        castControlView.findViewById(R.id.fullscreen_button).setVisibility(View.GONE);
        if(captions != null && captions.size() > 1) {
            castControlView.findViewById(R.id.topbar_menu_button).setVisibility(View.VISIBLE);
            castControlView.findViewById(R.id.topbar_menu_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(castOptionsMenu == null) {
                        castOptionsMenu = new CastOptionsMenu(playerContainer, context, castPlayer, castControlView, captions);
                    }
                    castOptionsMenu.show();
                }
            });
        } else {
            castControlView.findViewById(R.id.topbar_menu_button).setVisibility(View.GONE);
        }
        castControlView.findViewById(R.id.exo_play).setVisibility(View.GONE);
        castControlView.findViewById(R.id.exo_pause).setVisibility(View.GONE);
        castControlView.findViewById(R.id.cast_image_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                castControlView.findViewById(R.id.cast_dummy_button).performClick();
            }
        });

        ((ImageButton) castControlView.findViewById(R.id.cast_image_container)).setImageDrawable(context.getResources().getDrawable(R.drawable.ic_cast_connected_24dp));
        ((TextView) castControlView.findViewById(R.id.video_title_text)).setText(videoTitle.getText());
    }

    public void destroyCastPlayer(){
        if(castControlView != null && playerContainer != null)
            playerContainer.removeView(castControlView);
        if(castOptionsMenu != null)
            castOptionsMenu.destroy();
        castOptionsMenu = null;
        castControlView = null;
        sambaCastPlayer = null;
    }

    public void showCast() {
        castControlView.setVisibility(View.VISIBLE);
        playerView.setVisibility(View.GONE);
    }

    public void hideCast() {
        if(castOptionsMenu != null)
            castOptionsMenu.hide();
        castControlView.setVisibility(View.GONE);
        playerView.setVisibility(View.VISIBLE);
    }
}
