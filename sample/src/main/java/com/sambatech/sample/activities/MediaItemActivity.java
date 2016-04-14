package com.sambatech.sample.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sambatech.player.SambaApi;
import com.sambatech.player.SambaPlayer;
import com.sambatech.player.SambaPlayerView;
import com.sambatech.player.event.SambaApiCallback;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaRequest;
import com.sambatech.sample.R;
import com.sambatech.sample.model.LiquidMedia;

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

	@Bind(R.id.samba_player2)
	SambaPlayer player2;

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
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_item);

        ButterKnife.bind(this);

		if (getActionBar() != null)
      		getActionBar().setDisplayHomeAsUpEnabled(true);

	    if (activityMedia == null)
			activityMedia = EventBus.getDefault().removeStickyEvent(LiquidMedia.class);

	    loading_text.setText("Carregando mídia: " + activityMedia.title.split("\\.", 2)[0]);

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
		player2.destroy();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (player != null && player.hasStarted())
            player.pause();

	    if (player2 != null && player2.hasStarted())
		    player2.pause();
    }

	/**
	 * Request the given media
	 * @param media - Liquid media object
	 */
    private void requestMedia(LiquidMedia media) {

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

	    if (media.isAudioOnly) {
		    player.getLayoutParams().height = (int)(66.7f * getResources().getDisplayMetrics().density);
			player.setLayoutParams(player.getLayoutParams());
	    }

        player.setMedia(media);

	    //Play the media programmatically on its load ( similar to autoPlay=true param )
        player.play();

	    // TODO: remove; flag misused to test multiple players
	    if (activityMedia.highlighted) {
		    ((View)player2).setVisibility(View.VISIBLE);
		    SambaMedia m = new SambaMedia();
		    m.url = "http://svrp.sambavideos.sambatech.com/voda/_definst_/amlst%3Astg.test%3B100209%2C538%2C0a73c9dd8f10cc0d671b793788f1b642%3B%2Faccount%2F100209%2F71%2F2016-03-14%2Fvideo%2F%3B66792b1ddf07e70d8a69cbce67ebbe33%2FA120276_480p_360p.mp4%2C507904%2C640%2C360%2C6745cd4cb4d1cce7afb53ce3266c356c%2FA120276_480p_240p.mp4%2C297984%2C426%2C240%2C71b290a2fa04343e8e73b182a83b3159%2FA120276_480p_480p.mp4%2C657408%2C854%2C480%2C%3B/playlist.m3u8";
		    m.title = "Segundo vídeo";
		    m.type = "HLS";
		    player2.setMedia(m);
		    player2.play();
	    }
    }
}
