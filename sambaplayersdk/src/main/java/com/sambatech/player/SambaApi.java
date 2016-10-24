package com.sambatech.player;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.libraries.mediaframework.exoplayerextensions.DrmRequest;
import com.sambatech.player.event.SambaApiCallback;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaConfig;
import com.sambatech.player.model.SambaMediaRequest;
import com.sambatech.player.utils.Helpers;

import org.jose4j.base64url.internal.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
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
	private static final Map<String, Integer> outputMap = new HashMap<String, Integer>() {{
		put("_raw", -1);
		put("abr", 0);
		put("abr_hls", 0);
		put("240p", 1);
		put("360p", 2);
		put("480p", 3);
		put("720p", 4);
		put("1080p", 5);
	}};

	/**
	 * SambaApi constructor
	 *
	 * @param activity Reference to the current Activity
	 * @param accessToken Configured SambaTech access token (ignored for now, pass an empty string or null)
	 */
	public SambaApi(Activity activity, String accessToken) {
		//TODO validar token
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
			public void onMediaResponseError(String msg, SambaMediaRequest request) {
				callback.onMediaResponseError(msg, request);
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
		private SambaApiCallback listener;
		private SambaMediaRequest request;
		private String errorMsg;

		public RequestMediaTask(SambaApiCallback listener) {
			this.listener = listener;
		}

		@Override
		protected SambaMedia doInBackground(SambaMediaRequest... params) {
			request = params[0];

			int delimiter = request.mediaId != null ? Integer.parseInt(request.mediaId.split("(?=\\d[a-zA-Z]*$)")[1].substring(0, 1)) : 0;
			String endpoint;

			switch (request.environment) {
				case LOCAL:
					endpoint = activity.getString(R.string.player_endpoint_local);
					break;

				case TEST:
					endpoint = activity.getString(R.string.player_endpoint_test);
					break;

				case STAGING:
					endpoint = activity.getString(R.string.player_endpoint_staging);
					break;

				case PROD:
				default:
					endpoint = activity.getString(R.string.player_endpoint_prod);
			}

			String url = endpoint + request.projectHash + (request.mediaId != null ? "/" + request.mediaId : "?" +
					(request.streamUrls.length > 0 ? "alternateLive=" + request.streamUrls[0] : "streamName=" + request.streamName));

			InputStream inputStream = null;
			Scanner scanner = null;
			Scanner scannerDelimited = null;

			errorMsg = "Failed to load media data";

			try {
				inputStream = new URL(url).openStream();
				scanner = new Scanner(inputStream);
				scannerDelimited = scanner.useDelimiter("\\A");

				if (scannerDelimited.hasNext()) {
					String token = scannerDelimited.next();

					token = token.substring(delimiter, token.length() - delimiter).replaceAll("\\-", "+").replaceAll("_", "/");

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

					return parseMedia(json);
				}
			}
			catch (Exception e) {
				errorMsg = "Error opening server request";
				Log.w(getClass().getSimpleName(), errorMsg, e);
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
					errorMsg = "Error closing server request";
					Log.w(getClass().getSimpleName(), errorMsg, e);
				}
			}

			return null;
		}

		/**
		 * Triggered after the Samba Player API success response
		 * @param media Samba Media
		 */
		@Override
		protected void onPostExecute(SambaMedia media) {
			if (media == null) {
				listener.onMediaResponseError(errorMsg, request);
				return;
			}

			listener.onMediaResponse(media);
		}

		/**
		 * Get the json response and serializes it
		 * @param json Json Response
		 * @return Samba Media object
		 */
		private SambaMedia parseMedia(JSONObject json) {
			try {
				String qualifier = json.getString("qualifier").toLowerCase();

				if (!(qualifier.equals("video") || qualifier.equals("live") || qualifier.equals("audio")))
					return null;

				SambaMediaConfig media = new SambaMediaConfig();
				ArrayList<SambaMedia.Output> outputArray = new ArrayList<>();

				JSONObject playerConfig = json.getJSONObject("playerConfig");
				JSONObject apiConfig = json.getJSONObject("apiConfig");
				JSONObject playerSecurity = json.optJSONObject("playerSecurity");
				JSONObject projectConfig = json.getJSONObject("project");
				JSONArray ads = json.optJSONArray("advertisings");

				media.projectHash = projectConfig.getString("playerHash");
				media.projectId = projectConfig.getInt("id");
				media.title = json.getString("title");
				media.isAudioOnly = qualifier.equals("audio");

				if (json.has("id"))
					media.id = json.getString("id");

				if (json.has("categoryId"))
					media.categoryId = json.getInt("categoryId");

				if (json.has("deliveryRules")) {
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
							case "progressive":
								// do not repeat and keep priorities (dash, hls, progressive)
								if (filledRules.contains(type) ||
										type.equals("progressive") && filledRules.contains("hls") ||
										(type.equals("progressive") || type.equals("hls")) && filledRules.contains("dash"))
									continue;
								break;
							default:
								continue;
						}

						media.type = type;
						mediaOutputs = new ArrayList<>();
						outputs = rule.getJSONArray("outputs");
						isStreaming = type.equals("hls") || type.equals("dash");
						defaultOutputCurrent = isStreaming ? "abr" : defaultOutput;

						for (int j = outputs.length(); j-- > 0;) {
							output = outputs.getJSONObject(j);
							String label = output.getString("outputName");

							SambaMedia.Output cOutput = new SambaMedia.Output();
							cOutput.url = output.getString("url");
							cOutput.label = output.getString("outputName").startsWith("abr") ? "Auto" : output.getString("outputName");
							cOutput.position = outputMap.get(output.getString("outputName").toLowerCase());

							if (media.isAudioOnly) {
								if (!isStreaming || !cOutput.url.contains(".mp3"))
									mediaOutputs.add(cOutput);
								continue;
							}

							// TODO: checar comportamento de projeto sem default output
							if (!label.equalsIgnoreCase("_raw") && !output.isNull("url")) {
								if (label.startsWith(defaultOutputCurrent)) {
									media.url = output.getString("url");
									cOutput.current = true;
								}

								mediaOutputs.add(cOutput);
							}
						}

						// was it a valid iteration?
						if (mediaOutputs.size() > 0) {
							if (media.url == null)
								media.url = mediaOutputs.get(0).url;

							media.outputs = mediaOutputs;
							filledRules.add(media.type);
						}
					}

					if (media.outputs != null)
						sortOutputs(media.outputs);

				}
				else if (json.has("liveOutput")) {
					media.url = json.getJSONObject("liveOutput").getString("baseUrl");
					media.isLive = true;

					// media type relies on URL
					if (media.url.contains(".m3u8"))
						media.type = "hls";
					else if (media.url.contains(".mpd"))
						media.type = "dash";
				}

				JSONArray thumbs = json.optJSONArray("thumbnails");

				if (thumbs.length() > 0 && !thumbs.getJSONObject(0).isNull("url")) {
					/*Bitmap bmp = BitmapFactory.decodeStream(new URL(thumbs.getJSONObject(0).getString("url")).openStream());
					bmp.setDensity(Bitmap.DENSITY_NONE);
					media.thumb = new BitmapDrawable(activity.getResources(), bmp);*/
					media.thumb = Drawable.createFromStream(new URL(thumbs.getJSONObject(0).getString("url")).openStream(), "Thumbnail");
				}
				//else media.thumb = ContextCompat.getDrawable(activity, R.drawable.thumb);

				if (playerConfig.has("theme") && !playerConfig.getString("theme").toLowerCase().equals("default"))
					media.themeColor = (int)Long.parseLong("FF" + playerConfig.getString("theme").replaceAll("^#*", ""), 16);

				if (apiConfig.has("sttm")) {
					JSONObject sttm = apiConfig.getJSONObject("sttm");
					media.sttmUrl = sttm.optString("url", "http://sttm.sambatech.com.br/collector/__sttm.gif");
					media.sttmKey = sttm.optString("key", "ae810ebc7f0654c4fadc50935adcf5ec");
				}

				if (!media.isAudioOnly && (ads != null && ads.length() > 0)) {
					JSONObject ad = ads.optJSONObject(0);

					if (ad.getString("adServer").equalsIgnoreCase("dfp"))
						media.adUrl = ad.getString("tagVast");
				}

				if (playerSecurity != null) {
					JSONObject drm = playerSecurity.optJSONObject("drmSecurity");

					if (drm != null) {
						//media.url = "http://52.10.169.196:1935/Irdeto/mp4:samba1.mp4/manifest.mpd";
						media.url = "http://107.21.208.27/vodd/_definst_/mp4:myMovie.mp4/manifest_mvlist.mpd";
						//media.url = "http://107.21.208.27/vodd/_definst_/mp4:chaves3_480p.mp4/manifest_mvlist.mpd";
						media.drmRequest = new DrmRequest(drm.optString("widevineSignatureURL"));
						media.drmRequest.addUrlParam("SubContentType", drm.optString("subContentType", "Default"));
						media.drmRequest.addUrlParam("CrmId", drm.optString("crmId"));
						media.drmRequest.addUrlParam("AccountId", drm.optString("accountId"));
						media.drmRequest.addUrlParam("ContentId", "samba1"); //media.id
						//media.drmRequest.addUrlParam("ContentId", "samba3_tvod"); //media.id
						media.drmRequest.addHeaderParam("Content-Type", "application/octet-stream");
					}
				}

				return media;
			}
			catch (Exception e) {
				Log.e(getClass().getSimpleName(), "Failed to search media", e);
			}

			return null;
		}

		/**
		 * Sort the outputs
		 * @param outputs Current media outputs
		 */
		private void sortOutputs(ArrayList<SambaMedia.Output> outputs) {
			Collections.sort(outputs, new Helpers.CustomSorter());
		}
	}
}
