package com.google.android.libraries.mediaframework.layeredvideo;

public interface OptionsMenuController {
    void setHdButtonVisibility(Boolean visibility);

    void setCaptionsButtonVisibility(Boolean visibility);

    void setSpeedButtonVisibility(Boolean visibility);

    void showMenu();

    void hideMenu();

    OptionsMenuLayer.OptionsMenuCallback getCallback();

    void setCallback(OptionsMenuLayer.OptionsMenuCallback callback);
}
