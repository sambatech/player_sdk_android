package com.sambatech.sample.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.libraries.mediaframework.exoplayerextensions.DrmRequest;
import com.sambatech.player.SambaApi;
import com.sambatech.player.cast.CastOptionsProvider;
import com.sambatech.player.cast.SambaCast;
import com.sambatech.player.SambaPlayer;
import com.sambatech.player.event.SambaApiCallback;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaConfig;
import com.sambatech.player.model.SambaMediaRequest;
import com.sambatech.sample.MainApplication;
import com.sambatech.sample.R;
import com.sambatech.sample.model.LiquidMedia;
import com.sambatech.sample.utils.Helpers;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class MediaItemActivity extends Activity {

    private LiquidMedia activityMedia;

    @Bind(R.id.title)
    TextView titleView;

    @Bind(R.id.description)
    TextView descView;

    @Bind(R.id.status)
    TextView status;

    @Bind(R.id.samba_player)
    SambaPlayer player;

	@Bind(R.id.progressbar_view)
	LinearLayout loading;

	@Bind(R.id.loading_text)
	TextView loading_text;

	@Bind(R.id.validation_controlbar)
	View validationControlbar;

	@Bind(R.id.policies)
	Spinner policySpinner;

    @Bind(R.id.session_controls)
    LinearLayout sessionControls;

    @Bind(R.id.auth_controls)
    LinearLayout authControls;


	private boolean _autoPlay;
	private LiquidMedia.EntitlementScheme entitlementScheme;
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
		}

		@Override
		public void onFullscreenExit(SambaEvent e) {
			status.setText(String.format("Status: %s", e.getType()));
		}

		@Override
		public void onError(SambaEvent e) {
			String msg = String.format("%s", e.getType() + " - " + e.getData());
			status.setText(msg);
			Log.e("MediaItemActivity", msg);
		}

		@Override
		public void onProgress(SambaEvent event) {
			status.setText(String.format("Status: %s", event.getType()));
		}
	};

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_item);

		if (getIntent() != null)
			_autoPlay = getIntent().getBooleanExtra("autoPlay", true);

        ButterKnife.bind(this);

		if (getActionBar() != null)
      		getActionBar().setDisplayHomeAsUpEnabled(true);

	    if (activityMedia == null) {
		    activityMedia = EventBus.getDefault().removeStickyEvent(LiquidMedia.class);
		    loading_text.setText("Carregando mídia: " + activityMedia.title.split("\\.", 2)[0]);
	    }

	    // cast
		CastOptionsProvider.configProfile(this, activityMedia.environment);

		// testing...
		CastOptionsProvider.appId = "411C092F";
		CastOptionsProvider.playerUrl = "192.168.0.113:8000/";

		sambaCast = new SambaCast(this);

		SambaEventBus.subscribe(playerListener);
		requestMedia(activityMedia);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		destroy();
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
			if (player != null && player.hasStarted())
				player.pause();
		}
    }

	/**
	 * Request the given media
	 * @param liquidMedia - Liquid media object
	 */
    private void requestMedia(final LiquidMedia liquidMedia) {
	    SambaApiCallback callback = new SambaApiCallback() {
		    //Success response of one media only. Returns a SambaMedia object
		    @Override
		    public void onMediaResponse(SambaMedia media) {
			    if (media == null) return;

			    LiquidMedia.EntitlementScheme entitlementScheme = liquidMedia.entitlementScheme;

			    if (liquidMedia.url != null)
				    media.url = liquidMedia.url;

			    if (entitlementScheme != null) {
				    MediaItemActivity.this.media = (SambaMediaConfig)media;
				    MediaItemActivity.this.entitlementScheme = entitlementScheme;

				    loading.setVisibility(View.GONE);
				    titleView.setVisibility(View.VISIBLE);
				    titleView.setText(media.title);

				    validationControlbar.setVisibility(View.VISIBLE);

                    //DRM controls
                    if(((SambaMediaConfig) media).drmRequest != null ) {
                        sessionControls.setVisibility(View.VISIBLE);
                        authControls.setVisibility(View.VISIBLE);
                    }
				    return;
			    }

			    media.isAudioOnly = liquidMedia.qualifier.toLowerCase().equals("audio");

			    loadPlayer(media);

		    }

		    //Response error
		    @Override
		    public void onMediaResponseError(String msg, SambaMediaRequest request) {
			    Toast.makeText(MediaItemActivity.this, msg + " " + request, Toast.LENGTH_SHORT).show();
		    }
	    };

	    // if injected media
	    if (liquidMedia.ph == null && liquidMedia.url != null && !liquidMedia.url.isEmpty()) {
		    SambaMediaConfig m = new SambaMediaConfig();
		    m.url = liquidMedia.url;
		    m.title = liquidMedia.title;
		    m.type = liquidMedia.type;
		    m.isAudioOnly = liquidMedia.qualifier.toLowerCase().equals("audio");
		    callback.onMediaResponse(m);
		    return;
	    }

	    //Instantiates the SambaApi class
        SambaApi api = new SambaApi(this, "token");

	    //Instantiate a unique request. Params: playerHash, mediaId, streamName, streamUrl ( alternateLive on our browser version )
        SambaMediaRequest sbRequest = new SambaMediaRequest(liquidMedia.ph, liquidMedia.id, null, liquidMedia.streamUrl, liquidMedia.backupUrls);
		sbRequest.environment = liquidMedia.environment;

	    if (liquidMedia.description != null || liquidMedia.shortDescription != null) {
		    descView.setText(((liquidMedia.description != null) ? liquidMedia.description : "") +
				    "\n " + ((liquidMedia.shortDescription != null) ? liquidMedia.shortDescription : ""));
	    }

        sbRequest.protocol = SambaMediaRequest.Protocol.HTTP;
        api.requestMedia(sbRequest, callback);
    }

    private void loadPlayer(final SambaMedia media) {
	    if (media == null) return;

	    if (activityMedia.adTag != null) {
		    media.adUrl = activityMedia.adTag.url;
		    media.title = activityMedia.adTag.name;
	    }

		loading.setVisibility(View.GONE);
		titleView.setVisibility(View.VISIBLE);
		titleView.setText(media.title);

		// If audio, we recommend you to customize the player's height
		if (media.isAudioOnly) {
			player.getLayoutParams().height = (int)(66.7f * getResources().getDisplayMetrics().density);
			player.setLayoutParams(player.getLayoutParams());
		}

		// enabling Chromecast on player
		player.setSambaCast(sambaCast);
	    player.setMedia(media);

		/*//Disable controls randomically
		Random random = new Random();
		Boolean flag = random.nextBoolean();

		//Set enable controls
		player.setEnableControls(flag);

		if (!flag)
			descView.setText("Mídia com controls desabilitados");*/

		//ti = new Date().getTime();

		if (_autoPlay)
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
		destroy();
	}

	@OnClick(R.id.play) public void playHandler() {
		if (player != null)
			player.play();
	}

	@OnClick(R.id.pause) public void pauseHandler() {
		if (player != null)
			player.pause();
	}

	@OnClick(R.id.create_session) public void createSessionHandler() {
		if (entitlementScheme == null || media == null ||
				media.drmRequest == null) return;

		final DrmRequest drmRequest = media.drmRequest;

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

						drmRequest.addLicenseParam("SessionId", sessionId);
						drmRequest.addLicenseParam("Ticket", attributes.getNamedItem("Ticket").getTextContent());

						// for manually injected DRM media
						if (entitlementScheme.contentId != null)
							drmRequest.addLicenseParam("ContentId", entitlementScheme.contentId);

						status.setText(String.format("Session: %s", sessionId));
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		catch (IOException e) {
			e.printStackTrace();
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

		final DrmRequest drmRequest = media.drmRequest;

		status.setText(deauth ? "Deauthorizing..." : "Authorizing...");

		String url = String.format("%sservices/%s?CrmId=sambatech&AccountId=sambatech&SessionId=%s&UserIp=%s", getString(R.string.drm_url),
				deauth ? "Deauthorize" : "Authorize", drmRequest.getLicenseParam("SessionId"), MainApplication.getExternalIp());

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
			e.printStackTrace();
		}
	}

	void authorize() { authorize(false); }

	private void destroy() {
		SambaEventBus.unsubscribe(playerListener);
		player.destroy();
		finish();
	}
}
