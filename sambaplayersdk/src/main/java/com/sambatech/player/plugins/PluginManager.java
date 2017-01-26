package com.sambatech.player.plugins;

import android.support.annotation.NonNull;

import com.google.android.libraries.mediaframework.layeredvideo.SimpleVideoPlayer;
import com.sambatech.player.SambaPlayer;

/**
 * Responsible for managing plugins lifecycle.
 *
 * @author Leandro Zanol - 12/01/2016
 */
public class PluginManager implements Plugin {

	private static PluginManager instance = new PluginManager();

	private Plugin[] plugins;
	private SambaPlayer player;
	private int pluginsLoaded;
	private boolean isLoaded;
	private boolean pendingPlay;

	private PluginManager() {}

	public static PluginManager getInstance() {
		return instance;
	}

	public void onLoad(@NonNull SambaPlayer player) {
		this.player = player;
		pluginsLoaded = 0;

		plugins = new Plugin[] {
				new ImaWrapper(),
				new Tracking(),
				new Captions()
		};

		for (Plugin plugin : plugins)
			plugin.onLoad(player);
	}

	public void onInternalPlayerCreated(@NonNull SimpleVideoPlayer internalPlayer) {
		for (Plugin plugin : plugins)
			plugin.onInternalPlayerCreated(internalPlayer);
	}

	public void onDestroy() {
		if (plugins == null) return;

		for (Plugin plugin : plugins)
			plugin.onDestroy();

		plugins = null;
	}

	public boolean isLoaded() {
		return isLoaded;
	}

	public void setPendingPlay(boolean value) {
		pendingPlay = value;
	}

	public Plugin getPlugin(Class PluginRef) {
		for (Plugin plugin : plugins)
			if (plugin.getClass() == PluginRef)
				return plugin;

		return null;
		//return plugins.get(classRef);
	}

	/**
	 * Notifies plugin load to player.
	 */
	void notifyPluginLoaded(Plugin plugin) {
		if (++pluginsLoaded >= plugins.length) {
			isLoaded = true;

			if (pendingPlay) {
				pendingPlay = false;
				player.play();
			}
		}
	}
}
