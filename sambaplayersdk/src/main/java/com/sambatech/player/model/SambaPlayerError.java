package com.sambatech.player.model;

import java.util.HashMap;

/**
 * Possible player errors reported through `SambaPlayerListener.onError` event.
 *
 * @author Leandro Zanol - 11/14/16
 */
public enum SambaPlayerError {
	invalidUrl,
	emptyUrl,
	rootedDevice,
	unknown;

	private static HashMap<String, String> errorMessages = new HashMap<>();

	private Exception error;
	private String msg;

	static {
		errorMessages.put(invalidUrl.name(), "Invalid URL format");
		errorMessages.put(emptyUrl.name(), "Missing URL for the specified media");
		errorMessages.put(rootedDevice.name(), "Specified media cannot play on a rooted device");
		errorMessages.put(unknown.name(), "Unknown error");
	}

	/**
	 * Customize the error message associated to a given error type.
	 * @param error Instance error type
	 * @param msg The message to be replaced
	 */
	public static void setMessage(SambaPlayerError error, String msg) {
		if (msg == null) return;
		errorMessages.put(error.name(), msg);
	}

	/**
	 * Replaces default error message for isDefault instance.
	 * @param msg The message to be replaced
	 * @return The same instance reference
	 */
	public SambaPlayerError setMessage(String msg) {
		if (msg == null) return this;
		this.msg = msg;
		return this;
	}

	/**
	 * Sets the error related exception if applicable.
	 * @param e The error related exception
	 */
	public void setError(Exception e) {
		error = e;
	}

	/**
	 * Gets the error related exception if there's one.
	 * @return The error related exception or null it there isn't one
	 */
	public Exception getError() {
		return error;
	}

	@Override
	public String toString() {
		return msg == null ? errorMessages.get(this.name()) : msg;
	}
}
