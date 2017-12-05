package com.sambatech.player.mediasource;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.ext.ima.ImaAdsMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.sambatech.player.model.SambaMedia;

import java.util.ArrayList;

import static com.google.android.exoplayer2.C.SELECTION_FLAG_AUTOSELECT;

/**
 * Created by luizbyrro on 28/11/2017.
 */

public class PlayerMediaSource {

    protected static final int VIDEO_TRACK_GROUP_INDEX = 0;
    protected static final int VIDEO_RENDERER_INDEX = 0;
    protected static final int CAPTION_RENDERER_INDEX = 2;
    protected static final int CAPTION_FORMAT_INDEX = 0;

    protected PlayerInstanceDefault playerInstanceDefault;
    protected String url;
    protected MediaSource mediaSource;
    protected ImaAdsLoader imaAdsLoader;

    protected PlayerMediaSource(@NonNull PlayerInstanceDefault playerInstanceDefault) {
        this.playerInstanceDefault = playerInstanceDefault;
    }

    protected void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    protected void setMediaSource(MediaSource mediaSource) {
        this.mediaSource = mediaSource;
    }

    public MediaSource getMediaSource() {
        return mediaSource;
    }

    private MappingTrackSelector.MappedTrackInfo getMappedTrackInfo() {
        if (playerInstanceDefault.trackSelector == null) return null;
        return playerInstanceDefault.trackSelector.getCurrentMappedTrackInfo();
    }

    private TrackGroupArray getTrackGroupArray(int renderIndex) {
        if (getMappedTrackInfo() == null) return null;
        return getMappedTrackInfo().getTrackGroups(renderIndex);
    }

    public TrackGroup getVideoOutputsTracks() {
        TrackGroupArray trackGroupArray = getTrackGroupArray(VIDEO_RENDERER_INDEX);
        if (trackGroupArray == null || VIDEO_TRACK_GROUP_INDEX >= trackGroupArray.length) return null;
        return trackGroupArray.get(VIDEO_TRACK_GROUP_INDEX);
    }

    public void setVideoOutputTrack(Format format) {
        if (format == null) {
            playerInstanceDefault.trackSelector.clearSelectionOverride(VIDEO_RENDERER_INDEX, getTrackGroupArray(VIDEO_RENDERER_INDEX));
            return;
        }
        int index = getVideoOutputsTracks().indexOf(format);
        MappingTrackSelector.SelectionOverride override = new MappingTrackSelector.SelectionOverride(new FixedTrackSelection.Factory(), VIDEO_TRACK_GROUP_INDEX, index);
        playerInstanceDefault.trackSelector.setSelectionOverride(VIDEO_RENDERER_INDEX, getTrackGroupArray(VIDEO_RENDERER_INDEX), override);
    }

    public void addSubtitles(ArrayList<SambaMedia.Caption> captions) {
        if (captions == null || mediaSource == null) return;
        int captionID = 0;
        for (SambaMedia.Caption caption : captions) {
            if(caption.url != null && caption.label != null) {
                Format englishSubs = Format.createTextSampleFormat(String.valueOf(captionID), MimeTypes.APPLICATION_SUBRIP, SELECTION_FLAG_AUTOSELECT, caption.label);
                MediaSource subSource = new SingleSampleMediaSource(Uri.parse(caption.url), new DefaultHttpDataSourceFactory("userAgent"), englishSubs, C.TIME_UNSET);
                mediaSource = new MergingMediaSource(mediaSource, subSource);
                captionID++;
            }
        }
    }

    public TrackGroupArray getSubtitles(){
        return getTrackGroupArray(CAPTION_RENDERER_INDEX);
    }

    public void setSubtitle(TrackGroup trackGroup){
        if (trackGroup == null) {
            playerInstanceDefault.trackSelector.clearSelectionOverride(CAPTION_RENDERER_INDEX, getTrackGroupArray(CAPTION_RENDERER_INDEX));
            return;
        }
        int index = getTrackGroupArray(CAPTION_RENDERER_INDEX).indexOf(trackGroup);
        MappingTrackSelector.SelectionOverride override = new MappingTrackSelector.SelectionOverride(new FixedTrackSelection.Factory(), index, CAPTION_FORMAT_INDEX);
        playerInstanceDefault.trackSelector.setSelectionOverride(CAPTION_RENDERER_INDEX, getTrackGroupArray(CAPTION_RENDERER_INDEX), override);
    }

    public void addAds(String url, FrameLayout frameLayout) {
        this.imaAdsLoader = new ImaAdsLoader(playerInstanceDefault.context, Uri.parse(url));
        this.mediaSource =  new ImaAdsMediaSource(
                mediaSource,
                playerInstanceDefault.mediaDataSourceFactory,
                imaAdsLoader,
                frameLayout);
    }
}