package com.sambatech.player;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import com.sambatech.player.event.SambaApiCallback;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaConfig;
import com.sambatech.player.model.SambaMediaRequest;
import com.sambatech.player.plugins.DrmRequest;
import com.sambatech.player.utils.Helpers;

import org.jose4j.base64url.internal.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Manages media data request from server.
 *
 * @author Thiago Miranda, Leandro Zanol - 2/12/15
 */
public class SambaApi {

	private Activity activity;
	private String accessToken;

	/**
	 * Output map
	 */
	private static final Map<String, Integer> outputMap = new HashMap<>();

	static {
		outputMap.put("_raw", -1);
		outputMap.put("abr", 0);
		outputMap.put("abr_hls", 0);
		outputMap.put("240p", 1);
		outputMap.put("360p", 2);
		outputMap.put("480p", 3);
		outputMap.put("720p", 4);
		outputMap.put("1080p", 5);
	}

	/**
	 * SambaApi constructor
	 *
	 * @param activity Reference to the current Activity
	 * @param accessToken Configured SambaTech access token (ignored for now, pass an empty string or null)
	 */
	public SambaApi(Activity activity, String accessToken) {
		this.activity = activity;
		this.accessToken = accessToken;
	}

	/**
	 * Requests a media from server.
	 *
	 * @param request Request data
	 * @param callback Listener for server media response
	 */
	public void requestMedia(SambaMediaRequest request, SambaApiCallback callback) {
		new RequestMediaTask(callback).execute(request);
	}

	/**
	 * Requests several medias from server.
	 *
	 * @param requests Several request data
	 * @param callback Listener for server media response
	 */
	public void requestMedia(final SambaMediaRequest[] requests, final SambaApiCallback callback) {
		SambaApiCallback callbackReq = new SambaApiCallback() {
			private int counter = 0;
			private List<SambaMedia> mediaList = new ArrayList<>();

			@Override
			public void onMediaResponse(SambaMedia media) {
				callback.onMediaResponse(media);
				mediaList.add(media);
				checkLast();
			}

			@Override
			public void onMediaResponseError(Exception e, SambaMediaRequest request) {
				callback.onMediaResponseError(e, request);
				checkLast();
			}

			private void checkLast() {
				if (++counter == requests.length)
					callback.onMediaListResponse(mediaList.toArray(new SambaMedia[mediaList.size()]));
			}
		};

		for (SambaMediaRequest req : requests)
			requestMedia(req, callbackReq);
	}

	/**
	 * Asynchronous request to the Samba Player API. Retrieves the media.
	 */
	private class RequestMediaTask extends AsyncTask<SambaMediaRequest, Void, SambaMedia> {
		private final SambaApiCallback listener;
		private SambaMediaRequest request;
		private Exception exception;

		RequestMediaTask(SambaApiCallback listener) {
			this.listener = listener;
		}

		@Override
		protected SambaMedia doInBackground(SambaMediaRequest... params) {
			request = params[0];

			int delimiter = request.mediaId != null ? Integer.parseInt(request.mediaId.split("(?=\\d[a-zA-Z]*$)")[1].substring(0, 1)) : 0;

			InputStream inputStream = null;
			Scanner scanner = null;
			Scanner scannerDelimited = null;

			exception = null;

			try {
				inputStream = new URL(getRequestUrl(request)).openStream();
				scanner = new Scanner(inputStream);
				scannerDelimited = scanner.useDelimiter("\\A");

				if (scannerDelimited.hasNext()) {
					String token = scannerDelimited.next();

					token = token.substring(delimiter, token.length() - delimiter).replaceAll("-", "+").replaceAll("_", "/");

					switch (token.length()%4) {
						case 0:
							break;
						case 2:
							token += "==";
							break;
						case 3:
							token += "=";
							break;
						default:
					}

					byte[] tokenBytes = Base64.decodeBase64(token);
					String jsonString = new String(tokenBytes);
					JSONObject json = new JSONObject(jsonString);

					return parseMedia(json, request);
				}
			}
			catch (Exception e) {
				exception = e;
				Log.w(getClass().getSimpleName(), "Error opening server request", e);
			}
			finally {
				try {
					if (inputStream != null)
						inputStream.close();

					if (scanner != null)
						scanner.close();

					if (scannerDelimited != null)
						scannerDelimited.close();
				}
				catch (IOException e) {
					exception = e;
					Log.w(getClass().getSimpleName(), "Error closing server request", e);
				}
			}

			return null;
		}

