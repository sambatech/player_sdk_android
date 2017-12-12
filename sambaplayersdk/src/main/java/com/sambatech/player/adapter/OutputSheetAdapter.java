package com.sambatech.player.adapter;

import android.content.Context;
import android.media.MediaFormat;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.TrackGroup;
import com.sambatech.player.R;
import com.sambatech.player.SambaPlayer;

/**
 * Created by luizbyrro on 07/11/17.
 */

public class OutputSheetAdapter extends BaseAdapter {

    public int currentIndex = -1;
    private int offset = 0;

    private final @NonNull
    Context context;
    private final @NonNull
    TrackGroup outputs;

    public OutputSheetAdapter(@NonNull Context context, @NonNull TrackGroup outputs, boolean abrEnabled) {
        this.context = context;
        this.outputs = outputs;
        this.offset = abrEnabled ? 1 : 0;
    }

    @Override
    public int getCount() {
        return outputs.length + offset;
    }

    @Override
    public Object getItem(int position) {
        if (offset == 1 && position == 0) return null;
        return outputs.getFormat(position - offset);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    //TODO melhorar a renderizacao para nao ser duplicada
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final Format output = (Format) getItem(position);
        OutputSheetAdapter.OutputItem holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.action_sheet_item, parent, false);
            holder = new OutputSheetAdapter.OutputItem(convertView);
            convertView.setTag(holder);
        }
        else holder = (OutputSheetAdapter.OutputItem) convertView.getTag();

        if (output == null)
            holder.label.setText(R.string.qualitty_auto);
        else
            holder.label.setText(output.height > 0 ? output.height + "p" : Math.round(output.bitrate/1000f) + "k");
        holder.radio.setChecked(currentIndex == position);

        return convertView;
    }

    private static class OutputItem {
        TextView label;
        RadioButton radio;

        OutputItem(View view) {
            this.radio = (RadioButton) view.findViewById(R.id.sheet_output_radio);
            this.label = (TextView) view.findViewById(R.id.sheet_item_label);
        }
    }
}