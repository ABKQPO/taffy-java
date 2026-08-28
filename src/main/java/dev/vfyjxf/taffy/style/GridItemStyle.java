package dev.vfyjxf.taffy.style;

/** Style contract required from a Grid item by the low-level API. */
public interface GridItemStyle extends CoreStyle {
    default GridPlacement getGridRowStart() { return toTaffyStyle().getGridRowStart(); }
    default GridPlacement getGridRowEnd() { return toTaffyStyle().getGridRowEnd(); }
    default GridPlacement getGridColumnStart() { return toTaffyStyle().getGridColumnStart(); }
    default GridPlacement getGridColumnEnd() { return toTaffyStyle().getGridColumnEnd(); }
}
