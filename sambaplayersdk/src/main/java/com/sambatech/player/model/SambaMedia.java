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
    public Drawable thumb;
    public float duration;
	public boolean isLive;
	public boolean isAudioOnly;
	public ArrayList<Output> outputs;
	public ArrayList<Caption> captions;
	public String drmToken;

	public SambaMedia() {}

	public SambaMedia(SambaMedia media) {
		type = media.type;
		title = media.title;
		url = media.url;
		adUrl = media.adUrl;
		isLive = media.isLive;
		isAudioOnly = media.isAudioOnly;
		outputs = media.outputs;
		thumb = media.thumb;
		captions = media.captions;
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
}
