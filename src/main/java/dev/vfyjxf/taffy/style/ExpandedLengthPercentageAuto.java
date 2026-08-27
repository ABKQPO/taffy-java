package dev.vfyjxf.taffy.style;

import java.util.Objects;

/** Non-compact representation of a length-percentage-auto value. */
public class ExpandedLengthPercentageAuto {
    public enum Type { LENGTH, PERCENT, AUTO, CALC, MIN_CONTENT, MAX_CONTENT, FIT_CONTENT, STRETCH }

    private final Type type;
    private final float value;
    private final CalcExpression calcExpression;
    private final LengthPercentage fitContentLimit;

    private ExpandedLengthPercentageAuto(Type type, float value, CalcExpression calcExpression,
                                         LengthPercentage fitContentLimit) {
        this.type = type;
        this.value = value;
        this.calcExpression = calcExpression;
        this.fitContentLimit = fitContentLimit;
    }

    public static ExpandedLengthPercentageAuto length(float value) {
        return new ExpandedLengthPercentageAuto(Type.LENGTH, value, null, null);
    }
    public static ExpandedLengthPercentageAuto percent(float value) {
        return new ExpandedLengthPercentageAuto(Type.PERCENT, value, null, null);
    }
    public static ExpandedLengthPercentageAuto auto() {
        return new ExpandedLengthPercentageAuto(Type.AUTO, 0f, null, null);
    }
    public static ExpandedLengthPercentageAuto calc(CalcExpression expression) {
        return new ExpandedLengthPercentageAuto(Type.CALC, 0f, expression, null);
    }
    public static ExpandedLengthPercentageAuto minContent() {
        return new ExpandedLengthPercentageAuto(Type.MIN_CONTENT, 0f, null, null);
    }
    public static ExpandedLengthPercentageAuto maxContent() {
        return new ExpandedLengthPercentageAuto(Type.MAX_CONTENT, 0f, null, null);
    }
    public static ExpandedLengthPercentageAuto fitContent(LengthPercentage limit) {
        return new ExpandedLengthPercentageAuto(Type.FIT_CONTENT, 0f, null, limit);
    }
    public static ExpandedLengthPercentageAuto stretch() {
        return new ExpandedLengthPercentageAuto(Type.STRETCH, 0f, null, null);
    }

    public Type getType() { return type; }
    public float getValue() { return value; }
    public CalcExpression getCalcExpression() { return calcExpression; }
    public LengthPercentage getFitContentLimit() { return fitContentLimit; }

    public LengthPercentageAuto toLengthPercentageAuto() {
        switch (type) {
            case LENGTH: return LengthPercentageAuto.length(value);
            case PERCENT: return LengthPercentageAuto.percent(value);
            case AUTO: return LengthPercentageAuto.AUTO;
            case CALC: return LengthPercentageAuto.calc(calcExpression);
            case MIN_CONTENT: return LengthPercentageAuto.MIN_CONTENT;
            case MAX_CONTENT: return LengthPercentageAuto.MAX_CONTENT;
            case FIT_CONTENT: return fitContentLimit == null ? LengthPercentageAuto.FIT_CONTENT
                : LengthPercentageAuto.fitContent(fitContentLimit);
            case STRETCH: return LengthPercentageAuto.STRETCH;
            default: throw new IllegalStateException("Unexpected expanded length type: " + type);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ExpandedLengthPercentageAuto)) return false;
        ExpandedLengthPercentageAuto other = (ExpandedLengthPercentageAuto) object;
        return type == other.type && Float.compare(value, other.value) == 0
            && Objects.equals(calcExpression, other.calcExpression)
            && Objects.equals(fitContentLimit, other.fitContentLimit);
    }

    @Override
    public int hashCode() { return Objects.hash(type, value, calcExpression, fitContentLimit); }
}
