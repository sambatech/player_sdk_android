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
	public ArrayList<Outputs> outputs;

	public static class Outputs {
		public String url;
		public String label;
		public Boolean current = false;
		public int position;
	}
}
