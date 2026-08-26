package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.FloatRect;
import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.TaffyDimension;

/** Resolves exact sizing keywords for absolutely positioned boxes. */
public class AbsoluteSizing {
    public static FloatSize resolveStretch(
        TaffySize<TaffyDimension> size,
        FloatSize knownDimensions,
        FloatSize areaSize,
        FloatRect inset,
        FloatRect margin
    ) {
        float width = knownDimensions.width;
        float height = knownDimensions.height;
        if (Float.isNaN(width) && size.width.isStretch()) {
            width = Math.max(0f, areaSize.width - valueOrZero(inset.left) - valueOrZero(inset.right)
                - valueOrZero(margin.left) - valueOrZero(margin.right));
        }
        if (Float.isNaN(height) && size.height.isStretch()) {
            height = Math.max(0f, areaSize.height - valueOrZero(inset.top) - valueOrZero(inset.bottom)
                - valueOrZero(margin.top) - valueOrZero(margin.bottom));
        }
        return new FloatSize(width, height);
    }

    private static float valueOrZero(float value) {
        return Float.isNaN(value) ? 0f : value;
    }
}
