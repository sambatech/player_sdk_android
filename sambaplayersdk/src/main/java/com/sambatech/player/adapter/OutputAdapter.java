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
 * @author tmiranda - 02/02/16
 */
public class OutputAdapter extends BaseAdapter {

	private ArrayList<SambaMedia.Output> outputs;
	private Context oContext;


	public OutputAdapter(Context context, ArrayList<SambaMedia.Output> oList) {
		this.oContext = context;
		this.outputs = oList;
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

	//TODO melhorar a renderizacao para nao ser duplicada
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) oContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


		OutputItem holder;

		if(convertView == null) {
			convertView = inflater.inflate(R.layout.menu_item, parent, false);

			holder = new OutputItem(convertView);
			convertView.setTag(holder);
		}else {
			holder = (OutputItem) convertView.getTag();
		}

		SambaMedia.Output output = (SambaMedia.Output) getItem(position);

		holder.label.setText(output.label);

		if(output.isDefault) {
			holder.radio.setChecked(true);
		}else {
			holder.radio.setChecked(false);
		}

		/**holder.radio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.e("outputs", "clicked");
			}
		});**/

		return convertView;
	}

	static class OutputItem {
		TextView label;
		RadioButton radio;

		OutputItem(View view) {
			this.radio = (RadioButton) view.findViewById(R.id.output_radio);
			this.label = (TextView) view.findViewById(R.id.menu_item_label);
		}
	}
}
