package com.sambatech.player;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.sambatech.player.event.SambaApiCallback;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaRequest;

import org.jose4j.base64url.internal.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
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
		new RequestMediaTask(callback).execute(request);
	}

	/**
	 * Requests several medias from server.
	 *
	 * @param requests - Several request data
	 * @param callback - Listener for server media response
	 */
	public void requestMedia(final SambaMediaRequest[] requests, final SambaApiCallback callback) {
		SambaApiCallback callbackReq = new SambaApiCallback() {
			private int counter = 0;
			private SambaMedia[] mediaList = new SambaMedia[requests.length];

			@Override
			public void onMediaResponse(SambaMedia media) {
				callback.onMediaResponse(mediaList[counter++] = media);

				if (counter == requests.length)
					callback.onMediaListResponse(mediaList);
			}

			@Override
			public void onMediaResponseError(String msg, SambaMediaRequest request) {
				callback.onMediaResponseError(msg, request);
			}
		};

		for (SambaMediaRequest req : requests)
			requestMedia(req, callbackReq);
	}

	private class RequestMediaTask extends AsyncTask<SambaMediaRequest, Void, SambaMedia> {
		private SambaApiCallback listener;

		public RequestMediaTask(SambaApiCallback listener) {
			this.listener = listener;
		}

		@Override
		protected SambaMedia doInBackground(SambaMediaRequest... params) {
			SambaMediaRequest request = params[0];
			String url = activity.getString(R.string.player_endpoint) + request.projectId +
					(request.mediaId != null ? "/" + request.mediaId : "?" +
							(request.streamUrls.length > 0 ? "alternateLive=" + request.streamUrls[0] : "streamName=" + request.streamName));
			InputStream inputStream = null;
			Scanner scanner = null;
			Scanner scannerDelimited = null;
			String errorMsg = "Failed to load media data.";

			try {
				inputStream = new URL(url).openStream();
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
				errorMsg = "Error opening server request: " + e.getMessage() + "\n" + e.getCause();
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
					errorMsg = "Error closing server request: " + e.getMessage() + "\n" + e.getCause();
				}
			}

			listener.onMediaResponseError(errorMsg, request);

			return null;
		}

		@Override
		protected void onPostExecute(SambaMedia media) {
			if (media == null)
				return;

			listener.onMediaResponse(media);
		}

		private SambaMedia parseMedia(JSONObject json) {
			try {
				String qualifier = json.getString("qualifier").toLowerCase();

				if (!qualifier.equals("video") && !qualifier.equals("live"))
					return null;

				SambaMedia media = new SambaMedia();
				JSONObject playerConfig = json.getJSONObject("playerConfig");

				media.title = json.getString("title");

				if (json.has("deliveryRules")) {
					JSONArray rules = json.getJSONArray("deliveryRules");
					String defaultOutput = json.getJSONObject("project").getString("defaultOutput");
					JSONObject rule = null;
					JSONArray outputs;
					JSONObject output;

					// looks for HLS delivery or the last taken
					for (int i = rules.length(); i-- > 0; ) {
						rule = rules.getJSONObject(i);
						media.type = rule.getString("urlType").toLowerCase();

						if (media.type.equals("hls"))
							break;
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
				else if (json.has("liveOutput")) {
					media.url = json.getJSONObject("liveOutput").getString("baseUrl");

					// media type relies on URL
					if (media.url.contains(".m3u8"))
						media.type = "hls";
				}

				if (json.has("thumbnails")) {
					JSONArray thumbs = json.getJSONArray("thumbnails");

					if (thumbs.length() > 0 && !thumbs.getJSONObject(0).isNull("url")) {
						/*Bitmap bmp = BitmapFactory.decodeStream(new URL(thumbs.getJSONObject(0).getString("url")).openStream());
						bmp.setDensity(Bitmap.DENSITY_NONE);
						media.thumb = new BitmapDrawable(activity.getResources(), bmp);*/
						media.thumb = Drawable.createFromStream(new URL(thumbs.getJSONObject(0).getString("url")).openStream(), "Thumbnail");
					}
				}

				if (playerConfig.has("theme"))
					media.themeColor = (int)Long.parseLong("FF" + playerConfig.getString("theme").replaceAll("^#?", ""), 16);

				if (media.thumb == null)
					media.thumb = ContextCompat.getDrawable(activity, R.drawable.ic_action_play);

				return media;
			}
			catch (Exception e) {
				Log.e(getClass().getName(), "Failed to search media", e);
			}

			return null;
		}
	}
}
