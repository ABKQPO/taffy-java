package dev.vfyjxf.taffy.style;

import java.util.Objects;

/** Non-compact representation of a grid track sizing function. */
public class ExpandedTrackSizingFunction {
    public enum Type { FIXED, MIN_CONTENT, MAX_CONTENT, FIT_CONTENT, AUTO, FLEX, MINMAX }

    private final Type type;
    private final LengthPercentage lengthValue;
    private final float flexValue;
    private final ExpandedTrackSizingFunction minFunction;
    private final ExpandedTrackSizingFunction maxFunction;

    private ExpandedTrackSizingFunction(Type type, LengthPercentage lengthValue, float flexValue,
                                        ExpandedTrackSizingFunction minFunction,
                                        ExpandedTrackSizingFunction maxFunction) {
        this.type = type;
        this.lengthValue = lengthValue;
        this.flexValue = flexValue;
        this.minFunction = minFunction;
        this.maxFunction = maxFunction;
    }

    public static ExpandedTrackSizingFunction fixed(LengthPercentage value) {
        return new ExpandedTrackSizingFunction(Type.FIXED, value, 0f, null, null);
    }
    public static ExpandedTrackSizingFunction minContent() {
        return new ExpandedTrackSizingFunction(Type.MIN_CONTENT, null, 0f, null, null);
    }
    public static ExpandedTrackSizingFunction maxContent() {
        return new ExpandedTrackSizingFunction(Type.MAX_CONTENT, null, 0f, null, null);
    }
    public static ExpandedTrackSizingFunction fitContent(LengthPercentage limit) {
        return new ExpandedTrackSizingFunction(Type.FIT_CONTENT, limit, 0f, null, null);
    }
    public static ExpandedTrackSizingFunction auto() {
        return new ExpandedTrackSizingFunction(Type.AUTO, null, 0f, null, null);
    }
    public static ExpandedTrackSizingFunction flex(float value) {
        return new ExpandedTrackSizingFunction(Type.FLEX, null, value, null, null);
    }
    public static ExpandedTrackSizingFunction minmax(ExpandedTrackSizingFunction min,
                                                     ExpandedTrackSizingFunction max) {
        return new ExpandedTrackSizingFunction(Type.MINMAX, null, 0f, min, max);
    }

    public Type getType() { return type; }
    public LengthPercentage getLengthValue() { return lengthValue; }
    public float getFlexValue() { return flexValue; }
    public ExpandedTrackSizingFunction getMinFunction() { return minFunction; }
    public ExpandedTrackSizingFunction getMaxFunction() { return maxFunction; }

    public TrackSizingFunction toTrackSizingFunction() {
        switch (type) {
            case FIXED: return TrackSizingFunction.fixed(lengthValue);
            case MIN_CONTENT: return TrackSizingFunction.MIN_CONTENT;
            case MAX_CONTENT: return TrackSizingFunction.MAX_CONTENT;
            case FIT_CONTENT: return TrackSizingFunction.fitContent(lengthValue);
            case AUTO: return TrackSizingFunction.AUTO;
            case FLEX: return TrackSizingFunction.flex(flexValue);
            case MINMAX: return TrackSizingFunction.minmax(
                minFunction == null ? TrackSizingFunction.AUTO : minFunction.toTrackSizingFunction(),
                maxFunction == null ? TrackSizingFunction.AUTO : maxFunction.toTrackSizingFunction());
            default: throw new IllegalStateException("Unexpected expanded track type: " + type);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ExpandedTrackSizingFunction)) return false;
        ExpandedTrackSizingFunction other = (ExpandedTrackSizingFunction) object;
        return type == other.type && Float.compare(flexValue, other.flexValue) == 0
            && Objects.equals(lengthValue, other.lengthValue)
            && Objects.equals(minFunction, other.minFunction)
            && Objects.equals(maxFunction, other.maxFunction);
    }

    @Override
    public int hashCode() { return Objects.hash(type, lengthValue, flexValue, minFunction, maxFunction); }
}
