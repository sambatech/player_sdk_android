package com.sambatech.player.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.sambatech.player.R;
import com.sambatech.player.SambaPlayer;
import com.sambatech.player.model.SambaMedia;

import java.util.ArrayList;

/**
 * @author tmiranda - 02/02/16
 */
public class OutputAdapter extends BaseAdapter {

	private ArrayList<SambaMedia.Output> outputs;
	private Context oContext;
	private SambaPlayer playerView;

	public OutputAdapter(Context context, ArrayList<SambaMedia.Output> oList, SambaPlayer pView) {
		this.outputs = oList;
		this.oContext = context;
		this.playerView = pView;
	}

	@Override
	public int getCount() {
		return outputs.size();
	}

	@Override
	public Object getItem(int position) {
		return outputs.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) oContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


		OutputItem holder;

		if(convertView == null) {
			convertView = inflater.inflate(R.layout.output_menu_item, parent, false);

			holder = new OutputItem(convertView);
			convertView.setTag(holder);
		}else {
			holder = (OutputItem) convertView.getTag();
		}

		SambaMedia.Output output = (SambaMedia.Output) getItem(position);

		holder.label.setText(output.label);
		if(output.current)
			holder.radio.setChecked(true);

		/**holder.label.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.e("outputs", "clicked");
				SambaMedia.Outputs output = (SambaMedia.Outputs) getItem(position);
				playerView.changeOutput(output.url);
			}
		});**/

		return convertView;
	}

	static class OutputItem {
		TextView label;
		RadioButton radio;

		OutputItem(View view) {
			this.radio = (RadioButton) view.findViewById(R.id.output_radio);
			this.label = (TextView) view.findViewById(R.id.output_label);
		}
	}
}
