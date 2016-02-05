package com.sambatech.sample.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.sambatech.sample.R;
import com.sambatech.sample.model.LiquidMedia;
import com.sambatech.sample.utils.VolleySingleton;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tmiranda on 11/01/16.
 *
 * Adapter class to list each media item
 */
public class MediasAdapter extends BaseAdapter {

    private ArrayList<LiquidMedia> medias;
    private Context mContext;

    public MediasAdapter(Context mContext, ArrayList<LiquidMedia> mList) {
        this.medias = mList;
        this.mContext = mContext;
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
    public View getView(int position, View convertView, ViewGroup viewGroup) {
	    View view;
        MediaItem holder;

        if(convertView == null) {
	        LayoutInflater inflater = (LayoutInflater) mContext
			        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.media_list_item, viewGroup, false);
        }else {
	        view = convertView;
        }

	    holder = new MediaItem(view);
	    view.setTag(holder);

	    //Cores
	    if(position % 2 == 0){
		    view.setBackgroundColor(Color.parseColor("#EEEEEE"));
	    }
	    else{
		    view.setBackgroundColor(Color.parseColor("#DDDDDD"));
	    }

        LiquidMedia media = (LiquidMedia) getItem(position);
        ImageLoader mImageLoader = VolleySingleton.getInstance().getImageLoader();


        holder.thumb.setImageUrl(getIdealThumb(media.thumbs), mImageLoader);
        holder.title.setText(media.title.split("\\.", 2)[0]);

	    if(media.description != null || media.shortDescription != null) {
		    String text = (media.description != null ? media.description : "") + "\n " + (media.shortDescription != null ? media.shortDescription : "");
		    holder.description.setText(text);
	    }else {
		    holder.description.setText("");
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

        MediaItem(View view) {
            ButterKnife.bind(this, view);
        }
    }


}

