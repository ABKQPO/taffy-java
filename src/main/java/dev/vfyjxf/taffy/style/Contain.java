package dev.vfyjxf.taffy.style;

/**
 * The layout-affecting containment modes of the CSS contain property.
 */
public class Contain {

    public static final Contain NONE = new Contain(0);
    public static final Contain LAYOUT = new Contain(1);
    public static final Contain PAINT = new Contain(2);
    public static final Contain CONTENT = new Contain(3);

    private final int flags;

    public Contain(int flags) {
        this.flags = flags;
    }

    public boolean contains(Contain other) {
        return (flags & other.flags) == other.flags;
    }

    public boolean establishesIndependentFormattingContext() {
        return (flags & CONTENT.flags) != 0;
    }

    public boolean suppressesBaseline() {
        return contains(LAYOUT);
    }

    public boolean containsScrollableOverflow() {
        return establishesIndependentFormattingContext();
    }
}
