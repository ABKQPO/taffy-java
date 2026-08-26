package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.FloatLine;
import dev.vfyjxf.taffy.geometry.FloatRect;
import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.style.GridPlacement;
import dev.vfyjxf.taffy.style.TaffyDirection;
import dev.vfyjxf.taffy.style.TrackSizingFunction;
import dev.vfyjxf.taffy.tree.grid.NamedLineResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Detailed track and item information produced by a grid layout. */
public class DetailedGridInfo {
    private final DetailedGridTracksInfo rows;
    private final DetailedGridTracksInfo columns;
    private final List<DetailedGridItemInfo> items;
    private final String gridTemplateRows;
    private final String gridTemplateColumns;
    private final NamedLineResolver namedLineResolver;
    private final TaffyDirection direction;
    private final FloatRect paddingBox;

    public DetailedGridInfo(
        DetailedGridTracksInfo rows,
        DetailedGridTracksInfo columns,
        List<DetailedGridItemInfo> items,
        String gridTemplateRows,
        String gridTemplateColumns,
        NamedLineResolver namedLineResolver,
        TaffyDirection direction,
        FloatRect paddingBox) {
        this.rows = rows;
        this.columns = columns;
        this.items = Collections.unmodifiableList(new ArrayList<>(items == null ? Collections.emptyList() : items));
        this.gridTemplateRows = gridTemplateRows == null ? "" : gridTemplateRows;
        this.gridTemplateColumns = gridTemplateColumns == null ? "" : gridTemplateColumns;
        this.namedLineResolver = namedLineResolver;
        this.direction = direction == null ? TaffyDirection.LTR : direction;
        this.paddingBox = paddingBox == null ? FloatRect.zero() : paddingBox.copy();
    }

    public DetailedGridTracksInfo rows() {
        return rows;
    }

    public DetailedGridTracksInfo columns() {
        return columns;
    }

    public List<DetailedGridItemInfo> items() {
        return items;
    }

    /** Return the used row track list in resolved CSS-like form. */
    public String gridTemplateRows() {
        return gridTemplateRows;
    }

    /** Return the used column track list in resolved CSS-like form. */
    public String gridTemplateColumns() {
        return gridTemplateColumns;
    }

    /** Return the physical area occupied by an item, or null for an invalid index. */
    public FloatRect itemGridArea(int itemIndex) {
        if (itemIndex < 0 || itemIndex >= items.size()) return null;
        DetailedGridItemInfo item = items.get(itemIndex);
        FloatLine columnStart = columns.positionForTrackLine(item.columnStart());
        FloatLine columnEnd = columns.positionForTrackLine(item.columnEnd() - 1);
        FloatLine rowStart = rows.positionForTrackLine(item.rowStart());
        FloatLine rowEnd = rows.positionForTrackLine(item.rowEnd() - 1);
        if (columnStart == null || columnEnd == null || rowStart == null || rowEnd == null) return null;
        FloatRect area = FloatRect.ltrb(
            Math.min(columnStart.start, columnEnd.start),
            Math.min(rowStart.start, rowEnd.start),
            Math.max(columnStart.end, columnEnd.end),
            Math.max(rowStart.end, rowEnd.end));
        return direction.isRtl() ? mirrorInlineAxis(area, paddingBox) : area;
    }

    /** Resolve an absolutely positioned grid area from CSS Grid line placement values. */
    public FloatRect resolveAbsoluteGridArea(
        TaffyLine<GridPlacement> gridRow,
        TaffyLine<GridPlacement> gridColumn,
        TaffyDirection direction,
        FloatRect paddingBox) {
        if (paddingBox == null) return null;
        TaffyLine<GridPlacement> resolvedRows = resolveNames(gridRow, true);
        TaffyLine<GridPlacement> resolvedColumns = resolveNames(gridColumn, false);
        TaffyLine<Integer> rowIndices = resolveAbsoluteTrackIndices(rows, resolvedRows);
        TaffyLine<Integer> columnIndices = resolveAbsoluteTrackIndices(columns, resolvedColumns);
        FloatLine row = resolveLinePair(rows, rowIndices.start, rowIndices.end, paddingBox.top, paddingBox.bottom, false);
        FloatLine column = resolveLinePair(columns, columnIndices.start, columnIndices.end, paddingBox.left, paddingBox.right,
            direction != null && direction.isRtl());
        FloatRect area = FloatRect.ltrb(column.start, row.start, column.end, row.end);
        return direction != null && direction.isRtl() ? mirrorInlineAxis(area, paddingBox) : area;
    }

