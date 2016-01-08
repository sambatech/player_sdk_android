package com.sambatech.player;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.sambatech.player.model.JSONMedia;
import com.sambatech.player.utils.GsonRequest;
import com.sambatech.player.utils.VolleySingleton;

import de.greenrobot.event.EventBus;

public class MainActivity extends Activity {

	private ListView list;
	private TextView status;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		list = (ListView)findViewById(R.id.media_list);
		initList();

		requestJson();
	}

	private void initList() {

		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//loadMedia((SambaMedia) parent.getAdapter().getItem(position));
				JSONMedia media = (JSONMedia) parent.getAdapter().getItem(position);
				EventBus.getDefault().postSticky(media);
				Intent intent = new Intent(parent.getContext(), MediaItemActivity.class);
				startActivity(intent);

				PendingIntent pendingIntent = TaskStackBuilder.create(parent.getContext()).addNextIntent(intent).getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
				NotificationCompat.Builder builder = new NotificationCompat.Builder(parent.getContext());
				builder.setContentIntent(pendingIntent);
			}
		});
	}

	private void createListAdapter(JSONMedia[] mediaList) {
		list.setAdapter(new ArrayAdapter<JSONMedia>(MainActivity.this, 0, mediaList) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null)
					convertView = LayoutInflater.from(getContext()).inflate(R.layout.media_list_item, parent, false);

				JSONMedia media = this.getItem(position);

				ImageLoader mImageLoader = VolleySingleton.getInstance().getImageLoader();
				Log.e("mock:", "creating");
				((NetworkImageView) convertView.findViewById(R.id.image)).setImageUrl(media.getThumbURL(), mImageLoader);
				((TextView) convertView.findViewById(R.id.text)).setText(media.title);

				return convertView;
			}
		});
	}

	private void requestJson() {
		String url = this.getString(R.string.mock_endpoint);
		RequestQueue queue = Volley.newRequestQueue(this);
		GsonRequest<JSONMedia[]> mockRequest = new GsonRequest<>(url, JSONMedia[].class, null, onRequestJsonSuccess(), onRequestJsonError());

		queue.add(mockRequest);
	}

	private Response.Listener<JSONMedia[]> onRequestJsonSuccess() {
		return new Response.Listener<JSONMedia[]>() {
			@Override
			public void onResponse(JSONMedia[] medias) {
				createListAdapter(medias);
			}
		};
	}

	private Response.ErrorListener onRequestJsonError() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e("mock:", error.getMessage());
				// Do whatever you want to do with error.getMessage();
			}
		};
	}
}
