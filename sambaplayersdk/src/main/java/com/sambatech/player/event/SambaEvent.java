package com.sambatech.player.event;

/**
 * @author tmiranda - 9/12/15
 */
public class SambaEvent {

	private final SambaEventType type;
	private final Object data;
	private final Object[] dataAll;

	/**
	 * SambaEvent default constructor
	 * @param type {@link SambaPlayerListener.EventType}
	 */
	public SambaEvent(SambaEventType type) {
		this(type, new Object[]{});
	}

	/**
	 * SambaEvent constructor
	 * @param type {@link SambaPlayerListener.EventType}
	 * @param data An data that can be passed in the event
	 */
	public SambaEvent(SambaEventType type, Object ... data) {
		this.type = type;
		this.data = data.length > 0 ? data[0] : null;
		this.dataAll = data;
	}


	/**
	 * Get the current event type
	 * @return {@link SambaPlayerListener.EventType}
	 */
	public SambaEventType getType() {
		return type;
	}

	/**
	 * Get the current data
	 * @return Object
	 */
	public Object getData() {
		return data;
	}

	/**
	 * Get all the data
	 * @return array of objects
	 */
	public Object[] getDataAll() {
		return dataAll;
	}
}

