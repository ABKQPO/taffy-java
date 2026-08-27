package dev.vfyjxf.taffy.tree;

/** A slot for a formatting context whose border box must avoid active floats. */
public class BfcSlot {
    public final Integer segmentId;
    public final float x;
    public final float y;
    public final float borderWidth;
    public final float stretchWidth;

    public BfcSlot(Integer segmentId, float x, float y, float borderWidth, float stretchWidth) {
        this.segmentId = segmentId;
        this.x = x;
        this.y = y;
        this.borderWidth = borderWidth;
        this.stretchWidth = stretchWidth;
    }
}
