package com.sambatech.player.model;

import android.graphics.drawable.Drawable;

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
	public boolean isLive;
	public boolean isAudioOnly;
	public ArrayList<Output> outputs;

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
	}

	public static class Output {
		public String url;
		public String label;
		public Boolean current = false;
		public int position;
	}
}