		private String getRequestUrl(SambaMediaRequest request) {
			String endpoint;

			switch (request.environment) {
				case LOCAL:
					endpoint = activity.getString(R.string.player_endpoint_local);
					break;

				case DEV:
					endpoint = activity.getString(R.string.player_endpoint_test);
					break;

				case STAGING:
					endpoint = normalizeProtocol(activity.getString(R.string.player_endpoint_staging), request.protocol);
					break;

				case PROD:
				default:
					endpoint = normalizeProtocol(activity.getString(R.string.player_endpoint_prod), request.protocol);
			}

			String url = String.format("%s%s/", endpoint, request.projectHash);

			if (request.mediaId != null)
				url += String.format("%s", request.mediaId);
			else if (request.liveChannelId != null)
				url += String.format("live/%s", request.liveChannelId);

			if (request.streamUrl != null)
				url += String.format("?alternateLive=%s", request.streamUrl);
			else if (request.streamName != null)
				url += String.format("?streamName=%s", request.streamName);

			return url;
		}

		/**
		 * Triggered after the Samba Player API success response
		 * @param media Samba Media
		 */
		@Override
		protected void onPostExecute(SambaMedia media) {
			if (media == null) {
				listener.onMediaResponseError(exception != null ? exception :
						new Exception("Failed to load media data"), request);
				return;
			}

			listener.onMediaResponse(media);
		}

		/**
		 * Get the json response and serializes it
		 * @param json Json Response
		 * @return Samba Media object
		 */
		private SambaMedia parseMedia(JSONObject json, SambaMediaRequest request) {
			try {
				String qualifier = json.getString("qualifier").toLowerCase();

				if (!("video".equals(qualifier) || "live".equals(qualifier) || "audio".equals(qualifier)))
					return null;

				SambaMediaConfig media = new SambaMediaConfig();
				JSONObject playerConfig = json.getJSONObject("playerConfig");
				JSONObject apiConfig = json.getJSONObject("apiConfig");
				JSONObject playerSecurity = json.optJSONObject("playerSecurity");
				JSONObject projectConfig = json.getJSONObject("project");
				JSONArray ads = json.optJSONArray("advertisings");

				media.request = request;
				media.projectHash = projectConfig.getString("playerHash");
				media.projectId = projectConfig.getInt("id");
				media.title = json.getString("title");
				media.isAudioOnly = "audio".equals(qualifier) || request.isStreamAudio;

				if (json.has("id"))
					media.id = json.getString("id");

				if (json.has("clientId")) {
					media.clientId = json.getInt("clientId");
				}

				if (json.has("categoryId"))
					media.categoryId = json.getInt("categoryId");

				if (json.has("deliveryRules"))
					fillDeliveryRules(json, media);
				else if (json.has("liveOutput"))
					fillLive(json, request, media);

				fillThumb(json, request, media);
				fillCaptions(json, request, media);
				fillTheme(media, playerConfig);
				fillSttm(request, media, apiConfig);
				fillAds(media, ads);
				fillSecurity(media, playerSecurity);

				return media;
			}
			catch (Exception e) {
				Log.e(getClass().getSimpleName(), "Failed to search media", e);
			}

			return null;
		}

		private void fillThumb(JSONObject json, SambaMediaRequest request, SambaMediaConfig media) throws JSONException, IOException {
			JSONArray thumbs = json.optJSONArray("thumbnails");

			if (thumbs != null && thumbs.length() > 0 && !thumbs.getJSONObject(0).isNull("url")) {
				media.thumb = Drawable.createFromStream(new URL(normalizeProtocol(
						thumbs.getJSONObject(0).getString("url"), request.protocol)).openStream(), "Thumbnail");
			}
		}

		private void fillAds(SambaMediaConfig media, JSONArray ads) throws JSONException {
			if (!media.isAudioOnly && (ads != null && ads.length() > 0)) {
				JSONObject ad = ads.optJSONObject(0);

				if ("dfp".equalsIgnoreCase(ad.getString("adServer")))
					media.adUrl = ad.getString("tagVast");
			}
		}

