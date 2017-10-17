package com.google.android.libraries.mediaframework.layeredvideo;

/**
 * Represents all controls available.
 *
 * NOTE: It could not be an <code>enum</code> because it's needed for inheritance.
 *
 * @author Leandro Zanol - 17/10/17
 */
public class Controls {
	public static final Controls PLAY = new Controls();
	public static final Controls PLAY_LARGE = new Controls();
	public static final Controls FULLSCREEN = new Controls();
	public static final Controls OUTPUT = new Controls();
	public static final Controls CAPTION = new Controls();
	public static final Controls SEEKBAR = new Controls();
	public static final Controls TOP_CHROME = new Controls();
	public static final Controls BOTTOM_CHROME = new Controls();
	public static final Controls TIME = new Controls();
}
