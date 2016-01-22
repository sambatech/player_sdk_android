package com.sambatech.player;

import android.view.View;

/**
 * Represents the player front-end API.
 *
 * @author Leandro Zanol - 28/12/15
 */
public interface SambaPlayer extends SambaPlayerBase {

	/**
	 * Returns the view layer associated with the player.
	 * @return Android View layer
	 */
	View getView();
}
