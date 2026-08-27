package dev.vfyjxf.taffy.style;

import java.util.Objects;

/** Non-compact representation of a length-percentage value. */
public class ExpandedLengthPercentage {
    public enum Type { LENGTH, PERCENT, CALC }

    private final Type type;
    private final float value;
    private final CalcExpression calcExpression;

    private ExpandedLengthPercentage(Type type, float value, CalcExpression calcExpression) {
        this.type = type;
        this.value = value;
        this.calcExpression = calcExpression;
    }

    public static ExpandedLengthPercentage length(float value) {
        return new ExpandedLengthPercentage(Type.LENGTH, value, null);
    }

    public static ExpandedLengthPercentage percent(float value) {
        return new ExpandedLengthPercentage(Type.PERCENT, value, null);
    }

    public static ExpandedLengthPercentage calc(CalcExpression expression) {
        return new ExpandedLengthPercentage(Type.CALC, 0f, expression);
    }

    public Type getType() { return type; }
    public float getValue() { return value; }
    public CalcExpression getCalcExpression() { return calcExpression; }

    public LengthPercentage toLengthPercentage() {
        switch (type) {
            case LENGTH: return LengthPercentage.length(value);
            case PERCENT: return LengthPercentage.percent(value);
            case CALC: return LengthPercentage.calc(calcExpression);
            default: throw new IllegalStateException("Unexpected expanded length type: " + type);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ExpandedLengthPercentage)) return false;
        ExpandedLengthPercentage other = (ExpandedLengthPercentage) object;
        return type == other.type && Float.compare(value, other.value) == 0
            && Objects.equals(calcExpression, other.calcExpression);
    }

    @Override
    public int hashCode() { return Objects.hash(type, value, calcExpression); }
}
