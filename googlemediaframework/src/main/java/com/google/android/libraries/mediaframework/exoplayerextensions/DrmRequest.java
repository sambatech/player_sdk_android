package com.google.android.libraries.mediaframework.exoplayerextensions;

import java.util.HashMap;

public class DrmRequest {

	public String licenseServerUrl;
	public HashMap<String, String> requestProperties;
	public boolean methodGet = true;

	public DrmRequest() {}

	/**
	 * @param licenseServerUrl The URL for the license server.
	 * @param requestProperties The request properties for the license server.
	 */
	public DrmRequest(String licenseServerUrl, HashMap<String, String> requestProperties) {
		this.licenseServerUrl = licenseServerUrl;
		this.requestProperties = requestProperties;
	}
}
