package dev.vfyjxf.taffy.style;

import java.util.Objects;

/** A CSS Grid maximum track sizing function backed by the shared runtime track model. */
public class MaxTrackSizingFunction {
    private final TrackSizingFunction value;

    private MaxTrackSizingFunction(TrackSizingFunction value) {
        this.value = requireValid(value);
    }

    public static MaxTrackSizingFunction from(TrackSizingFunction value) {
        return new MaxTrackSizingFunction(value);
    }

    /** Converts a minimum track value into a maximum track value. */
    public static MaxTrackSizingFunction from(MinTrackSizingFunction value) {
        return from(Objects.requireNonNull(value, "value").toTrackSizingFunction());
    }

    /** Converts a dimension using the valid maximum-track subset. */
    public static MaxTrackSizingFunction from(TaffyDimension value) {
        return from(LengthPercentageAuto.from(Objects.requireNonNull(value, "value")));
    }

    /** Converts a length-percentage-auto value using the valid maximum-track subset. */
    public static MaxTrackSizingFunction from(LengthPercentageAuto value) {
        return switch (Objects.requireNonNull(value, "value").getType()) {
            case LENGTH -> length(value.getValue());
            case PERCENT -> percent(value.getValue());
            case CALC -> fixed(LengthPercentage.calc(value.getCalcExpression()));
            case AUTO -> auto();
            case MIN_CONTENT -> minContent();
            case MAX_CONTENT -> maxContent();
            case FIT_CONTENT -> value.getFitContentLimit() == null ? auto() : fitContent(value.getFitContentLimit());
            case STRETCH -> auto();
        };
    }

    /** Converts a length-percentage value into a fixed maximum track value. */
    public static MaxTrackSizingFunction from(LengthPercentage value) {
        return fixed(Objects.requireNonNull(value, "value"));
    }

    public static MaxTrackSizingFunction length(float value) {
        return from(TrackSizingFunction.fixed(value));
    }

    public static MaxTrackSizingFunction percent(float value) {
        return from(TrackSizingFunction.percent(value));
    }

    public static MaxTrackSizingFunction fixed(LengthPercentage value) {
        return from(TrackSizingFunction.fixed(value));
    }

    public static MaxTrackSizingFunction auto() {
        return from(TrackSizingFunction.auto());
    }

    public static MaxTrackSizingFunction minContent() {
        return from(TrackSizingFunction.minContent());
    }

    public static MaxTrackSizingFunction maxContent() {
        return from(TrackSizingFunction.maxContent());
    }

    public static MaxTrackSizingFunction fitContent(LengthPercentage limit) {
        return from(TrackSizingFunction.fitContent(limit));
    }

    public static MaxTrackSizingFunction fr(float value) {
        return from(TrackSizingFunction.fr(value));
    }

    /** Parses a complete CSS maximum track sizing function. */
    public static MaxTrackSizingFunction parse(String value) {
        return CssParser.parseMaxTrackSizingFunction(value);
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

    public boolean isFitContent() {
        return value.isFitContent();
    }

    public boolean isFr() {
        return value.isFr();
    }

    public boolean isIntrinsic() {
        return value.isIntrinsic();
    }

    public boolean isMaxContentAlike() {
        return value.isAuto() || value.isMaxContent() || value.isFitContent();
    }

    public boolean usesPercentage() {
        return value.usesPercentage();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof MaxTrackSizingFunction function && value.equals(function.value);
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
        if (candidate.isFixed() || candidate.isAuto() || candidate.isMinContent() || candidate.isMaxContent()
            || candidate.isFitContent() || candidate.isFr()) {
            return candidate;
        }
        throw new IllegalArgumentException("Invalid maximum track sizing function: " + candidate);
    }
}
