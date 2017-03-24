package com.sambatech.player.event;

import com.google.android.gms.cast.framework.CastSession;

/**
 * @author Leandro Zanol on 3/24/17
 */

public interface SambaCastListener {

	/**
	 * Dispatched when a connection is established with Chromecast.
	 */
	void onConnected(CastSession castSession);

	/**
	 * Dispatched when a connection is close with Chromecast.
	 */
	void onDisconnected();
}
