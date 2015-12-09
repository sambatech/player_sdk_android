package com.sambatech.player.event;

/**
 * @author tmiranda - 9/12/15
 */
public class SambaEvent {

    private SambaEventType type;
    private Object data;

    public SambaEvent(SambaEventType type, Object data) {
        this.type = type;
        this.data = data;
    }

    public SambaEventType getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
}

