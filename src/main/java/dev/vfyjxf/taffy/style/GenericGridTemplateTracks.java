package dev.vfyjxf.taffy.style;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Parsed grid-template components and positional line names with application-defined identifiers. */
public record GenericGridTemplateTracks<S>(
    List<GenericGridTemplateComponent<S>> tracks,
    List<List<S>> lineNames
) {
    public GenericGridTemplateTracks {
        tracks = List.copyOf(Objects.requireNonNull(tracks, "tracks"));
        lineNames = copyLineNames(lineNames);
        if (!lineNames.isEmpty() && lineNames.size() != tracks.size() + 1) {
            throw new IllegalArgumentException(
                "Grid template line names must contain one group for every component boundary");
        }
    }

    private static <S> List<List<S>> copyLineNames(List<List<S>> lineNames) {
        if (lineNames == null || lineNames.isEmpty()) return List.of();
        List<List<S>> copied = new ArrayList<>(lineNames.size());
        for (List<S> names : lineNames) {
            copied.add(List.copyOf(Objects.requireNonNull(names, "line name group")));
        }
        return List.copyOf(copied);
    }
}
