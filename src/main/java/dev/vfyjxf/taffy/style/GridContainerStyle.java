package dev.vfyjxf.taffy.style;

import dev.vfyjxf.taffy.geometry.TaffySize;
import java.util.List;

/** Style contract required from a Grid container by the low-level API. */
public interface GridContainerStyle extends CoreStyle {
    default List<TrackSizingFunction> getGridTemplateRows() { return toTaffyStyle().getGridTemplateRows(); }
    default List<TrackSizingFunction> getGridTemplateColumns() { return toTaffyStyle().getGridTemplateColumns(); }
    default List<TrackSizingFunction> getGridAutoRows() { return toTaffyStyle().getGridAutoRows(); }
    default List<TrackSizingFunction> getGridAutoColumns() { return toTaffyStyle().getGridAutoColumns(); }
    default GridAutoFlow getGridAutoFlow() { return toTaffyStyle().getGridAutoFlow(); }
}
