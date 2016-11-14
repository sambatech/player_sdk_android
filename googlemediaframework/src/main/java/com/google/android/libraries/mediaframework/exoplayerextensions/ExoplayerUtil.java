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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.google.android.exoplayer.ExoPlayerLibraryInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

/**
 * Utility methods for the Exoplayer extension.
 */
public class ExoplayerUtil {


  /**
   * The UUID of the Widevine DRM scheme.
   */
  public static final UUID WIDEVINE_UUID = new UUID(0xEDEF8BA979D64ACEL, 0xA3C827DCD51D21EDL);

  /**
   * Generate a User-Agent string that should be sent with HTTP requests. A User-Agent string is
   * used to provide information such as the operating system and version to a server when it makes
   * a request. The server can use this information to select the version of the content which is
   * best suited for your system. For instance, a server could use the User-Agent string provided by
   * this app to ensure that it returns a video format that works on Android.
   * @param context The context (ex {@link android.app.Activity} which is calling this method.
   * @return The User-Agent string.
   */
  public static String getUserAgent(Context context) {
    String versionName;
    try {
      String packageName = context.getPackageName();
      PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
      versionName = info.versionName;
    } catch (NameNotFoundException e) {
      versionName = "?";
    }
    return "ExoPlayerDemo/" + versionName + " (Linux;Android " + Build.VERSION.RELEASE +
        ") " + "ExoPlayerLib/" + ExoPlayerLibraryInfo.VERSION;
  }

  /**
   * Do an HTTP POST and return the data as a byte array.
   */
  public static byte[] executePost(String url, byte[] data, Map<String, String> requestProperties)
      throws UnsupportedDrmException, IOException {
    HttpURLConnection urlConnection = null;
    //long d = new Date().getTime();
    try {
      urlConnection = (HttpURLConnection) new URL(url).openConnection();
      urlConnection.setRequestMethod("POST");
      urlConnection.setDoOutput(data != null);
      urlConnection.setDoInput(true);
      if (requestProperties != null) {
        for (Map.Entry<String, String> requestProperty : requestProperties.entrySet()) {
          urlConnection.setRequestProperty(requestProperty.getKey(), requestProperty.getValue());
        }
      }
      if (data != null) {
        OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
        out.write(data);
        out.close();
      }

      urlConnection.connect();

      int responseCode = urlConnection.getResponseCode();

      if (responseCode < 200 || responseCode > 299) {
        printRequestData(urlConnection);
        throw new UnsupportedDrmException(UnsupportedDrmException.REASON_INVALID_SERVER_CODE);
      }

      InputStream in = new BufferedInputStream(urlConnection.getInputStream());
      return convertInputStreamToByteArray(in);
    } finally {
      if (urlConnection != null) {
        urlConnection.disconnect();
      }
      //Log.i("asdf", String.format("%d", (new Date()).getTime() - d));
    }
  }

  /**
   * Write the contents of an input stream into a byte array.
   * @param inputStream The stream to convert into a byte array.
   * @return A byte array containing the contents of the input stream.
   * @throws IOException
   */
  private static byte[] convertInputStreamToByteArray(InputStream inputStream) throws IOException {
    byte[] bytes = null;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte data[] = new byte[1024];
    int count;
    while ((count = inputStream.read(data)) != -1) {
      bos.write(data, 0, count);
    }
    bos.flush();
    bos.close();
    inputStream.close();
    bytes = bos.toByteArray();
    return bytes;
  }

  private static void printRequestData(HttpURLConnection con) {
    if (con == null) return;

    String headers = "";
    String content = "";
    String error = "";
    String requestMethod = "";
    String permission = "";
    int responseCode = -1;
    String responseMessage = "";

    Scanner ctn = null;
    Scanner ctnd = null;
    Scanner escn = null;
    Scanner escnd = null;

    try {
      requestMethod = con.getRequestMethod();
      permission = con.getPermission().toString();
      responseCode = con.getResponseCode();
      responseMessage = con.getResponseMessage();

      for (Map.Entry<String, List<String>> kv : con.getHeaderFields().entrySet())
        headers += kv.getKey() + ": " + kv.getValue() + "\n";

      ctn = new Scanner((InputStream) con.getContent());
      ctnd = ctn.useDelimiter("\\A");

      content = ctnd.hasNext() ? ctnd.next() : "";

      InputStream err = con.getErrorStream();

      if (err != null) {
        escn = new Scanner(err);
        escnd = escn.useDelimiter("\\A");

        error = escnd.hasNext() ? escnd.next() : "";
      }
    }
    catch (Exception e) {
      error = "DRM connection failed.";
    }
    finally {
      Log.i("DRM", String.format("URL: %s\nRequest method: %s\nPermission: %s\nResponse code: %s\nResponse msg: %s\nHeaders: %s\nContent: %s\nError: %s",
              con.getURL(), requestMethod, permission, responseCode, responseMessage, headers, content, error));

      if (ctn != null) ctn.close();
      if (ctnd != null) ctnd.close();
      if (escn != null) escn.close();
      if (escnd != null) escnd.close();
    }
  }
}