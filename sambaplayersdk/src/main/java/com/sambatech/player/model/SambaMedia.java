package com.sambatech.player.model;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;

import com.sambatech.player.SambaCast;

import java.util.ArrayList;

/**
 * Data entity representing a media.
 *
 * @author Thiago Miranda, Leandro Zanol - 02/12/15
 */
public class SambaMedia {

	public String title = "";
	public String url;
	public @NonNull String[] backupUrls = new String[]{};
	public String type = "";
	public String adUrl;
    public Drawable thumb;
    public float duration;
	public boolean isLive;
	public boolean isAudioOnly;
	public ArrayList<Output> outputs;
	public ArrayList<Caption> captions;
	public @NonNull CaptionsConfig captionsConfig = new CaptionsConfig();
	public String drmToken;
	public View castButton;

	public SambaMedia() {}

	public SambaMedia(SambaMedia media) {
		type = media.type;
		title = media.title;
		url = media.url;
		backupUrls = media.backupUrls;
		adUrl = media.adUrl;
		isLive = media.isLive;
		isAudioOnly = media.isAudioOnly;
		outputs = media.outputs;
		thumb = media.thumb;
		captions = media.captions;
		drmToken = media.drmToken;
		castButton = media.castButton;
	}

	/**
	 * If set, Chromecast support will be enabled inside player view.
	 * @param cast The SambaCast instance
	 */
	public void setSambaCast(@NonNull SambaCast cast) {
		castButton = cast.getButton();
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
