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
import android.text.TextUtils;

import com.google.android.exoplayer.drm.MediaDrmCallback;

import java.io.IOException;
import java.util.HashMap;
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
	private static final String WIDEVINE_LICENSE_SERVER = "https://drm-widevine-licensing.axtest.net/AcquireLicense";

	/**
	 * Token used to validate media execution.
	 */
	private final String videoId;

	/**
	 * @param videoId The ID of the video to be played.
	 */
	public WidevineMediaDrmCallback(String videoId) {
		this.videoId = videoId;
	}

	@Override
	public byte[] executeProvisionRequest(UUID uuid, ProvisionRequest request) throws IOException {
		String url = request.getDefaultUrl() + "&signedRequest=" + new String(request.getData());
		return ExoplayerUtil.executePost(url, null, null);
	}

	@Override
	public byte[] executeKeyRequest(UUID uuid, KeyRequest request) throws IOException {
		HashMap<String, String> requestProperties = null;
		String url = request.getDefaultUrl();

		if (videoId != null && !videoId.isEmpty()) {
			requestProperties = new HashMap<>();
			requestProperties.put("X-AxDRM-Message", videoId);
			url = WIDEVINE_LICENSE_SERVER;
		}
		else if (url.isEmpty())
			url = WIDEVINE_GTS_DEFAULT_BASE_URI;

		return ExoplayerUtil.executePost(url, request.getData(), requestProperties);
	}
}