		private void fillSecurity(SambaMediaConfig media, JSONObject playerSecurity) {
			if (playerSecurity != null) {
				JSONObject drm = playerSecurity.optJSONObject("drmSecurity");

				if (drm != null) {
//					media.drmRequest = new DrmRequest(drm.optString("widevineSignatureURL"));
//					media.drmRequest.addLicenseParam("SubContentType", drm.optString("subContentType", "Default"));
//					media.drmRequest.addLicenseParam("CrmId", drm.optString("crmId"));
//					media.drmRequest.addLicenseParam("AccountId", drm.optString("accountId"));
//					media.drmRequest.addLicenseParam("ContentId", drm.optString("contentId"));
//					media.drmRequest.addHeaderParam("Content-Type", "application/octet-stream");
				}

				media.blockIfRooted = playerSecurity.optBoolean("rootedDevices", false);
			}
		}

		private void fillSttm(SambaMediaRequest request, SambaMediaConfig media, JSONObject apiConfig) throws JSONException {
			if (apiConfig.has("sttm")) {
				JSONObject sttm = apiConfig.getJSONObject("sttm");
				media.sttmUrl = normalizeProtocol(sttm.optString("url", "http://sttm.sambatech.com.br/collector/__sttm.gif"), request.protocol);
				media.sttmKey = sttm.optString("key", "ae810ebc7f0654c4fadc50935adcf5ec");

				JSONObject sttm2 = apiConfig.getJSONObject("sttm2");
				if (sttm2 != null) {
					media.sttm2Url = normalizeProtocol(sttm2.optString("url", "https://sttm2.sambatech.com/"), request.protocol);
					media.sttm2Key = sttm2.optString("key", "eyJhbGciOiJIUzM4NCIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzYW1iYXRlY2gtcGxheWVyIiwibmJmIjoxNTM1NDgyNTY0LCJpYXQiOjE1MzU0ODI5ODQsImV4cCI6MTUzNTUyNjE4NH0.AjRlzx_V4z9RVZ0zW3RDc0H2yZOefPy1X7QskVENuQBwatiwnaJiQL26vhGB1mmo");
				}

			}
		}

		private void fillTheme(SambaMediaConfig media, JSONObject playerConfig) throws JSONException {
			final String theme = "theme";

			if (playerConfig.has(theme) && !"default".equalsIgnoreCase(playerConfig.getString(theme))) {
				media.themeColorHex = "#" + playerConfig.getString(theme);
				media.themeColor = (int) Long.parseLong("FF" + playerConfig.getString(theme).replaceAll("^#*", ""), 16);
			}
		}

		private void fillCaptions(JSONObject json, SambaMediaRequest request, SambaMediaConfig media) throws JSONException {
			JSONArray captions = json.optJSONArray("captions");

			if (captions != null && captions.length() > 0) {
				ArrayList<SambaMedia.Caption> captionArray = new ArrayList<>();
				HashMap<String, String> langLookup = new HashMap<>();
			    JSONObject caption;
				String lang;

				// TODO: localization
				langLookup.put("pt-br", "Português");
				langLookup.put("en-us", "Inglês");
				langLookup.put("es-es", "Espanhol");
				langLookup.put("it-it", "Italiano");
				langLookup.put("fr-fr", "Francês");
				langLookup.put("zh-cn", "Chinês");
				langLookup.put("ru-ru", "Russo");
				langLookup.put("disable", "Desativar");

			    // captionArray
			    for (int j = captions.length(); j-- > 0;) {
			        caption = captions.getJSONObject(j);
				    JSONObject fileInfo = caption.getJSONObject("fileInfo");
				    lang = fileInfo.getString("captionLanguage").toLowerCase().replace('_', '-');
				    String subtitleType = fileInfo.optBoolean("autoGenerated", false) ? " (Auto)" : fileInfo.optBoolean("closedCaption", false) ? " (CC)" : "";

				    captionArray.add(new SambaMedia.Caption(
					        normalizeProtocol(caption.getString("url"), request.protocol),
						    langLookup.get(lang) + subtitleType,
						    lang,
					        fileInfo.getBoolean("closedCaption"),
						    false
			        ));
			    }

				// disable option (as default)
				captionArray.add(new SambaMedia.Caption(null, langLookup.get("disable"), null, false, true));

			    media.captions = captionArray;
			}
		}

