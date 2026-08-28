package dev.vfyjxf.taffy.tree;

/**
 * First and last baselines produced by a layout algorithm.
 * <p>
 * A NaN value represents an unavailable baseline, matching the float-based
 * optional value convention used by the rest of the layout engine.
 *
 * @param first the first baseline, or NaN when unavailable
 * @param last the last baseline, or NaN when unavailable
 */
public record Baselines(float first, float last) {
    /** A baseline pair with neither baseline available. */
    public static final Baselines NONE = new Baselines(Float.NaN, Float.NaN);

    /** Creates a baseline pair with only a first baseline. */
    public static Baselines first(float value) {
        return new Baselines(value, Float.NaN);
    }

    /** Creates a baseline pair with only a last baseline. */
    public static Baselines last(float value) {
        return new Baselines(Float.NaN, value);
    }

    /** Returns true when a first baseline is available. */
    public boolean hasFirst() {
        return !Float.isNaN(first);
    }

    /** Returns true when a last baseline is available. */
    public boolean hasLast() {
        return !Float.isNaN(last);
    }

    /** Returns true when neither baseline is available. */
    public boolean isNone() {
        return !hasFirst() && !hasLast();
    }

    /** Converts the legacy two-dimensional baseline representation. */
    public static Baselines fromLegacy(float first) {
        return first(first);
    }
}
