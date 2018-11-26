package com.sambatech.sample.adapters;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sambatech.player.offline.SambaDownloadManager;
import com.sambatech.player.offline.model.DownloadState;
import com.sambatech.sample.R;
import com.sambatech.sample.model.MediaInfo;
import com.sambatech.sample.model.OnMediaClickListener;

import java.util.List;

public class MediasOfflineAdapter extends RecyclerView.Adapter<MediasOfflineAdapter.MediasOfflineHolder> {


    private List<MediaInfo> mediaItems;
    private OnMediaClickListener listener;

    public MediasOfflineAdapter(List<MediaInfo> mediaItems, OnMediaClickListener listener) {

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

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onDownloadButtonClicked(mediaInfo, view);
            }
        };

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onMediaClicked(mediaInfo, view);
            }
        });

        holder.downloadButton.setOnClickListener(clickListener);
        holder.progressBar.setOnClickListener(clickListener);

        if (mediaInfo.getDownloadState() != null) {
            DownloadState downloadState = mediaInfo.getDownloadState();

            switch (downloadState.state) {
                case WAITING:
                    holder.progressContainer.setVisibility(View.VISIBLE);
                    holder.progressText.setVisibility(View.INVISIBLE);
                    holder.progressBar.setIndeterminate(true);
                    holder.downloadButton.setVisibility(View.GONE);
                    break;
                case IN_PROGRESS:
                    holder.progressContainer.setVisibility(View.VISIBLE);
                    holder.progressText.setVisibility(View.VISIBLE);
                    holder.progressBar.setIndeterminate(false);
                    holder.progressBar.setMax(100);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        holder.progressBar.setMin(0);
                    }
                    holder.progressBar.setProgress((int) downloadState.downloadPercentage);
                    holder.progressText.setText(String.format("%.1f%%", downloadState.downloadPercentage >= 0 ? downloadState.downloadPercentage : 0));
                    holder.downloadButton.setVisibility(View.GONE);
                    break;
                case FAILED:
                    holder.downloadButton.setVisibility(View.VISIBLE);
                    holder.downloadButton.setColorFilter(0xFF42A5F5);
                    holder.progressContainer.setVisibility(View.GONE);
                    break;
                case COMPLETED:
                    holder.downloadButton.setVisibility(View.VISIBLE);
                    holder.downloadButton.setColorFilter(0xFFBDBDBD);
                    holder.progressContainer.setVisibility(View.GONE);
                    break;
                case CANCELED:
                case DELETED:
                    holder.downloadButton.setVisibility(View.VISIBLE);
                    holder.downloadButton.setColorFilter(0xFF42A5F5);
                    holder.progressContainer.setVisibility(View.GONE);
                    break;
            }


        } else if (SambaDownloadManager.getInstance().isDownloading(mediaInfo.getId())) {
            holder.progressContainer.setVisibility(View.VISIBLE);
            holder.progressText.setVisibility(View.INVISIBLE);
            holder.progressBar.setIndeterminate(true);
            holder.downloadButton.setVisibility(View.GONE);
        } else {
            boolean isDownloaded = SambaDownloadManager.getInstance().isDownloaded(mediaInfo.getId());
            holder.downloadButton.setVisibility(View.VISIBLE);
            holder.downloadButton.setColorFilter(!isDownloaded ? 0xFF42A5F5 : 0xFFBDBDBD);
            holder.progressContainer.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return mediaItems.size();
    }

    public static class MediasOfflineHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView progressText;
        public ProgressBar progressBar;
        public ImageButton downloadButton;
        public LinearLayout progressContainer;
        public View container;

        public MediasOfflineHolder(View v) {
            super(v);
            container = v;
            titleTextView = v.findViewById(R.id.title);
            progressBar = v.findViewById(R.id.mProgress);
            downloadButton = v.findViewById(R.id.mButton);
            progressContainer = v.findViewById(R.id.progressContainer);
            progressText = v.findViewById(R.id.progressText);
        }
    }

}
