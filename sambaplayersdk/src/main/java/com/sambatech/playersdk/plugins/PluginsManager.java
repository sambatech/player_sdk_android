package com.sambatech.playersdk.plugins;

import com.sambatech.playersdk.SambaPlayer;

/**
 * Responsible for managing plugins lifecycle.
 *
 * @author Leandro Zanol - 12/01/2016
 */
public class PluginsManager implements Plugin {

	private static final PluginsManager instance = new PluginsManager();

	private Plugin[] plugins;

	private PluginsManager() {}

	public static PluginsManager getInstance() {
		return instance;
	}

	public void initialize() {}

	public void onLoad(SambaPlayer player) {
		if (plugins != null)
			return;

		plugins = new Plugin[] {
				new Tracking()
		};

		for (Plugin plugin : plugins)
			plugin.onLoad(player);
	}

	public void onDestroy() {
		if (plugins == null)
			return;

		for (Plugin plugin : plugins)
			plugin.onDestroy();

		plugins = null;
	}
}
