package dev.vfyjxf.taffy.style;

import java.util.Objects;
import java.util.function.Function;

/** Encodes application-owned calc expressions for value-level persistence. */
public interface CalcExpressionCodec {
    String encode(CalcExpression expression);

    CalcExpression decode(String key);

    static CalcExpressionCodec of(
        Function<CalcExpression, String> encoder,
        Function<String, CalcExpression> decoder) {
        Objects.requireNonNull(encoder, "encoder");
        Objects.requireNonNull(decoder, "decoder");
        return new CalcExpressionCodec() {
            @Override
            public String encode(CalcExpression expression) {
                return Objects.requireNonNull(encoder.apply(expression), "encoded calc expression");
            }

            @Override
            public CalcExpression decode(String key) {
                return Objects.requireNonNull(decoder.apply(key), "decoded calc expression");
            }
        };
    }
}
