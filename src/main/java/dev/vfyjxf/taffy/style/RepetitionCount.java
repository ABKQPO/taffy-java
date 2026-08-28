package dev.vfyjxf.taffy.style;

import java.util.Objects;

/** The first argument of a CSS Grid {@code repeat()} definition. */
public record RepetitionCount(Type type, int count) {
    public enum Type {
        AUTO_FILL,
        AUTO_FIT,
        COUNT
    }

    public RepetitionCount {
        Objects.requireNonNull(type, "type");
        if (count < 0 || count > 65535) {
            throw new IllegalArgumentException("Repeat count must be between 0 and 65535");
        }
        if (type != Type.COUNT && count != 0) {
            throw new IllegalArgumentException("Automatic repetitions do not have a numeric count");
        }
    }

    public static RepetitionCount autoFill() {
        return new RepetitionCount(Type.AUTO_FILL, 0);
    }

    public static RepetitionCount autoFit() {
        return new RepetitionCount(Type.AUTO_FIT, 0);
    }

    public static RepetitionCount count(int count) {
        return new RepetitionCount(Type.COUNT, count);
    }

    /** Parses a complete CSS {@code repeat()} count value. */
    public static RepetitionCount parse(String value) {
        return CssParser.parseRepetitionCount(value);
    }

    public boolean isAutoFill() {
        return type == Type.AUTO_FILL;
    }

    public boolean isAutoFit() {
        return type == Type.AUTO_FIT;
    }

    public boolean isCount() {
        return type == Type.COUNT;
    }
}
