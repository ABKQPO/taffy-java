package dev.vfyjxf.taffy.style;

import java.util.Objects;
import java.util.function.Function;

/** Converts application-defined grid identifiers to and from Taffy's runtime names. */
public interface CustomIdentCodec<S> {
    String encode(S identifier);

    S decode(String identifier);

    static <S> CustomIdentCodec<S> of(Function<S, String> encoder, Function<String, S> decoder) {
        Objects.requireNonNull(encoder, "encoder");
        Objects.requireNonNull(decoder, "decoder");
        return new CustomIdentCodec<>() {
            @Override
            public String encode(S identifier) {
                return Objects.requireNonNull(encoder.apply(identifier), "encoded identifier");
            }

            @Override
            public S decode(String identifier) {
                return Objects.requireNonNull(decoder.apply(identifier), "decoded identifier");
            }
        };
    }

    static CustomIdentCodec<String> strings() {
        return of(Function.identity(), Function.identity());
    }
}
