package com.sambatech.player.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.sambatech.player.model.SambaMedia;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.Scanner;

/**
 * Useful stuff for whole SambaPlayer project.
 *
 * @author Leandro Zanol - 29/12/15
 */
public final class Helpers {

	public static String getSessionId() {
		StringBuilder s = new StringBuilder();

		for (int i = 8; i-- > 0;) {
			s.append(getSessionComponent());

			switch (i) {
				case 1:
				case 3:
				case 5:
				case 7:
					s.append("-");
					break;
			}
		}

		return s.toString();
	}

	public static boolean isDeviceRooted() {
		return RootUtil.isDeviceRooted();
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

		return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
	}

	private static String getSessionComponent() {
		return Integer.toHexString((int)((Math.random() + 1) * 0x10000)).substring(1);
	}

	//Order outputs
	public static class CustomSorter implements Comparator<SambaMedia.Output> {

		@Override
		public int compare(SambaMedia.Output lhs, SambaMedia.Output rhs) {
			return lhs.position - rhs.position;
		}
	}

	public static void requestUrl(String url, RequestCallback callback) {
		try {
			requestUrl((HttpURLConnection)new URL(url).openConnection(), callback);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void requestUrl(HttpURLConnection con, final RequestCallback callback) {
		new AsyncTask<HttpURLConnection, Void, Response>() {
			@Override
			protected Response doInBackground(HttpURLConnection... params) {
				HttpURLConnection con = params[0];
				InputStream inputStream = null;
				Scanner scanner = null;
				Scanner scannerDelimited = null;

				try {
					con.connect();

					/*System.out.println("Request method: " + con.getRequestMethod());
					System.out.println("Permission: " + con.getPermission());
					System.out.println("Response code: " + con.getResponseCode());
					System.out.println("Response msg: " + con.getResponseMessage());

					s = "";
					for (Map.Entry<String, List<String>> kv : con.getHeaderFields().entrySet())
						s += kv.getKey() + ": " + kv.getValue() + "\n";
					System.out.println(s);*/

					inputStream = con.getInputStream();
					scanner = new Scanner(inputStream);
					scannerDelimited = scanner.useDelimiter("\\A");

					return new Response(scannerDelimited.hasNext() ? scannerDelimited.next() : "", null);
				}
				catch (Exception e) {
					return new Response(null, e);
				}
				finally {
					try {
						if (con != null) con.disconnect();
						if (inputStream != null) inputStream.close();
						if (scanner != null) scanner.close();
						if (scannerDelimited != null) scannerDelimited.close();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			protected void onPostExecute(Response response) {
				if (response.error != null) {
					callback.onError(response.error, response.response);
					return;
				}

				callback.onSuccess(response.response);
			}
		}.execute(con);
	}

	public interface RequestCallback {
		void onSuccess(String response);
		void onError(Exception e, String response);
	}

	private static class Response {
		public final String response;
		public final Exception error;

		Response(String response, Exception error) {
			this.response = response;
			this.error = error;
		}
	}
}

/**
 * @author Kevin Kowalewski
 */
class RootUtil {
	static boolean isDeviceRooted() {
		return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
	}

	private static boolean checkRootMethod1() {
		String buildTags = android.os.Build.TAGS;
		return buildTags != null && buildTags.contains("test-keys");
	}

	private static boolean checkRootMethod2() {
		String[] paths = { "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
				"/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"};
		for (String path : paths) {
			if (new File(path).exists()) return true;
		}
		return false;
	}

	private static boolean checkRootMethod3() {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(new String[] { "/system/xbin/which", "su" });
			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			return in.readLine() != null;
		} catch (Throwable t) {
			return false;
		} finally {
			if (process != null) process.destroy();
		}
	}
}
