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
import com.sambatech.sample.model.LiquidMedia;
import com.sambatech.sample.utils.VolleySingleton;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tmiranda on 11/01/16.
 */
public class MediasAdapter extends BaseAdapter {

    private ArrayList<LiquidMedia> medias;
    private Context mContext;
	private View globalView;

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

	public void resetView() {

	}

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        MediaItem holder;

        if(view == null) {
            view = inflater.inflate(R.layout.media_list_item, viewGroup, false);

            holder = new MediaItem(view);
            view.setTag(holder);
        }else {
            holder = (MediaItem) view.getTag();
        }

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
		    holder.description.setText((media.description != null ? media.description : "") + " " + (media.shortDescription != null ? media.shortDescription : ""));
	    }

        return view;
    }



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

