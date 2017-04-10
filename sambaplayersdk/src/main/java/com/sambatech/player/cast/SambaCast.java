package com.sambatech.player.cast;

import android.app.MediaRouteButton;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;

import com.google.android.gms.cast.framework.AppVisibilityListener;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.sambatech.player.R;
import com.sambatech.player.event.SambaCastListener;

import java.io.IOException;

/**
 * It must have a 1-to-1 relationship with activities to avoid memory leakage.
 * It could not be a Singleton for instance, static objects cannot hold Context references
 * (like widgets, resources, etc.), they must die together with their activity.
 *
 * There are some steps to integrate:
 *
 * 1. Initialize inside "onCreate" state
 * 2. Notify "onResume" state
 * 3. Notify "onPause" state
 * 4. Pass the instance to the SambaPlayer
 *
 * @author Leandro Zanol on 23/03/17
 */
public final class SambaCast {

	private final CastStateListener stateListener = new CastStateListener() {
		@Override
		public void onCastStateChanged(int i) {
			Log.i("cast", "state: " + i);
		}
	};

	private final AppVisibilityListener appVisibilityListener = new AppVisibilityListener() {
		@Override
		public void onAppEnteredForeground() {
			Log.i("cast", "foreground");
		}

		@Override
		public void onAppEnteredBackground() {
			Log.i("cast", "background");
		}
	};

	private final SessionManagerListener<CastSession> sessionManagerListener = new SessionManagerListener<CastSession>() {
		@Override
		public void onSessionStarting(CastSession castSession) {
			Log.i("cast", "starting");
		}

		@Override
		public void onSessionStarted(CastSession castSession, String s) {
			Log.i("cast", "started " + s);
			onApplicationConnected(castSession);
		}

		@Override
		public void onSessionStartFailed(CastSession castSession, int i) {
			Log.i("cast", "start failed " + i);
			onApplicationDisconnected();
		}

		@Override
		public void onSessionEnding(CastSession castSession) {
			Log.i("cast", "ending");
		}

		@Override
		public void onSessionEnded(CastSession castSession, int i) {
			Log.i("cast", "ended");
			onApplicationDisconnected();
		}

		@Override
		public void onSessionResuming(CastSession castSession, String s) {
			Log.i("cast", "resuming " + s);
		}

		@Override
		public void onSessionResumed(CastSession castSession, boolean b) {
			Log.i("cast", "resumed " + b);
			onApplicationConnected(castSession);
		}

		@Override
		public void onSessionResumeFailed(CastSession castSession, int i) {
			Log.i("cast", "resume failed " + i);
			onApplicationDisconnected();
		}

		@Override
		public void onSessionSuspended(CastSession castSession, int i) {
			Log.i("cast", "suspended " + i);
		}

		private void onApplicationConnected(CastSession castSession) {
			if (listener != null)
				listener.onConnected(castSession);
		}

		private void onApplicationDisconnected() {
			if (listener != null)
				listener.onDisconnected();
		}
	};

	private MediaRouteButton castButton;
	private SessionManager sessionManager;
	private CastContext castContext;
	private SambaCastListener listener;

	/**
	 * Initializes Chromecast SDK.
	 * Must be called inside "Activity.onCreate".
	 */
	public SambaCast(@NonNull Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		castButton = (MediaRouteButton)inflater.inflate(R.layout.cast_button, null);
		castContext = CastContext.getSharedInstance(context);
		sessionManager = castContext.getSessionManager();
	}

	/**
	 * Returns the cast button to be added on some view.
	 * @return The cast button instance
	 */
	public MediaRouteButton getButton() {
		return castButton;
	}

	/**
	 * Sets the listener to handle cast events.
	 * To remove pass null.
	 * @param listener The instance of the listener
	 */
	public void setEventListener(SambaCastListener listener) {
		this.listener = listener;
	}

	/**
	 * Called within "Activity.onResume()".
	 */
	public void notifyActivityResume() {
		castContext.addCastStateListener(stateListener);
		castContext.addAppVisibilityListener(appVisibilityListener);
		sessionManager.addSessionManagerListener(sessionManagerListener, CastSession.class);
	}

