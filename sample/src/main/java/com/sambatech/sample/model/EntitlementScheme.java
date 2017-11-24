package com.sambatech.sample.model;

/**
 * @author Leandro Zanol - 16/10/17
 */
public final class EntitlementScheme {

	/**
	 * The DRM content ID
	 */
	public final String contentId;

	public EntitlementScheme() {
		this(null);
	}

	public EntitlementScheme(String contentId) {
		this.contentId = contentId;
	}
}
