package com.google.android.libraries.mediaframework.exoplayerextensions;

import java.util.HashMap;
import java.util.Map;

public class DrmRequest {

	private String licenseUrl;
	private HashMap<String, String> urlParams = new HashMap<>();
	private HashMap<String, String> headerParams = new HashMap<>();

	/**
	 * @param licenseUrl The URL for the license server.
	 */
	public DrmRequest(String licenseUrl) {
		this.licenseUrl = licenseUrl;
	}

	public String getLicenseUrl() {
		String params = licenseUrl.contains("?") ? "" : "?";
		String sep = "";

		for (Map.Entry<String, String> kv : urlParams.entrySet()) {
			params += sep + kv.getKey() + "=" + kv.getValue();
			sep = "&";
		}

		return licenseUrl + params;
	}

	public HashMap<String, String> getHeaderParams() {
		return headerParams;
	}

	public void addUrlParam(String k, String v) {
		urlParams.put(k, v);
	}

	public void addHeaderParam(String k, String v) {
		headerParams.put(k, v);
	}

	public String getUrlParam(String k) {
		return urlParams.get(k);
	}
}
