package com.sambatech.player;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sambatech.player.event.SambaApiCallback;
import com.sambatech.player.event.SambaEvent;
import com.sambatech.player.event.SambaEventBus;
import com.sambatech.player.event.SambaPlayerListener;
import com.sambatech.player.model.JSONMedia;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.model.SambaMediaRequest;

import de.greenrobot.event.EventBus;

public class MediaItemActivity extends Activity {

    private SambaPlayer player;
    private TextView titleView;
    private TextView descView;
    private Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_media_item);

        player = (SambaPlayer) findViewById(R.id.samba_player);
        titleView = (TextView) findViewById(R.id.title);
        descView = (TextView) findViewById(R.id.description);
        back = (Button) findViewById(R.id.back);


        getActionBar().setDisplayHomeAsUpEnabled(true);

        JSONMedia media = (JSONMedia) EventBus.getDefault().removeStickyEvent(JSONMedia.class);

        Log.e("mediaitem:", "carregada título " + media.getTitle());

        requestMedia(media.getProjectHash(), media.getMediaId());
    }


    private void initPlayer() {
        //p.setListener(new SambaPlayerListener() {...});
        SambaEventBus.subscribe(new SambaPlayerListener() {
            @Override
            public void onLoad(SambaEvent e) {
                //status.setText(String.format("Status: %s", e.getType()));
            }

            @Override
            public void onPlay(SambaEvent e) {
                //status.setText(String.format("Status: %s", e.getType()));
            }

            @Override
            public void onPause(SambaEvent e) {
                //status.setText(String.format("Status: %s", e.getType()));
            }

            @Override
            public void onStop(SambaEvent e) {
                //status.setText(String.format("Status: %s", e.getType()));
            }

            @Override
            public void onFinish(SambaEvent e) {
                //status.setText(String.format("Status: %s", e.getType()));
            }

            @Override
            public void onFullscreen(SambaEvent e) {
                //status.setText(String.format("Status: %s", e.getType()));
            }

            @Override
            public void onFullscreenExit(SambaEvent e) {
                //status.setText(String.format("Status: %s", e.getType()));
            }

            @Override
            public void onError(SambaEvent e) {
                //status.setText(String.format("Status: %s", e.getType()));
                //Toast.makeText(MainActivity.this, (String) e.getData(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestMedia(String ph, String mediaId) {

        SambaApi api = new SambaApi(this, "token");

        SambaMediaRequest sbRequest = new SambaMediaRequest(ph, mediaId);

        api.requestMedia(sbRequest, new SambaApiCallback() {
            @Override
            public void onMediaResponse(SambaMedia media) {
                //status.setText(String.format("Loading...%s", media != null ? media.title : ""));
                loadMedia(media);
            }

            @Override
            public void onMediaListResponse(SambaMedia[] mediaList) {
                /**for (SambaMedia m : mediaList) {
                 if (m.title.isEmpty())
                 m.title = "Sem título";

                 m.title += " (" + m.type.toUpperCase() + (m.isLive ? " Live" : "") + ")";
                 }

                 //http://test.d.sambavideos.sambatech.com/account/100209/50/2014-10-06/video/9ba974f571a8bf28db3d48636a04baa1/30DIFERENCAS.2.mp4
                 mediaList[2].title += ": preroll";
                 mediaList[2].adUrl = "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=sample_ct%3Dredirecterror&correlator=";
                 mediaList[3].title += ": Pre+mid+post+bumpers";
                 mediaList[3].adUrl = "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=%2F3510761%2FadRulesSampleTags&ciu_szs=160x600%2C300x250%2C728x90&cust_params=adrule%3Dpremidpostpodandbumpers&impl=s&gdfp_req=1&env=vp&ad_rule=1&vid=12345&cmsid=3601&output=xml_vast2&unviewed_position_start=1&url=%5Breferrer_url%5D&correlator=%5Btimestamp%5D";
                 mediaList[4].title += ": postroll";
                 mediaList[4].adUrl = "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=%2F3510761%2FadRulesSampleTags&ciu_szs=160x600%2C300x250%2C728x90&cust_params=adrule%3Dpostrollonly&impl=s&gdfp_req=1&env=vp&ad_rule=1&vid=12345&cmsid=3601&output=xml_vast2&unviewed_position_start=1&url=%5Breferrer_url%5D&correlator=%5Btimestamp%5D";
                 mediaList[5].title += ": skippable";
                 mediaList[5].adUrl = "http://pubads.g.doubleclick.net/gampad/ads?sz=640x360&iu=/6062/iab_vast_samples/skippable&ciu_szs=300x250,728x90&impl=s&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=%5Breferrer_url%5D&correlator=%5Btimestamp%5D";
                 mediaList[8].title += ": Ad";
                 mediaList[8].adUrl = "http://pubads.g.doubleclick.net/gampad/ads?sz=640x360&iu=/6062/iab_vast_samples/skippable&ciu_szs=300x250,728x90&impl=s&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=%5Breferrer_url%5D&correlator=%5Btimestamp%5D";

                 //status.setText("Loaded!");
                 //createListAdapter(mediaList);
                 //loadMedia(mediaList[0]);**/
            }

            @Override
            public void onMediaResponseError(String msg, SambaMediaRequest request) {
                //Toast.makeText(MainActivity.this, msg + " " + request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMedia(SambaMedia media) {
        titleView.setVisibility(View.VISIBLE);
        titleView.setText(media.title);
        player.setMedia(media);
        player.play();
    }

    private void bindEvents() {
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                //intent.putExtra(JSONMedia.class, (JSONMedia) parent.getAdapter().getItem(position));
                startActivity(intent);
            }
        });
    }


}
