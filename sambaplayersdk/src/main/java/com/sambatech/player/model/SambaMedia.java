package com.sambatech.player.model;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Data entity representing a media.
 *
 * @author Thiago Miranda, Leandro Zanol - 02/12/15
 */
public class SambaMedia {

	public String title = "";
	public String url;
	public String type = "";
	public String adUrl;
	public @NonNull AdsSettings adsSettings = new AdsSettings();
	public Drawable thumb;
	public float initialTime = 0f;
	public float duration = 0f;
	public boolean isLive;
	public boolean isAudioOnly;
	public @NonNull String[] backupUrls = new String[]{};
	public ArrayList<Output> outputs;
	public ArrayList<Caption> captions;
	public @NonNull CaptionsConfig captionsConfig = new CaptionsConfig();
	public String drmToken;

	public SambaMedia() {}

	public SambaMedia(SambaMedia media) {
		type = media.type;
		title = media.title;
		url = media.url;
		adUrl = media.adUrl;
		adsSettings = media.adsSettings;
		thumb = media.thumb;
		initialTime = media.initialTime;
		duration = media.duration;
		isLive = media.isLive;
		isAudioOnly = media.isAudioOnly;
		backupUrls = media.backupUrls;
		outputs = media.outputs;
		captions = media.captions;
		captionsConfig = media.captionsConfig;
		drmToken = media.drmToken;
	}

	public static class Output {
		public String url;
		public String label;
		public boolean isDefault = false;
		public int position;
	}

    public static class Caption {
        public final String url;
	    public final String label;
	    public final String language;
	    public final boolean cc;
        public final boolean isDefault;

	    public Caption(String url, String label, String language, boolean cc, boolean isDefault) {
		    this.url = url;
		    this.label = label;
		    this.language = language;
		    this.cc = cc;
		    this.isDefault = isDefault;
	    }
    }

	public static class CaptionsConfig {
		public final int color;
		public final float size;
		public final String language;

		public CaptionsConfig(int color, float size, String language) {
			this.color = color;
			this.size = size;
			this.language = language;
		}

		public CaptionsConfig(int color, float size) {
			this(color, size, null);
		}

		public CaptionsConfig(int color) {
			this(color, 20f);
		}

		public CaptionsConfig() {
			this(0xFFFFCC00);
		}
	}
}
