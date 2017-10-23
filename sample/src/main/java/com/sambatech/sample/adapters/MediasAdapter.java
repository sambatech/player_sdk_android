package com.sambatech.sample.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.sambatech.sample.R;
import com.sambatech.sample.model.MediaInfo;
import com.sambatech.sample.utils.VolleySingleton;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tmiranda on 11/01/16.
 *
 * Adapter class to list each media item
 */
public class MediasAdapter extends BaseAdapter {

    private List<MediaInfo> mediaList;
	private LayoutInflater inflater;

    public MediasAdapter(Context mContext, List<MediaInfo> mList) {
        mediaList = mList;
	    inflater = (LayoutInflater) mContext
			    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mediaList.size();
    }

    @Override
    public Object getItem(int position) {
        return mediaList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

	@Override
	public int getViewTypeCount() {
		return getCount() > 0 ? getCount() : 1;
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
		if (view == null) {
			final View customView = inflater.inflate(R.layout.media_list_item, viewGroup, false);
			final MediaItem holder = new MediaItem(customView);
			final MediaInfo media = (MediaInfo) getItem(position);
			final ImageLoader mImageLoader = VolleySingleton.getInstance().getImageLoader();

			// if there's any thumb
			if (media.getThumbnail() != null)
				holder.thumb.setImageUrl(media.getThumbnail(), mImageLoader);
				// audio
			else if ("audio".equalsIgnoreCase(media.getQualifier()))
				holder.thumb.setImageUrl("https://cdn3.iconfinder.com/data/icons/buttons-1/512/Notes.png", mImageLoader);

			holder.title.setText(media.getTitle() != null ? media.getTitle().split("\\.", 2)[0] : "");
			holder.description.setText(media.getDescription() != null ? media.getDescription() : "");

			final boolean odd = (position & 1) == 1;

			// colors
			switch (media.getEnvironment()) {
				case PROD:
					customView.setBackgroundColor(Color.parseColor(odd ? "#eeffee" : "#ddffdd"));
					break;
				case STAGING:
					customView.setBackgroundColor(Color.parseColor(odd ? "#ddeeff" : "#ccddff"));
					break;
				case DEV:
					customView.setBackgroundColor(Color.parseColor(odd ? "#ffeeee" : "#ffdddd"));
					break;
				case LOCAL:
				default:
					customView.setBackgroundColor(Color.parseColor(odd ? "#eeeeee" : "#ffffff"));
					break;
			}

			customView.setTag(holder);

			return customView;
		}

        return view;
    }

    static class MediaItem {
        @Bind(R.id.thumbPreview)
        NetworkImageView thumb;

        @Bind(R.id.titlePreview)
        TextView title;

	    @Bind(R.id.description)
	    TextView description;

        MediaItem(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
