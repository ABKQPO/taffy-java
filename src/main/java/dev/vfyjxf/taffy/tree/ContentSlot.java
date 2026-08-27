package dev.vfyjxf.taffy.tree;

/** A horizontal slot available to content that must avoid active floats. */
public class ContentSlot {
    public final Integer segmentId;
    public final float x;
    public final float y;
    public final float width;
    public final float height;

    public ContentSlot(Integer segmentId, float x, float y, float width, float height) {
        this.segmentId = segmentId;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
