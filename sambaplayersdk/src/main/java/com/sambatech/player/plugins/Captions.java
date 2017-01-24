package com.sambatech.player.plugins;

import android.support.annotation.NonNull;

import com.google.android.libraries.mediaframework.layeredvideo.SimpleVideoPlayer;
import com.google.android.libraries.mediaframework.layeredvideo.SubtitleLayer;
import com.sambatech.player.SambaPlayer;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.model.SambaMedia;

import java.util.ArrayList;

/**
 * Plugin responsible for managing captions.
 *
 * @author Leandro Zanol - 23/1/2017
 */
final class Captions extends SambaPlayerListener implements Plugin {

	private ArrayList<SambaMedia.Caption> _captionsRequest;

	@Override
	public void onLoad(@NonNull SambaPlayer player) {
		SambaMedia media = player.getMedia();

		if (media != null) {
			_captionsRequest = media.captions;

			if (_captionsRequest != null && _captionsRequest.size() > 0) {
				changeCaption(0);
				SambaEventBus.subscribe(this);
			}
		}

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

	public void changeCaption(int index) {

	}
}
