package dev.vfyjxf.taffy.util;

import dev.vfyjxf.taffy.geometry.FloatPoint;
import dev.vfyjxf.taffy.geometry.FloatRect;
import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffyPoint;
import dev.vfyjxf.taffy.style.Overflow;
import dev.vfyjxf.taffy.style.Contain;

/**
 * Helpers for computing CSS content size contributions.
 *
 * <p>Mirrors Rust's {@code compute_content_size_contribution} logic:
 * a node contributes its border-box size, except that if its overflow is VISIBLE in an axis,
 * then its contribution in that axis is {@code max(size, contentSize)}.
 */
public class ContentSizeUtil {

    private ContentSizeUtil() {
    }

    private static float sanitize(float v) {
        return Float.isNaN(v) ? 0f : v;
    }

    public static FloatSize max(FloatSize a, FloatSize b) {
        return new FloatSize(
            Math.max(sanitize(a.width), sanitize(b.width)),
            Math.max(sanitize(a.height), sanitize(b.height))
        );
    }

    /**
     * Determine how much width/height a given node contributes to its parent's content size.
     */
    public static FloatSize computeContentSizeContribution(
        FloatPoint location,
        FloatSize size,
        FloatSize contentSize,
        TaffyPoint<Overflow> overflow,
        Contain contain
    ) {
        float x = sanitize(location.x);
        float y = sanitize(location.y);

        float sizeW = sanitize(size.width);
        float sizeH = sanitize(size.height);

        float contentW = sanitize(contentSize.width);
        float contentH = sanitize(contentSize.height);

        boolean containsScrollableOverflow = contain.containsScrollableOverflow();
        float contributionW = !containsScrollableOverflow && overflow.x == Overflow.VISIBLE ? Math.max(sizeW, contentW) : sizeW;
        float contributionH = !containsScrollableOverflow && overflow.y == Overflow.VISIBLE ? Math.max(sizeH, contentH) : sizeH;

        if (contributionW > 0f && contributionH > 0f) {
            return new FloatSize(x + contributionW, y + contributionH);
        }

        return FloatSize.zero();
    }

    /**
     * Computes a contribution from a child's scrollable overflow rectangle.
     * The rectangle is relative to the child's border box; its right and bottom
     * edges therefore describe the extent visible to the parent. Containment
     * and non-visible overflow keep the contribution at the border-box size.
     */
    public static FloatSize computeContentSizeContribution(
        FloatPoint location,
        FloatSize size,
        FloatRect scrollableOverflowRect,
        TaffyPoint<Overflow> overflow,
        Contain contain
    ) {
        if (scrollableOverflowRect == null) {
            return computeContentSizeContribution(location, size, size, overflow, contain);
        }

        float x = sanitize(location.x);
        float y = sanitize(location.y);
        float sizeW = sanitize(size.width);
        float sizeH = sanitize(size.height);
        boolean isolated = contain.containsScrollableOverflow();
        float extentW = !isolated && overflow.x == Overflow.VISIBLE
            ? Math.max(sizeW, sanitize(scrollableOverflowRect.right)) : sizeW;
        float extentH = !isolated && overflow.y == Overflow.VISIBLE
            ? Math.max(sizeH, sanitize(scrollableOverflowRect.bottom)) : sizeH;

        if (extentW > 0f && extentH > 0f) {
            return new FloatSize(x + extentW, y + extentH);
        }
        return FloatSize.zero();
    }
}
