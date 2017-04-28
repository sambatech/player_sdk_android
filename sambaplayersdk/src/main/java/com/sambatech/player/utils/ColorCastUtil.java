package com.sambatech.player.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.ContextThemeWrapper;

/**
 * Created by andrei on 30/09/16.
 */

public class ColorCastUtil {

    public static Drawable getMediaRouteButtonDrawable(Context context) {
        Context castContext = new ContextThemeWrapper(context, android.support.v7.mediarouter.R.style.Theme_MediaRouter);

        Drawable drawable = null;
        TypedArray a = castContext.obtainStyledAttributes(null,
                android.support.v7.mediarouter.R.styleable.MediaRouteButton, android.support.v7.mediarouter.R.attr.mediaRouteButtonStyle, 0);
        drawable = a.getDrawable(
                android.support.v7.mediarouter.R.styleable.MediaRouteButton_externalRouteEnabledDrawable);
        a.recycle();

        return drawable;
    }
}