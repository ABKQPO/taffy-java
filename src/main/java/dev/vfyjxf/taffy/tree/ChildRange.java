package dev.vfyjxf.taffy.tree;

/** A child index range using Rust-style half-open bounds. */
public record ChildRange(int start, int end) {
    public ChildRange {
        if (start < 0 || end < start) throw new IllegalArgumentException("Invalid child range");
    }

    public static ChildRange of(int start, int end) {
        return new ChildRange(start, end);
    }

    /** Creates a range from an inclusive start and inclusive end. */
    public static ChildRange inclusive(int start, int end) {
        if (end == Integer.MAX_VALUE) throw new IllegalArgumentException("Inclusive end is too large");
        return new ChildRange(start, end + 1);
    }

    public static ChildRange from(int start) {
        return new ChildRange(start, Integer.MAX_VALUE);
    }

    public static ChildRange to(int end) {
        return new ChildRange(0, end);
    }

    /** Creates a range ending at an inclusive index. */
    public static ChildRange toInclusive(int end) {
        return inclusive(0, end);
    }

    public static ChildRange all() {
        return new ChildRange(0, Integer.MAX_VALUE);
    }

    /** Parses common half-open range spellings such as {@code 1..4}, {@code 1..}, and {@code ..4}. */
    public static ChildRange parse(String value) {
        if (value == null) throw new IllegalArgumentException("range must not be null");
        String text = value.trim();
        int separator = text.indexOf("..");
        if (separator < 0 || text.indexOf("..", separator + 2) >= 0) {
            throw new IllegalArgumentException("Expected a range in the form start..end");
        }
        String startText = text.substring(0, separator).trim();
        String endText = text.substring(separator + 2).trim();
        int start = startText.isEmpty() ? 0 : Integer.parseInt(startText);
        int end = endText.isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(endText);
        return new ChildRange(start, end);
    }
}
