package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.FloatLine;
import dev.vfyjxf.taffy.style.GridPlacement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Used track positions and implicit/explicit track counts for one grid axis. */
public class DetailedGridTracksInfo {
    private final TrackCounts counts;
    private final List<Float> sizes;
    private final List<FloatLine> positions;
    private final Float emptyAxisLine;
    private final Map<Integer, List<String>> lineNames;

    private DetailedGridTracksInfo(
        TrackCounts counts,
        List<Float> sizes,
        List<FloatLine> positions,
        Float emptyAxisLine,
        Map<Integer, List<String>> lineNames) {
        this.counts = new TrackCounts(counts);
        this.sizes = Collections.unmodifiableList(new ArrayList<>(sizes));
        this.positions = Collections.unmodifiableList(new ArrayList<>(positions));
        this.emptyAxisLine = emptyAxisLine;
        Map<Integer, List<String>> copiedNames = new HashMap<>();
        for (Map.Entry<Integer, List<String>> entry : lineNames.entrySet()) {
            copiedNames.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        this.lineNames = Collections.unmodifiableMap(copiedNames);
    }

    static DetailedGridTracksInfo from(
        TrackCounts counts,
        List<Float> sizes,
        List<Float> offsets,
        Map<Integer, List<String>> lineNames) {
        List<FloatLine> positions = new ArrayList<>();
        for (int i = 0; i < sizes.size(); i++) {
            float start = offsets.get(i);
            positions.add(new FloatLine(start, start + sizes.get(i)));
        }
        Float emptyAxisLine = positions.isEmpty() && !offsets.isEmpty() ? offsets.get(0) : null;
        return new DetailedGridTracksInfo(counts, sizes, positions, emptyAxisLine,
            lineNames == null ? Collections.emptyMap() : lineNames);
    }

    public TrackCounts counts() {
        return new TrackCounts(counts);
    }

    public List<Float> sizes() {
        return sizes;
    }

    public List<FloatLine> positions() {
        return positions;
    }

    /** Return the lone grid-line position for a trackless axis, or null when tracks exist. */
    public Float emptyAxisLine() {
        return emptyAxisLine;
    }

    /** Return names for a full-grid zero-based line index, including implicit-line padding. */
    public List<String> namesForLine(int lineIndex) {
        int explicitLine = lineIndex - counts.negativeImplicit + 1;
        List<String> names = lineNames.get(explicitLine);
        return names == null ? Collections.emptyList() : names;
    }

    /** Return all explicit named grid lines keyed by their one-based line index. */
    public Map<Integer, List<String>> lineNames() {
        return lineNames;
    }

    /** Serialize used track sizes and line names in CSS resolved-track-list form. */
    public String resolvedTrackList() {
        StringBuilder builder = new StringBuilder();
        for (int line = 0; line <= positions.size(); line++) {
            List<String> names = namesForLine(line);
            if (!names.isEmpty()) {
                appendToken(builder, "[" + String.join(" ", names) + "]");
            }
            if (line < sizes.size()) {
                appendToken(builder, String.format(Locale.ROOT, "%.4fpx", sizes.get(line)));
            }
        }
        return builder.toString();
    }

    private static void appendToken(StringBuilder builder, String token) {
        if (builder.length() > 0) builder.append(' ');
        builder.append(token);
    }

    FloatLine positionForTrackLine(int line) {
        int index = line + counts.negativeImplicit;
        if (index < 0 || index >= positions.size()) return null;
        return positions.get(index);
    }

    Integer resolveLine(GridPlacement placement) {
        if (placement == null || placement.isAuto() || !placement.isLine()) return null;
        int line = placement.getValue();
        int originZero = line > 0 ? line - 1 : line < 0 ? counts.explicit + 1 + line : Integer.MIN_VALUE;
        if (originZero < -counts.negativeImplicit || originZero > counts.implicitEndLine()) return null;
        return originZero + counts.negativeImplicit;
    }

    float startLinePosition(Integer index, float fallbackStart, float fallbackEnd, boolean reversed) {
        if (index != null && index >= 0 && index < positions.size()) {
            FloatLine position = positions.get(index);
            return reversed ? position.end : position.start;
        }
        if (index != null && index == positions.size() && !positions.isEmpty()) {
            FloatLine position = positions.get(positions.size() - 1);
            return reversed ? position.start : position.end;
        }
        if (emptyAxisLine != null) return emptyAxisLine;
        return reversed ? fallbackEnd : fallbackStart;
    }

    float endLinePosition(Integer index, float fallbackStart, float fallbackEnd, boolean reversed) {
        if (index != null && index > 0 && index <= positions.size()) {
            FloatLine position = positions.get(index - 1);
            return reversed ? position.start : position.end;
        }
        if (index != null && index == 0 && !positions.isEmpty()) {
            FloatLine position = positions.get(0);
            return reversed ? position.end : position.start;
        }
        if (emptyAxisLine != null) return emptyAxisLine;
        return reversed ? fallbackStart : fallbackEnd;
    }

    @Override
    public String toString() {
        return "DetailedGridTracksInfo{counts=" + counts + ", sizes=" + sizes + "}";
    }
}
