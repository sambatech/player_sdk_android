package com.sambatech.player;

import android.content.Context;
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

	private SambaPlayerBase playerController = SambaPlayerControllerNull.getInstance();

	public SambaPlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);

		/*applyAttributes(getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SambaPlayerView, 0, 0));

        if (!isInEditMode() && media.url != null)
            createPlayer();*/
	}

	@Override
	public void setMedia(SambaMedia media) {
		if (media == null)
			throw new IllegalArgumentException("Media data is null");

		SambaPlayerController.getInstance().setMedia(media);
	}

	@Override
	public SambaMedia getMedia() {
		return playerController.getMedia();
	}

	@Override
	public void play() {
		playerController.play();
	}

	@Override
	public void pause() {
		playerController.pause();
	}

	@Override
	public void stop() {
		playerController.stop();
	}

	@Override
	public void seek(float position) {
		playerController.seek(position);
	}

	@Override
	public void setFullscreen(boolean flag) {
		playerController.setFullscreen(flag);
	}

	@Override
	public boolean isFullscreen() {
		return playerController.isFullscreen();
	}

	@Override
	public void show() {
		playerController.show();
	}

	@Override
	public void hide() {
		playerController.hide();
	}

	@Override
	public float getCurrentTime() {
		return playerController.getCurrentTime();
	}

	@Override
	public float getDuration() {
		return playerController.getDuration();
	}

	@Override
	public boolean hasFinished() {
		return playerController.hasFinished();
	}

	@Override
	public boolean hasStarted() {
		return playerController.hasStarted();
	}

	@Override
	public void destroy() {
		playerController.destroy();
	}

	@Override
	public View getView() {
		return this;
	}
}
