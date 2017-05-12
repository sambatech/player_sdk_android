package com.sambatech.player.model;

import android.support.annotation.DrawableRes;

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

	/**
	 * The severity of the error:
	 *
	 * minor - dispatches error notification without showing the error screen
	 * info - shows error screen without destroying the player
	 * recoverable - display a retry button
	 * critical - player must be destroyed
	 */
	public enum Severity {
		minor,
		info,
		recoverable,
		critical
	}

	private int code;
	private String message;
	private Severity severity = Severity.minor;
	private Exception exception;
	private @DrawableRes int drawableRes;

	static {
		invalidUrl.setValues(1, "Invalid URL format", Severity.critical);
		emptyUrl.setValues(2, "Missing URL for the specified media", Severity.critical);
		rootedDevice.setValues(3, "Specified media cannot play on a rooted device", Severity.critical);
		unknown.setValues(-1, "Unknown exception");
	}

	/**
	 * The exception related code
	 * @return The exception code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * The exception related message
	 * @return The exception message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * The severity of the error
	 * @return The error severity
	 */
	public Severity getSeverity() {
		return severity;
	}

	/**
	 * The exception related exception
	 * @return The exception exception
	 */
	public Exception getException() {
		return exception;
	}

	public @DrawableRes int getDrawableRes() {
		return drawableRes;
	}

	/**
	 * Replaces default exception message for current instance.
	 * @param code The exception code
	 * @param message The message to be replaced
	 * @param severity The severity of the error
	 * @param exception The exception related exception
	 * @param drawableRes A custom error image to show with the message
	 * @return The reference to itself
	 */
	public SambaPlayerError setValues(int code, String message, Severity severity, Exception exception, @DrawableRes int drawableRes) {
		this.code = code;
		this.message = message;
		this.severity = severity;
		this.exception = exception;
		this.drawableRes = drawableRes;
		return this;
	}

	/**
	 * Replaces default exception message for current instance.
	 * @param code The exception code
	 * @param message The message to be replaced
	 * @param severity The severity of the error
	 * @param exception The exception related exception
	 * @return The reference to itself
	 */
	public SambaPlayerError setValues(int code, String message, Severity severity, Exception exception) {
		return setValues(code, message, severity, exception, 0);
	}

	/**
	 * Replaces default exception message for current instance.
	 * @param code The exception code
	 * @param message The message to be replaced
	 * @param severity The severity of the error
	 * @return The same instance reference
	 */
	public SambaPlayerError setValues(int code, String message, Severity severity) {
		return setValues(code, message, severity, null);
	}

	/**
	 * Replaces default exception message for current instance.
	 * @param code The exception code
	 * @param message The message to be replaced
	 * @return The same instance reference
	 */
	public SambaPlayerError setValues(int code, String message) {
		return setValues(code, message, Severity.minor);
	}

	@Override
	public String toString() {
		return message;
	}
}
