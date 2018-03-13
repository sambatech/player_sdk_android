package com.sambatech.player.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.sambatech.player.R;

/**
 * Created by luizbyrro on 01/12/2017.
 */

public class SpeedSheetAdapter extends BaseAdapter {

    public int currentIndex = -1;

    private Context cContext;
    private float[] speeds;

    public SpeedSheetAdapter(Context cContext, float[] speeds) {
        this.cContext = cContext;
        this.speeds = speeds;
    }
    @Override
    public int getCount() {
        return speeds.length;
    }

    @Override
    public Object getItem(int position) {
        return speeds[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SpeedSheetAdapter.SpeedItem holder;

        if (convertView == null) {
            convertView = ((LayoutInflater) cContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.action_sheet_item, parent, false);

            holder = new SpeedSheetAdapter.SpeedItem(convertView);
            convertView.setTag(holder);
        }
        else {
            holder = (SpeedSheetAdapter.SpeedItem) convertView.getTag();
        }

        float speed = (float) getItem(position);

        holder.label.setText(String.valueOf(speed) + "x");

        holder.radio.setChecked(currentIndex == position);

        return convertView;
    }

    private static class SpeedItem {
        TextView label;
        RadioButton radio;

        SpeedItem(View view) {
            this.radio = (RadioButton) view.findViewById(R.id.sheet_output_radio);
            this.label = (TextView) view.findViewById(R.id.sheet_item_label);
        }
    }
}
