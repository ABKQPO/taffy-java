package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.FloatLine;
import dev.vfyjxf.taffy.style.GridPlacement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Used track positions and implicit/explicit track counts for one grid axis. */
public class DetailedGridTracksInfo {
    private final TrackCounts counts;
    private final List<Float> sizes;
    private final List<FloatLine> positions;

    private DetailedGridTracksInfo(TrackCounts counts, List<Float> sizes, List<FloatLine> positions) {
        this.counts = new TrackCounts(counts);
        this.sizes = Collections.unmodifiableList(new ArrayList<>(sizes));
        this.positions = Collections.unmodifiableList(new ArrayList<>(positions));
    }

    static DetailedGridTracksInfo from(TrackCounts counts, List<Float> sizes, List<Float> offsets) {
        List<FloatLine> positions = new ArrayList<>();
        for (int i = 0; i < sizes.size(); i++) {
            float start = offsets.get(i);
            positions.add(new FloatLine(start, start + sizes.get(i)));
        }
        return new DetailedGridTracksInfo(counts, sizes, positions);
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

    FloatLine positionForTrackLine(int line) {
        int index = line + counts.negativeImplicit;
        if (index < 0 || index >= positions.size()) return null;
        return positions.get(index);
    }

    Integer resolveLine(GridPlacement placement, boolean start) {
        if (placement == null || placement.isAuto() || !placement.isLine()) return null;
        int line = placement.getValue();
        int originZero = line > 0 ? line - 1 : line < 0 ? counts.explicit + 1 + line : Integer.MIN_VALUE;
        if (originZero < -counts.negativeImplicit || originZero > counts.implicitEndLine()) return null;
        return originZero + counts.negativeImplicit;
    }

    float linePosition(Integer index, float fallbackStart, float fallbackEnd, boolean reversed, boolean start) {
        if (index == null || index < 0 || index >= positions.size() + 1) {
            return reversed ? (start ? fallbackEnd : fallbackStart) : (start ? fallbackStart : fallbackEnd);
        }
        if (index == positions.size()) return reversed ? fallbackStart : fallbackEnd;
        FloatLine position = positions.get(index);
        return reversed ? (start ? position.end : position.start) : (start ? position.start : position.end);
    }

    @Override
    public String toString() {
        return "DetailedGridTracksInfo{counts=" + counts + ", sizes=" + sizes + "}";
    }
}
