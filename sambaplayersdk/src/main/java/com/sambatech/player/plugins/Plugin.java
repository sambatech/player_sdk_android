package com.sambatech.player.plugins;

import android.support.annotation.NonNull;

import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.sambatech.player.SambaPlayer;

/**
 * Represents a SambaPlayer plugin.
 *
 * @author Leandro Zanol - 12/01/2016
 */
interface Plugin {

	/**
	 * Dispatched when player gets loaded.
	 *
	 * @param player Instance of SambaPlayer
	 */
	void onLoad(@NonNull SambaPlayer player);

	/**
	 * Dispatched when internal player (GMF) is created.
	 *
	 * @param internalPlayer Instance of internal player (GMF)
	 */
	void onInternalPlayerCreated(@NonNull SimpleExoPlayerView internalPlayer);

	/**
	 * Dispatched when player gets destroyed.
	 */
	void onDestroy();
}
