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

import java.util.ArrayList;

/**
 * Created by thiagomir on 12/01/17.
 */

public class CaptionAdapter extends BaseAdapter {

    private ArrayList<SambaMedia.Caption> captions;
    private Context cContext;

    public CaptionAdapter(Context context, ArrayList<SambaMedia.Caption> cList) {
        this.cContext = context;
        this.captions = cList;
    }

    @Override
    public int getCount() {
        return captions.size();
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
        LayoutInflater inflater = (LayoutInflater) cContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        CaptionItem holder;

        if(convertView == null) {
            convertView = inflater.inflate(R.layout.menu_item, parent, false);

            holder = new CaptionItem(convertView);
            convertView.setTag(holder);
        }else {
            holder = (CaptionItem) convertView.getTag();
        }

        SambaMedia.Caption caption = (SambaMedia.Caption) getItem(position);

        holder.label.setText(caption.language);

        if(caption.current) {
            holder.radio.setChecked(true);
        }else {
            holder.radio.setChecked(false);
        }

        return convertView;
    }

    static class CaptionItem {
        TextView label;
        RadioButton radio;

        CaptionItem(View view) {
            this.radio = (RadioButton) view.findViewById(R.id.output_radio);
            this.label = (TextView) view.findViewById(R.id.menu_item_label);
        }
    }
}
