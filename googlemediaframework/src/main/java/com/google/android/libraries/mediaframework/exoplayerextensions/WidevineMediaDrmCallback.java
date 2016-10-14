/**
 Copyright 2014 Google Inc. All rights reserved.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

/**
 * This file has been taken from the ExoPlayer demo project with minor modifications.
 * https://github.com/google/ExoPlayer/
 */

package com.google.android.libraries.mediaframework.exoplayerextensions;

import android.annotation.TargetApi;
import android.media.MediaDrm.KeyRequest;
import android.media.MediaDrm.ProvisionRequest;

import com.google.android.exoplayer.drm.MediaDrmCallback;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A {@link MediaDrmCallback} for Widevine test content.
 */
@TargetApi(18)
public class WidevineMediaDrmCallback implements MediaDrmCallback {

	/**
	 * The Widevine URL.
	 */
	private static final String WIDEVINE_GTS_DEFAULT_BASE_URI = "http://wv-staging-proxy.appspot.com/proxy?provider=YouTube&video_id=";

	/**
	 * DRM info to play encrypted media.
	 */
	private final DrmRequest drmRequest;

	/**
	 * @param drmRequest DRM info to play encrypted media.
	 */
	public WidevineMediaDrmCallback(DrmRequest drmRequest) {
		this.drmRequest = drmRequest != null ? drmRequest : new DrmRequest();
	}

	@Override
	public byte[] executeProvisionRequest(UUID uuid, ProvisionRequest request) throws IOException {
		String url = request.getDefaultUrl() + "&signedRequest=" + new String(request.getData());
		return ExoplayerUtil.executePost(url, null, null);
	}

	@Override
	public byte[] executeKeyRequest(UUID uuid, KeyRequest request) throws IOException {
		String url = request.getDefaultUrl();
		HashMap<String, String> requestProperties = drmRequest.requestProperties;
		byte[] data = request.getData();

		if (drmRequest.licenseServerUrl != null) {
			url = drmRequest.licenseServerUrl;

			if (drmRequest.paramsByGet) {
				String sep = "";

				url += "?";

				for (Map.Entry<String, String> kv : drmRequest.requestProperties.entrySet()) {
					url += sep + kv.getKey() + "=" + kv.getValue();
					sep = "&";
				}

				requestProperties = null;
			}
			else if (drmRequest.postData != null) {
				data = drmRequest.postData.getBytes();
			}
		}
		else if (url.isEmpty())
			url = WIDEVINE_GTS_DEFAULT_BASE_URI;

		// Irdeto path and query string need to go by post
		//return ExoplayerUtil.executePost(url, request.getData(), requestProperties);
		return ExoplayerUtil.executePost(url, data, requestProperties);
	}
}