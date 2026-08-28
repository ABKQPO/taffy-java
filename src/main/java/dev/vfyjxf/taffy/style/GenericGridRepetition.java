package dev.vfyjxf.taffy.style;

import dev.vfyjxf.taffy.style.GridRepetition.RepetitionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** A grid repeat definition whose line names use an application-defined identifier type. */
public class GenericGridRepetition<S> {
    private final RepetitionType type;
    private final int count;
    private final List<TrackSizingFunction> tracks;
    private final List<List<S>> lineNames;

    private GenericGridRepetition(
        RepetitionType type,
        int count,
        List<TrackSizingFunction> tracks,
        List<List<S>> lineNames) {
        this.type = Objects.requireNonNull(type, "type");
        this.count = count;
        this.tracks = List.copyOf(Objects.requireNonNull(tracks, "tracks"));
        this.lineNames = copyGroups(lineNames);
    }

    public static <S> GenericGridRepetition<S> count(
        int count, List<TrackSizingFunction> tracks, List<List<S>> lineNames) {
        return new GenericGridRepetition<>(RepetitionType.COUNT, count, tracks, lineNames);
    }

    public static <S> GenericGridRepetition<S> autoFill(List<TrackSizingFunction> tracks, List<List<S>> lineNames) {
        return new GenericGridRepetition<>(RepetitionType.AUTO_FILL, 0, tracks, lineNames);
    }

    public static <S> GenericGridRepetition<S> autoFit(List<TrackSizingFunction> tracks, List<List<S>> lineNames) {
        return new GenericGridRepetition<>(RepetitionType.AUTO_FIT, 0, tracks, lineNames);
    }

    public RepetitionType type() {
        return type;
    }

    public int count() {
        return count;
    }

    public List<TrackSizingFunction> tracks() {
        return tracks;
    }

    public List<List<S>> lineNames() {
        return lineNames;
    }

    public GridRepetition toGridRepetition(CustomIdentCodec<S> codec) {
        List<List<String>> runtimeNames = new ArrayList<>(lineNames.size());
        for (List<S> group : lineNames) {
            List<String> names = new ArrayList<>(group.size());
            for (S identifier : group) {
                names.add(codec.encode(identifier));
            }
            runtimeNames.add(List.copyOf(names));
        }
        return switch (type) {
            case COUNT -> GridRepetition.count(count, tracks, runtimeNames);
            case AUTO_FILL -> GridRepetition.autoFill(tracks, runtimeNames);
            case AUTO_FIT -> GridRepetition.autoFit(tracks, runtimeNames);
        };
    }

    private static <S> List<List<S>> copyGroups(List<List<S>> groups) {
        Objects.requireNonNull(groups, "lineNames");
        List<List<S>> copied = new ArrayList<>(groups.size());
        for (List<S> group : groups) {
            copied.add(List.copyOf(Objects.requireNonNull(group, "line name group")));
        }
        return List.copyOf(copied);
    }
}