		private void fillLive(JSONObject json, SambaMediaRequest request, SambaMediaConfig media) throws JSONException {
			final String reHds = "[\\w]+\\.f4m$";

			// tries to fallback from HDS
			media.url = normalizeProtocol(json.getJSONObject("liveOutput").getString("baseUrl").replaceAll(reHds, "playlist.m3u8"), request.protocol);

			for (int i = 0; i < request.backupUrls.length; ++i)
				request.backupUrls[i] = normalizeProtocol(request.backupUrls[i].replaceAll(reHds, "playlist.m3u8"), request.protocol);

			media.backupUrls = request.backupUrls;
			media.isLive = true;
			media.isDvr = json.getJSONObject("liveOutput").has("dvr") ? json.getJSONObject("liveOutput").getBoolean("dvr") : false;

			// media type relies on URL
			if (media.url.contains(".m3u8"))
				media.type = "hls";
			else if (media.url.contains(".mpd"))
				media.type = "dash";
		}

		private void fillDeliveryRules(JSONObject json, SambaMediaConfig media) throws JSONException {
			final String outputName = "outputName";
			final String progressive = "progressive";

			String defaultOutput = json.getJSONObject("project").getString("defaultOutput");
			JSONArray rules = json.getJSONArray("deliveryRules");
			int totalRules = rules.length();
			JSONObject rule;
			JSONArray outputs;
			JSONObject output;
			ArrayList<SambaMedia.Output> mediaOutputs;
			String defaultOutputCurrent;
			String type;
			boolean isStreaming;

			// looks for Dash, HLS or the last taken
			ArrayList<String> filledRules = new ArrayList<>();

			for (int i = 0; i < totalRules; ++i) {
				rule = rules.getJSONObject(i);
				type = rule.getString("urlType").toLowerCase();

				// must be one of the following delivery types
				switch (type) {
					case "dash":
					case "hls":
					case progressive:
						// do not repeat and keep priorities (dash, hls, progressive)
						if (filledRules.contains(type) ||
								type.equals(progressive) && filledRules.contains("hls") ||
								(type.equals(progressive) || type.equals("hls")) && filledRules.contains("dash"))
							continue;
						break;
					default:
						continue;
				}

				media.type = type;
				mediaOutputs = new ArrayList<>();
				outputs = rule.getJSONArray("outputs");
				isStreaming = "hls".equals(type) || "dash".equals(type);
				defaultOutputCurrent = isStreaming ? "abr" : defaultOutput;

				for (int j = outputs.length(); j-- > 0;) {
					output = outputs.getJSONObject(j);
					String label = output.getString(outputName);

					SambaMedia.Output cOutput = new SambaMedia.Output();
					cOutput.url = normalizeProtocol(output.getString("url"), request.protocol);
					cOutput.label = output.getString(outputName).startsWith("abr") ? "Auto" : output.getString(outputName);
					cOutput.position = outputMap.get(output.getString(outputName).toLowerCase());

					//Duration
					media.duration = output.getJSONObject("fileInfo").getLong("duration")/1000f;

					if (media.isAudioOnly) {
						if (!isStreaming || !cOutput.url.contains(".mp3"))
							mediaOutputs.add(cOutput);
						continue;
					}

					if (!"_raw".equalsIgnoreCase(label) && !output.isNull("url")) {
						if (label.startsWith(defaultOutputCurrent)) {
							media.url = normalizeProtocol(output.getString("url"), request.protocol);
							media.defaultOutputIndex = j;
							cOutput.isDefault = true;
						}

						mediaOutputs.add(cOutput);
					}
				}

				// was it a valid iteration?
				if (!mediaOutputs.isEmpty()) {
					if (media.url == null)
						media.url = normalizeProtocol(mediaOutputs.get(0).url, request.protocol);

					media.outputs = mediaOutputs;
					filledRules.add(media.type);
				}
			}

			if (media.outputs != null)
				sortOutputs(media.outputs);
		}

		/**
		 * Sort the outputs
		 * @param outputs Current media outputs
		 */
		private void sortOutputs(ArrayList<SambaMedia.Output> outputs) {
			Collections.sort(outputs, new Helpers.CustomSorter());
		}

        /**
         * Replaces URL protocol with the informed one.
         */
        private String normalizeProtocol(String url, SambaMediaRequest.Protocol protocol) {
            return url.replaceAll("(https?)", protocol.toString().toLowerCase());
        }
	}
}
