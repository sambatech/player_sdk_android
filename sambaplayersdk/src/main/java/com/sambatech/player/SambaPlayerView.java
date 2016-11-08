package com.sambatech.player;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.plugins.Plugin;
import com.sambatech.player.plugins.PluginManager;
import com.sambatech.player.plugins.PluginManagerImpl;

/**
 * View layer for SambaPlayer.
 *
 * @author Leandro Zanol - 22/1/16
 */
public class SambaPlayerView extends FrameLayout implements SambaPlayer, PluginManager {

	private SambaPlayer controllerReal = new SambaPlayerController(this);
	private SambaPlayer controller = SambaPlayerControllerNull.getInstance();
	private boolean autoFsMode;
	private boolean enableControls = true;
	private boolean pendingPlay;

	public SambaPlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		applyAttributes(getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SambaPlayerView, 0, 0));
	}

	public void setMedia(SambaMedia media) {
		controllerReal.setMedia(media);
		controllerReal.setAutoFullscreenMode(autoFsMode);
		controllerReal.setEnableControls(enableControls);

		PluginManagerImpl.getCurrentInstance().onLoad(this);
	}

	public SambaMedia getMedia() {
		return controllerReal.getMedia();
	}

	public void setEnableControls(boolean flag) {
		//Private attrs
		controller.setEnableControls(flag);
	}

	public void play() {
		controller.play();
		pendingPlay = true;
	}

	public void pause() {
		controller.pause();
	}

	public void stop() {
		controller.stop();
	}

	public void seek(float position) {
		controller.seek(position);
	}

	public void setFullscreen(boolean flag) {
		controller.setFullscreen(flag);
	}

	public boolean isFullscreen() {
		return controller.isFullscreen();
	}

	public void show() {
		controller.show();
	}

	public void hide() {
		controller.hide();
	}

	public void setAutoFullscreenMode(boolean flag) {
		controller.setAutoFullscreenMode(flag);
	}

	public float getCurrentTime() {
		return controller.getCurrentTime();
	}

	public float getDuration() {
		return controller.getDuration();
	}

	public boolean hasFinished() {
		return controller.hasFinished();
	}

	public boolean hasStarted() {
		return controller.hasStarted();
	}

	// TODO: alterar para "void changeOutput(int index)"
	// TODO: implementar "int totalOutputs()"

	public void changeOutput(SambaMedia.Output output) {
		controller.changeOutput(output);
	}

	public void destroy() {
		PluginManagerImpl.getCurrentInstance().onDestroy();
		controller.destroy();

		// disable controller
		controller = SambaPlayerControllerNull.getInstance();
		pendingPlay = false;

		// TODO: migrar para "reset()"
		// rearming player
		setMedia(getMedia());
	}

	public void notifyPluginLoaded(Plugin plugin) {
		// enable real controller
		controller = controllerReal;

		if (pendingPlay)
			play();
	}

	private void applyAttributes(TypedArray attrs) {
		try {
			autoFsMode = attrs.getBoolean(R.styleable.SambaPlayerView_autoFullscreenMode, false);
			enableControls = attrs.getBoolean(R.styleable.SambaPlayerView_enableControls, true);
		}
		finally {
			attrs.recycle();
		}
	}
}
