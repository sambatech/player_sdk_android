package com.sambatech.player.plugins;

import com.sambatech.player.SambaPlayer;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;

/**
 * Plugin responsible for managing captions.
 *
 * @author Leandro Zanol - 23/1/2017
 */
public final class Captions implements Plugin {

	private SambaPlayerListener playerListener = new SambaPlayerListener() {
		@Override
		public void onProgress(SambaEvent event) {

		}
	};

	@Override
	public void onLoad(SambaPlayer player) {
		SambaEventBus.subscribe(playerListener);
	}

	@Override
	public void onDestroy() {
		SambaEventBus.unsubscribe(playerListener);
	}
}
