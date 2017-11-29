package com.sambatech.player.mediasource;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.sambatech.player.model.SambaMedia;

import java.util.ArrayList;

/**
 * Created by luizbyrro on 28/11/2017.
 */

public interface PlayerMediaSourceInterface {
    void setUrl(String url);

    MediaSource getMediaSource();

    TrackGroup getVideoOutputsTracks();

    void setVideoOutputTrack(Format format);

    void addSubtitles(ArrayList<SambaMedia.Caption> captions);

    TrackGroupArray getSubtitles();

    void setSubtitle(TrackGroup trackGroup);
}
