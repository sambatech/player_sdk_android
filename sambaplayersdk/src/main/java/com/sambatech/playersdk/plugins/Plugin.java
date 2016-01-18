package com.sambatech.playersdk.plugins;

import com.sambatech.playersdk.SambaPlayer;

/**
 * Represents a SambaPlayer plugin.
 *
 * @author Leandro Zanol - 12/01/2016
 */
public interface Plugin {

	/**
	 * Dispatched when player gets loaded.
	 *
	 * @param player Instance of SambaPlayer
	 */
	void onLoad(SambaPlayer player);

	/**
	 * Dispatched when player gets destroyed.
	 */
	void onDestroy();
}
