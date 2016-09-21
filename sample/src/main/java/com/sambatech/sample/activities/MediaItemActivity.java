package com.sambatech.sample.activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Random;
import java.util.Scanner;

import butterknife.Bind;
import butterknife.ButterKnife;
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
			String token = player.getMedia().drmToken != null ? " (token: \"" + player.getMedia().drmToken.substring(0, 10) + "...\")" : "";
			status.setText(String.format("Status: %s%s", e.getType(), token));
		}

		@Override
		public void onPlay(SambaEvent e) {
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

        ButterKnife.bind(this);

		if (getActionBar() != null)
      		getActionBar().setDisplayHomeAsUpEnabled(true);

	    if (activityMedia == null) {
		    activityMedia = EventBus.getDefault().removeStickyEvent(LiquidMedia.class);

		    loading_text.setText("Carregando mídia: " + activityMedia.title.split("\\.", 2)[0]);
	    }

	    initPlayer();
		requestMedia(activityMedia);
	}

	/**
	 * Subscribe the listeners of the player
	 */
    private void initPlayer() {
        SambaEventBus.unsubscribe(playerListener);
        SambaEventBus.subscribe(playerListener);
    }

	@Override
	public void onBackPressed() {
		super.onBackPressed();
        player.destroy();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (player != null && player.hasStarted())
            player.pause();

    }

	/**
	 * Request the given media
	 * @param media - Liquid media object
	 */
    private void requestMedia(LiquidMedia media) {
	    final String drmUrl = media.drmUrl;

	    // if injected media
	    if (media.url != null && !media.url.isEmpty()) {
		    SambaMediaConfig m = new SambaMediaConfig();
		    m.url = media.url;
		    m.title = media.title;
		    m.type = media.type;

		    loadMedia(m, drmUrl);
		    return;
	    }

	    //Instantiates the SambaApi class
        SambaApi api = new SambaApi(this, "token");

	    //Instantiate a unique request. Params: playerHash, mediaId, streamName, streamUrl ( alternateLive on our browser version )
        SambaMediaRequest sbRequest = new SambaMediaRequest(media.ph, media.id, null, media.streamUrl);

	    if (media.environment != null)
		    sbRequest.environment = media.environment;

	    if(media.description != null || media.shortDescription != null) {
		    descView.setText(((media.description != null) ? media.description : ""
		    ) + "\n " + ((media.shortDescription != null) ? media.shortDescription : ""));
	    }

		//Make the media request
        api.requestMedia(sbRequest, new SambaApiCallback() {

	        //Success response of one media only. Returns a SambaMedia object
            @Override
            public void onMediaResponse(SambaMedia media) {
                if(activityMedia.adTag != null) {
                    media.adUrl = activityMedia.adTag.url;
                    media.title = activityMedia.adTag.name;
                }

                loadMedia(media, drmUrl);
            }

	        //Response error
            @Override
            public void onMediaResponseError(String msg, SambaMediaRequest request) {
                Toast.makeText(MediaItemActivity.this, msg + " " + request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMedia(final SambaMedia media, String drmUrl) {
	    // if DRM media
	    if (drmUrl != null && !drmUrl.isEmpty()) {
		    new AsyncTask<String, Void, String>() {

			    @Override
			    protected String doInBackground(String... params) {
				    InputStream inputStream = null;
				    Scanner scanner = null;
				    Scanner scannerDelimited = null;

				    try {
					    inputStream = new URL(params[0]).openStream();
					    scanner = new Scanner(inputStream);
					    scannerDelimited = scanner.useDelimiter("\\A");
					    String token = scannerDelimited.hasNext() ? scannerDelimited.next() : "";

					    return token.substring(1, token.length() - 1);
				    }
				    catch (Exception e) {
					    Log.e("SampleApp", "Error requesting DRM.");
				    }
				    finally {
					    try {
						    if (inputStream != null)
							    inputStream.close();

						    if (scanner != null)
							    scanner.close();

						    if (scannerDelimited != null)
							    scannerDelimited.close();
					    }
					    catch (Exception e) {
						    Log.e("SampleApp", "Error closing DRM request stream.");
					    }
				    }

				    return null;
			    }

			    @Override
			    protected void onPostExecute(String token) {
				    media.drmToken = token;

				    doLoadMedia(media);
			    }
		    }.execute(drmUrl);
	    }
		else doLoadMedia(media);
    }

	private void doLoadMedia(SambaMedia media) {
		loading.setVisibility(View.GONE);
		titleView.setVisibility(View.VISIBLE);
		titleView.setText(media.title);

		/** If audio, we recommend you to customize the player's height**/
		if (media.isAudioOnly) {
			player.getLayoutParams().height = (int)(66.7f * getResources().getDisplayMetrics().density);
			player.setLayoutParams(player.getLayoutParams());
		}

		player.setMedia(media);

		//Disable controls randomically
		Random random = new Random();
		Boolean flag = random.nextBoolean();

		//Set enable controls
		player.setEnableControls(flag);

		if (!flag)
			descView.setText("Mídia com controls desabilitados");

		//Play the media programmatically on its load ( similar to autoPlay=true param )
		player.play();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		player.destroy();
		finish();
	}

	@Override
	protected void onStop() {
		super.onStop();
		player.destroy();
		finish();
	}
}
