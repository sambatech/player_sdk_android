package com.sambatech.exoplayerv2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sambatech.exoplayerv2.model.MediaContent;

import java.util.List;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item_list);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setTitle(getTitle());

		View recyclerView = findViewById(R.id.item_list);
		assert recyclerView != null;
		setupRecyclerView((RecyclerView) recyclerView);

		if (findViewById(R.id.item_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-w900dp).
			// If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;
		}
	}

	private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
		recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(MediaContent.items));
	}

	public class SimpleItemRecyclerViewAdapter
			extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

		private final List<MediaContent.Media> mValues;

		public SimpleItemRecyclerViewAdapter(List<MediaContent.Media> items) {
			mValues = items;
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.item_list_content, parent, false);
			return new ViewHolder(view, (TextView)view.findViewById(R.id.id),
					(TextView)view.findViewById(R.id.content));
		}

		@Override
		public void onBindViewHolder(final ViewHolder holder, int position) {
			holder.mItem = mValues.get(position);
			holder.mIdView.setText(mValues.get(position).id);
			holder.mContentView.setText(mValues.get(position).title);

			holder.mView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mTwoPane) {
						Bundle arguments = new Bundle();
						arguments.putInt(ItemDetailFragment.ARG_ITEM_ID, MediaContent.items.indexOf(holder.mItem));
						ItemDetailFragment fragment = new ItemDetailFragment();
						fragment.setArguments(arguments);
						getSupportFragmentManager().beginTransaction()
								.replace(R.id.item_detail_container, fragment)
								.commit();
					} else {
						Context context = v.getContext();
						Intent intent = new Intent(context, ItemDetailActivity.class);
						intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, MediaContent.items.indexOf(holder.mItem));

						context.startActivity(intent);
					}
				}
			});
		}

		@Override
		public int getItemCount() {
			return mValues.size();
		}

		public class ViewHolder extends RecyclerView.ViewHolder {
			public final View mView;
			public final TextView mIdView;
			public final TextView mContentView;
			public MediaContent.Media mItem;

			public ViewHolder(View view, TextView idView, TextView contentView) {
				super(view);
				mView = view;
				mIdView = idView;
				mContentView = contentView;
			}

			@Override
			public String toString() {
				return (mItem != null ? mItem : mView).toString();
			}
		}
	}
}
