package com.sambatech.player.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.exoplayer.MediaFormat;
import com.sambatech.player.R;
import com.sambatech.player.SambaPlayer;

/**
 * Created by luizbyrro on 07/11/17.
 */

public class OutputSheetAdapter extends BaseAdapter {

    private final @NonNull
    Context context;
    private final @NonNull MediaFormat[] outputs;
    private final @NonNull
    SambaPlayer player;
    private final int outputOffset;

    public OutputSheetAdapter(@NonNull Context context, @NonNull MediaFormat[] outputs, @NonNull SambaPlayer player, int outputOffset) {
        this.context = context;
        this.outputs = outputs;
        this.player = player;
        this.outputOffset = outputOffset;
    }

    @Override
    public int getCount() {
        return outputs.length - outputOffset;
    }

    @Override
    public Object getItem(int position) {
        return outputs[position + outputOffset];
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

        final MediaFormat output = (MediaFormat) getItem(position);
        OutputSheetAdapter.OutputItem holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.action_sheet_item, parent, false);
            holder = new OutputSheetAdapter.OutputItem(convertView);

            convertView.setTag(holder);
        }
        else holder = (OutputSheetAdapter.OutputItem) convertView.getTag();

        holder.label.setText(output.adaptive ? "Auto" : output.height > 0 ?
                output.height + "p" : Math.round(output.bitrate/1000f) + "k");
        holder.radio.setChecked(position == player.getCurrentOutputIndex());

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