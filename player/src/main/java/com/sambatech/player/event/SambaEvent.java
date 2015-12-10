package com.sambatech.player.event;

/**
 * @author tmiranda - 9/12/15
 */
public class SambaEvent {

	private final SambaEventType type;
	private final Object data;
	private final Object[] dataAll;

	public SambaEvent(SambaEventType type) {
		this(type, new Object[]{});
	}

	public SambaEvent(SambaEventType type, Object ... data) {
		this.type = type;
		this.data = data.length > 0 ? data[0] : null;
		this.dataAll = data;
	}

	public SambaEventType getType() {
		return type;
	}

	public Object getData() {
		return data;
	}

	public Object[] getDataAll() {
		return dataAll;
	}
}

