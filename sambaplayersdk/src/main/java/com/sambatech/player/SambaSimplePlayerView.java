package com.sambatech.player;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.CaptionStyleCompat;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.sambatech.player.adapter.CaptionsSheetAdapter;
import com.sambatech.player.adapter.OutputSheetAdapter;
import com.sambatech.player.mediasource.PlayerMediaSource;
import com.sambatech.player.mediasource.PlayerMediaSourceInterface;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.utils.OptionsMenuLayer;

import static android.graphics.Typeface.NORMAL;
import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static android.view.accessibility.CaptioningManager.CaptionStyle.EDGE_TYPE_NONE;

/**
 * Created by luizbyrro on 30/11/2017.
 */

public class SambaSimplePlayerView implements View.OnClickListener{

    private Context context;
    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;
    private ImageButton optionsButton;
    private TextView videoTitle;
    private OptionsMenuLayer optionsMenuLayer;

    private boolean isFullscreen;


    public SambaSimplePlayerView(Context context) {
        this.context = context;
        playerView = new SimpleExoPlayerView(context);
        bindMethods();
        createMenuView();
    }

    public void bindMethods(){
        videoTitle = (TextView) playerView.findViewById(R.id.video_title_text);
        optionsButton = (ImageButton) playerView.findViewById(R.id.options_menu);
        optionsButton.setOnClickListener(this);
    }

    public void createMenuView() {
        View menuPlaceholder = playerView.findViewById(R.id.exo_menu_placeholder);
        ViewGroup parent = ((ViewGroup) menuPlaceholder.getParent());
        this.optionsMenuLayer = new OptionsMenuLayer(context, parent);
        parent.addView(optionsMenuLayer, menuPlaceholder.getLayoutParams());
        optionsMenuLayer.setCallback(optionsMenuCallBack);
    }

    private OptionsMenuLayer.OptionsMenuCallback optionsMenuCallBack = new OptionsMenuLayer.OptionsMenuCallback() {
        @Override
        public void onTouchHD() {
            if (outputSheetDialog == null) return;
            outputSheetDialog.show();
        }

        @Override
        public void onTouchCaptions() {
            if(captionsSheetDialog == null) return;
            captionsSheetDialog.show();
        }

        @Override
        public void onTouchSpeed() {

        }

        @Override
        public void onMenuDismiss() {
            if (menuWasPlaying) {
                player.setPlayWhenReady(true);
                setEnableControls(false);
            } else {
                setEnableControls(true);
            }
            if (isFullscreen) {
                //getLayerManager().getActivity().getWindow().getDecorView().setSystemUiVisibility(mFullScreenFlags);
            }
        }
    };

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.options_menu) {
            optionsMenuLayer.showMenu();
            menuWasPlaying = player.isPlayingAd() || player.getPlayWhenReady();
            player.setPlayWhenReady(false);
            playerView.hideController();
        }
    }

    public void setPlayer(@NonNull SimpleExoPlayer simpleExoPlayer){
        this.player = simpleExoPlayer;
        playerView.setPlayer(player);
    }

    public void setEnableControls(boolean flag) {
        if (flag)
            playerView.hideController();
        else
            playerView.showController();
    }

    public void hide() {

    }

    public void show() {

    }

    public void setVideoTitle(String title){
        videoTitle.setText(title);
    }

    public void configureSubTitle(SambaMedia.CaptionsConfig captionsConfig){
        Typeface typeface = Typeface.create((Typeface) null, NORMAL);
        CaptionStyleCompat captionStyleCompat = new CaptionStyleCompat(captionsConfig.color, 0, 0, EDGE_TYPE_NONE, 0,  typeface);
        playerView.getSubtitleView().setStyle(captionStyleCompat);
        playerView.getSubtitleView().setFixedTextSize(COMPLEX_UNIT_SP, captionsConfig.size);
    }

    public void attachPlayerToView(FrameLayout view){
        view.addView(playerView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        view.setBackgroundColor(Color.BLACK);
    }


    private View outputSheetView;
    private View captionSheetView;

    public void setupMenu(PlayerMediaSourceInterface playerMediaSource) {
        if(outputSheetView == null){
            initOutputMenu(playerMediaSource);
        }
        if(captionSheetView == null){
            initCaptionMenu(playerMediaSource);
        }
    }

    private void initCaptionMenu(final PlayerMediaSourceInterface playerMediaSource) {
        final TrackGroupArray captions = playerMediaSource.getSubtitles();
        captionSheetView = ((Activity) context).getLayoutInflater().inflate(R.layout.action_sheet, null);
        TextView title = (TextView) captionSheetView.findViewById(R.id.action_sheet_title);
        title.setText(context.getString(R.string.captions));
        final ListView menuList = (ListView) captionSheetView.findViewById(R.id.sheet_list);
        final CaptionsSheetAdapter adapter = new CaptionsSheetAdapter(context, captions);
        menuList.setAdapter(adapter);
        adapter.currentIndex = 0;
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
        setCaptionMenu(captionSheetView);
    }

    private void initOutputMenu(final PlayerMediaSourceInterface playerMediaSource) {
        final TrackGroup outputs = playerMediaSource.getVideoOutputsTracks();
        outputSheetView = ((Activity) context).getLayoutInflater().inflate(R.layout.action_sheet, null);
        TextView title = (TextView) outputSheetView.findViewById(R.id.action_sheet_title);
        title.setText(context.getString(R.string.output));

        final ListView menuList = (ListView) outputSheetView.findViewById(R.id.sheet_list);
        final OutputSheetAdapter adapter = new OutputSheetAdapter(context, outputs);
        menuList.setAdapter(adapter);
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                closeOutputMenu();
                playerMediaSource.setVideoOutputTrack(outputs.getFormat(position));
                menuList.smoothScrollToPosition(0);
                adapter.currentIndex = position;
            }
        });
        menuList.deferNotifyDataSetChanged();
        setOutputMenu(outputSheetView);
    }

    /**
     * The output menu modalsheet.
     */
    private BottomSheetDialog outputSheetDialog;

    /**
     * Captions menu modalsheet
     */
    private BottomSheetDialog captionsSheetDialog;

    /**
     * Indicates playback last state before the output menu has open.
     */
    private boolean menuWasPlaying;

    /**
     * Sets the adapter for the output menu.
     * @param view The view for the output menu.
     */
    public void setOutputMenu(View view) {
        outputSheetDialog = new BottomSheetDialog(context);
        outputSheetDialog.setContentView(view);
        outputSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
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
                    //getLayerManager().getActivity().getWindow().getDecorView().setSystemUiVisibility(mFullScreenFlags);
                }
            }
        });
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(((View) view.getParent()));
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setPeekHeight(Integer.MAX_VALUE);
    }

    /**
     * Sets the adapter for the caption menu
     * @param view The view for the caption menu
     */
    public void setCaptionMenu(View view) {
        captionsSheetDialog = new BottomSheetDialog(context);
        captionsSheetDialog.setContentView(view);
        captionsSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
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
                    //getLayerManager().getActivity().getWindow().getDecorView().setSystemUiVisibility(mFullScreenFlags);
                }
            }
        });
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(((View) view.getParent()));
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setPeekHeight(Integer.MAX_VALUE);
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
}
