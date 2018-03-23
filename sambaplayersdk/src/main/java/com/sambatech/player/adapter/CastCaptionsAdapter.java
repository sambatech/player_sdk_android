package com.sambatech.player.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.sambatech.player.R;
import com.sambatech.player.model.SambaMedia;

import java.util.List;

/**
 * Created by luizbyrro on 20/02/2018.
 */

public class CastCaptionsAdapter extends BaseAdapter {

    public int currentIndex = -1;

    private List<SambaMedia.Caption> captionsList;
    private Context cContext;

    public CastCaptionsAdapter(List<SambaMedia.Caption> captionsList, Context cContext) {
        this.captionsList = captionsList;
        this.cContext = cContext;
        currentIndex = captionsList.size() - 1;
    }

    @Override
    public int getCount() {
        return captionsList.size();
    }

    @Override
    public Object getItem(int i) {
        return captionsList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        CastCaptionsAdapter.CaptionItem holder;

        if (convertView == null) {
            convertView = ((LayoutInflater) cContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.action_sheet_item, parent, false);

            holder = new CastCaptionsAdapter.CaptionItem(convertView);
            convertView.setTag(holder);
        }
        else {
            holder = (CastCaptionsAdapter.CaptionItem) convertView.getTag();
        }

        SambaMedia.Caption caption = (SambaMedia.Caption) getItem(position);

        if (caption.label != null && caption.label.length() > 0) {
            holder.label.setText(caption.label);
        } else {
            holder.label.setText(caption.language);
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
