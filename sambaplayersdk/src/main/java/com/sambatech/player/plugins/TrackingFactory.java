package com.sambatech.player.plugins;

class TrackingFactory {

    public static Tracking getInstance(boolean isLive) {
        if (isLive) {
            return new TrackingLive();
        } else {
            return new TrackingVOD();
        }
    }

}
