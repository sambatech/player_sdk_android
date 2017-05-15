package com.sambatech.player.cast;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.sambatech.player.R;
import com.sambatech.player.model.SambaMediaRequest;

import java.util.List;

/**
 * @author Leandro Zanol on 3/21/17.
 */
public final class CastOptionsProvider implements OptionsProvider {

	public static final String CUSTOM_NAMESPACE = "urn:x-cast:com.sambatech.player";

	// It can be configured before instantiating "SambaCast"
	public static String appId;
	public static String playerUrl;
	public static SambaMediaRequest.Environment environment;

	public CastOptionsProvider() {}

	@Override
	public CastOptions getCastOptions(Context context) {
		configProfile(context, SambaMediaRequest.Environment.PROD, false);
		/*List<String> supportedNamespaces = new ArrayList<>();
		supportedNamespaces.add(CUSTOM_NAMESPACE);*/

		return new CastOptions.Builder()
				//.setReceiverApplicationId(appId)
				.setReceiverApplicationId(appId)
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

	/**
	 * Configures cast params according to a predefined profile.
	 * Must be called BEFORE SambaCast is initialized.
	 * @param context The activity context
	 * @param environment The environment to match the profile values
	 */
	public static void configProfile(@NonNull Context context, @NonNull SambaMediaRequest.Environment environment) {
		configProfile(context, environment, true);
	}

	/**
	 * Configures cast params according to a predefined profile.
	 * Must be called BEFORE SambaCast is initialized.
	 * @param context The activity context
	 * @param environment The environment to match the profile values
	 * @param overwrite Overwrite existing configured values?
	 */
	public static void configProfile(@NonNull Context context, @NonNull SambaMediaRequest.Environment environment, boolean overwrite) {
		if (CastOptionsProvider.environment == null || overwrite) CastOptionsProvider.environment = environment;

		switch (environment) {
			case DEV:
				if (appId == null || overwrite) appId = context.getString(R.string.cast_app_id_dev);
				if (playerUrl == null || overwrite) playerUrl = context.getString(R.string.base_url_dev);
				break;
			case STAGING:
				if (appId == null || overwrite) appId = context.getString(R.string.cast_app_id_staging);
				if (playerUrl == null || overwrite) playerUrl = context.getString(R.string.base_url_staging);
				break;
			case PROD:
				if (appId == null || overwrite) appId = context.getString(R.string.cast_app_id_prod);
				if (playerUrl == null || overwrite) playerUrl = context.getString(R.string.base_url_prod);
				break;
		}
	}

	/*private static class ImagePickerImpl extends ImagePicker {

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
	}*/
}
