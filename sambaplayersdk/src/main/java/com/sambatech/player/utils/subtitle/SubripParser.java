package com.sambatech.player.utils.subtitle;

/**
 * Created by thiagomir on 19/01/17.
 */
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.text.Subtitle;
import com.google.android.exoplayer.text.SubtitleParser;
import com.google.android.exoplayer.util.LongArray;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.ParsableByteArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple SubRip parser.
 */
public final class SubripParser implements SubtitleParser {

    private static final String TAG = "SubripParser";

    private static final String SUBRIP_TIMECODE = "(?:(\\d+):)?(\\d+):(\\d+),(\\d+)";
    private static final Pattern SUBRIP_TIMING_LINE =
            Pattern.compile("\\s*(" + SUBRIP_TIMECODE + ")\\s*-->\\s*(" + SUBRIP_TIMECODE + ")?\\s*");

    private final StringBuilder textBuilder;

    public SubripParser() {
        textBuilder = new StringBuilder();
    }

    @Override
    public boolean canParse(String mimeType) {
        return MimeTypes.APPLICATION_SUBRIP.equals(mimeType);
    }

    @Override
    public Subtitle parse(InputStream inputStream) throws IOException {
        return null;
    }

    public SubripSubtitle parse(byte[] bytes, int offset, int length) {
        ArrayList<Cue> cues = new ArrayList<>();
        LongArray cueTimesUs = new LongArray();
        ParsableByteArray subripData = new ParsableByteArray(bytes, offset + length);

        subripData.setPosition(offset);
        String currentLine;

        while ((currentLine = subripData.readLine()) != null) {
            if (currentLine.length() == 0) {
                // Skip blank lines.
                continue;
            }

            // Parse the index line as a sanity check.
            try {
                Integer.parseInt(currentLine);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Skipping invalid index: " + currentLine);
                continue;
            }

            // Read and parse the timing line.
            boolean haveEndTimecode = false;
            currentLine = subripData.readLine();
            Matcher matcher = SUBRIP_TIMING_LINE.matcher(currentLine);
            if (matcher.matches()) {
                cueTimesUs.add(parseTimecode(matcher, 1));
                if (!TextUtils.isEmpty(matcher.group(6))) {
                    haveEndTimecode = true;
                    cueTimesUs.add(parseTimecode(matcher, 6));
                }
            } else {
                Log.w(TAG, "Skipping invalid timing: " + currentLine);
                continue;
            }

            // Read and parse the text.
            textBuilder.setLength(0);
            while (!TextUtils.isEmpty(currentLine = subripData.readLine())) {
                if (textBuilder.length() > 0) {
                    textBuilder.append("<br>");
                }
                textBuilder.append(currentLine.trim());
            }

            Spanned text = Html.fromHtml(textBuilder.toString());
            cues.add(new Cue(text));
            if (haveEndTimecode) {
                cues.add(null);
            }
        }

        Cue[] cuesArray = new Cue[cues.size()];
        cues.toArray(cuesArray);
        long[] cueTimesUsArray = cueTimesUs.toArray();
        return new SubripSubtitle(cuesArray, cueTimesUsArray);
    }

    private static long parseTimecode(Matcher matcher, int groupOffset) {
        long timestampMs = Long.parseLong(matcher.group(groupOffset + 1)) * 60 * 60 * 1000;
        timestampMs += Long.parseLong(matcher.group(groupOffset + 2)) * 60 * 1000;
        timestampMs += Long.parseLong(matcher.group(groupOffset + 3)) * 1000;
        timestampMs += Long.parseLong(matcher.group(groupOffset + 4));
        return timestampMs * 1000;
    }

}

public final class ParsableByteArray2 implements ParsableByteArray {
    public String readLine() {
        if (bytesLeft() == 0) {
            return null;
        }
        int lineLimit = position;
        while (lineLimit < limit && data[lineLimit] != '\n' && data[lineLimit] != '\r') {
            lineLimit++;
        }
        if (lineLimit - position >= 3 && data[position] == (byte) 0xEF
                && data[position + 1] == (byte) 0xBB && data[position + 2] == (byte) 0xBF) {
            // There's a byte order mark at the start of the line. Discard it.
            position += 3;
        }
        String line = new String(data, position, lineLimit - position);
        position = lineLimit;
        if (position == limit) {
            return line;
        }
        if (data[position] == '\r') {
            position++;
            if (position == limit) {
                return line;
            }
        }
        if (data[position] == '\n') {
            position++;
        }
        return line;
    }
}