	/**
	 * Called within "Activity.onPause()".
	 */
	public void notifyActivityPause() {
		castContext.removeCastStateListener(stateListener);
		castContext.removeAppVisibilityListener(appVisibilityListener);
		sessionManager.removeSessionManagerListener(sessionManagerListener, CastSession.class);
	}


	public void pauseCast(){
		if(hasMediaSession(true)) {
			sessionManager.getCurrentCastSession().sendMessage(CastOptionsProvider.CUSTOM_NAMESPACE, "{\"type\": \"pause\"}").setResultCallback(new ResultCallbacks<Status>() {
				@Override
				public void onSuccess(@NonNull Status status) {
					Log.i("message", "Message Sent OK: namespace:" + CastOptionsProvider.CUSTOM_NAMESPACE + " message:" + CastOptionsProvider.CUSTOM_NAMESPACE);
				}

				@Override
				public void onFailure(@NonNull Status status) {
					Log.i("message", "Sending message failed");
				}
			});
		}
	}

	public void playCast(){
		if(hasMediaSession(true)) {
			sessionManager.getCurrentCastSession().sendMessage(CastOptionsProvider.CUSTOM_NAMESPACE, "{\"type\": \"play\"}").setResultCallback(new ResultCallbacks<Status>() {
				@Override
				public void onSuccess(@NonNull Status status) {
					Log.i("message", "Message Sent OK: namespace:" + CastOptionsProvider.CUSTOM_NAMESPACE + " message:" + CastOptionsProvider.CUSTOM_NAMESPACE);
				}

				@Override
				public void onFailure(@NonNull Status status) {
					Log.i("message", "Sending message failed");
				}
			});
		}

		//seekTo(0);
	}
	public void seekTo(int posisiton){
		String seekRequest = String.format("{\"type\": \"seek\", \"data\": %d }", posisiton/1000);
		if(hasMediaSession(true)) {
			if(hasMediaSession(true)) {
				sessionManager.getCurrentCastSession().sendMessage(CastOptionsProvider.CUSTOM_NAMESPACE, seekRequest).setResultCallback(new ResultCallbacks<Status>() {
					@Override
					public void onSuccess(@NonNull Status status) {
						Log.i("message", "Message Sent OK: namespace:" + CastOptionsProvider.CUSTOM_NAMESPACE + " message:" + CastOptionsProvider.CUSTOM_NAMESPACE);
					}

					@Override
					public void onFailure(@NonNull Status status) {
						Log.i("message", "Sending message failed");
					}
				});
			}
		}
	}

	public void registerDeviceForProgress(boolean register){
		String resgiterRequest = String.format("{\"type\": \"registerForProgressUpdate\", \"data\": %s }", register==true? "true" : "false");
		if(hasMediaSession(true)) {
				sessionManager.getCurrentCastSession().sendMessage(CastOptionsProvider.CUSTOM_NAMESPACE, resgiterRequest).setResultCallback(new ResultCallbacks<Status>() {
					@Override
					public void onSuccess(@NonNull Status status) {
						Log.i("message", "Message Sent OK: namespace:" + CastOptionsProvider.CUSTOM_NAMESPACE + " message:" + CastOptionsProvider.CUSTOM_NAMESPACE);
					}

					@Override
					public void onFailure(@NonNull Status status) {
						Log.i("message", "Sending message failed");
					}
				});
		}
	}


	public void setMute(boolean mute){
		try {
			sessionManager.getCurrentCastSession().setMute(mute);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setVolume(double volume){
		//sessionManager.getCurrentCastSession().getRemoteMediaClient().setStreamVolume(volume);
		try {
			sessionManager.getCurrentCastSession().setVolume(volume);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean hasMediaSession(boolean validateCastConnectingState) {
		if (sessionManager.getCurrentCastSession() == null) {
			return false;
		}
		boolean isCastSessionValid = sessionManager.getCurrentCastSession().isConnected();
		if (validateCastConnectingState) {
			boolean isCastSessionInConnectingMode = sessionManager.getCurrentCastSession().isConnecting();
			if (isCastSessionInConnectingMode) {
				return false; // no session to work with
			}
		}
		return isCastSessionValid;
	}

}
