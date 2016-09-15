package com.sambatech.player.plugins;

import com.sambatech.player.SambaPlayer;

/**
 * Represents a SambaPlayer internal functionality.
 *
 * @author Leandro Zanol - 15/09/2016
 */
public interface PluginManager {

	/**
	 * Notifies plugin load to player.
	 */
	void notifyPluginLoaded(Plugin plugin);
}
