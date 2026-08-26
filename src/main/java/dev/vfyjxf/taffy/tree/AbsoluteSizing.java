package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.FloatRect;
import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.LengthPercentage;
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

    /** Resolves measurement-based sizing keywords for an absolutely positioned box. */
    public static FloatSize resolveMeasurementKeywords(
        LayoutComputer layoutComputer,
        NodeId node,
        TaffySize<TaffyDimension> size,
        FloatSize knownDimensions,
        FloatSize areaSize,
        FloatRect inset,
        FloatRect margin,
        SizingMode sizingMode
    ) {
        FloatSize stretchSize = new FloatSize(
            Math.max(0f, areaSize.width - valueOrZero(inset.left) - valueOrZero(inset.right)
                - valueOrZero(margin.left) - valueOrZero(margin.right)),
            Math.max(0f, areaSize.height - valueOrZero(inset.top) - valueOrZero(inset.bottom)
                - valueOrZero(margin.top) - valueOrZero(margin.bottom))
        );
        FloatSize stretchedDimensions = resolveStretch(size, knownDimensions, areaSize, inset, margin);
        AvailableSpace widthConstraint = measurementConstraint(size.width, stretchSize.width, areaSize.width);
        AvailableSpace heightConstraint = measurementConstraint(size.height, stretchSize.height, areaSize.height);
        boolean measureWidth = Float.isNaN(stretchedDimensions.width) && widthConstraint != null;
        boolean measureHeight = Float.isNaN(stretchedDimensions.height) && heightConstraint != null;
        if (!measureWidth && !measureHeight) {
            return stretchedDimensions;
        }

        FloatSize measuredSize = layoutComputer.measureChildSize(
            node,
            stretchedDimensions,
            areaSize,
            new TaffySize<>(
                measureWidth ? widthConstraint : AvailableSpace.definite(stretchSize.width),
                measureHeight ? heightConstraint : AvailableSpace.definite(stretchSize.height)
            ),
            sizingMode,
            new TaffyLine<>(false, false)
        );
        return new FloatSize(
            measureWidth ? measuredSize.width : stretchedDimensions.width,
            measureHeight ? measuredSize.height : stretchedDimensions.height
        );
    }

    private static AvailableSpace measurementConstraint(TaffyDimension dimension, float stretchSize, float basis) {
        if (dimension.isMinContent()) {
            return AvailableSpace.minContent();
        }
        if (dimension.isMaxContent()) {
            return AvailableSpace.maxContent();
        }
        if (dimension.isFitContent()) {
            LengthPercentage limit = dimension.getFitContentLimit();
            float resolvedLimit = limit == null ? stretchSize : limit.maybeResolve(basis);
            return Float.isNaN(resolvedLimit) ? null : AvailableSpace.definite(resolvedLimit);
        }
        return null;
    }

    private static float valueOrZero(float value) {
        return Float.isNaN(value) ? 0f : value;
    }
}
