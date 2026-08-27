package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.Clear;
import dev.vfyjxf.taffy.style.FloatDirection;

/** Computes intrinsic width contributions from a sequence of floated boxes. */
public class FloatIntrinsicWidthCalculator {
    private final AvailableSpace availableWidth;
    private final float[] sideSums = new float[] {0f, 0f};
    private float contribution;
    private float widest;

    /** Creates a calculator for the supplied available-width constraint. */
    public FloatIntrinsicWidthCalculator(AvailableSpace availableWidth) {
        this.availableWidth = availableWidth;
    }

    /** Adds one float to the intrinsic-width calculation. */
    public void addFloat(float width, FloatDirection direction, Clear clear) {
        float safeWidth = Math.max(0f, width);
        if (availableWidth.isMinContent()) {
            contribution = Math.max(contribution, safeWidth);
        } else {
            if (clear == Clear.LEFT || clear == Clear.BOTH) sideSums[0] = 0f;
            if (clear == Clear.RIGHT || clear == Clear.BOTH) sideSums[1] = 0f;
            sideSums[direction.index()] += safeWidth;
            contribution = Math.max(contribution, sideSums[0] + sideSums[1]);
        }
        widest = Math.max(widest, safeWidth);
    }

    /** Returns the computed intrinsic width contribution. */
    public float result() {
        if (availableWidth.isDefinite()) {
            return Math.max(widest, Math.min(contribution, availableWidth.getValue()));
        }
        return contribution;
    }
}
