package com.sambatech.player.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.sambatech.player.R;
import com.sambatech.player.model.SambaMedia;

import java.util.List;

/**
 * Created by luizbyrro on 07/11/17.
 */

public class CaptionsSheetAdapter extends BaseAdapter {

    public int currentIndex = -1;

    private Context cContext;
    private TrackGroupArray captions;

    public CaptionsSheetAdapter(Context context, TrackGroupArray trackGroupArray) {
        this.cContext = context;
        this.captions = trackGroupArray;
    }

    @Override
    public int getCount() {
        return captions.length;
    }

    @Override
    public Object getItem(int position) {
        return captions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CaptionItem holder;

        if (convertView == null) {
            convertView = ((LayoutInflater) cContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.action_sheet_item, parent, false);

            holder = new CaptionItem(convertView);
            convertView.setTag(holder);
        }
        else {
            holder = (CaptionItem) convertView.getTag();
        }

        TrackGroup caption = (TrackGroup) getItem(position);

        if (caption.getFormat(0).language != null && caption.getFormat(0).language.length() > 0) {
            holder.label.setText(caption.getFormat(0).language);
        } else {
            holder.label.setText(R.string.no_caption);
        }


        holder.radio.setChecked(currentIndex == position);

        return convertView;
    }

    private static class CaptionItem {
        TextView label;
        RadioButton radio;

        CaptionItem(View view) {
            this.radio = (RadioButton) view.findViewById(R.id.sheet_output_radio);
            this.label = (TextView) view.findViewById(R.id.sheet_item_label);
        }
    }
}