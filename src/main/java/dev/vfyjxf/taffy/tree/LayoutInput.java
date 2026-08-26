package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;

/**
 * A struct containing the input constraints and hints for laying out a node.
 *
 * @param runMode Whether to compute only the node size or perform full layout
 * @param sizingMode Whether style sizes are taken into account
 * @param axis Which axis needs to be measured
 * @param knownDimensions Dimensions treated as fixed during layout
 * @param parentSize Parent dimensions used for percentage resolution
 * @param availableSpace Available space used as a soft wrapping constraint
 * @param verticalMarginsAreCollapsible Whether block margins may collapse
 */
public record LayoutInput(
    RunMode runMode,
    SizingMode sizingMode,
    RequestedAxis axis,
    FloatSize knownDimensions,
    FloatSize parentSize,
    TaffySize<AvailableSpace> availableSpace,
    TaffyLine<Boolean> verticalMarginsAreCollapsible
) {

    /**
     * Create a LayoutInput for hidden layout.
     */
    public static final LayoutInput HIDDEN = new LayoutInput(
        RunMode.PERFORM_HIDDEN_LAYOUT,
        SizingMode.INHERENT_SIZE,
        RequestedAxis.BOTH,
        FloatSize.none(),
        FloatSize.none(),
        new TaffySize<>(AvailableSpace.MAX_CONTENT, AvailableSpace.MAX_CONTENT),
        TaffyLine.FALSE
    );

    /**
     * Static factory method for hidden layout input.
     */
    public static LayoutInput hidden() {
        return HIDDEN;
    }

    /**
     * Create a copy with modified known dimensions.
     */
    public LayoutInput withKnownDimensions(FloatSize knownDimensions) {
        return new LayoutInput(
            runMode,
            sizingMode,
            axis,
            knownDimensions,
            parentSize,
            availableSpace,
            verticalMarginsAreCollapsible
        );
    }

    /**
     * Create a copy with modified available space.
     */
    public LayoutInput withAvailableSpace(TaffySize<AvailableSpace> availableSpace) {
        return new LayoutInput(
            runMode,
            sizingMode,
            axis,
            knownDimensions,
            parentSize,
            availableSpace,
            verticalMarginsAreCollapsible
        );
    }

    @Override
    public String toString() {
        return "LayoutInput{runMode=" + runMode + ", knownDimensions=" + knownDimensions
            + ", availableSpace=" + availableSpace + "}";
    }
}
