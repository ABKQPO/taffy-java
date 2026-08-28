package dev.vfyjxf.taffy.style;

import java.util.Objects;

/** A CSS Grid minimum track sizing function backed by the shared runtime track model. */
public class MinTrackSizingFunction {
    private final TrackSizingFunction value;

    private MinTrackSizingFunction(TrackSizingFunction value) {
        this.value = requireValid(value);
    }

    public static MinTrackSizingFunction from(TrackSizingFunction value) {
        return new MinTrackSizingFunction(value);
    }

    /** Converts a maximum track value, replacing invalid minimum values with {@code auto}. */
    public static MinTrackSizingFunction from(MaxTrackSizingFunction value) {
        TrackSizingFunction track = Objects.requireNonNull(value, "value").toTrackSizingFunction();
        return track.isFr() || track.isFitContent() ? auto() : from(track);
    }

    /** Converts a dimension using the valid minimum-track subset. */
    public static MinTrackSizingFunction from(TaffyDimension value) {
        return from(LengthPercentageAuto.from(Objects.requireNonNull(value, "value")));
    }

    /** Converts a length-percentage-auto value using the valid minimum-track subset. */
    public static MinTrackSizingFunction from(LengthPercentageAuto value) {
        return switch (Objects.requireNonNull(value, "value").getType()) {
            case LENGTH -> length(value.getValue());
            case PERCENT -> percent(value.getValue());
            case CALC -> fixed(LengthPercentage.calc(value.getCalcExpression()));
            case AUTO -> auto();
            case MIN_CONTENT -> minContent();
            case MAX_CONTENT -> maxContent();
            case FIT_CONTENT, STRETCH -> auto();
        };
    }

    /** Converts a length-percentage value into a fixed minimum track value. */
    public static MinTrackSizingFunction from(LengthPercentage value) {
        return fixed(Objects.requireNonNull(value, "value"));
    }

    public static MinTrackSizingFunction length(float value) {
        return from(TrackSizingFunction.fixed(value));
    }

    public static MinTrackSizingFunction percent(float value) {
        return from(TrackSizingFunction.percent(value));
    }

    public static MinTrackSizingFunction fixed(LengthPercentage value) {
        return from(TrackSizingFunction.fixed(value));
    }

    public static MinTrackSizingFunction auto() {
        return from(TrackSizingFunction.auto());
    }

    public static MinTrackSizingFunction minContent() {
        return from(TrackSizingFunction.minContent());
    }

    public static MinTrackSizingFunction maxContent() {
        return from(TrackSizingFunction.maxContent());
    }

    /** Parses a complete CSS minimum track sizing function. */
    public static MinTrackSizingFunction parse(String value) {
        return CssParser.parseMinTrackSizingFunction(value);
    }

    public TrackSizingFunction toTrackSizingFunction() {
        return value;
    }

    public ExpandedTrackSizingFunction expand() {
        return value.expand();
    }

    public boolean isAuto() {
        return value.isAuto();
    }

    public boolean isMinContent() {
        return value.isMinContent();
    }

    public boolean isMaxContent() {
        return value.isMaxContent();
    }

    public boolean isIntrinsic() {
        return value.isIntrinsic();
    }

    public boolean isMinOrMaxContent() {
        return value.isMinContent() || value.isMaxContent();
    }

    public boolean usesPercentage() {
        return value.usesPercentage();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof MinTrackSizingFunction function && value.equals(function.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }

    private static TrackSizingFunction requireValid(TrackSizingFunction value) {
        TrackSizingFunction candidate = Objects.requireNonNull(value, "value");
        if (candidate.isFixed() || candidate.isAuto() || candidate.isMinContent() || candidate.isMaxContent()) {
            return candidate;
        }
        throw new IllegalArgumentException("Invalid minimum track sizing function: " + candidate);
    }
}
