package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.FloatPoint;
import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.style.Clear;
import dev.vfyjxf.taffy.style.FloatDirection;
import dev.vfyjxf.taffy.style.TaffyDirection;

/**
 * Per-block state within a shared block formatting context.
 *
 * <p>Nested block boxes that do not establish an independent formatting context share the same
 * float placement state while converting slot coordinates to their local coordinate system.</p>
 */
public class BlockContext {
    private final FloatContext floatContext;
    private final float yOffset;
    private final float leftInset;
    private final float rightInset;
    private final boolean root;
    private boolean adjoiningLeftFloat;
    private boolean adjoiningRightFloat;
    private Boolean topAdjoiningLeftFloat;
    private Boolean topAdjoiningRightFloat;

    private BlockContext(FloatContext floatContext, float yOffset, float leftInset, float rightInset, boolean root) {
        this.floatContext = floatContext;
        this.yOffset = yOffset;
        this.leftInset = leftInset;
        this.rightInset = rightInset;
        this.root = root;
    }

    /** Create the root context for a block formatting context. */
    public static BlockContext root(float width) {
        return new BlockContext(new FloatContext(width), 0f, 0f, 0f, true);
    }

    /** Create a child context in the same block formatting context. */
    public BlockContext subContext(float additionalYOffset, float additionalLeftInset, float additionalRightInset) {
        BlockContext context = new BlockContext(
            floatContext,
            yOffset + additionalYOffset,
            leftInset + additionalLeftInset,
            rightInset + additionalRightInset,
            false
        );
        context.adjoiningLeftFloat = adjoiningLeftFloat;
        context.adjoiningRightFloat = adjoiningRightFloat;
        return context;
    }

    /** Apply a block's content-box insets before laying out its in-flow children. */
    public BlockContext withContentBoxInsets(float additionalLeftInset, float additionalRightInset) {
        BlockContext context = new BlockContext(
            floatContext,
            yOffset,
            leftInset + additionalLeftInset,
            rightInset + additionalRightInset,
            root
        );
        context.adjoiningLeftFloat = adjoiningLeftFloat;
        context.adjoiningRightFloat = adjoiningRightFloat;
        context.topAdjoiningLeftFloat = topAdjoiningLeftFloat;
        context.topAdjoiningRightFloat = topAdjoiningRightFloat;
        return context;
    }

    /** Returns whether this context belongs to the root box of its formatting context. */
    public boolean isRoot() {
        return root;
    }

    /** Update the width used by the root formatting context. */
    public void setWidth(float width) {
        if (root) floatContext.setWidth(width);
    }

    /** Returns whether this formatting context contains any floats. */
    public boolean hasFloats() {
        return floatContext.hasFloats();
    }

    /** Returns whether a float remains active at the local vertical position. */
    public boolean hasActiveFloats(float minY) {
        return floatContext.hasActiveFloats(minY + yOffset);
    }

    /** Place a float using local box coordinates. */
    public FloatPoint placeFloatedBox(
        FloatSize size,
        float minY,
        FloatDirection direction,
        Clear clear,
        boolean adjoinsUnresolvedStrut) {
        if (adjoinsUnresolvedStrut) {
            if (direction == FloatDirection.LEFT) adjoiningLeftFloat = true;
            else adjoiningRightFloat = true;
        }
        FloatPoint position = floatContext.placeFloatedBox(
            size,
            minY + yOffset,
            new float[] {leftInset, rightInset},
            direction,
            clear
        );
        return new FloatPoint(position.x - leftInset, position.y - yOffset);
    }

    /** Returns the lowest bottom edge of floats cleared by the supplied value. */
    public float clearedThreshold(Clear clear) {
        return floatContext.clearedThreshold(clear) - yOffset;
    }

    /** Returns whether a float adjoining the current margin strut is relevant to the clear value. */
    public boolean hasAdjoiningFloat(Clear clear) {
        if (clear == Clear.LEFT) return adjoiningLeftFloat;
        if (clear == Clear.RIGHT) return adjoiningRightFloat;
        return clear == Clear.BOTH && (adjoiningLeftFloat || adjoiningRightFloat);
    }

    /** Merge the adjacent-float state from a same-BFC child into this block context. */
    public void mergeTopAdjoiningFloats(BlockContext childContext) {
        adjoiningLeftFloat |= childContext.topAdjoiningLeftFloat != null
            ? childContext.topAdjoiningLeftFloat : childContext.adjoiningLeftFloat;
        adjoiningRightFloat |= childContext.topAdjoiningRightFloat != null
            ? childContext.topAdjoiningRightFloat : childContext.adjoiningRightFloat;
    }

    /** Commit the current margin strut after in-flow content establishes a final position. */
    public void commitStrut() {
        if (topAdjoiningLeftFloat == null) {
            topAdjoiningLeftFloat = adjoiningLeftFloat;
            topAdjoiningRightFloat = adjoiningRightFloat;
        }
        adjoiningLeftFloat = false;
        adjoiningRightFloat = false;
    }

    /** Find a content slot in local box coordinates. */
    public ContentSlot findContentSlot(float minY, Clear clear, Integer after) {
        ContentSlot slot = floatContext.findContentSlot(
            minY + yOffset,
            new float[] {leftInset, rightInset},
            clear,
            after
        );
        return new ContentSlot(slot.segmentId, slot.x - leftInset, slot.y - yOffset, slot.width, slot.height);
    }

    /** Find an independent-formatting-context slot in local box coordinates. */
    public BfcSlot findBfcSlot(float minY, float[] margins, TaffyDirection direction, Clear clear, Integer after) {
        BfcSlot slot = floatContext.findBfcSlot(
            minY + yOffset,
            new float[] {leftInset, rightInset},
            margins,
            direction,
            clear,
            after
        );
        return new BfcSlot(slot.segmentId, slot.x - leftInset, slot.y - yOffset, slot.borderWidth, slot.stretchWidth);
    }

    /** Return the lowest float bottom edge in this box's local coordinate system. */
    public float floatedContentHeight() {
        return floatContext.maxBottom() - yOffset;
    }
}
