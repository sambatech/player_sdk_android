package com.sambatech.sample.utils;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * @author zanol - 11/8/16
 */
public class Helpers {

	public static void requestUrl(String url, Callback callback) {
		try {
			requestUrl((HttpURLConnection)new URL(url).openConnection(), callback);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void requestUrl(HttpURLConnection con, final Callback callback) {
		new AsyncTask<HttpURLConnection, Void, String>() {
			@Override
			protected String doInBackground(HttpURLConnection... params) {
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

					return scannerDelimited.hasNext() ? scannerDelimited.next() : "";
				}
				catch (Exception e) {
					e.printStackTrace();
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

				return null;
			}

			@Override
			protected void onPostExecute(String s) {
				callback.call(s);
			}
		}.execute(con);
	}

	public interface Callback {
		void call(String response);
	}
}

