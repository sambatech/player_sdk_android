package com.sambatech.player.cast;

import android.content.Context;

import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.sambatech.player.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Leandro Zanol on 3/21/17.
 */
public final class CastOptionsProvider implements OptionsProvider {

	public static final String CUSTOM_NAMESPACE = "urn:x-cast:com.sambatech.player";

	@Override
	public CastOptions getCastOptions(Context context) {
		return new CastOptions.Builder()
				.setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
				//.setReceiverApplicationId(context.getString(R.string.cast_app_id))
				//.setSupportedNamespaces(new ArrayList<>(Collections.singletonList(CUSTOM_NAMESPACE)))
				.build();
	}

	@Override
	public List<SessionProvider> getAdditionalSessionProviders(Context context) {
		return null;
	}
}
