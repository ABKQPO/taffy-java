package dev.vfyjxf.taffy.style;

import java.util.Arrays;
import java.util.List;

/** Convenience constructors corresponding to Taffy's style helper functions. */
public class StyleHelpers {
    private StyleHelpers() {
    }

    public static GridTemplateComponent repeat(int count, TrackSizingFunction... tracks) {
        return repeat(count, Arrays.asList(tracks));
    }

    public static GridTemplateComponent repeat(int count, List<TrackSizingFunction> tracks) {
        if (count < 0 || count > 65535) {
            throw new IllegalArgumentException("Repeat count must fit in an unsigned 16-bit integer");
        }
        return GridTemplateComponent.repeat(GridRepetition.count(count, tracks));
    }

    public static List<GridTemplateComponent> evenlySizedTracks(int count) {
        return List.of(repeat(count, flex(1f)));
    }

    public static GridPlacement line(int index) {
        return GridPlacement.line(index);
    }

    public static GridPlacement span(int span) {
        return GridPlacement.span(span);
    }

    public static TrackSizingFunction minmax(TrackSizingFunction min, TrackSizingFunction max) {
        return TrackSizingFunction.minmax(min, max);
    }

    public static TrackSizingFunction flex(float fraction) {
        return minmax(TrackSizingFunction.fixed(0f), fr(fraction));
    }

    public static TrackSizingFunction fr(float fraction) {
        return TrackSizingFunction.fr(fraction);
    }

    public static TaffyDimension length(float value) {
        return TaffyDimension.length(value);
    }

    public static TaffyDimension percent(float value) {
        return TaffyDimension.percent(value);
    }

    public static TaffyDimension zero() {
        return TaffyDimension.ZERO;
    }

    public static TaffyDimension auto() {
        return TaffyDimension.AUTO;
    }

    public static TaffyDimension minContent() {
        return TaffyDimension.MIN_CONTENT;
    }

    public static TaffyDimension maxContent() {
        return TaffyDimension.MAX_CONTENT;
    }

    public static TaffyDimension fitContent(LengthPercentage argument) {
        if (argument == null) throw new IllegalArgumentException("argument must not be null");
        return TaffyDimension.fitContent(argument);
    }
}
