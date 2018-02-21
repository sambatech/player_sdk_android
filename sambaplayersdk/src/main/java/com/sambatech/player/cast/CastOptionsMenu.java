package com.sambatech.player.cast;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.sambatech.player.R;
import com.sambatech.player.adapter.CastCaptionsAdapter;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.utils.OptionsMenuLayer;

import java.util.ArrayList;

/**
 * Created by luizbyrro on 20/02/2018.
 */

public class CastOptionsMenu implements OptionsMenuLayer.OptionsMenuCallback {

    private Context context;
    private FrameLayout container;
    private OptionsMenuLayer optionsMenuLayer;
    private CastPlayer castPlayer;
    private PlaybackControlView playbackControlView;

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

    private View outputSheetView;
    private View captionSheetView;
    private View speedSheetView;

    public CastOptionsMenu(FrameLayout container, Context context, CastPlayer castPlayer, PlaybackControlView playbackControlView, ArrayList<SambaMedia.Caption> captions) {
        this.container = container;
        this.context = context;
        this.castPlayer = castPlayer;
        this.playbackControlView = playbackControlView;
        this.optionsMenuLayer = new OptionsMenuLayer(context, container);
        this.container.addView(optionsMenuLayer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.container.bringChildToFront(optionsMenuLayer);
        optionsMenuLayer.setCallback(this);
        optionsMenuLayer.setHdButtonVisibility(false);
        optionsMenuLayer.setSpeedButtonVisibility(false);
        optionsMenuLayer.setCaptionsButtonVisibility(false);
        if (captions != null || captions.size() > 0) {
            optionsMenuLayer.setCaptionsButtonVisibility(true);
            initCaptionMenu(captions);
        }
    }

    public void show() {
        if (playbackControlView != null)
            playbackControlView.hide();
        if (optionsMenuLayer != null)
            optionsMenuLayer.showMenu();
    }

    public void hide() {
        if (playbackControlView != null)
            playbackControlView.show();
        if (optionsMenuLayer != null)
            optionsMenuLayer.hideMenu();
    }

    private void initCaptionMenu(final ArrayList<SambaMedia.Caption> captions) {
        captionSheetView = ((Activity) context).getLayoutInflater().inflate(R.layout.action_sheet, null);
        TextView title = (TextView) captionSheetView.findViewById(R.id.action_sheet_title);
        title.setText(context.getString(R.string.captions));
        final ListView menuList = (ListView) captionSheetView.findViewById(R.id.sheet_list);
        final CastCaptionsAdapter adapter = new CastCaptionsAdapter(captions, context);
        menuList.setAdapter(adapter);
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                hide();
                captionsSheetDialog.dismiss();
                adapter.currentIndex = position;
                SambaMedia.Caption caption = captions.get(position);
                castPlayer.sendSubtitle(caption.language);
                menuList.smoothScrollToPosition(0);
                adapter.currentIndex = position;
            }
        });
        menuList.deferNotifyDataSetChanged();
        captionsSheetDialog = setupMenuDialog(captionSheetView);
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
                hide();
            }
        });
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(((View) view.getParent()));
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setPeekHeight(Integer.MAX_VALUE);
        return bottomSheetDialog;
    }

    @Override
    public void onTouchHD() {

    }

    @Override
    public void onTouchCaptions() {
        if (captionsSheetDialog == null) return;
        captionsSheetDialog.show();
    }

    @Override
    public void onTouchSpeed() {

    }

    @Override
    public void onMenuDismiss() {
        hide();
    }

    public void destroy() {
        container.removeView(optionsMenuLayer);
        optionsMenuLayer = null;
        outputSheetDialog = null;
        captionsSheetDialog = null;
        speedSheetDialog = null;
        outputSheetView = null;
        captionSheetView = null;
        speedSheetView = null;
    }
}
