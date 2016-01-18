package com.sambatech.sample.activities;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sambatech.sample.R;
import com.sambatech.sample.model.LiquidMedia;
import com.sambatech.player.SambaApi;
import com.sambatech.player.SambaPlayer;
import com.sambatech.player.event.SambaApiCallback;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaRequest;

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
    SambaPlayer player;

	@Bind(R.id.progressbar_view)
	LinearLayout loading;

	@Bind(R.id.loading_text)
	TextView loading_text;

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

    private void initPlayer() {
        SambaEventBus.unsubscribe(playerListener);
        SambaEventBus.subscribe(playerListener);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.i("mediaitem:", String.valueOf((newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)));

        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            player.setFullscreen(true);
        }else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            player.setFullscreen(false);
        }
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

    private void requestMedia(LiquidMedia media) {

        SambaApi api = new SambaApi(this, "token");
        SambaMediaRequest sbRequest = new SambaMediaRequest(media.ph, media.id);

        api.requestMedia(sbRequest, new SambaApiCallback() {
            @Override
            public void onMediaResponse(SambaMedia media) {
                //status.setText(String.format("Loading...%s", media != null ? media.title : ""));
                if(activityMedia.adTag != null) {
                    media.adUrl = activityMedia.adTag.url;
                    media.title = activityMedia.adTag.name;
                }

                loadMedia(media);
            }

            @Override
            public void onMediaListResponse(SambaMedia[] mediaList) {
            }

            @Override
            public void onMediaResponseError(String msg, SambaMediaRequest request) {
                //Toast.makeText(MainActivity.this, msg + " " + request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMedia(SambaMedia media) {
	    loading.setVisibility(View.GONE);
        titleView.setVisibility(View.VISIBLE);
        titleView.setText(media.title);
        player.setMedia(media);
        player.play();
    }
}
