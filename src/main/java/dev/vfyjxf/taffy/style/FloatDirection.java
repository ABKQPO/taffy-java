package dev.vfyjxf.taffy.style;

/** The resolved side on which a box is floated. */
public enum FloatDirection {
    LEFT,
    RIGHT;

    /** Returns the side index used by float slot calculations. */
    public int index() {
        return this == LEFT ? 0 : 1;
    }
}
