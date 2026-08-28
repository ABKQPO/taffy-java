package dev.vfyjxf.taffy.style;

import java.util.Objects;

/** Non-compact representation of a dimension value. */
public class ExpandedDimension {
    public enum Type {
        LENGTH, PERCENT, AUTO, CALC, MIN_CONTENT, MAX_CONTENT, FIT_CONTENT, STRETCH, CONTENT
    }

    private final Type type;
    private final float value;
    private final CalcExpression calcExpression;
    private final LengthPercentage fitContentLimit;

    private ExpandedDimension(Type type, float value, CalcExpression calcExpression,
                              LengthPercentage fitContentLimit) {
        this.type = type;
        this.value = value;
        this.calcExpression = calcExpression;
        this.fitContentLimit = fitContentLimit;
    }

    public static ExpandedDimension length(float value) {
        return new ExpandedDimension(Type.LENGTH, value, null, null);
    }
    public static ExpandedDimension percent(float value) {
        return new ExpandedDimension(Type.PERCENT, value, null, null);
    }
    public static ExpandedDimension auto() {
        return new ExpandedDimension(Type.AUTO, 0f, null, null);
    }
    public static ExpandedDimension calc(CalcExpression expression) {
        return new ExpandedDimension(Type.CALC, 0f, expression, null);
    }
    public static ExpandedDimension minContent() {
        return new ExpandedDimension(Type.MIN_CONTENT, 0f, null, null);
    }
    public static ExpandedDimension maxContent() {
        return new ExpandedDimension(Type.MAX_CONTENT, 0f, null, null);
    }
    public static ExpandedDimension fitContent(LengthPercentage limit) {
        return new ExpandedDimension(Type.FIT_CONTENT, 0f, null, limit);
    }
    public static ExpandedDimension stretch() {
        return new ExpandedDimension(Type.STRETCH, 0f, null, null);
    }
    public static ExpandedDimension content() {
        return new ExpandedDimension(Type.CONTENT, 0f, null, null);
    }

    public Type getType() { return type; }
    public float getValue() { return value; }
    public CalcExpression getCalcExpression() { return calcExpression; }
    public LengthPercentage getFitContentLimit() { return fitContentLimit; }

    public TaffyDimension toDimension() {
        return switch (type) {
            case LENGTH -> TaffyDimension.length(value);
            case PERCENT -> TaffyDimension.percent(value);
            case AUTO -> TaffyDimension.AUTO;
            case CALC -> TaffyDimension.calc(calcExpression);
            case MIN_CONTENT -> TaffyDimension.MIN_CONTENT;
            case MAX_CONTENT -> TaffyDimension.MAX_CONTENT;
            case FIT_CONTENT -> fitContentLimit == null ? TaffyDimension.FIT_CONTENT
                    : TaffyDimension.fitContent(fitContentLimit);
            case STRETCH -> TaffyDimension.STRETCH;
            case CONTENT -> TaffyDimension.CONTENT;
        };
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ExpandedDimension other)) return false;
        return type == other.type && Float.compare(value, other.value) == 0
            && Objects.equals(calcExpression, other.calcExpression)
            && Objects.equals(fitContentLimit, other.fitContentLimit);
    }

    @Override
    public int hashCode() { return Objects.hash(type, value, calcExpression, fitContentLimit); }
}
