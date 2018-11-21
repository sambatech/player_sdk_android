package com.sambatech.player.offline;

import android.content.res.Resources;
import android.text.TextUtils;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.ui.TrackNameProvider;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import java.util.Locale;

public class SambaTrackNameProvider implements TrackNameProvider {

    private final Resources resources;


    public SambaTrackNameProvider(Resources resources) {
        this.resources = Assertions.checkNotNull(resources);
    }

    @Override
    public String getTrackName(Format format) {
        String trackName;
        int trackType = inferPrimaryTrackType(format);
        if (trackType == C.TRACK_TYPE_VIDEO) {
            trackName = buildResolutionString(format);
        } else if (trackType == C.TRACK_TYPE_AUDIO) {
            trackName = joinWithSeparator(
                            buildLabelString(format),
                            buildAudioChannelString(format)
            );
        } else {
            trackName = buildLabelString(format);
        }
        return trackName.length() == 0 ? resources.getString(com.google.android.exoplayer2.ui.R.string.exo_track_unknown) : trackName;
    }

    private String buildResolutionString(Format format) {
        int width = format.width;
        int height = format.height;
        return width == Format.NO_VALUE || height == Format.NO_VALUE
                ? ""
                : resources.getString(com.google.android.exoplayer2.ui.R.string.exo_track_resolution, width, height);
    }

    private String buildAudioChannelString(Format format) {
        int channelCount = format.channelCount;
        if (channelCount == Format.NO_VALUE || channelCount < 1) {
            return "";
        }
        switch (channelCount) {
            case 1:
                return resources.getString(com.google.android.exoplayer2.ui.R.string.exo_track_mono);
            case 2:
                return resources.getString(com.google.android.exoplayer2.ui.R.string.exo_track_stereo);
            case 6:
            case 7:
                return resources.getString(com.google.android.exoplayer2.ui.R.string.exo_track_surround_5_point_1);
            case 8:
                return resources.getString(com.google.android.exoplayer2.ui.R.string.exo_track_surround_7_point_1);
            default:
                return resources.getString(com.google.android.exoplayer2.ui.R.string.exo_track_surround);
        }
    }

    private String buildLabelString(Format format) {
        if (!TextUtils.isEmpty(format.label)) {
            return format.label;
        }
        // Fall back to using the language.
        String language = format.language;
        return TextUtils.isEmpty(language) || C.LANGUAGE_UNDETERMINED.equals(language)
                ? ""
                : buildLanguageString(language);
    }

    private String buildLanguageString(String language) {
        Locale locale = Util.SDK_INT >= 21 ? Locale.forLanguageTag(language) : new Locale(language);
        return locale.getDisplayLanguage();
    }

    private String joinWithSeparator(String... items) {
        String itemList = "";
        for (String item : items) {
            if (item.length() > 0) {
                if (TextUtils.isEmpty(itemList)) {
                    itemList = item;
                } else {
                    itemList = resources.getString(com.google.android.exoplayer2.ui.R.string.exo_item_list, itemList, item);
                }
            }
        }
        return itemList;
    }

    private static int inferPrimaryTrackType(Format format) {
        int trackType = MimeTypes.getTrackType(format.sampleMimeType);
        if (trackType != C.TRACK_TYPE_UNKNOWN) {
            return trackType;
        }
        if (MimeTypes.getVideoMediaMimeType(format.codecs) != null) {
            return C.TRACK_TYPE_VIDEO;
        }
        if (MimeTypes.getAudioMediaMimeType(format.codecs) != null) {
            return C.TRACK_TYPE_AUDIO;
        }
        if (format.width != Format.NO_VALUE || format.height != Format.NO_VALUE) {
            return C.TRACK_TYPE_VIDEO;
        }
        if (format.channelCount != Format.NO_VALUE || format.sampleRate != Format.NO_VALUE) {
            return C.TRACK_TYPE_AUDIO;
        }
        return C.TRACK_TYPE_UNKNOWN;
    }
}
