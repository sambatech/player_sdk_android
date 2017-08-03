package com.sambatech.exoplayerv2.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 */
public class MediaContent {

	public static final List<Media> items = new ArrayList<>();

	static {
		// sample items
		addItem(new Media("http://liveabr2.sambatech.com.br/abr/sbtabr_8fcdc5f0f8df8d4de56b22a2c6660470/livestreamabrsbtbkp.m3u8",
				"SBT (Live)", true));
		addItem(new Media("http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8",
				"Apple bipbop"));
		addItem(new Media("http://streaming.almg.gov.br/live/tvalmg2.m3u8",
				"ALMG (Live)", true));
	}

	public static void addItem(Media item) {
		items.add(item);
	}

	public static Media getItem(int i) {
		return items.get(i);
	}

	/**
	 * Represents a media item.
	 */
	public static final class Media {
		public final String id;
		public final String ph;
		public final String url;
		public final String title;
		public final boolean isLive;
		public final boolean isAudio;

		public Media(String id, String ph, String title) {
			this(id, ph, title, false);
		}

		public Media(String id, String ph, String title, boolean isAudio) {
			this.title = title;
			this.isLive = id == null || id.isEmpty();
			this.isAudio = isAudio;
			this.id = id;
			this.ph = ph;
			this.url = null;
		}

		public Media(String url, String title) {
			this(url, title, false);
		}

		public Media(String url, String title, boolean isLive) {
			this(url, title, isLive, false);
		}

		public Media(String url, String title, boolean isLive, boolean isAudio) {
			this.title = title;
			this.isLive = isLive;
			this.isAudio = isAudio;
			this.url = url;
			this.id = null;
			this.ph = null;
		}

		@Override
		public String toString() {
			return String.format("id=%s ph=%s url=%s title=%s isLive=%s isAudio=%s",
					id, ph, url, title, isLive, isAudio);
		}
	}
}
