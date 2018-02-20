package com.sambatech.player.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.sambatech.player.R;

/**
 * Created by luizbyrro on 08/11/17.
 */

public class OptionsMenuLayer extends FrameLayout implements View.OnClickListener, OptionsMenuController{

    private LinearLayout hdButton;
    private LinearLayout captionsButton;
    private LinearLayout speedButton;
    private ImageButton closeButton;
    private ViewGroup parentView;
    private OptionsMenuCallback callback;


    public OptionsMenuLayer(Context context, ViewGroup parentView) {
        this(context, parentView, null);
    }

    public OptionsMenuLayer(Context context, ViewGroup parentView, AttributeSet attrs) {
        this(context, parentView, attrs, 0);
    }

    public OptionsMenuLayer(Context context, ViewGroup parentView, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.menu_options_layer, this);
        this.parentView = parentView;
        hdButton = (LinearLayout) findViewById(R.id.quality_button);
        captionsButton = (LinearLayout) findViewById(R.id.subtitle_button);
        speedButton = (LinearLayout) findViewById(R.id.speed_button);
        closeButton = (ImageButton) findViewById(R.id.close_menu_button);
        hdButton.setOnClickListener(this);
        captionsButton.setOnClickListener(this);
        speedButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);
        setVisibility(View.GONE);
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
            callback.onMenuDismiss();
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
        setVisibility(View.VISIBLE);
        bringToFront();
    }

    @Override
    public void hideMenu() {
        setVisibility(View.GONE);
    }

    @Override
    public OptionsMenuCallback getCallback() {
        return callback;
    }

    @Override
    public void setCallback(OptionsMenuCallback callback) {
        this.callback = callback;
    }

    @Override
    public void bringToFront() {
        parentView.bringChildToFront(this);
    }

    @Override
    public boolean isVisible() {
        return getVisibility() == View.VISIBLE;
    }

    public interface OptionsMenuCallback {
        void onTouchHD();
        void onTouchCaptions();
        void onTouchSpeed();
        void onMenuDismiss();
    }
}

