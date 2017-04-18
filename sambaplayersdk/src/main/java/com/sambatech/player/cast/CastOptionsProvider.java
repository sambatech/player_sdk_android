package com.sambatech.player.cast;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.google.android.gms.cast.framework.media.CastMediaOptions;
import com.google.android.gms.cast.framework.media.ImagePicker;
import com.google.android.gms.cast.framework.media.MediaIntentReceiver;
import com.google.android.gms.cast.framework.media.NotificationOptions;
import com.google.android.gms.common.images.WebImage;
import com.sambatech.player.R;
import com.sambatech.player.model.SambaMediaRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Leandro Zanol on 3/21/17.
 */
public final class CastOptionsProvider implements OptionsProvider {

	public static final String CUSTOM_NAMESPACE = "urn:x-cast:com.sambatech.player";
	public static final String CAST_APP_ID_ENVIROMENT_KEY = "CAST_APP_ID_ENVIROMENT_KEY";

	// It can be configured before instantiating "SambaCast"
	public static String applicationId = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;

	@Override
	public CastOptions getCastOptions(Context context) {
		List<String> supportedNamespaces = new ArrayList<>();
		supportedNamespaces.add(CUSTOM_NAMESPACE);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		String enviromentString = sharedPreferences.getString(CAST_APP_ID_ENVIROMENT_KEY, SambaMediaRequest.Environment.PROD.toString());
		SambaMediaRequest.Environment environment =  SambaMediaRequest.Environment.stringToEnviroment(enviromentString);

		String castApplicationID;
		switch (environment) {
			case DEV:
				castApplicationID = context.getString(R.string.cast_app_id_dev);
				break;
			case STAGING:
				castApplicationID = context.getString(R.string.cast_app_id_staging);
				break;
			case PROD:
			default:
				castApplicationID = context.getString(R.string.cast_app_id);
				break;
		}

		return new CastOptions.Builder()
				//.setReceiverApplicationId(applicationId)
				.setReceiverApplicationId(castApplicationID)
				//.setCastMediaOptions(mediaOptions)
				.setResumeSavedSession(true)
				.setStopReceiverApplicationWhenEndingSession(true)
				.setEnableReconnectionService(true)
				//.setSupportedNamespaces(supportedNamespaces)
				.build();
	}

	@Override
	public List<SessionProvider> getAdditionalSessionProviders(Context context) {
		return null;
	}

	private static class ImagePickerImpl extends ImagePicker {

		@Override
		public WebImage onPickImage(MediaMetadata mediaMetadata, int type) {
			if ((mediaMetadata == null) || !mediaMetadata.hasImages()) {
				return null;
			}
			List<WebImage> images = mediaMetadata.getImages();
			if (images.size() == 1) {
				return images.get(0);
			} else {
				if (type == ImagePicker.IMAGE_TYPE_MEDIA_ROUTE_CONTROLLER_DIALOG_BACKGROUND) {
					return images.get(0);
				} else {
					return images.get(1);
				}
			}
		}
	}
}
