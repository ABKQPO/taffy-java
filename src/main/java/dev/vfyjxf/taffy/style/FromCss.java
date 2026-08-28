package dev.vfyjxf.taffy.style;

import java.util.Objects;

/** Parses one complete CSS value into a public Taffy value type. */
@FunctionalInterface
public interface FromCss<T> {
    T fromCss(String input);

    /** Parse a complete non-null CSS value through an application-provided parser. */
    static <T> T parseEntirely(String input, FromCss<T> parser) {
        return Objects.requireNonNull(parser, "parser").fromCss(Objects.requireNonNull(input, "input"));
    }
}
