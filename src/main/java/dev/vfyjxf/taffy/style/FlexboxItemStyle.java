package dev.vfyjxf.taffy.style;

/** Style contract required from a Flexbox item by the low-level API. */
public interface FlexboxItemStyle extends CoreStyle {
    default TaffyDimension getFlexBasis() { return toTaffyStyle().getFlexBasis(); }
    default float getFlexGrow() { return toTaffyStyle().getFlexGrow(); }
    default float getFlexShrink() { return toTaffyStyle().getFlexShrink(); }
    default AlignItems getAlignSelf() { return toTaffyStyle().getAlignSelf(); }
}
