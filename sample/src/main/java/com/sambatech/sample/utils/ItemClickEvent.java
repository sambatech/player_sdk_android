package com.sambatech.sample.utils;

import com.sambatech.sample.model.LiquidMedia;

/**
 * Created by tmiranda on 19/04/16.
 */
public class ItemClickEvent {
	public final String type;
	public final LiquidMedia media;

	public ItemClickEvent(String type, LiquidMedia media) {
		this.type = type;
		this.media = media;
	}
}
