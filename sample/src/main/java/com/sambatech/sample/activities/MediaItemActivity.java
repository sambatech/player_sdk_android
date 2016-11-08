package com.sambatech.sample.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.libraries.mediaframework.exoplayerextensions.DrmRequest;
import com.sambatech.player.SambaApi;
import com.sambatech.player.SambaPlayerView;
import com.sambatech.player.event.SambaApiCallback;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaConfig;
import com.sambatech.player.model.SambaMediaRequest;
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
    SambaPlayerView player;

	@Bind(R.id.progressbar_view)
	LinearLayout loading;

	@Bind(R.id.loading_text)
	TextView loading_text;

	@Bind(R.id.validationControlbar)
	View validationControlbar;

	private boolean _autoPlay;
	private LiquidMedia.ValidationRequest validationRequest;
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
			status.setText(String.format("Status: %s", e.getType() + " " + e.getData()));
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

		SambaEventBus.subscribe(playerListener);
		requestMedia(activityMedia);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (player != null && player.hasStarted())
            player.pause();
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
		/*if (validationRequest == null || validationRequest.media == null) return;

		final DrmRequest drmRequest = validationRequest.media.drmRequest;

		status.setText("Creating session...");

		try {
			HttpURLConnection con = (HttpURLConnection)new URL("http://sambatech.stage.ott.irdeto.com/services/CreateSession?CrmId=sambatech&UserId=smbUserTest").openConnection();

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

						drmRequest.addUrlParam("SessionId", sessionId);
						drmRequest.addUrlParam("Ticket", attributes.getNamedItem("Ticket").getTextContent());

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
		}*/
	}

	@OnClick(R.id.authorize) public void authorizeHandler() {
		status.setText("Authorizing...");
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

			    LiquidMedia.ValidationRequest validationRequest = liquidMedia.validationRequest;

			    if (validationRequest != null) {
				    //validationRequest.media = (SambaMediaConfig)media;
				    MediaItemActivity.this.validationRequest = validationRequest;

				    validationControlbar.setVisibility(View.VISIBLE);
			    }

			    loadPlayer(media);
		    }

		    //Response error
		    @Override
		    public void onMediaResponseError(String msg, SambaMediaRequest request) {
			    Toast.makeText(MediaItemActivity.this, msg + " " + request, Toast.LENGTH_SHORT).show();
		    }
	    };

	    // if injected media
	    if (liquidMedia.url != null && !liquidMedia.url.isEmpty()) {
		    SambaMediaConfig m = new SambaMediaConfig();
		    m.url = liquidMedia.url;
		    m.title = liquidMedia.title;
		    m.type = liquidMedia.type;
		    callback.onMediaResponse(m);
		    return;
	    }

	    //Instantiates the SambaApi class
        SambaApi api = new SambaApi(this, "token");

	    //Instantiate a unique request. Params: playerHash, mediaId, streamName, streamUrl ( alternateLive on our browser version )
        SambaMediaRequest sbRequest = new SambaMediaRequest(liquidMedia.ph, liquidMedia.id, null, liquidMedia.streamUrl);

	    if (liquidMedia.environment != null)
		    sbRequest.environment = liquidMedia.environment;

	    if (liquidMedia.description != null || liquidMedia.shortDescription != null) {
		    descView.setText(((liquidMedia.description != null) ? liquidMedia.description : "") +
				    "\n " + ((liquidMedia.shortDescription != null) ? liquidMedia.shortDescription : ""));
	    }

		//Make the media request
        api.requestMedia(sbRequest, callback);
    }

    private void loadPlayer(final SambaMedia media) {
	    if (activityMedia.url != null)
		    media.url = activityMedia.url;

	    if (activityMedia.adTag != null) {
		    media.adUrl = activityMedia.adTag.url;
		    media.title = activityMedia.adTag.name;
	    }

		loading.setVisibility(View.GONE);
		titleView.setVisibility(View.VISIBLE);
		titleView.setText(media.title);

		/** If audio, we recommend you to customize the player's height**/
		if (media.isAudioOnly) {
			player.getLayoutParams().height = (int)(66.7f * getResources().getDisplayMetrics().density);
			player.setLayoutParams(player.getLayoutParams());
		}

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

	private void destroy() {
		SambaEventBus.unsubscribe(playerListener);
		player.destroy();
		finish();
	}
}
