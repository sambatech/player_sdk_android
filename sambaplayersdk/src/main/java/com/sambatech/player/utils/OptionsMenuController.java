package com.sambatech.player.utils;

public interface OptionsMenuController {
    void setHdButtonVisibility(Boolean visibility);

    void setCaptionsButtonVisibility(Boolean visibility);

    void setSpeedButtonVisibility(Boolean visibility);

    void showMenu();

    void hideMenu();

    OptionsMenuLayer.OptionsMenuCallback getCallback();

    void setCallback(OptionsMenuLayer.OptionsMenuCallback callback);

    void bringToFront();

    boolean isVisible();
}
