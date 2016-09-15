package com.sambatech.sample.activities;

import android.app.Activity;
import android.os.Bundle;
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
import com.sambatech.player.model.SambaMediaRequest;
import com.sambatech.sample.R;
import com.sambatech.sample.model.LiquidMedia;

import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class MediaItemActivity extends Activity {

	private static final String API_CATALOG = "https://drm-quick-start.azurewebsites.net/api/catalog/videos";

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
			status.setText(String.format("Status: %s", e.getType()));
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
	    // if injected media
	    if (media.url != null && !media.url.isEmpty()) {
		    SambaMedia m = new SambaMedia();
		    m.url = media.url;
		    m.title = media.title;
		    m.type = media.type;
		    loadMedia(m);
		    return;
	    }

	    //Instantiates the SambaApi class
        SambaApi api = new SambaApi(this, "token");

	    //Instantiate a unique request. Params: playerHash, mediaId, streamName, streamUrl ( alternateLive on our browser version )
        SambaMediaRequest sbRequest = new SambaMediaRequest(media.ph, media.id, null, media.streamUrl);

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

                loadMedia(media);
            }

	        //Response error
            @Override
            public void onMediaResponseError(String msg, SambaMediaRequest request) {
                Toast.makeText(MediaItemActivity.this, msg + " " + request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMedia(SambaMedia media) {
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
	    if(!flag)
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
