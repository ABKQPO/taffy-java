package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.style.FloatDirection;

/** Geometry recorded for a box placed in a float context. */
public class PlacedFloatedBox {
    public final FloatDirection direction;
    public final float width;
    public final float height;
    public final float xInset;
    public final float y;

    public PlacedFloatedBox(FloatDirection direction, float width, float height, float xInset, float y) {
        this.direction = direction;
        this.width = width;
        this.height = height;
        this.xInset = xInset;
        this.y = y;
    }

    /** Returns the bottom edge of this float. */
    public float bottom() {
        return y + height;
    }

    /** Returns the physical left edge for the supplied containing width. */
    public float left(float containingWidth) {
        return direction == FloatDirection.LEFT ? xInset : containingWidth - xInset - width;
    }
}
