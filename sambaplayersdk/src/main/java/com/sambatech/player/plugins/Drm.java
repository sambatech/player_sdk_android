package com.sambatech.player.plugins;

import com.sambatech.player.SambaPlayer;

/**
 * DRM licensing manager.
 *
 * @author Leandro Zanol - 14/9/2016
 */
public class Drm implements Plugin {

	public static final String WIDEVINE_LICENSE_SERVER = "https://drm-widevine-licensing.axtest.net/AcquireLicense";

	private static final String API_AUTH = "https://drm-quick-start.azurewebsites.net/api/authorization/";

	@Override
	public void onLoad(SambaPlayer player) {
		PluginManagerImpl.getInstance().notifyPluginLoaded(this);
	}

	@Override
	public void onDestroy() {

	}
}
