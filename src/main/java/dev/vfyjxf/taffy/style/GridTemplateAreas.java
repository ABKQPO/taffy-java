package dev.vfyjxf.taffy.style;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The value of CSS {@code grid-template-areas}, including unnamed-cell dimensions.
 *
 * <p>The stored row and column counts may exceed the bounds of named areas because CSS
 * templates can contain {@code .} cells. Those counts therefore participate in constructing
 * the explicit grid.</p>
 */
public class GridTemplateAreas {
    private final List<GridTemplateArea> areas;
    private final int rowCount;
    private final int columnCount;

    /** Creates a dimension-preserving grid area template. */
    public GridTemplateAreas(List<GridTemplateArea> areas, int rowCount, int columnCount) {
        if (rowCount < 0 || columnCount < 0) {
            throw new IllegalArgumentException("Grid template area dimensions must not be negative");
        }
        this.areas = List.copyOf(areas == null ? List.of() : areas);
        this.rowCount = rowCount;
        this.columnCount = columnCount;
    }

    /** Creates a template with dimensions inferred from its named areas. */
    public static GridTemplateAreas fromAreas(List<GridTemplateArea> areas) {
        int rowCount = 0;
        int columnCount = 0;
        if (areas != null) {
            for (GridTemplateArea area : areas) {
                if (area == null) continue;
                rowCount = Math.max(rowCount, Math.max(1, area.getRowEnd()) - 1);
                columnCount = Math.max(columnCount, Math.max(1, area.getColumnEnd()) - 1);
            }
        }
        return new GridTemplateAreas(areas, rowCount, columnCount);
    }

    /** Parses the quoted row strings used by CSS {@code grid-template-areas}. */
    public static GridTemplateAreas fromRows(List<String> rows) {
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("Grid template areas requires at least one row");
        }
        Map<String, AreaBounds> bounds = new LinkedHashMap<>();
        int columnCount = -1;
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            String row = rows.get(rowIndex);
            if (row == null || row.trim().isEmpty()) {
                throw new IllegalArgumentException("Grid template area rows must not be empty");
            }
            String[] cells = row.trim().split("\\s+");
            if (columnCount < 0) columnCount = cells.length;
            if (cells.length != columnCount) {
                throw new IllegalArgumentException("Grid template area rows must have equal column counts");
            }
            for (int columnIndex = 0; columnIndex < cells.length; columnIndex++) {
                String name = cells[columnIndex];
                if (name.equals(".")) continue;
                if (name.isEmpty()) throw new IllegalArgumentException("Grid area names must not be empty");
                bounds.computeIfAbsent(name, ignored -> new AreaBounds()).add(rowIndex + 1, columnIndex + 1);
            }
        }
        List<GridTemplateArea> areas = new ArrayList<>();
        for (Map.Entry<String, AreaBounds> entry : bounds.entrySet()) {
            AreaBounds area = entry.getValue();
            if (area.cellCount != (area.rowEnd - area.rowStart) * (area.columnEnd - area.columnStart)) {
                throw new IllegalArgumentException("Grid area '" + entry.getKey() + "' must form a rectangle");
            }
            areas.add(new GridTemplateArea(entry.getKey(), area.rowStart, area.rowEnd, area.columnStart, area.columnEnd));
        }
        return new GridTemplateAreas(areas, rows.size(), columnCount);
    }

    /** Returns named areas in source order. */
    public List<GridTemplateArea> areas() {
        return areas;
    }

    /** Returns named areas in source order. */
    public List<GridTemplateArea> getAreas() {
        return areas();
    }

    /** Returns the number of rows in the template, including unnamed rows. */
    public int rowCount() {
        return rowCount;
    }

    /** Returns the number of rows in the template, including unnamed rows. */
    public int getRowCount() {
        return rowCount();
    }

    /** Returns the number of columns in the template, including unnamed columns. */
    public int columnCount() {
        return columnCount;
    }

    /** Returns the number of columns in the template, including unnamed columns. */
    public int getColumnCount() {
        return columnCount();
    }

    /** Returns a copy of this immutable value object. */
    public GridTemplateAreas copy() {
        return new GridTemplateAreas(areas, rowCount, columnCount);
    }

    /** Parses a CSS {@code grid-template-areas} value. */
    public static GridTemplateAreas parse(String value) {
        return CssParser.parseGridTemplateAreas(value);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GridTemplateAreas other)) return false;
        return rowCount == other.rowCount && columnCount == other.columnCount && areas.equals(other.areas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(areas, rowCount, columnCount);
    }

    @Override
    public String toString() {
        return "GridTemplateAreas{areas=" + areas + ", rowCount=" + rowCount + ", columnCount=" + columnCount + "}";
    }

    private static class AreaBounds {
        private int rowStart = Integer.MAX_VALUE;
        private int rowEnd;
        private int columnStart = Integer.MAX_VALUE;
        private int columnEnd;
        private int cellCount;

        private void add(int row, int column) {
            rowStart = Math.min(rowStart, row);
            rowEnd = Math.max(rowEnd, row + 1);
            columnStart = Math.min(columnStart, column);
            columnEnd = Math.max(columnEnd, column + 1);
            cellCount++;
        }
    }
}
