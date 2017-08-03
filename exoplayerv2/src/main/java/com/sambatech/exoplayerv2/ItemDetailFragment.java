package com.sambatech.exoplayerv2;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.sambatech.exoplayerv2.model.MediaContent;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private MediaContent.Media mItem;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ItemDetailFragment() {}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final int index = getArguments().getInt(ARG_ITEM_ID, -1);

		if (index != -1) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			mItem = MediaContent.getItem(index);

			Activity activity = this.getActivity();
			CollapsingToolbarLayout appBarLayout = activity.findViewById(R.id.toolbar_layout);
			if (appBarLayout != null) {
				appBarLayout.setTitle(mItem.title);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		SimpleExoPlayerView playerView = (SimpleExoPlayerView)inflater.inflate(R.layout.item_detail, container, false);

		if (mItem != null) {
			SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(getContext(), new DefaultTrackSelector());
			playerView.setPlayer(player);

			// preparing
			// all deliveries
			DefaultBandwidthMeter bwMeter = new DefaultBandwidthMeter();
			DefaultHttpDataSourceFactory baseDataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(getContext(), "ExoPlayer v2"), bwMeter);
			DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(getContext(), bwMeter, baseDataSourceFactory);

			// hls
			MediaSource mediaSource = new HlsMediaSource(Uri.parse(mItem.url), dataSourceFactory, null, null);

			player.prepare(mediaSource);
			player.setPlayWhenReady(true);
		}

		return playerView;
	}
}
