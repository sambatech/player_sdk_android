package com.sambatech.player;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import com.google.android.libraries.mediaframework.exoplayerextensions.Video;
import com.google.android.libraries.mediaframework.layeredvideo.SimpleVideoPlayer;
import com.sambatech.player.model.SambaMedia;

/**
 * Controller for SambaPlayer view.
 *
 * @author Leandro Zanol
 */
public class SambaPlayer extends FrameLayout {

	private ImaPlayer playerIma;
	private SimpleVideoPlayer player;
	private SambaMedia media = new SambaMedia();

	public SambaPlayer(Context context, AttributeSet attrs) {
		super(context, attrs);
		applyAttributes(getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SambaPlayer, 0, 0));
	}

	/**
	 * Defines/overwrites current media.
	 *
	 * @param media
	 */
	public void setMedia(SambaMedia media) {
		this.media = media;
	}

	public void play() {
		Log.i("asdf", media.url);
		if (media.url == null)
			return;

		Video.VideoType videoType = Video.VideoType.OTHER;

		switch (media.type.toLowerCase()) {
			case "progressive":
				videoType = Video.VideoType.OTHER;
				break;
			case "hls":
				videoType = Video.VideoType.HLS;
				break;
		}

		playerIma = new ImaPlayer((Activity)getContext(), this,
				new Video(media.url, videoType),
				media.title);

		player = playerIma.getPlayer();
	}

	private void applyAttributes(TypedArray attrs) {
		media.url = attrs.getString(R.styleable.SambaPlayer_url);
		media.title = attrs.getString(R.styleable.SambaPlayer_title);
		attrs.recycle();
	}
}
