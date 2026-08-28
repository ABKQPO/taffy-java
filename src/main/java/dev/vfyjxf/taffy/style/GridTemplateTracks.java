package dev.vfyjxf.taffy.style;

import java.util.ArrayList;
import java.util.List;

/** Parsed grid-template track components and their positional outer line-name groups. */
public class GridTemplateTracks {
    private final List<GridTemplateComponent> tracks;
    private final List<List<String>> lineNames;

    public GridTemplateTracks(List<GridTemplateComponent> tracks, List<List<String>> lineNames) {
        this.tracks = List.copyOf(tracks);
        this.lineNames = copyLineNames(lineNames);
        if (!this.lineNames.isEmpty() && this.lineNames.size() != this.tracks.size() + 1) {
            throw new IllegalArgumentException(
                "Grid template line names must contain one group for every component boundary");
        }
    }

    public List<GridTemplateComponent> getTracks() {
        return List.copyOf(tracks);
    }

    public List<List<String>> getLineNames() {
        return lineNames;
    }

    private static List<List<String>> copyLineNames(List<List<String>> lineNames) {
        if (lineNames == null || lineNames.isEmpty()) return List.of();
        List<List<String>> copied = new ArrayList<>(lineNames.size());
        for (List<String> names : lineNames) {
            copied.add(names == null ? List.of() : List.copyOf(names));
        }
        return List.copyOf(copied);
    }
}
