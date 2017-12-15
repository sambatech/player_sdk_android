package com.sambatech.sample.activities;

import android.app.Activity;
import android.drm.DrmInfoRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sambatech.player.SambaApi;
import com.sambatech.player.SambaPlayer;
import com.sambatech.player.cast.CastOptionsProvider;
import com.sambatech.player.cast.SambaCast;
import com.sambatech.player.event.SambaApiCallback;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.model.AdsSettings;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaConfig;
import com.sambatech.player.model.SambaMediaRequest;
import com.sambatech.player.model.SambaPlayerError;
import com.sambatech.sample.MainApplication;
import com.sambatech.sample.R;
import com.sambatech.sample.model.EntitlementScheme;
import com.sambatech.sample.model.MediaInfo;
import com.sambatech.sample.utils.Helpers;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * The activity where the media player is shown.
 */
public class MediaItemActivity extends Activity {

    private MediaInfo activityMedia;

    @BindView(R.id.title)
    TextView titleView;

    @BindView(R.id.description)
    TextView descView;

    @BindView(R.id.status)
    TextView status;

    @BindView(R.id.samba_player)
    SambaPlayer player;

	@BindView(R.id.progressbar_view)
	LinearLayout loading;

	@BindView(R.id.loading_text)
	TextView loadingText;

	@BindView(R.id.validation_controlbar)
	View validationControlbar;

	@BindView(R.id.policies)
	Spinner policySpinner;

    @BindView(R.id.session_controls)
    LinearLayout sessionControls;

    @BindView(R.id.auth_controls)
    LinearLayout authControls;

	private EntitlementScheme entitlementScheme;
	private SambaMediaConfig media;
	private SambaCast sambaCast;
	//private long ti; // benchmark

