package dev.vfyjxf.taffy.style;

import java.util.Objects;

/**
 * A tagged public sizing value compatible with Rust Taffy's {@code CompactLength} value model.
 * Java stores the tag and payload explicitly instead of using Rust's tagged-pointer representation.
 */
public class CompactLength {
    public static final int CALC_TAG = 0b000;
    public static final int LENGTH_TAG = 0b0000_0001;
    public static final int PERCENT_TAG = 0b0000_0010;
    public static final int AUTO_TAG = 0b0000_0011;
    public static final int FR_TAG = 0b0000_0100;
    public static final int MIN_CONTENT_TAG = 0b0000_0111;
    public static final int MAX_CONTENT_TAG = 0b0000_1111;
    public static final int FIT_CONTENT_PX_TAG = 0b0001_0111;
    public static final int FIT_CONTENT_PERCENT_TAG = 0b0001_1111;
    public static final int FIT_CONTENT_KEYWORD_TAG = 0b0010_0111;
    public static final int STRETCH_TAG = 0b0010_1111;
    public static final int CONTENT_TAG = 0b0011_0111;
    public static final CompactLength ZERO = new CompactLength(LENGTH_TAG, 0f, null);

    private final int tag;
    private final float value;
    private final CalcExpression calcExpression;

    private CompactLength(int tag, float value, CalcExpression calcExpression) {
        this.tag = tag;
        this.value = value;
        this.calcExpression = calcExpression;
    }

    public static CompactLength length(float value) { return new CompactLength(LENGTH_TAG, value, null); }
    public static CompactLength percent(float value) { return new CompactLength(PERCENT_TAG, value, null); }
    public static CompactLength calc(CalcExpression value) { return new CompactLength(CALC_TAG, 0f, Objects.requireNonNull(value, "value")); }
    public static CompactLength auto() { return new CompactLength(AUTO_TAG, 0f, null); }
    public static CompactLength fr(float value) { return new CompactLength(FR_TAG, value, null); }
    public static CompactLength minContent() { return new CompactLength(MIN_CONTENT_TAG, 0f, null); }
    public static CompactLength maxContent() { return new CompactLength(MAX_CONTENT_TAG, 0f, null); }
    public static CompactLength fitContentPx(float value) { return new CompactLength(FIT_CONTENT_PX_TAG, value, null); }
    public static CompactLength fitContentPercent(float value) { return new CompactLength(FIT_CONTENT_PERCENT_TAG, value, null); }
    public static CompactLength fitContentKeyword() { return new CompactLength(FIT_CONTENT_KEYWORD_TAG, 0f, null); }
    public static CompactLength stretch() { return new CompactLength(STRETCH_TAG, 0f, null); }
    public static CompactLength content() { return new CompactLength(CONTENT_TAG, 0f, null); }

    /** Converts a length-percentage value to its compact representation. */
    public static CompactLength from(LengthPercentage value) {
        Objects.requireNonNull(value, "value");
        return switch (value.getType()) {
            case LENGTH -> length(value.getValue());
            case PERCENT -> percent(value.getValue());
            case CALC -> calc(value.getCalcExpression());
        };
    }

    /** Converts a length-percentage-auto value to its compact representation. */
    public static CompactLength from(LengthPercentageAuto value) {
        Objects.requireNonNull(value, "value");
        return switch (value.getType()) {
            case LENGTH -> length(value.getValue());
            case PERCENT -> percent(value.getValue());
            case AUTO -> auto();
            case CALC -> calc(value.getCalcExpression());
            case MIN_CONTENT -> minContent();
            case MAX_CONTENT -> maxContent();
            case FIT_CONTENT -> value.getFitContentLimit() == null ? fitContentKeyword()
                : fromFitContentLimit(value.getFitContentLimit());
            case STRETCH -> stretch();
        };
    }

