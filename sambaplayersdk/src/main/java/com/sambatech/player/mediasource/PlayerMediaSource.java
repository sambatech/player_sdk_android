package com.sambatech.player.mediasource;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.FrameLayout;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.util.MimeTypes;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.offline.SambaDownloadManager;

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
    protected AdsLoader adsLoader;

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
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = getMappedTrackInfo();
        if (mappedTrackInfo == null) return null;
        return mappedTrackInfo.getTrackGroups(renderIndex);
    }

    public TrackGroup getVideoOutputsTracks() {
        TrackGroupArray trackGroupArray = getTrackGroupArray(VIDEO_RENDERER_INDEX);
        if (trackGroupArray == null || VIDEO_TRACK_GROUP_INDEX >= trackGroupArray.length)
            return null;
        return trackGroupArray.get(VIDEO_TRACK_GROUP_INDEX);
    }

    public void setVideoOutputTrack(Format format) {
        int index = getVideoOutputsTracks().indexOf(format);

        DefaultTrackSelector.SelectionOverride override = new DefaultTrackSelector.SelectionOverride(VIDEO_RENDERER_INDEX, index);

        DefaultTrackSelector.ParametersBuilder parametersBuilder = playerInstanceDefault.trackSelector.buildUponParameters();
        if (format != null) {
            parametersBuilder.setSelectionOverride(VIDEO_RENDERER_INDEX, getTrackGroupArray(VIDEO_RENDERER_INDEX), override);
        } else {
            parametersBuilder.clearSelectionOverrides(index);
        }
        playerInstanceDefault.trackSelector.setParameters(parametersBuilder);
    }

    public void addSubtitles(ArrayList<SambaMedia.Caption> captions) {
        if (captions == null || mediaSource == null) return;
        int captionID = 0;
        for (SambaMedia.Caption caption : captions) {
            if (caption.url != null && caption.label != null) {
                Format subs = Format.createTextSampleFormat(String.valueOf(captionID), MimeTypes.APPLICATION_SUBRIP, SELECTION_FLAG_AUTOSELECT, caption.label);
                MediaSource subSource = new SingleSampleMediaSource.Factory(SambaDownloadManager.getInstance().buildDataSourceFactory()).createMediaSource(Uri.parse(caption.url), subs, C.TIME_UNSET);
                mediaSource = new MergingMediaSource(mediaSource, subSource);
                captionID++;
            }
        }
    }

    public TrackGroupArray getSubtitles() {
        return getTrackGroupArray(CAPTION_RENDERER_INDEX);
    }

    public void setSubtitle(TrackGroup trackGroup) {

        DefaultTrackSelector.ParametersBuilder parametersBuilder = playerInstanceDefault.trackSelector.buildUponParameters();

        int index = getTrackGroupArray(CAPTION_RENDERER_INDEX).indexOf(trackGroup);

        DefaultTrackSelector.SelectionOverride override = new DefaultTrackSelector.SelectionOverride(index, CAPTION_FORMAT_INDEX);

        parametersBuilder.setSelectionOverride(CAPTION_RENDERER_INDEX, getTrackGroupArray(CAPTION_RENDERER_INDEX), override);

        playerInstanceDefault.trackSelector.setParameters(parametersBuilder);

    }

    public void addAds(String url, FrameLayout frameLayout) {
        this.adsLoader = new ImaAdsLoader(playerInstanceDefault.context, Uri.parse(url));
        this.mediaSource = new AdsMediaSource(
                mediaSource,
                playerInstanceDefault.mediaDataSourceFactory,
                adsLoader,
                frameLayout);
    }

    public void forceOutuputTrackTo(int index, boolean isAbrEnabled) {
        setVideoOutputTrack(getOutputByIndex(index,isAbrEnabled));
    }

    public int getCurrentOutputTrackIndex(TrackSelectionArray trackSelections, boolean isAbrEnabled) {
        Format video = null;
        TrackSelection videos = null;
        TrackGroup trackGroup = getVideoOutputsTracks();
        int index = C.INDEX_UNSET;
        if (trackSelections.length > 0) videos = trackSelections.get(VIDEO_RENDERER_INDEX);
        if (videos == null || trackGroup == null || trackSelections == null) return index;
        if (videos.getSelectionReason() != C.SELECTION_REASON_INITIAL && videos.getSelectionReason() != C.SELECTION_REASON_TRICK_PLAY) { //SELECTION_REASON_INITIAL == auto,
            if (trackSelections.length > 0 && trackSelections.get(0) != null)
                video = trackSelections.get(0).getSelectedFormat();
            if (video != null) index = trackGroup.indexOf(video);
            if (index != C.INDEX_UNSET) index = isAbrEnabled ? (index + 1) : index;
        } else {
            if (isAbrEnabled) return 0;
        }
        return index;
    }

    public void forceCaptionTrackTo(int index) {
        TrackGroup forcedCaption = getCaptionByIndex(index);
        if (forcedCaption != null) setSubtitle(forcedCaption);
    }

    public Format getOutputByIndex(int index, boolean isAbrEnabled){
        Format output = null;
        TrackGroup trackGroup = getVideoOutputsTracks();
        if (trackGroup != null) {
            if (!isAbrEnabled || index != 0) {
                index = index - (isAbrEnabled ? 1 : 0);
                if (trackGroup.length > index) {
                    output = getVideoOutputsTracks().getFormat(index);
                } else {
                    if (!isAbrEnabled) {
                        output = getVideoOutputsTracks().getFormat(0);
                    }
                }
            }
        }
        return output;
    }

    public int getCurrentCaptionTrackIndex(TrackSelectionArray trackSelections) {
        TrackGroup legenda = null;
        int index = C.INDEX_UNSET;
        TrackGroupArray trackGroupArray = getTrackGroupArray(CAPTION_RENDERER_INDEX);
        if (trackGroupArray != null && trackSelections != null) {
            if (trackSelections.length > CAPTION_RENDERER_INDEX && trackSelections.get(CAPTION_RENDERER_INDEX) != null)
                legenda = trackSelections.get(CAPTION_RENDERER_INDEX).getTrackGroup();
            index = trackGroupArray.indexOf(legenda);
        }
        return index;
    }

    public TrackGroup getCaptionByIndex(int index){
        TrackGroupArray captions = getTrackGroupArray(CAPTION_RENDERER_INDEX);
        TrackGroup caption = null;
        if (captions != null) {
            if (captions.length > index) {
                caption = captions.get(index);
            }
        }
        return caption;
    }

    protected void destroy() {
        playerInstanceDefault = null;
        url = null;
        mediaSource = null;
        adsLoader = null;
    }
}