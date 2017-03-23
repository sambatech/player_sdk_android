package com.sambatech.player;

import android.app.MediaRouteButton;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;

import com.google.android.gms.cast.framework.AppVisibilityListener;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;

/**
 * It must have a 1-to-1 relationship with activities.
 *
 * There are some steps to integrate:
 *
 * 1. Initialize inside "onCreate" state
 * 2. Notify "onResume" state
 * 3. Notify "onPause" state
 *
 * @author Leandro Zanol on 23/03/17.
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
		}

		@Override
		public void onSessionStartFailed(CastSession castSession, int i) {
			Log.i("cast", "start failed " + i);
		}

		@Override
		public void onSessionEnding(CastSession castSession) {
			Log.i("cast", "ending");
		}

		@Override
		public void onSessionEnded(CastSession castSession, int i) {
			Log.i("cast", "ended");
		}

		@Override
		public void onSessionResuming(CastSession castSession, String s) {
			Log.i("cast", "resuming " + s);
		}

		@Override
		public void onSessionResumed(CastSession castSession, boolean b) {
			Log.i("cast", "resumed " + b);
		}

		@Override
		public void onSessionResumeFailed(CastSession castSession, int i) {
			Log.i("cast", "resume failed " + i);
		}

		@Override
		public void onSessionSuspended(CastSession castSession, int i) {
			Log.i("cast", "suspended " + i);
		}
	};

	private boolean enabledOnPlayer;
	private MediaRouteButton castButton;
	private SessionManager sessionManager;
	private CastContext castContext;

	/**
	 * Initializes Chromecast SDK.
	 * Must be called inside "Activity.onCreate".
	 */
	public SambaCast(@NonNull Context context) {
		this(context, true);
	}

	/**
	 * Initializes Chromecast SDK and informs whether to enable support for the player itself.
	 * Must be called inside "Activity.onCreate".
	 * @param enabledOnPlayer Whether to enable Chromecast support in the player view
	 */
	public SambaCast(@NonNull Context context, boolean enabledOnPlayer) {
		this.enabledOnPlayer = enabledOnPlayer;

		LayoutInflater inflater = LayoutInflater.from(context);
		castButton = (MediaRouteButton)inflater.inflate(R.layout.cast_button, null);
		castContext = CastContext.getSharedInstance(context);
		sessionManager = castContext.getSessionManager();
	}

	public MediaRouteButton getButton() {
		return castButton;
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
}
