package com.sambatech.player.utils;

/**
 * Created by luizbyrro on 05/12/2017.
 */

public class Orientation {

    public static final int INVALID = -1;
    public static final int PORTRAIT = 0;
    public static final int LANDSCAPE = 270;
    public static final int REVERSE_PORTRAIT = 180;
    public static final int REVERSE_LANDSCAPE = 90;
    private static final int DEFAULT_THRESHOLD = 40;

    private int treshold;

    public Orientation() {
        this(DEFAULT_THRESHOLD);
    }

    public Orientation(int treshold) {
        this.treshold = treshold;
    }

    public int getMeasuredOrientation(int orientation) {
        if(orientation >= 360 + PORTRAIT - treshold && orientation < 360 || orientation >= 0 && orientation <= PORTRAIT + treshold)
            return PORTRAIT;
        else if(orientation >= LANDSCAPE - treshold && orientation <= LANDSCAPE + treshold)
            return LANDSCAPE;
        else if(orientation >= REVERSE_PORTRAIT - treshold && orientation <= REVERSE_PORTRAIT + treshold)
            return REVERSE_PORTRAIT;
        else if(orientation >= REVERSE_LANDSCAPE - treshold && orientation <= REVERSE_LANDSCAPE + treshold)
            return REVERSE_LANDSCAPE;
        else return INVALID;
    }
}
