package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.FloatLine;
import dev.vfyjxf.taffy.style.CustomIdentCodec;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Read-only typed view of grid track diagnostics with caller-defined identifiers. */
public class GenericDetailedGridTracksInfo<S> {
    private final DetailedGridTracksInfo runtime;
    private final CustomIdentCodec<S> identifierCodec;

    GenericDetailedGridTracksInfo(DetailedGridTracksInfo runtime, CustomIdentCodec<S> identifierCodec) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.identifierCodec = Objects.requireNonNull(identifierCodec, "identifierCodec");
    }

    public TrackCounts counts() {
        return runtime.counts();
    }

    public List<Float> sizes() {
        return runtime.sizes();
    }

    public List<FloatLine> positions() {
        return runtime.positions();
    }

    public Float emptyAxisLine() {
        return runtime.emptyAxisLine();
    }

    public List<S> namesForLine(int lineIndex) {
        return decode(runtime.namesForLine(lineIndex));
    }

    public Map<Integer, List<S>> lineNames() {
        Map<Integer, List<S>> decoded = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<String>> entry : runtime.lineNames().entrySet()) {
            decoded.put(entry.getKey(), decode(entry.getValue()));
        }
        return Map.copyOf(decoded);
    }

    public String resolvedTrackList() {
        return runtime.resolvedTrackList();
    }

    private List<S> decode(List<String> names) {
        return names.stream().map(identifierCodec::decode).toList();
    }
}
