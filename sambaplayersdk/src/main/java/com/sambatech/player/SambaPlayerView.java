package com.sambatech.player;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.sambatech.player.model.SambaMedia;

/**
 * View layer for SambaPlayer.
 *
 * @author Leandro Zanol - 22/1/16
 */
public class SambaPlayerView extends FrameLayout implements SambaPlayer {

	private SambaPlayer controllerInstance = new SambaPlayerController(this);
	private SambaPlayer controller = SambaPlayerControllerNull.getInstance();
	private boolean autoFsMode;

	public SambaPlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		applyAttributes(getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SambaPlayerView, 0, 0));
	}

	@Override
	public void setMedia(SambaMedia media) {
		// enable controller
		controller = controllerInstance;
		controller.setMedia(media);
		controller.setAutoFullscreenMode(autoFsMode);
	}

	@Override
	public SambaMedia getMedia() {
		return controller.getMedia();
	}

	@Override
	public void play() {
		controller.play();
	}

	@Override
	public void pause() {
		controller.pause();
	}

	@Override
	public void stop() {
		controller.stop();
	}

	@Override
	public void seek(float position) {
		controller.seek(position);
	}

	@Override
	public void setFullscreen(boolean flag) {
		controller.setFullscreen(flag);
	}

	@Override
	public boolean isFullscreen() {
		return controller.isFullscreen();
	}

	@Override
	public void show() {
		controller.show();
	}

	@Override
	public void hide() {
		controller.hide();
	}

	@Override
	public void setAutoFullscreenMode(boolean flag) {
		controller.setAutoFullscreenMode(flag);
	}

	@Override
	public float getCurrentTime() {
		return controller.getCurrentTime();
	}

	@Override
	public float getDuration() {
		return controller.getDuration();
	}

	@Override
	public boolean hasFinished() {
		return controller.hasFinished();
	}

	@Override
	public boolean hasStarted() {
		return controller.hasStarted();
	}

	// TODO: alterar para "void changeOutput(int index)"
	// TODO: implementar "int totalOutputs()"

	@Override
	public void changeOutput(SambaMedia.Output output) {
		controller.changeOutput(output);
	}

	@Override
	public void destroy() {
		controller.destroy();
		// disable controller
		controller = SambaPlayerControllerNull.getInstance();
	}

	private void applyAttributes(TypedArray attrs) {
		try {
			autoFsMode = attrs.getBoolean(R.styleable.SambaPlayerView_autoFullscreenMode, false);
		}
		finally {
			attrs.recycle();
		}
	}
}
