package com.sambatech.player.event;

import net.engio.mbassy.bus.MBassador;

/**
 * Created by tmiranda on 09/12/15.
 */
public class SambaEventBus {

    private static MBassador eventBus = new MBassador();

    public static MBassador getEventBus() {
        return eventBus;
    }

}