	/**
	 * Player Events
	 *
	 * onLoad - triggered when the media is loaded
	 * onPlay - triggered when the media is played
	 * onPause - triggered when the media is paused
	 * onStop - triggered when the player is destroyed
	 * onFinish - triggered when the media is finished
	 * onFullscreen - triggered when the fullscreen is enabled
	 * onFullscreenExit - triggered when the user exit the fullscreen
	 *
	 */
	private SambaPlayerListener playerListener = new SambaPlayerListener() {
		@Override
		public void onLoad(SambaEvent e) {
			//Log.i("bench", String.format("Load time: %s", new Date().getTime() - ti));
			String token = player.getMedia().drmToken != null ? " (token: \"" + player.getMedia().drmToken.substring(0, 10) + "...\")" : "";
			status.setText(String.format("Status: %s%s", e.getType(), token));
		}

		@Override
		public void onPlay(SambaEvent e) {
			//Log.i("bench", String.format("Play time: %s", new Date().getTime() - ti));
			status.setText(String.format("Status: %s", e.getType()));
		}

		@Override
		public void onPause(SambaEvent e) {
			status.setText(String.format("Status: %s", e.getType()));
		}

		@Override
		public void onStop(SambaEvent e) {
			status.setText(String.format("Status: %s", e.getType()));
		}

		@Override
		public void onFinish(SambaEvent e) {
			status.setText(String.format("Status: %s", e.getType()));
		}

		@Override
		public void onFullscreen(SambaEvent e) {
			status.setText(String.format("Status: %s", e.getType()));
			getActionBar().hide();
		}

		@Override
		public void onFullscreenExit(SambaEvent e) {
			status.setText(String.format("Status: %s", e.getType()));
			getActionBar().show();
		}

		@Override
		public void onError(SambaEvent e) {
			String msg = String.format("%s", e.getType() + " - " + e.getData());
			status.setText(msg);

			if (e.getData() instanceof SambaPlayerError) {
				final SambaPlayerError error = (SambaPlayerError)e.getData();

				if (error.getException() != null)
					msg = error.getException().getMessage();
			}

			Log.e("MediaItemActivity", msg);
		}

		@Override
		public void onProgress(SambaEvent event) {
			status.setText(String.format("Status: %s", event.getType()));
			Log.d("Player Time", "current: " + player.getCurrentTime() + " total: " + player.getDuration());

			Log.d("Player CurrentOut", "current: " + player.getCurrentOutputIndex());

			Log.d("Player CurrentCap", "current cap: " + player.getCurrentCaptionIndex() + " lenguage: " + player.getCaption());
		}
	};

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_item);

        ButterKnife.bind(this);

		if (getActionBar() != null)
      		getActionBar().setDisplayHomeAsUpEnabled(true);

	    if (activityMedia == null) {
		    activityMedia = EventBus.getDefault().removeStickyEvent(MediaInfo.class);
		    loadingText.setText(String.format("Carregando mídia: %s", activityMedia.getTitle().split("\\.", 2)[0]));
	    }

	    // cast
		CastOptionsProvider.configProfile(this, activityMedia.getEnvironment());

		// testing...
		//CastOptionsProvider.appId = "411C092F";
		//CastOptionsProvider.playerUrl = "192.168.0.113:8000/";

		sambaCast = new SambaCast(this);

		SambaEventBus.subscribe(playerListener);
		requestMedia(activityMedia);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		player.pause();
    }

	@Override
	protected void onResume() {
		super.onResume();
		sambaCast.notifyActivityResume();
	}

	@Override
    protected void onPause() {
        super.onPause();
		sambaCast.notifyActivityPause();


		if(!sambaCast.isCasting()) {
			//if (player != null && player.hasStarted())
				player.pause();
		}
    }

	/**
	 * Request the given media
	 * @param mediaInfo - Liquid media object
	 */
    private void requestMedia(final MediaInfo mediaInfo) {
	    SambaApiCallback callback = new SambaApiCallback() {
		    //Success response of one media only. Returns a SambaMedia object
		    @Override
		    public void onMediaResponse(SambaMedia media) {
			    if (media == null)
			    	return;

			    // media injection
			    if (mediaInfo.getUrl() != null)
				    media.url = mediaInfo.getUrl();

			    // DRM
			    if (mediaInfo.getEntitlementScheme() != null) {
				    MediaItemActivity.this.media = (SambaMediaConfig)media;
				    MediaItemActivity.this.entitlementScheme = mediaInfo.getEntitlementScheme();

				    loading.setVisibility(View.GONE);
				    titleView.setVisibility(View.VISIBLE);
				    titleView.setText(media.title);

				    validationControlbar.setVisibility(View.VISIBLE);

                    // DRM controls
                    if (((SambaMediaConfig) media).drmRequest != null) {
                        sessionControls.setVisibility(View.VISIBLE);
                        authControls.setVisibility(View.VISIBLE);
                    }

				    return;
			    }

			    media.isAudioOnly = mediaInfo.isAudioLive() || "audio".equalsIgnoreCase(mediaInfo.getQualifier());

			    loadPlayer(media);
		    }

		    //Response error
		    @Override
		    public void onMediaResponseError(Exception e, SambaMediaRequest request) {
			    Toast.makeText(MediaItemActivity.this, e.getMessage() + " " + request, Toast.LENGTH_SHORT).show();
		    }
	    };

	    // if injected media
	    if (mediaInfo.getProjectHash() == null && mediaInfo.getUrl() != null && !mediaInfo.getUrl().isEmpty()) {
		    SambaMediaConfig m = new SambaMediaConfig();
		    m.url = mediaInfo.getUrl();
		    m.title = mediaInfo.getTitle();
		    m.type = mediaInfo.getType();
		    m.isAudioOnly = "audio".equalsIgnoreCase(mediaInfo.getQualifier());
		    callback.onMediaResponse(m);
		    return;
	    }

	    //Instantiates the SambaApi class
        final SambaApi api = new SambaApi(this, "token");

	    //Instantiate a unique request. Params: playerHash, mediaId, streamName, streamUrl ( alternateLive on our browser version )
        final SambaMediaRequest sbRequest = mediaInfo.getLiveChannelId() != null ?
		        new SambaMediaRequest(mediaInfo.getProjectHash(), mediaInfo.getLiveChannelId(), true) :
		        new SambaMediaRequest(mediaInfo.getProjectHash(), mediaInfo.getId(), null,
				        mediaInfo.getStreamUrl(), mediaInfo.getBackupUrls(), mediaInfo.isAudioLive());

		sbRequest.environment = mediaInfo.getEnvironment();

	    descView.setText((mediaInfo.getDescription() != null ? mediaInfo.getDescription() : ""));

        sbRequest.protocol = SambaMediaRequest.Protocol.HTTP;
        api.requestMedia(sbRequest, callback);
    }

    private void loadPlayer(final SambaMedia media) {
	    if (media == null)
	    	return;

	    // injected ad
	    if (activityMedia.getAdUrl() != null)
		    media.adUrl = activityMedia.getAdUrl();

		loading.setVisibility(View.GONE);
		titleView.setVisibility(View.VISIBLE);
		titleView.setText(media.title);

		// If audio, we recommend you to customize the player's height
		if (media.isAudioOnly) {
			//player.getLayoutParams().height = (int)(66.7f * getResources().getDisplayMetrics().density);
			//player.setLayoutParams(player.getLayoutParams());
		}

		final AdsSettings settings = new AdsSettings(20000f, 5);

	    //settings.rendering.setMimeTypes(Arrays.asList("text/html", "video/mpeg", "image/jpeg"));
	    //settings.rendering.setBitrateKbps(200000);
	    //settings.debugMode = true;
	    media.adsSettings = settings;

		// enabling Chromecast on player
		player.setSambaCast(sambaCast);
	    //player.setControlsVisibility(activityMedia.isControlsEnabled());
	    player.setMedia(media);

		//ti = new Date().getTime();

		if (activityMedia.isAutoPlay())
			player.play();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		destroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@OnClick(R.id.play) public void playHandler() {
		if (player != null)
			player.play();
	}

	@OnClick(R.id.pause) public void pauseHandler() {
		if (player != null)
			player.pause();
	}

	@OnClick(R.id.hide_controls) public void hideControlsHandler() {
		//if (player != null)
			//player.setHideControls(SambaPlayer.Controls.SEEKBAR, SambaPlayer.Controls.FULLSCREEN, SambaPlayer.Controls.MENU);
	}

	@OnClick(R.id.create_session) public void createSessionHandler() {
		if (entitlementScheme == null || media == null ||
				media.drmRequest == null) return;

		final DrmInfoRequest drmRequest = media.drmRequest;

		status.setText("Creating session...");

		try {
			HttpURLConnection con = (HttpURLConnection)new URL(String.format("%sservices/CreateSession?CrmId=sambatech&UserId=samba",
					getString(R.string.drm_url))).openConnection();

			con.setRequestMethod("POST");
			con.addRequestProperty("MAN-user-id", "app@sambatech.com");
			con.addRequestProperty("MAN-user-password", "c5kU6DCTmomi9fU");

			Helpers.requestUrl(con, new Helpers.Callback() {
				@Override
				public void call(String response) {
					try {
						Document parse = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(response.getBytes()));
						NamedNodeMap attributes = parse.getElementsByTagName("Session").item(0).getAttributes();
						String sessionId = attributes.getNamedItem("SessionId").getTextContent();

						drmRequest.put("SessionId", sessionId);
						drmRequest.put("Ticket", attributes.getNamedItem("Ticket").getTextContent());

						// for manually injected DRM media
						if (entitlementScheme.contentId != null)
							drmRequest.put("ContentId", entitlementScheme.contentId);

						status.setText(String.format("Session: %s", sessionId));
					}
					catch (Exception e) {
						Log.e(MediaItemActivity.class.getSimpleName(), "Error parsing DRM session data.", e);
					}
				}
			});
		}
		catch (IOException e) {
			Log.e(MediaItemActivity.class.getSimpleName(), "Error requesting DRM session creation.", e);
		}
	}

	@OnClick(R.id.authorize) void authorizeHandler() {
		authorize();
	}

	@OnClick(R.id.deauthorize) void deauthorizeHandler() {
		authorize(true);
	}

	@OnClick(R.id.load) public void loadHandler() {
		loadPlayer(media);
	}

	void authorize(final boolean deauth) {
		if (media == null || media.drmRequest == null ||
				entitlementScheme == null)
			return;

		final DrmInfoRequest drmRequest = media.drmRequest;

		status.setText(deauth ? "Deauthorizing..." : "Authorizing...");

		String url = String.format("%sservices/%s?CrmId=sambatech&AccountId=sambatech&SessionId=%s&UserIp=%s", getString(R.string.drm_url),
				deauth ? "Deauthorize" : "Authorize", drmRequest.get("SessionId"), MainApplication.getExternalIp());

		switch ((int)policySpinner.getSelectedItemId()) {
			case 0:
				url += "&OptionId=6&ContentId=" + media.id;
				break;

			case 1:
				url += "&PackageId=2";
				break;

			case 2:
				url += "&OptionId=7&ContentId=" + media.id;
				break;

			case 3:
				url += "&PackageId=3";
				break;
			default: break;
		}

		try {
			HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();

			con.setRequestMethod("POST");
			con.addRequestProperty("MAN-user-id", "app@sambatech.com");
			con.addRequestProperty("MAN-user-password", "c5kU6DCTmomi9fU");

			Helpers.requestUrl(con, new Helpers.Callback() {
				@Override
				public void call(String response) {
                    status.setText(String.format("%s: %s", deauth ? "Deauthorized" : "Authorized", policySpinner.getSelectedItem()));
				}
			});
		}
		catch (IOException e) {
			Log.e(MediaItemActivity.class.getSimpleName(), "Error requesting DRM authorization.", e);
		}
	}

	void authorize() { authorize(false); }

	private void destroy() {
		SambaEventBus.unsubscribe(playerListener);
		player.destroy();
	}
}
