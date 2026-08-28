package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.FloatRect;
import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.style.CustomIdentCodec;
import dev.vfyjxf.taffy.style.GenericGridPlacement;
import dev.vfyjxf.taffy.style.GridPlacement;
import dev.vfyjxf.taffy.style.TaffyDirection;

import java.util.List;
import java.util.Objects;

/** Read-only typed view of detailed grid diagnostics with caller-defined identifiers. */
public class GenericDetailedGridInfo<S> {
    private final DetailedGridInfo runtime;
    private final CustomIdentCodec<S> identifierCodec;
    private final GenericDetailedGridTracksInfo<S> rows;
    private final GenericDetailedGridTracksInfo<S> columns;

    public GenericDetailedGridInfo(DetailedGridInfo runtime, CustomIdentCodec<S> identifierCodec) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.identifierCodec = Objects.requireNonNull(identifierCodec, "identifierCodec");
        this.rows = new GenericDetailedGridTracksInfo<>(runtime.rows(), identifierCodec);
        this.columns = new GenericDetailedGridTracksInfo<>(runtime.columns(), identifierCodec);
    }

    public GenericDetailedGridTracksInfo<S> rows() {
        return rows;
    }

    public GenericDetailedGridTracksInfo<S> columns() {
        return columns;
    }

    public List<DetailedGridItemInfo> items() {
        return runtime.items();
    }

    public String gridTemplateRows() {
        return runtime.gridTemplateRows();
    }

    public String gridTemplateColumns() {
        return runtime.gridTemplateColumns();
    }

    public FloatRect itemGridArea(int itemIndex) {
        return runtime.itemGridArea(itemIndex);
    }

    public FloatRect resolveAbsoluteGridArea(
        TaffyLine<GenericGridPlacement<S>> gridRow,
        TaffyLine<GenericGridPlacement<S>> gridColumn,
        TaffyDirection direction,
        FloatRect paddingBox) {
        return runtime.resolveAbsoluteGridArea(
            encode(gridRow), encode(gridColumn), direction, paddingBox);
    }

    DetailedGridInfo runtime() {
        return runtime;
    }

    private TaffyLine<GridPlacement> encode(
        TaffyLine<GenericGridPlacement<S>> placement) {
        if (placement == null) return null;
        return new TaffyLine<>(
            placement.start.toGridPlacement(identifierCodec),
            placement.end.toGridPlacement(identifierCodec));
    }
}
