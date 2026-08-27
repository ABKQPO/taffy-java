package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.FloatPoint;
import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.style.Clear;
import dev.vfyjxf.taffy.style.FloatDirection;
import dev.vfyjxf.taffy.style.TaffyDirection;
import dev.vfyjxf.taffy.style.TaffyFloat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Maintains the floats in one block formatting context and finds non-overlapping slots.
 *
 * <p>The context stores the resolved margin-box geometry of each float. Slot queries scan
 * the vertical boundaries of those boxes, which keeps the public low-level API independent
 * of the block algorithm's internal representation.</p>
 */
public class FloatContext {
    private static final float FIT_TOLERANCE = 0.001f;

    private float availableWidth;
    private float floatCeiling = Float.NEGATIVE_INFINITY;
    private final List<PlacedFloatedBox> floats = new ArrayList<>();

    /** Creates an empty context with zero available width. */
    public FloatContext() {
    }

    /** Creates an empty context with the supplied available width. */
    public FloatContext(float availableWidth) {
        setWidth(availableWidth);
    }

    /** Returns whether at least one float has been placed. */
    public boolean hasFloats() {
        return !floats.isEmpty();
    }

    /** Returns whether a float extends below the supplied vertical position. */
    public boolean hasActiveFloats(float minY) {
        for (PlacedFloatedBox placed : floats) {
            if (placed.bottom() > minY) return true;
        }
        return false;
    }

    /** Sets the width of the containing block. */
    public void setWidth(float availableWidth) {
        this.availableWidth = Math.max(0f, availableWidth);
    }

    /** Returns the containing block width. */
    public float width() {
        return availableWidth;
    }

    /** Returns the placed left floats in source order. */
    public List<PlacedFloatedBox> leftFloats() {
        return sideFloats(FloatDirection.LEFT);
    }

    /** Returns the placed right floats in source order. */
    public List<PlacedFloatedBox> rightFloats() {
        return sideFloats(FloatDirection.RIGHT);
    }

    private List<PlacedFloatedBox> sideFloats(FloatDirection direction) {
        List<PlacedFloatedBox> result = new ArrayList<>();
        for (PlacedFloatedBox placed : floats) {
            if (placed.direction == direction) result.add(placed);
        }
        return Collections.unmodifiableList(result);
    }

    /** Places a float and returns its physical top-left position. */
    public FloatPoint placeFloatedBox(
        FloatSize floatedBox,
        float minY,
        float[] containingBlockInsets,
        FloatDirection direction,
        Clear clear) {
        requireInsets(containingBlockInsets);
        float width = Math.max(0f, floatedBox.width);
        float height = Math.max(0f, floatedBox.height);
        float y = Math.max(Math.max(minY, clearedThreshold(clear)), floatCeiling);
        while (true) {
            float[] insets = floatInsets(y, height);
            float leadInset = Math.max(insets[direction.index()], containingBlockInsets[direction.index()]);
            float trailInset = Math.max(insets[1 - direction.index()], containingBlockInsets[1 - direction.index()]);
            boolean fitsOpposite = trailInset == containingBlockInsets[1 - direction.index()]
                || leadInset + width <= availableWidth - trailInset + FIT_TOLERANCE;
            boolean fitsContainer = !hasFloatOnSide(y, height, direction)
                || leadInset + width <= availableWidth - containingBlockInsets[1 - direction.index()] + FIT_TOLERANCE;
            if (fitsOpposite && fitsContainer) {
                PlacedFloatedBox placed = new PlacedFloatedBox(direction, width, height, leadInset, y);
                floats.add(placed);
                floatCeiling = Math.max(floatCeiling, y);
                float x = direction == FloatDirection.LEFT ? leadInset : availableWidth - leadInset - width;
                return new FloatPoint(x, y);
            }
            float next = nextBoundary(y, height);
            if (Float.isInfinite(next)) {
                PlacedFloatedBox placed = new PlacedFloatedBox(direction, width, height,
                    containingBlockInsets[direction.index()], y);
                floats.add(placed);
                floatCeiling = Math.max(floatCeiling, y);
                float x = direction == FloatDirection.LEFT ? placed.xInset : availableWidth - placed.xInset - width;
                return new FloatPoint(x, y);
            }
            y = Math.max(y + FIT_TOLERANCE, next);
        }
    }

    /** Returns the lowest bottom edge of floats cleared by the supplied value. */
    public float clearedThreshold(Clear clear) {
        float result = 0f;
        boolean found = false;
        for (PlacedFloatedBox placed : floats) {
            if (clear.clears(placed.direction == FloatDirection.LEFT
                ? TaffyFloat.LEFT : TaffyFloat.RIGHT)) {
                result = found ? Math.max(result, placed.bottom()) : placed.bottom();
                found = true;
            }
        }
        return found ? result : Float.NEGATIVE_INFINITY;
    }

