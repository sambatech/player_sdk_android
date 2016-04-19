package com.sambatech.sample.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.sambatech.sample.R;
import com.sambatech.sample.activities.MediaItemActivity;
import com.sambatech.sample.model.LiquidMedia;
import com.sambatech.sample.utils.ItemClickEvent;
import com.sambatech.sample.utils.VolleySingleton;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

/**
 * Created by tmiranda on 11/01/16.
 *
 * Adapter class to list each media item
 */
public class MediasAdapter extends BaseAdapter {

    private ArrayList<LiquidMedia> medias;
    private Context mContext;
	private LayoutInflater inflater;

    public MediasAdapter(Context mContext, ArrayList<LiquidMedia> mList) {
        this.medias = mList;
        this.mContext = mContext;

	    inflater = (LayoutInflater) mContext
			    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return medias.size();
    }

    @Override
    public Object getItem(int position) {
        return medias.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

	@Override
	public int getViewTypeCount() {

		if (getCount() != 0)
			return getCount();

		return 1;
	}

	@Override
	public int getItemViewType(int position) {

		return position;
	}

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        MediaItem holder;

	    if(view == null) {

		    view = inflater.inflate(R.layout.media_list_item, viewGroup, false);
	        holder = new MediaItem(view);

	        LiquidMedia media = (LiquidMedia) getItem(position);
	        ImageLoader mImageLoader = VolleySingleton.getInstance().getImageLoader();
	        if (media.qualifier.equals("AUDIO")) {
		        //Log.e("player:", media.title);
		        //Drawable drawable = view.getResources().getDrawable(R.drawable.ic_audio);
		        //holder.thumb.setImageDrawable(drawable);
		        holder.thumb.setImageUrl("https://cdn4.iconfinder.com/data/icons/defaulticon/icons/png/256x256/media-volume-2.png", mImageLoader);
	        } else {
		        holder.thumb.setImageUrl(getIdealThumb(media.thumbs), mImageLoader);
	        }

	        holder.title.setText(media.title.split("\\.", 2)[0]);

	        if (media.description != null || media.shortDescription != null) {
		        String text = (media.description != null ? media.description : "") + "\n " + (media.shortDescription != null ? media.shortDescription : "");
		        holder.description.setText(text);
	        } else {
		        holder.description.setText("");
	        }

		    //Cores
		    if (position % 2 == 0) {
			    view.setBackgroundColor(Color.parseColor("#EEEEEE"));
		    } else {
			    view.setBackgroundColor(Color.parseColor("#DDDDDD"));
		    }

		    view.setTag(holder);

		    holder.arrow.setOnClickListener(new View.OnClickListener() {
			    @Override
			    public void onClick(View v) {
				    LiquidMedia media = (LiquidMedia) getItem(position);
				    EventBus.getDefault().post(new ItemClickEvent("newActivity", media));
			    }
		    });

		    holder.thumb.setOnClickListener(new View.OnClickListener() {
			    @Override
			    public void onClick(View v) {
				    LiquidMedia media = (LiquidMedia) getItem(position);
				    EventBus.getDefault().post(new ItemClickEvent("sameActivity", media));
			    }
		    });

		    holder.title.setOnClickListener(new View.OnClickListener() {
			    @Override
			    public void onClick(View v) {
				    LiquidMedia media = (LiquidMedia) getItem(position);
				    EventBus.getDefault().post(new ItemClickEvent("sameActivity", media));
			    }
		    });

	    }

        return view;
    }

	/**
	 * Get the smallest thumb of the media ( for optimization purposes )
	 * @param thumbs
	 * @return
	 */
	private String getIdealThumb(ArrayList<LiquidMedia.Thumb> thumbs) {
        int size = 0;
        String thumbUrl = "";
        for(LiquidMedia.Thumb thumb : thumbs) {

            if(size == 0 || size > thumb.size) {
                size = thumb.size;
                thumbUrl = thumb.url;
            }

        }
        return thumbUrl;
    }

    static class MediaItem {
        @Bind(R.id.thumbPreview)
        NetworkImageView thumb;

        @Bind(R.id.titlePreview)
        TextView title;

	    @Bind(R.id.description)
	    TextView description;

	    @Bind(R.id.arrow)
	    ImageView arrow;

        MediaItem(View view) {
            ButterKnife.bind(this, view);
        }
    }


}

