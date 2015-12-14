package com.sambatech.player;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.sambatech.player.event.SambaApiCallback;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaRequest;

import org.jose4j.base64url.internal.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

/**
 * Requests media data from server.
 *
 * @author tmiranda - 2/12/15
 */
public class SambaApi {

	private Activity activity;
	private boolean inited = false;
	private String accessToken;

	public SambaApi(Activity activity, String accessToken) {
		// TODO: validar "accessToken"
		this.activity = activity;
		this.accessToken = accessToken;
	}

	/**
	 * Requests a media from server.
	 *
	 * @param request - Request data
	 * @param callback - Listener for server media response
	 */
	public void requestMedia(SambaMediaRequest request, SambaApiCallback callback) {
		new RequestMediaTask(callback).execute(activity.getString(R.string.player_endpoint) + request.projectId +
				(request.mediaId != null ? "/" + request.mediaId : "?" +
						(request.streamName != null ? "streamName=" + request.streamName : "alternateLive=" + request.streamUrl)));
	}

	public void requestMedia(final SambaMediaRequest[] requests, final SambaApiCallback callback) {
		SambaApiCallback callbackReq = new SambaApiCallback() {
			private int counter = 0;
			private SambaMedia[] mediaList = new SambaMedia[requests.length];

			@Override
			public void onMediaResponse(SambaMedia media) {
				addAndDispatchCallback(media);
			}

			@Override
			public void onMediaResponseError(Exception e) {
				callback.onMediaResponseError(e);
				addAndDispatchCallback(new SambaMedia());
			}

			private void addAndDispatchCallback(SambaMedia media) {
				callback.onMediaResponse(mediaList[counter++] = media);

				if (counter == requests.length)
					callback.onMediaListResponse(mediaList);
			}
		};

		for (SambaMediaRequest req : requests)
			requestMedia(req, callbackReq);
	}

	private class RequestMediaTask extends AsyncTask<String, Void, SambaMedia> {
		private SambaApiCallback listener;

		public RequestMediaTask(SambaApiCallback listener) {
			this.listener = listener;
		}

		@Override
		protected SambaMedia doInBackground(String... params) {
			InputStream inputStream = null;
			Scanner scanner = null;
			Scanner scannerDelimited = null;

			try {
				inputStream = new URL(params[0]).openStream();
				scanner = new Scanner(inputStream);
				scannerDelimited = scanner.useDelimiter("\\A");

				if (scannerDelimited.hasNext()) {
					String result = scannerDelimited.next();
					String token = result.split("mediaToken\\s*=\\s*'")[1].split("';")[0];
					int x = (Integer.parseInt(result.split("caching[^\\d]+")[1].replaceAll("\".+$", "")) ^ 345) - 10000;

					token = token.substring(x, token.length() - x).replaceAll("\\-", "+").replaceAll("_", "/");

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

					return parseMedia(new JSONObject(new String(Base64.decodeBase64(token))));
				}
			}
			catch (Exception e) {
				Log.e(getClass().getName(), "Error opening JSON guys: " + e.getMessage() + "\n" + e.getCause(), e);
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
					Log.e(getClass().getName(), "Error closing JSON guys: " + e.getMessage() + "\n" + e.getCause(), e);
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(SambaMedia media) {
			if (media == null) {
				listener.onMediaResponseError(new Exception("Media loaded returned null."));
				return;
			}

			listener.onMediaResponse(media);
		}

		private SambaMedia parseMedia(JSONObject json) {
			try {
				String qualifier = json.getString("qualifier").toLowerCase();

				if (!qualifier.equals("video"))//json.has("deliveryRules") ? )
					return null;

				SambaMedia media = new SambaMedia();
				JSONArray rules = json.has("deliveryRules") ? json.getJSONArray("deliveryRules") : null;
				JSONArray thumbs = json.has("thumbnails") ? json.getJSONArray("thumbnails") : null;
				String defaultOutput = json.getJSONObject("project").getString("defaultOutput");
				JSONObject rule = null;
				JSONArray outputs;
				JSONObject output;
				int i;

				media.title = json.getString("title");

				if (rules != null) {
					// looks for HLS delivery or the last taken
					for (i = rules.length(); i-- > 0; ) {
						rule = rules.getJSONObject(i);
						media.type = rule.getString("urlType").toLowerCase();

						if (media.type.equals("hls")) {
							break;
						}
					}

					if (media.type.equals("hls"))
						defaultOutput = "abr_hls";

					if (rule != null) {
						outputs = rule.getJSONArray("outputs");

						for (int j = outputs.length(); j-- > 0; ) {
							output = outputs.getJSONObject(j);

							if (!output.getString("outputName").equalsIgnoreCase(defaultOutput))
								media.url = output.getString("url");
						}
					}
				}
				else if (json.getJSONObject("liveOutput") != null)
					media.url = json.getJSONObject("liveOutput").getString("baseUrl");
//Log.i("req", json.getJSONObject("liveOutput") + " " + media.url);
				if (thumbs.length() > 0)
					media.thumbUrl = thumbs.getJSONObject(0).getString("url");

				return media;
			}
			catch (JSONException e) {
				Log.e(getClass().getName(), "Failed to search media", e);
			}

			return null;
		}
	}
}
