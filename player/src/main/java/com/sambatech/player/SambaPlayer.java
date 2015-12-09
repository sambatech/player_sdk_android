package com.sambatech.player;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import com.google.android.libraries.mediaframework.exoplayerextensions.ExoplayerWrapper;
import com.google.android.libraries.mediaframework.exoplayerextensions.Video;
import com.google.android.libraries.mediaframework.layeredvideo.PlaybackControlLayer;
import com.google.android.libraries.mediaframework.layeredvideo.SimpleVideoPlayer;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.model.SambaMedia;

/**
 * Controller for SambaPlayer view.
 *
 * @author Leandro Zanol - 7/12/15
 */
public class SambaPlayer extends FrameLayout {

	private ImaPlayer playerIma;
	private SimpleVideoPlayer player;
	private SambaMedia media = new SambaMedia();
	private SambaPlayerListener listener;

	public SambaPlayer(Context context, AttributeSet attrs) {
		super(context, attrs);
		applyAttributes(getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SambaPlayer, 0, 0));
	}

	/**
	 * Defines/overwrites current media.
	 *
	 * @param media The media to be played.
	 */
	public void setMedia(SambaMedia media) {
		this.media = media;
	}

	public void play() {
		if (media.url == null || media.url.isEmpty())
			return;

		Video.VideoType videoType = Video.VideoType.OTHER;

		switch (media.type.toLowerCase()) {
			/*case "progressive":
				videoType = Video.VideoType.MP4;
				break;*/
			case "hls":
				videoType = Video.VideoType.HLS;
				break;
			case "dash":
				videoType = Video.VideoType.DASH;
				break;
		}

		playerIma = new ImaPlayer((Activity)getContext(), this,
				new Video(media.url, videoType),
				media.title);

		player = playerIma.getPlayer();

		player.setPlayCallback(new PlaybackControlLayer.PlayCallback() {
			@Override
			public void onPlay() {
				Log.i("evt", "play");
			}
		});

		player.addPlaybackListener(new ExoplayerWrapper.PlaybackListener() {
			@Override
			public void onStateChanged(boolean playWhenReady, int playbackState) {
				Log.i("evt", "state: " + playWhenReady + " " + playbackState);
			}

			@Override
			public void onError(Exception e) {
				Log.i("evt", "error", e);
			}

			@Override
			public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
				Log.i("evt", "size: " + width + ' ' + height + ' ' + unappliedRotationDegrees + ' ' + pixelWidthHeightRatio);
			}
		});
	}

	private void applyAttributes(TypedArray attrs) {
		media.url = attrs.getString(R.styleable.SambaPlayer_url);
		media.title = attrs.getString(R.styleable.SambaPlayer_title);
		attrs.recycle();
	}

	public void setListener(SambaPlayerListener listener) {
		this.listener = listener;
	}
}
