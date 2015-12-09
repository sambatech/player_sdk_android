package com.sambatech.player;

import android.os.AsyncTask;
import android.util.Log;

import com.sambatech.player.event.SambaApiListener;
import com.sambatech.player.model.SambaMedia;

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

	private boolean inited = false;
	private String accessToken;

	public SambaApi(String accessToken) {
		// TODO: validar "accessToken"
		this.accessToken = accessToken;
	}

	/**
	 *
	 * Requests a specific media from server.
	 *
	 * @param projectId - (player hash)
	 * @param mediaId -
	 */
	public void requestMedia(String projectId, String mediaId, SambaApiListener listener) {
		new RequestMediaTask(listener).execute("http://playerapitest2.liquidplatform.com:7091/embed/" + projectId + "/" + mediaId);
	}

	public void requestLive(String projectId, String streamName, SambaApiListener listener) {
		requestLive(projectId, streamName, null, listener);
	}

	public void requestLive(String projectId, String streamName, String streamUrl, SambaApiListener listener) {
		new RequestMediaTask(listener).execute("http://playerapitest2.liquidplatform.com:7091/embed/" + projectId + "?" +
				(streamName != null ? "streamName=" + streamName : "alternateLive=" + streamUrl));
	}

	public void requestMediaList(String projectId, String mediaId, SambaApiListener listener) {

	}

	private class RequestMediaTask extends AsyncTask<String, Void, SambaMedia> {

		private SambaApiListener listener;

		public RequestMediaTask(SambaApiListener listener) {
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
				Log.e(getClass().getName(), "Error opening JSON guys", e);
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
					Log.e(getClass().getName(), "Error closing JSON guys", e);
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(SambaMedia media) {
			if (media == null || this.listener == null)
				return;

			this.listener.onMediaResponse(media);
		}

		private SambaMedia parseMedia(JSONObject json) {
			try {
				if (!json.getString("qualifier").equalsIgnoreCase("video"))
					return null;

				SambaMedia media = new SambaMedia();
				JSONArray rules = json.getJSONArray("deliveryRules");
				JSONArray thumbs = json.getJSONArray("thumbnails");
				String defaultOutput = json.getJSONObject("project").getString("defaultOutput");
				JSONObject rule = null;
				JSONArray outputs;
				JSONObject output;
				int i;

				media.title = json.getString("title");

				// looks for HLS delivery or the last taken
				for (i = rules.length(); i-- > 0;) {
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
