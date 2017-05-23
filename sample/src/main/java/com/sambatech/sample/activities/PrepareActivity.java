package com.sambatech.sample.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Activity to prepare the environment to run the app.
 * @author zanol on 5/23/17
 */
public final class PrepareActivity extends Activity {

	private static final String TAG = "PrepareActivity";

	@Override
	protected void onResume() {
		super.onResume();
		checkAndUpdateGPSIfNeeded();
	}

	/**
	 * Verify whether Google Play Services (GPS) is up-to-date and try to update it.
	 */
	private void checkAndUpdateGPSIfNeeded() {
		GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
		int status = apiAvailability.isGooglePlayServicesAvailable(this);

		Log.i(TAG, "checkAndUpdateGPSIfNeeded: " + apiAvailability.getErrorString(status));

		if (status == ConnectionResult.SUCCESS) {
			startActivity(new Intent(PrepareActivity.this, MainActivity.class));
			finish();
			return;
		}

		// update GPS to latest version
		Dialog errorDialog = apiAvailability.getErrorDialog(this, status, 0);
		errorDialog.setCancelable(false);
		errorDialog.show();
	}
}
