package dev.vfyjxf.taffy.style;

/** Style contract required from a Block item by the low-level API. */
public interface BlockItemStyle extends CoreStyle {
    default boolean isTable() { return toTaffyStyle().getItemIsTable(); }
    default TaffyFloat getFloatMode() { return toTaffyStyle().getFloatMode(); }
    default Clear getClear() { return toTaffyStyle().getClear(); }
}