    /** Finds a slot for normal in-flow content. */
    public ContentSlot findContentSlot(
        float minY,
        float[] containingBlockInsets,
        Clear clear,
        Integer after) {
        requireInsets(containingBlockInsets);
        float y = Math.max(minY, clearedThreshold(clear));
        if (!hasActiveFloats(y)) {
            return new ContentSlot(null, containingBlockInsets[0], y,
                availableWidth - containingBlockInsets[0] - containingBlockInsets[1], Float.POSITIVE_INFINITY);
        }
        List<Float> boundaries = boundaries(y);
        int start = after == null ? 0 : Math.min(boundaries.size(), Math.max(0, after + 1));
        for (int index = start; index < boundaries.size(); index++) {
            float candidateY = boundaries.get(index);
            float[] insets = floatInsets(candidateY, 0f);
            float left = Math.max(insets[0], containingBlockInsets[0]);
            float right = Math.max(insets[1], containingBlockInsets[1]);
            if (candidateY >= y && availableWidth - left - right >= 0f) {
                return new ContentSlot(index, left, candidateY,
                    availableWidth - left - right, Float.POSITIVE_INFINITY);
            }
        }
        float fallbackY = boundaries.isEmpty() ? y : boundaries.get(boundaries.size() - 1);
        return new ContentSlot(null, containingBlockInsets[0], fallbackY,
            availableWidth - containingBlockInsets[0] - containingBlockInsets[1], Float.POSITIVE_INFINITY);
    }

    /** Finds a slot for a box establishing an independent formatting context. */
    public BfcSlot findBfcSlot(
        float minY,
        float[] containingBlockInsets,
        float[] margins,
        TaffyDirection direction,
        Clear clear,
        Integer after) {
        requireInsets(containingBlockInsets);
        requireInsets(margins);
        int lead = direction != null && direction.isRtl() ? 1 : 0;
        float[] marginInsets = new float[] {
            containingBlockInsets[0] + margins[0], containingBlockInsets[1] + margins[1]
        };
        float noFloatWidth = availableWidth - marginInsets[0] - marginInsets[1];
        float y = Math.max(minY, clearedThreshold(clear));
        if (!hasActiveFloats(y)) {
            return new BfcSlot(null, marginInsets[0], y, noFloatWidth, noFloatWidth);
        }
        List<Float> boundaries = boundaries(y);
        int start = after == null ? 0 : Math.min(boundaries.size(), Math.max(0, after + 1));
        for (int index = start; index < boundaries.size(); index++) {
            BfcSlot slot = bfcSlotAt(boundaries.get(index), containingBlockInsets, marginInsets, lead, index);
            if (slot != null) return slot;
        }
        return new BfcSlot(null, marginInsets[0], y, noFloatWidth, noFloatWidth);
    }

    private BfcSlot bfcSlotAt(
        float y,
        float[] containingBlockInsets,
        float[] marginInsets,
        int lead,
        int segmentId) {
        int trail = 1 - lead;
        float[] floatInsets = floatInsets(y, 0f);
        boolean hasLeadFloat = hasFloatOnSide(y, 0f, lead == 0 ? FloatDirection.LEFT : FloatDirection.RIGHT);
        boolean hasTrailFloat = hasFloatOnSide(y, 0f, trail == 0 ? FloatDirection.LEFT : FloatDirection.RIGHT);
        float[] fitInsets = new float[2];
        float[] stretchInsets = new float[2];
        fitInsets[lead] = hasLeadFloat ? Math.max(floatInsets[lead], marginInsets[lead]) : marginInsets[lead];
        stretchInsets[lead] = fitInsets[lead];
        fitInsets[trail] = hasTrailFloat ? Math.max(floatInsets[trail], containingBlockInsets[trail])
            : Math.min(marginInsets[trail], containingBlockInsets[trail]);
        stretchInsets[trail] = hasTrailFloat ? Math.max(floatInsets[trail], marginInsets[trail]) : marginInsets[trail];
        return new BfcSlot(segmentId, fitInsets[0], y,
            availableWidth - fitInsets[0] - fitInsets[1],
            availableWidth - stretchInsets[0] - stretchInsets[1]);
    }

    private float[] floatInsets(float y, float height) {
        float left = 0f;
        float right = 0f;
        for (PlacedFloatedBox placed : floats) {
            if (placed.bottom() <= y || (height > 0f && placed.y >= y + height)) continue;
            if (placed.direction == FloatDirection.LEFT) left = Math.max(left, placed.xInset + placed.width);
            else right = Math.max(right, placed.xInset + placed.width);
        }
        return new float[] {left, right};
    }

    private float nextBoundary(float y, float height) {
        float next = Float.POSITIVE_INFINITY;
        for (PlacedFloatedBox placed : floats) {
            if (placed.bottom() > y) next = Math.min(next, placed.bottom());
        }
        return next;
    }

    private List<Float> boundaries(float y) {
        List<Float> result = new ArrayList<>();
        result.add(y);
        for (PlacedFloatedBox placed : floats) {
            if (placed.bottom() > y) result.add(placed.bottom());
        }
        result.sort(Comparator.naturalOrder());
        for (int index = result.size() - 1; index > 0; index--) {
            if (Math.abs(result.get(index) - result.get(index - 1)) <= FIT_TOLERANCE) result.remove(index);
        }
        return result;
    }

    private boolean hasFloatOnSide(float y, float height, FloatDirection direction) {
        for (PlacedFloatedBox placed : floats) {
            if (placed.direction == direction && placed.bottom() > y
                && (height <= 0f || placed.y < y + height)) return true;
        }
        return false;
    }

    private static void requireInsets(float[] values) {
        if (values == null || values.length != 2) {
            throw new IllegalArgumentException("Insets must contain exactly two values");
        }
    }
}
