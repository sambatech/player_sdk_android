package com.sambatech.player.plugins;

import com.sambatech.player.SambaPlayer;

/**
 * Responsible for managing plugins lifecycle.
 *
 * @author Leandro Zanol - 12/01/2016
 */
public class PluginManagerImpl implements Plugin, PluginManager {

	private static PluginManagerImpl currentInstance = new PluginManagerImpl();

	private Plugin[] plugins;
	private PluginManager player;
	private int pluginsLoaded;

	private PluginManagerImpl() {}

	public static PluginManagerImpl getCurrentInstance() {
		return currentInstance;
	}

	public void onLoad(SambaPlayer player) {
		this.player = (PluginManager)player;
		pluginsLoaded = 0;

		plugins = new Plugin[] {
				new ImaWrapper(),
				new Tracking(),
				new Drm()
		};

		for (Plugin plugin : plugins)
			plugin.onLoad(player);
	}

	public void onDestroy() {
		if (plugins == null) return;

		for (Plugin plugin : plugins)
			plugin.onDestroy();

		plugins = null;
	}

	public void notifyPluginLoaded(Plugin plugin) {
		if (++pluginsLoaded == plugins.length)
			player.notifyPluginLoaded(this);
	}
}
