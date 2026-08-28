package dev.vfyjxf.taffy.style;

import java.util.Locale;
import java.util.Objects;

/**
 * The layout-affecting containment modes of the CSS contain property.
 */
public class Contain {

    public static final Contain NONE = new Contain(0);
    public static final Contain LAYOUT = new Contain(1);
    public static final Contain PAINT = new Contain(2);
    public static final Contain CONTENT = new Contain(3);
    public static final Contain DEFAULT = NONE;

    private final int flags;

    public Contain(int flags) {
        if (flags < 0 || flags > 3) {
            throw new IllegalArgumentException("Contain flags must be a combination of layout and paint");
        }
        this.flags = flags;
    }

    public boolean contains(Contain other) {
        return (flags & other.flags) == other.flags;
    }

    public boolean intersects(Contain other) {
        return (flags & other.flags) != 0;
    }

    public Contain union(Contain other) {
        return new Contain(flags | other.flags);
    }

    /** Parse the layout-affecting CSS contain keywords. */
    public static Contain parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ParseError("Contain value must not be empty");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("none")) return NONE;
        if (normalized.equals("content")) return CONTENT;

        Contain result = NONE;
        boolean sawKeyword = false;
        for (String token : normalized.split("\\s+")) {
            switch (token) {
                case "layout":
                    if (result.contains(LAYOUT)) throw new ParseError("Duplicate contain keyword: layout");
                    result = result.union(LAYOUT);
                    sawKeyword = true;
                    break;
                case "paint":
                    if (result.contains(PAINT)) throw new ParseError("Duplicate contain keyword: paint");
                    result = result.union(PAINT);
                    sawKeyword = true;
                    break;
                case "style":
                    sawKeyword = true;
                    break;
                default:
                    throw new ParseError("Unknown contain keyword: " + token);
            }
        }
        if (!sawKeyword) throw new ParseError("Contain value must not be empty");
        return result;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Contain && flags == ((Contain) object).flags;
    }

    @Override
    public int hashCode() {
        return Objects.hash(flags);
    }

    @Override
    public String toString() {
        if (equals(NONE)) return "none";
        if (equals(CONTENT)) return "content";
        StringBuilder value = new StringBuilder();
        if (contains(LAYOUT)) value.append("layout");
        if (contains(PAINT)) {
            if (value.length() > 0) value.append(' ');
            value.append("paint");
        }
        return value.toString();
    }

    public boolean establishesIndependentFormattingContext() {
        return intersects(LAYOUT) || intersects(PAINT);
    }

    public boolean suppressesBaseline() {
        return contains(LAYOUT);
    }

    public boolean containsScrollableOverflow() {
        return intersects(LAYOUT) || intersects(PAINT);
    }
}
