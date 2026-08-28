package dev.vfyjxf.taffy.style;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a repeat() function in grid-template-rows or grid-template-columns.
 * Used to express: repeat(count, track-list) where count can be:
 * - A specific integer (e.g., repeat(3, 1fr))
 * - auto-fill: Fill as many tracks as possible without overflowing
 * - auto-fit: Like auto-fill but collapses empty tracks
 */
public class GridRepetition {
    
    /**
     * The type of repetition count
     */
    public enum RepetitionType {
        /** A specific integer count */
        COUNT,
        /** auto-fill: Fill as many tracks as possible */
        AUTO_FILL,
        /** auto-fit: Like auto-fill but collapses empty tracks */
        AUTO_FIT
    }
    
    private final RepetitionType type;
    private final int count;
    private final List<TrackSizingFunction> tracks;
    private final List<List<String>> lineNames;
    
    private GridRepetition(
        RepetitionType type,
        int count,
        List<TrackSizingFunction> tracks,
        List<List<String>> lineNames) {
        this.type = type;
        this.count = count;
        this.tracks = new ArrayList<>(tracks);
        this.lineNames = copyLineNames(lineNames);
    }
    
    /**
     * Creates a repeat with a specific count
     */
    public static GridRepetition count(int count, List<TrackSizingFunction> tracks) {
        return count(count, tracks, List.of());
    }

    /** Creates a repetition from the typed public repeat count value. */
    public static GridRepetition of(RepetitionCount count, List<TrackSizingFunction> tracks) {
        return of(count, tracks, List.of());
    }

    /** Creates a repetition from the typed public repeat count and positional line names. */
    public static GridRepetition of(
        RepetitionCount count,
        List<TrackSizingFunction> tracks,
        List<List<String>> lineNames) {
        return switch (Objects.requireNonNull(count, "count").type()) {
            case COUNT -> count(count.count(), tracks, lineNames);
            case AUTO_FILL -> autoFill(tracks, lineNames);
            case AUTO_FIT -> autoFit(tracks, lineNames);
        };
    }

    /** Creates a repeat with positional line names for every repeated track line. */
    public static GridRepetition count(
        int count,
        List<TrackSizingFunction> tracks,
        List<List<String>> lineNames) {
        return new GridRepetition(RepetitionType.COUNT, count, tracks, lineNames);
    }
    
    /**
     * Creates a repeat with a specific count and a single track
     */
    public static GridRepetition count(int count, TrackSizingFunction track) {
        List<TrackSizingFunction> tracks = new ArrayList<>();
        tracks.add(track);
        return count(count, tracks);
    }
    
    /**
     * Creates an auto-fill repeat
     */
    public static GridRepetition autoFill(List<TrackSizingFunction> tracks) {
        return autoFill(tracks, List.of());
    }

    /** Creates an auto-fill repeat with positional line names. */
    public static GridRepetition autoFill(List<TrackSizingFunction> tracks, List<List<String>> lineNames) {
        return new GridRepetition(RepetitionType.AUTO_FILL, 0, tracks, lineNames);
    }
    
    /**
     * Creates an auto-fill repeat with a single track
     */
    public static GridRepetition autoFill(TrackSizingFunction track) {
        List<TrackSizingFunction> tracks = new ArrayList<>();
        tracks.add(track);
        return autoFill(tracks);
    }
    
    /**
     * Creates an auto-fit repeat
     */
    public static GridRepetition autoFit(List<TrackSizingFunction> tracks) {
        return autoFit(tracks, List.of());
    }

    /** Creates an auto-fit repeat with positional line names. */
    public static GridRepetition autoFit(List<TrackSizingFunction> tracks, List<List<String>> lineNames) {
        return new GridRepetition(RepetitionType.AUTO_FIT, 0, tracks, lineNames);
    }
    
    /**
     * Creates an auto-fit repeat with a single track
     */
    public static GridRepetition autoFit(TrackSizingFunction track) {
        List<TrackSizingFunction> tracks = new ArrayList<>();
        tracks.add(track);
        return autoFit(tracks);
    }
    
    public RepetitionType getType() {
        return type;
    }
    
    public int getCount() {
        return count;
    }

    /** Returns the typed public form of this repetition's count. */
    public RepetitionCount getRepetitionCount() {
        return switch (type) {
            case COUNT -> RepetitionCount.count(count);
            case AUTO_FILL -> RepetitionCount.autoFill();
            case AUTO_FIT -> RepetitionCount.autoFit();
        };
    }
    
    public List<TrackSizingFunction> getTracks() {
        return List.copyOf(tracks);
    }

    /**
     * Returns positional line-name sets for one repetition.
     * The list is empty when all repeated lines are unnamed; otherwise it must contain
     * exactly {@code getTrackCount() + 1} sets, including both edge lines.
     */
    public List<List<String>> getLineNames() {
        return lineNames;
    }
    
    public int getTrackCount() {
        return tracks.size();
    }
    
    public boolean isAutoRepetition() {
        return type == RepetitionType.AUTO_FILL || type == RepetitionType.AUTO_FIT;
    }
    
    /**
     * Checks if all tracks in this repetition have a fixed component.
     * Required for auto-fill/auto-fit to work correctly.
     */
    public boolean hasFixedComponent() {
        return tracks.stream().allMatch(TrackSizingFunction::hasFixedComponent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GridRepetition that = (GridRepetition) o;
        if (type != that.type) return false;
        if (count != that.count) return false;
        return Objects.equals(tracks, that.tracks) && Objects.equals(lineNames, that.lineNames);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + count;
        result = 31 * result + (tracks != null ? tracks.hashCode() : 0);
        result = 31 * result + (lineNames != null ? lineNames.hashCode() : 0);
        return result;
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
