package com.sambatech.sample.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sambatech.sample.R;
import com.sambatech.sample.model.MediaInfo;
import com.sambatech.sample.model.OnMediaClickListener;

import java.util.ArrayList;
import java.util.List;

public class MediasOfflineAdapter extends RecyclerView.Adapter<MediasOfflineAdapter.MediasOfflineHolder> {


    private List<MediaInfo> mediaItems = new ArrayList<>();
    private OnMediaClickListener listener;

    public  MediasOfflineAdapter(List<MediaInfo> mediaItems, OnMediaClickListener listener) {

        this.mediaItems = mediaItems;


        this.listener = listener;
    }

    @NonNull
    @Override
    public MediasOfflineHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_offline_cell, null);

        return new MediasOfflineHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediasOfflineHolder holder, int position) {

        final MediaInfo mediaInfo = mediaItems.get(position);

        holder.titleTextView.setText(mediaInfo.getTitle());

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onMediaClicked(mediaInfo, view);
            }
        });

        holder.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onDownloadButtonClicked(mediaInfo, view);
            }
        });

        holder.downloadButton.setColorFilter(true ? 0xFF42A5F5 : 0xFFBDBDBD);

    }

    @Override
    public int getItemCount() {
        return mediaItems.size();
    }

    public static class MediasOfflineHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public ProgressBar progressBar;
        public ImageButton downloadButton;
        public View container;

        public MediasOfflineHolder(View v) {
            super(v);
            container = v;
            titleTextView =  v.findViewById(R.id.title);
            progressBar =  v.findViewById(R.id.mProgress);
            downloadButton =  v.findViewById(R.id.mButton);

        }
    }

}