    private static FloatRect mirrorInlineAxis(FloatRect area, FloatRect paddingBox) {
        float left = paddingBox.left + paddingBox.right - area.right;
        float right = paddingBox.left + paddingBox.right - area.left;
        return new FloatRect(left, right, area.top, area.bottom);
    }

    private TaffyLine<GridPlacement> resolveNames(TaffyLine<GridPlacement> placement, boolean row) {
        if (placement == null) return new TaffyLine<>(GridPlacement.auto(), GridPlacement.auto());
        if (namedLineResolver == null) return placement;
        return row ? namedLineResolver.resolveRowNames(placement) : namedLineResolver.resolveColumnNames(placement);
    }

    private static TaffyLine<Integer> resolveAbsoluteTrackIndices(
        DetailedGridTracksInfo tracks,
        TaffyLine<GridPlacement> placement) {
        GridPlacement start = placement == null ? null : placement.start;
        GridPlacement end = placement == null ? null : placement.end;
        Integer startLine = tracks.resolveOriginZeroLine(start);
        Integer endLine = tracks.resolveOriginZeroLine(end);

        if (startLine != null && endLine != null) {
            if (startLine.equals(endLine)) endLine++;
            else {
                int low = Math.min(startLine, endLine);
                int high = Math.max(startLine, endLine);
                startLine = low;
                endLine = high;
            }
        } else if (startLine != null && end != null && end.isSpan()) {
            endLine = startLine + end.getValue();
        } else if (endLine != null && start != null && start.isSpan()) {
            startLine = endLine - start.getValue();
        }

        return new TaffyLine<>(
            tracks.resolveLineIndex(startLine),
            tracks.resolveLineIndex(endLine));
    }

    private static FloatLine resolveLinePair(
        DetailedGridTracksInfo tracks,
        Integer start,
        Integer end,
        float fallbackStart,
        float fallbackEnd,
        boolean reversed) {
        float startPosition = tracks.startLinePosition(start, fallbackStart, fallbackEnd, reversed);
        float endPosition = tracks.endLinePosition(end, fallbackStart, fallbackEnd, reversed);
        return new FloatLine(Math.min(startPosition, endPosition), Math.max(startPosition, endPosition));
    }

    /** Build detail metadata from the values generated by GridComputer. */
    public static DetailedGridInfo from(
        TrackCounts rowCounts,
        TrackCounts columnCounts,
        List<Float> rowSizes,
        List<Float> columnSizes,
        List<Float> rowOffsets,
        List<Float> columnOffsets,
        List<DetailedGridItemInfo> items,
        List<TrackSizingFunction> rowTemplate,
        List<TrackSizingFunction> columnTemplate,
        NamedLineResolver namedLineResolver,
        TaffyDirection direction,
        FloatRect paddingBox) {
        DetailedGridTracksInfo rows = DetailedGridTracksInfo.from(
            rowCounts, rowSizes, rowOffsets,
            namedLineResolver == null ? Collections.emptyMap() : namedLineResolver.getRowLineNamesByIndex());
        DetailedGridTracksInfo columns = DetailedGridTracksInfo.from(
            columnCounts, columnSizes, columnOffsets,
            namedLineResolver == null ? Collections.emptyMap() : namedLineResolver.getColumnLineNamesByIndex());
        return new DetailedGridInfo(
            rows,
            columns,
            items,
            rows.resolvedTrackList(),
            columns.resolvedTrackList(),
            namedLineResolver,
            direction,
            paddingBox);
    }

    @Override
    public String toString() {
        return "DetailedGridInfo{rows=" + rows + ", columns=" + columns + ", items=" + items.size() + "}";
    }
}