    /** Converts a dimension value to its compact representation. */
    public static CompactLength from(TaffyDimension value) {
        Objects.requireNonNull(value, "value");
        return switch (value.getType()) {
            case LENGTH -> length(value.getValue());
            case PERCENT -> percent(value.getValue());
            case AUTO -> auto();
            case CALC -> calc(value.getCalcExpression());
            case MIN_CONTENT -> minContent();
            case MAX_CONTENT -> maxContent();
            case FIT_CONTENT -> value.getFitContentLimit() == null ? fitContentKeyword()
                : fromFitContentLimit(value.getFitContentLimit());
            case STRETCH -> stretch();
            case CONTENT -> content();
        };
    }

    public int tag() { return tag; }
    public float value() { return value; }
    public CalcExpression calcValue() { return calcExpression; }
    public boolean isCalc() { return tag == CALC_TAG; }
    public boolean isZero() { return tag == LENGTH_TAG && value == 0f; }
    public boolean isLengthOrPercentage() { return tag == LENGTH_TAG || tag == PERCENT_TAG; }
    public boolean isAuto() { return tag == AUTO_TAG; }
    public boolean isContent() { return tag == CONTENT_TAG; }
    public boolean isMinContent() { return tag == MIN_CONTENT_TAG; }
    public boolean isMaxContent() { return tag == MAX_CONTENT_TAG; }
    public boolean isFitContent() { return tag == FIT_CONTENT_PX_TAG || tag == FIT_CONTENT_PERCENT_TAG; }
    public boolean isSizingKeyword() {
        return tag == MIN_CONTENT_TAG || tag == MAX_CONTENT_TAG || tag == FIT_CONTENT_KEYWORD_TAG
            || tag == FIT_CONTENT_PX_TAG || tag == FIT_CONTENT_PERCENT_TAG || tag == STRETCH_TAG;
    }
    public boolean isMaxOrFitContent() {
        return tag == MAX_CONTENT_TAG || tag == FIT_CONTENT_PX_TAG || tag == FIT_CONTENT_PERCENT_TAG;
    }
    public boolean isMaxContentAlike() {
        return tag == AUTO_TAG || tag == MAX_CONTENT_TAG || tag == FIT_CONTENT_PX_TAG
            || tag == FIT_CONTENT_PERCENT_TAG;
    }
    public boolean isMinOrMaxContent() { return tag == MIN_CONTENT_TAG || tag == MAX_CONTENT_TAG; }
    public boolean isIntrinsic() {
        return tag == AUTO_TAG || tag == MIN_CONTENT_TAG || tag == MAX_CONTENT_TAG
            || tag == FIT_CONTENT_PX_TAG || tag == FIT_CONTENT_PERCENT_TAG;
    }
    public boolean isFr() { return tag == FR_TAG; }
    public boolean usesPercentage() {
        return tag == PERCENT_TAG || tag == FIT_CONTENT_PERCENT_TAG || isCalc();
    }

    /** Returns the stable data form for non-calc values. */
    public CompactLengthData toData() {
        if (isCalc()) {
            throw new IllegalStateException("A calc expression requires a CalcExpressionCodec");
        }
        return new CompactLengthData(tag, value, null);
    }

    /** Returns the stable data form using the caller's calc expression codec when needed. */
    public CompactLengthData toData(CalcExpressionCodec codec) {
        if (!isCalc()) return toData();
        return new CompactLengthData(tag, value,
            Objects.requireNonNull(codec, "codec").encode(calcExpression));
    }

    /** Restores a non-calc compact value from its stable data form. */
    public static CompactLength fromData(CompactLengthData data) {
        Objects.requireNonNull(data, "data");
        if (data.tag() == CALC_TAG) {
            throw new IllegalArgumentException("A calc CompactLengthData requires a CalcExpressionCodec");
        }
        return fromData(data, null);
    }

    /** Restores a compact value from its stable data form. */
    public static CompactLength fromData(CompactLengthData data, CalcExpressionCodec codec) {
        Objects.requireNonNull(data, "data");
        return switch (data.tag()) {
            case LENGTH_TAG -> length(data.value());
            case PERCENT_TAG -> percent(data.value());
            case AUTO_TAG -> auto();
            case FR_TAG -> fr(data.value());
            case MIN_CONTENT_TAG -> minContent();
            case MAX_CONTENT_TAG -> maxContent();
            case FIT_CONTENT_PX_TAG -> fitContentPx(data.value());
            case FIT_CONTENT_PERCENT_TAG -> fitContentPercent(data.value());
            case FIT_CONTENT_KEYWORD_TAG -> fitContentKeyword();
            case STRETCH_TAG -> stretch();
            case CONTENT_TAG -> content();
            case CALC_TAG -> calc(Objects.requireNonNull(codec, "codec").decode(
                Objects.requireNonNull(data.calcKey(), "calc key")));
            default -> throw new IllegalArgumentException("Unknown CompactLength tag: " + data.tag());
        };
    }

