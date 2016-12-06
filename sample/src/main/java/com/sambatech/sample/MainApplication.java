package com.sambatech.sample;

import android.app.Application;
import android.content.Context;

import com.sambatech.sample.utils.Helpers;

public class MainApplication extends Application {
    private static MainApplication _instance;
	private static String _externalIp = "";

    @Override
    public void onCreate() {
        super.onCreate();

        _instance = this;

	    loadIp();
    }

    public static Context getAppContext() {
        return _instance.getApplicationContext();
    }

	public static String getExternalIp() {
		return _externalIp;
	}

	private void loadIp() {
		Helpers.requestUrl("https://api.ipify.org", new Helpers.Callback() {
			@Override
			public void call(String response) {
				_externalIp = response.trim();
			}
		});
	}
}