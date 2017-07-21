package com.sambatech.player.model;

import com.google.ads.interactivemedia.v3.api.AdsRenderingSettings;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;

/**
 * @author Leandro Zanol - 7/19/17
 */
public final class AdsSettings {

	public final AdsRenderingSettings rendering = ImaSdkFactory.getInstance().createAdsRenderingSettings();
	public final int maxRedirects;
	public final float vastLoadTimeout; // ms
	public boolean debugMode;

	public AdsSettings() {
		this(8000f);
	}

	public AdsSettings(float vastLoadTimeout) {
		this(vastLoadTimeout, ImaSdkSettings.DEFAULT_MAX_REDIRECTS);
	}

	public AdsSettings(float vastLoadTimeout, int maxRedirects) {
		this.vastLoadTimeout = vastLoadTimeout;
		this.maxRedirects = maxRedirects;
	}
}