    /** Resolves a percentage or calc value, or returns {@code null} for non-percentage values. */
    public Float resolvedPercentageSize(float parentSize, CalcValueResolver resolver) {
        if (tag == PERCENT_TAG) return value * parentSize;
        if (isCalc()) {
            if (resolver == null) {
                throw new IllegalArgumentException("A calc resolver is required for a calc CompactLength");
            }
            return resolver.resolve(calcExpression, parentSize);
        }
        return null;
    }

    public LengthPercentage toLengthPercentage() {
        return switch (tag) {
            case LENGTH_TAG -> LengthPercentage.length(value);
            case PERCENT_TAG -> LengthPercentage.percent(value);
            case CALC_TAG -> LengthPercentage.calc(calcExpression);
            default -> throw new IllegalStateException("CompactLength is not a LengthPercentage: " + tag);
        };
    }

    public LengthPercentageAuto toLengthPercentageAuto() {
        return switch (tag) {
            case LENGTH_TAG -> LengthPercentageAuto.length(value);
            case PERCENT_TAG -> LengthPercentageAuto.percent(value);
            case CALC_TAG -> LengthPercentageAuto.calc(calcExpression);
            case AUTO_TAG, CONTENT_TAG -> LengthPercentageAuto.auto();
            case MIN_CONTENT_TAG -> LengthPercentageAuto.minContent();
            case MAX_CONTENT_TAG -> LengthPercentageAuto.maxContent();
            case FIT_CONTENT_PX_TAG -> LengthPercentageAuto.fitContent(LengthPercentage.length(value));
            case FIT_CONTENT_PERCENT_TAG -> LengthPercentageAuto.fitContent(LengthPercentage.percent(value));
            case FIT_CONTENT_KEYWORD_TAG -> LengthPercentageAuto.fitContent();
            case STRETCH_TAG -> LengthPercentageAuto.stretch();
            default -> throw new IllegalStateException("CompactLength is not a LengthPercentageAuto: " + tag);
        };
    }

    public TaffyDimension toDimension() {
        return switch (tag) {
            case LENGTH_TAG -> TaffyDimension.length(value);
            case PERCENT_TAG -> TaffyDimension.percent(value);
            case CALC_TAG -> TaffyDimension.calc(calcExpression);
            case AUTO_TAG -> TaffyDimension.auto();
            case MIN_CONTENT_TAG -> TaffyDimension.minContent();
            case MAX_CONTENT_TAG -> TaffyDimension.maxContent();
            case FIT_CONTENT_PX_TAG -> TaffyDimension.fitContent(LengthPercentage.length(value));
            case FIT_CONTENT_PERCENT_TAG -> TaffyDimension.fitContent(LengthPercentage.percent(value));
            case FIT_CONTENT_KEYWORD_TAG -> TaffyDimension.fitContent();
            case STRETCH_TAG -> TaffyDimension.stretch();
            case CONTENT_TAG -> TaffyDimension.content();
            default -> throw new IllegalStateException("CompactLength is not a Dimension: " + tag);
        };
    }

    private static CompactLength fromFitContentLimit(LengthPercentage value) {
        return switch (value.getType()) {
            case LENGTH -> fitContentPx(value.getValue());
            case PERCENT -> fitContentPercent(value.getValue());
            case CALC -> throw new IllegalArgumentException("fit-content() does not accept calc CompactLength values");
        };
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof CompactLength compactLength)) return false;
        return tag == compactLength.tag
            && Float.compare(value, compactLength.value) == 0
            && Objects.equals(calcExpression, compactLength.calcExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, value, calcExpression);
    }
}
