package com.sambatech.player.plugins;

import java.util.HashMap;
import java.util.Map;

public class DrmRequest {

	private String licenseUrl;
	private HashMap<String, String> urlParams = new HashMap<>();
	private HashMap<String, String> headerParams = new HashMap<>();
	private String token;
	private String provider;
	private String drmOfflinePayload;

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

		if (token != null && !token.isEmpty()) {
			params += "&ls_session=" + token;
		}

		return licenseUrl + params;
	}

	public HashMap<String, String> getHeaderParams() {
		return headerParams;
	}

	public void addLicenseParam(String k, String v) {
		urlParams.put(k, v);
	}

	public void addHeaderParam(String k, String v) {
		headerParams.put(k, v);
	}

	public String getLicenseParam(String k) {
		return urlParams.get(k);
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getDrmOfflinePayload() {
		return drmOfflinePayload;
	}

	public void setDrmOfflinePayload(String drmOfflinePayload) {
		this.drmOfflinePayload = drmOfflinePayload;
	}
}
