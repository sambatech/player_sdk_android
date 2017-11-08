package com.google.android.libraries.mediaframework.layeredvideo;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.libraries.mediaframework.R;

/**
 * Created by luizbyrro on 08/11/17.
 */

public class OptionsMenuLayer implements Layer, View.OnClickListener, OptionsMenuController{

    private FrameLayout view;
    private LinearLayout hdButton;
    private LinearLayout captionsButton;
    private LinearLayout speedButton;
    private ImageButton closeButton;
    private OptionsMenuCallback callback;
    private LayerManager layerManager;


    @Override
    public FrameLayout createView(LayerManager layerManager) {
        LayoutInflater inflater = layerManager.getActivity().getLayoutInflater();
        this.layerManager = layerManager;
        view = (FrameLayout) inflater.inflate(R.layout.menu_options_layer, null);
        hdButton = (LinearLayout) view.findViewById(R.id.quality_button);
        captionsButton = (LinearLayout) view.findViewById(R.id.subtitle_button);
        speedButton = (LinearLayout) view.findViewById(R.id.speed_button);
        closeButton = (ImageButton) view.findViewById(R.id.close_menu_button);
        hdButton.setOnClickListener(this);
        captionsButton.setOnClickListener(this);
        speedButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);
        view.setVisibility(View.GONE);
        speedButton.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onLayerDisplayed(LayerManager layerManager) {

    }

    @Override
    public void onClick(View v) {
        if (callback == null) return;
        int i = v.getId();
        if (i == R.id.quality_button) {
            callback.onTouchHD();
        } else if (i == R.id.subtitle_button) {
            callback.onTouchCaptions();
        } else if (i == R.id.speed_button) {
            callback.onTouchSpeed();
        } else if (i == R.id.close_menu_button) {
            hideMenu();
        }
    }

    @Override
    public void setHdButtonVisibility(Boolean visibility){
        if (hdButton == null) return;
        hdButton.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setCaptionsButtonVisibility(Boolean visibility){
        if (captionsButton == null) return;
        captionsButton.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setSpeedButtonVisibility(Boolean visibility){
        if (speedButton == null) return;
        speedButton.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showMenu() {
        view.setVisibility(View.VISIBLE);
        layerManager.getContainer().bringChildToFront(view);
    }

    @Override
    public void hideMenu() {
        view.setVisibility(View.GONE);
    }

    @Override
    public OptionsMenuCallback getCallback() {
        return callback;
    }

    @Override
    public void setCallback(OptionsMenuCallback callback) {
        this.callback = callback;
    }

    public interface OptionsMenuCallback {
        void onTouchHD();
        void onTouchCaptions();
        void onTouchSpeed();
    }
}

