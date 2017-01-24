package com.sambatech.player.plugins;

import android.support.annotation.NonNull;

import com.google.android.libraries.mediaframework.layeredvideo.SimpleVideoPlayer;
import com.google.android.libraries.mediaframework.layeredvideo.SubtitleLayer;
import com.sambatech.player.SambaPlayer;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;

/**
 * Plugin responsible for managing captions.
 *
 * @author Leandro Zanol - 23/1/2017
 */
final class Captions extends SambaPlayerListener implements Plugin {

	@Override
	public void onLoad(@NonNull SambaPlayer player) {
		SambaEventBus.subscribe(this);
		PluginManager.getInstance().notifyPluginLoaded(this);
	}

	@Override
	public void onInternalPlayerCreated(@NonNull SimpleVideoPlayer internalPlayer) {
		SubtitleLayer subtitleLayer = internalPlayer.getSubtitleLayer();

		if (subtitleLayer != null) {
			subtitleLayer.onText("asdofij");
		}
	}

	@Override
	public void onDestroy() {
		SambaEventBus.unsubscribe(this);
	}

	@Override
	public void onProgress(SambaEvent event) {

	}
}
