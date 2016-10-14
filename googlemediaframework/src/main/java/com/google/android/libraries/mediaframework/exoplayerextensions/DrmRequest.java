package com.google.android.libraries.mediaframework.exoplayerextensions;

import java.util.HashMap;

public class DrmRequest {

	public String licenseServerUrl;
	public HashMap<String, String> requestProperties;
	public boolean paramsByGet;
	public String postData;

	public DrmRequest() {}

	public DrmRequest(String licenseServerUrl, HashMap<String, String> requestProperties) {
		this(licenseServerUrl, requestProperties, null, false);
	}

	public DrmRequest(String licenseServerUrl, String postData) {
		this(licenseServerUrl, null, postData, false);
	}

	/**
	 * @param licenseServerUrl The URL for the license server.
	 * @param requestProperties The request properties for the license server.
	 * @param paramsByGet Should params be passed by GET?
	 */
	public DrmRequest(String licenseServerUrl, HashMap<String, String> requestProperties, String postData, boolean paramsByGet) {
		this.licenseServerUrl = licenseServerUrl;
		this.requestProperties = requestProperties;
		this.postData = postData;
		this.paramsByGet = paramsByGet;
	}
}
