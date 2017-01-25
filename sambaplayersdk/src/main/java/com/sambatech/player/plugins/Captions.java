package com.sambatech.player.plugins;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.libraries.mediaframework.layeredvideo.SimpleVideoPlayer;
import com.google.android.libraries.mediaframework.layeredvideo.SubtitleLayer;
import com.sambatech.player.SambaPlayer;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.utils.Helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Plugin responsible for managing captions.
 *
 * @author Leandro Zanol - 23/1/2017
 */
final class Captions extends SambaPlayerListener implements Plugin {

	private @NonNull ArrayList<SambaMedia.Caption> _captionsRequest = new ArrayList<>();
	private SubtitleLayer _subtitleLayer;
	private SambaPlayer _player;
	private int _currentIndex = -1;
	private SambaMedia.Caption _currentCaption;
	private boolean _parsed;

	private static final class Caption {
		final int index;
		final float startTime;
		final float endTime;
		final String text;

		public Caption(int index, float startTime, float endTime, String text) {
			this.index = index;
			this.startTime = startTime;
			this.endTime = endTime;
			this.text = text;
		}
	}

	// when data available
	@Override
	public void onLoad(@NonNull SambaPlayer player) {
		_player = player;

		SambaMedia media = player.getMedia();

		try {
			if (media == null || media.captions == null
					|| media.captions.size() == 0) return;

			_captionsRequest = media.captions;
			//_config = media.captionsConfig

			SambaEventBus.subscribe(this);
		}
		finally {
			PluginManager.getInstance().notifyPluginLoaded(this);
		}
	}

	// when view available
	@Override
	public void onInternalPlayerCreated(@NonNull SimpleVideoPlayer internalPlayer) {
		_subtitleLayer = internalPlayer.getSubtitleLayer();

		int index = -1;

		// look for default caption
		while (++index < _captionsRequest.size() && !_captionsRequest.get(index).isDefault);

		changeCaption(index);
	}

	@Override
	public void onDestroy() {
		SambaEventBus.unsubscribe(this);
	}

	@Override
	public void onProgress(SambaEvent event) {
		if (_subtitleLayer == null) return;

		_subtitleLayer.onText(">> " + _player.getCurrentTime());
	}

	public void changeCaption(int index) {
		if (index == _currentIndex || index >= _captionsRequest.size())
			return;

		_currentIndex = index;
		// clean up
		_currentCaption = null;
		_subtitleLayer.onText("");

		SambaMedia.Caption caption = _captionsRequest.get(index);

		if (caption.url.isEmpty()) return;

		Helpers.requestUrl(caption.url, new Helpers.RequestCallback() {
			@Override
			public void onSuccess(String response) {
				Log.i("SambaPlayer", response.substring(1));
			}

			@Override
			public void onError(Exception e, String response) {
				Log.e("SambaPlayer", response, e);
			}
		});
	}

	private void parse(String captionsText) {
		_parsed = false;

		ArrayList<Caption> _captions = new ArrayList<>();
		HashMap<Integer, Caption[]> _captionsMap = new HashMap<>();

		int index = -1;
		float startTime = 0f;
		float endTime = 0f;
		String text = "";
		int count = 0;
		int m = 0;
		int mLast = 0;
		String[] time;

		for (String s : captionsText.split("[\\r\\n]+")) {
			if (s.matches("^\\d+$")) {
				// skip first time or wrong index
				if (index != -1) {
					m = (int)(startTime/60);

					if (m != mLast) {
						_captionsMap.put(mLast, _captions.toArray(new Caption[_captions.size()]));
						_captions = new ArrayList<>();
						mLast = m;
					}

					_captions.add(new Caption(index, startTime, endTime, text));
				}

				try { index = Integer.parseInt(s); }
				catch(Exception e) { index = -1; }

				startTime = 0f;
				endTime = 0f;
				text = "";
				count = 1;
				System.out.println(index);
				continue;
			}

			switch (count) {
				case 1:
					time = s.split("\\D+");
					startTime = extractTime(time, 0);
					endTime = extractTime(time, 4);
					System.out.println(startTime + " >> " + endTime);
					break;
				default:
					text += (count > 2 ? " " : "") + s;
					System.out.println(text);
			}

			++count;
		}

		// adding last caption entry
		if (index != -1) {
			_captions.add(new Caption(index, startTime, endTime, text));
			_captionsMap.put(mLast, _captions.toArray(new Caption[_captions.size()]));
		}

		/*for (Map.Entry<Integer, Caption[]> kv : _captionsMap.entrySet()) {
			System.out.println(kv.getKey());

			for (Caption c : kv.getValue()) {
				System.out.println(c.index + ": " + c.startTime + "/" + c.endTime);
			}
		}*/

		_parsed = true;
	}

	private Float extractTime(String[] timeInterval, int offset) {
		if (timeInterval.length == 0 && (timeInterval.length + offset)%4 != 0)
			return 0f;

		try {
			// parse and convert all to seconds
			int h = Integer.parseInt(timeInterval[offset]);
			int m = Integer.parseInt(timeInterval[1 + offset]);
			int s = Integer.parseInt(timeInterval[2 + offset]);
			int ms = Integer.parseInt(timeInterval[3 + offset]);

			return h*3600 + m*60 + s + ms/1000f;
		}
		catch(Exception e) {
			return 0f;
		}
	}
}
