package com.sambatech.player.mediasource;

import android.widget.FrameLayout;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.sambatech.player.model.SambaMedia;

import java.util.List;

/**
 * Created by luizbyrro on 28/11/2017.
 */

public interface PlayerMediaSourceInterface {
    void setUrl(String url);

    MediaSource getMediaSource();

    TrackGroup getVideoOutputsTracks();

    void setVideoOutputTrack(Format format);

    void addSubtitles(List<SambaMedia.Caption> captions);

    TrackGroupArray getSubtitles();

    void setSubtitle(TrackGroup trackGroup);

    void addAds(String url, FrameLayout frame);

    void forceOutuputTrackTo(int index, boolean isAbrEnabled);

    int getCurrentOutputTrackIndex(TrackSelectionArray trackSelections, boolean isAbrEnabled);

    Format getOutputByIndex(int index, boolean isAbrEnabled);

    void forceCaptionTrackTo(int index);

    int getCurrentCaptionTrackIndex(TrackSelectionArray trackSelections);

    TrackGroup getCaptionByIndex(int index);

    void destroy();
}
