package dev.vfyjxf.taffy.tree.grid;

import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.style.GridPlacement;
import dev.vfyjxf.taffy.style.GridRepetition;
import dev.vfyjxf.taffy.style.GridTemplateArea;
import dev.vfyjxf.taffy.style.GridTemplateAreas;
import dev.vfyjxf.taffy.style.GridTemplateComponent;
import dev.vfyjxf.taffy.style.NamedGridLine;
import dev.vfyjxf.taffy.style.TaffyStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Resolves named grid lines and areas to numeric line indices.
 * <p>
 * This class builds maps of named lines from:
 * 1. Explicit named lines from grid-template-columns/rows
 * 2. Implicit named lines from grid-template-areas (e.g., "header" area creates "header-start" and "header-end" lines)
 * <p>
 * Corresponds to Rust's NamedLineResolver in taffy/src/compute/grid/types/named.rs
 */
public class NamedLineResolver {
    
    /** Map of row line names to line numbers (1-based). Each name may have multiple lines. */
    private final Map<String, List<Integer>> rowLines;
    
    /** Map of column line names to line numbers (1-based). Each name may have multiple lines. */
    private final Map<String, List<Integer>> columnLines;
    
    /** Map of area names to area definitions */
    private final Map<String, GridTemplateArea> areas;
    
    /** Number of columns implied by grid area definitions */
    private int areaColumnCount;
    
    /** Number of rows implied by grid area definitions */
    private int areaRowCount;
    
    /** The number of explicit columns in the grid (set after track expansion) */
    private int explicitColumnCount;
    
    /** The number of explicit rows in the grid (set after track expansion) */
    private int explicitRowCount;

    private final TaffyStyle style;
    
    /**
     * Create a new NamedLineResolver from container style.
     * @param style The grid container's style
     */
    public NamedLineResolver(TaffyStyle style) {
        this.style = style;
        this.rowLines = new HashMap<>();
        this.columnLines = new HashMap<>();
        this.areas = new HashMap<>();
        this.areaColumnCount = 0;
        this.areaRowCount = 0;
        this.explicitColumnCount = 0;
        this.explicitRowCount = 0;
        
        // Process grid-template-areas
        GridTemplateAreas templateAreas = style.gridTemplateAreas;
        if (templateAreas != null) {
            areaColumnCount = templateAreas.columnCount();
            areaRowCount = templateAreas.rowCount();
            for (GridTemplateArea area : templateAreas.areas()) {
                areas.put(area.getName(), area);

                // Create implicit line names from areas
                // Area "header" creates lines "header-start" and "header-end"
                upsertLineName(columnLines, area.getName() + "-start", area.getColumnStart());
                upsertLineName(columnLines, area.getName() + "-end", area.getColumnEnd());
                upsertLineName(rowLines, area.getName() + "-start", area.getRowStart());
                upsertLineName(rowLines, area.getName() + "-end", area.getRowEnd());
            }
        }
        
        // Process explicit named lines from grid-template-column-names
        if (style.gridTemplateColumnNames != null) {
            for (NamedGridLine namedLine : style.gridTemplateColumnNames) {
                upsertLineName(columnLines, namedLine.getName(), namedLine.getIndex());
            }
        }
        
        // Process explicit named lines from grid-template-row-names
        if (style.gridTemplateRowNames != null) {
            for (NamedGridLine namedLine : style.gridTemplateRowNames) {
                upsertLineName(rowLines, namedLine.getName(), namedLine.getIndex());
            }
        }
        
        // Sort and dedup lines for consistent lookup
        for (List<Integer> lines : columnLines.values()) {
            lines.sort(Integer::compareTo);
            // Dedup
            for (int i = lines.size() - 1; i > 0; i--) {
                if (lines.get(i).equals(lines.get(i - 1))) {
                    lines.remove(i);
                }
            }
        }
        for (List<Integer> lines : rowLines.values()) {
            lines.sort(Integer::compareTo);
            // Dedup
            for (int i = lines.size() - 1; i > 0; i--) {
                if (lines.get(i).equals(lines.get(i - 1))) {
                    lines.remove(i);
                }
            }
        }
    }
    
    private void upsertLineName(Map<String, List<Integer>> map, String name, int lineNumber) {
        map.computeIfAbsent(name, k -> new ArrayList<>()).add(lineNumber);
    }

    private void addRepeatedLineNames(
        Map<String, List<Integer>> lineMap,
        List<GridTemplateComponent> template,
        int explicitTrackCount) {
        if (template == null || template.isEmpty()) return;
        int fixedTrackCount = 0;
        int autoTrackCount = 0;
        for (GridTemplateComponent component : template) {
            if (component == null) continue;
            if (component.isSingle()) {
                fixedTrackCount++;
                continue;
            }
            GridRepetition repetition = component.getRepeat();
            if (repetition == null) continue;
            if (repetition.isAutoRepetition()) {
                autoTrackCount += repetition.getTrackCount();
            } else {
                fixedTrackCount += repetition.getCount() * repetition.getTrackCount();
            }
        }

        int currentLine = 1;
        for (GridTemplateComponent component : template) {
            if (component == null) continue;
            if (component.isSingle()) {
                currentLine++;
                continue;
            }
            GridRepetition repetition = component.getRepeat();
            if (repetition == null || repetition.getTrackCount() == 0) continue;
            List<List<String>> lineNames = repetition.getLineNames();
            if (!lineNames.isEmpty() && lineNames.size() != repetition.getTrackCount() + 1) {
                throw new IllegalArgumentException(
                    "Grid repetition line names must be empty or contain exactly track count + 1 sets");
            }
            int repetitionCount = repetition.isAutoRepetition()
                ? Math.max(1, (explicitTrackCount - fixedTrackCount) / Math.max(1, autoTrackCount))
                : repetition.getCount();
            for (int repeatIndex = 0; repeatIndex < repetitionCount; repeatIndex++) {
                for (int lineOffset = 0; lineOffset < lineNames.size(); lineOffset++) {
                    for (String name : lineNames.get(lineOffset)) {
                        upsertLineName(lineMap, name, currentLine + lineOffset);
                    }
                }
                currentLine += repetition.getTrackCount();
            }
        }
    }

    private void sortAndDeduplicate(Map<String, List<Integer>> lineMap) {
        for (List<Integer> lines : lineMap.values()) {
            lines.sort(Integer::compareTo);
            for (int i = lines.size() - 1; i > 0; i--) {
                if (lines.get(i).equals(lines.get(i - 1))) lines.remove(i);
            }
        }
    }
    
    /**
     * Resolve named lines in a row placement to numeric lines.
     * @param placement The row placement (start and end)
     * @return Line with resolved GridPlacements (NAMED_LINE -> LINE, NAMED_SPAN resolved against anchor)
     */
    public TaffyLine<GridPlacement> resolveRowNames(TaffyLine<GridPlacement> placement) {
        return resolveLineNames(placement, Axis.ROW);
    }
    
    /**
     * Resolve named lines in a column placement to numeric lines.
     * @param placement The column placement (start and end)
     * @return Line with resolved GridPlacements (NAMED_LINE -> LINE, NAMED_SPAN resolved against anchor)
     */
    public TaffyLine<GridPlacement> resolveColumnNames(TaffyLine<GridPlacement> placement) {
        return resolveLineNames(placement, Axis.COLUMN);
    }
    
    private enum Axis { ROW, COLUMN }
    private enum End { START, END }
    
    /**
     * Resolve named lines for both start and end of a grid placement.
     */
    private TaffyLine<GridPlacement> resolveLineNames(TaffyLine<GridPlacement> placement, Axis axis) {
        GridPlacement resolvedStart = placement.start;
        GridPlacement resolvedEnd = placement.end;
        
        // Resolve named lines first
        if (placement.start.isNamedLine()) {
            int lineIndex = findLineIndex(
                placement.start.getLineName(), 
                placement.start.getNthIndex(), 
                axis, 
                End.START,
                null  // No filter
            );
            resolvedStart = GridPlacement.line(lineIndex);
        }
        
        if (placement.end.isNamedLine()) {
            int lineIndex = findLineIndex(
                placement.end.getLineName(), 
                placement.end.getNthIndex(), 
                axis, 
                End.END,
                null  // No filter
            );
            resolvedEnd = GridPlacement.line(lineIndex);
        }
        
        // Handle named spans
        // If both are resolved to lines, we're done
        // Otherwise handle NAMED_SPAN
        
        int explicitTrackCount = (axis == Axis.ROW) ? explicitRowCount : explicitColumnCount;
        
        if (resolvedStart.isLine() && placement.end.isNamedSpan()) {
            int startLineNum = resolvedStart.getLineNumber();
            // Normalize start line (convert negative to positive position)
            int normalizedStart;
            if (startLineNum > 0) {
                normalizedStart = startLineNum;
            } else {
                normalizedStart = Math.max(0, explicitTrackCount + 1 + startLineNum);
            }
            
            // Find nth line with name that comes AFTER normalizedStart
            String spanName = placement.end.getLineName();
            int spanCount = placement.end.getSpan();
            int endLine = findLineIndex(spanName, spanCount, axis, End.END, 
                lines -> filterLinesAfter(lines, normalizedStart));
            resolvedEnd = GridPlacement.line(endLine);
        }
        
        if (resolvedEnd.isLine() && placement.start.isNamedSpan()) {
            int endLineNum = resolvedEnd.getLineNumber();
            // Normalize end line
            int normalizedEnd;
            if (endLineNum > 0) {
                normalizedEnd = endLineNum;
            } else {
                normalizedEnd = Math.max(0, explicitTrackCount + 1 + endLineNum);
            }
            
            // Find nth line with name that comes BEFORE normalizedEnd
            String spanName = placement.start.getLineName();
            int spanCount = placement.start.getSpan();
            int startLine = findLineIndex(spanName, spanCount, axis, End.START,
                lines -> filterLinesBefore(lines, normalizedEnd));
            resolvedStart = GridPlacement.line(startLine);
        }
        
        // If named span is paired with auto or another named span, treat as span(1)
        if (resolvedStart.isNamedSpan()) {
            resolvedStart = GridPlacement.span(1);
        }
        if (resolvedEnd.isNamedSpan()) {
            resolvedEnd = GridPlacement.span(1);
        }
        
        return new TaffyLine<>(resolvedStart, resolvedEnd);
    }
    
    /**
     * Filter lines to only those greater than the given value.
     */
    private List<Integer> filterLinesAfter(List<Integer> lines, int afterValue) {
        List<Integer> result = new ArrayList<>();
        for (Integer line : lines) {
            if (line > afterValue) {
                result.add(line);
            }
        }
        return result;
    }
    
    /**
     * Filter lines to only those less than the given value.
     */
    private List<Integer> filterLinesBefore(List<Integer> lines, int beforeValue) {
        List<Integer> result = new ArrayList<>();
        for (Integer line : lines) {
            if (line < beforeValue) {
                result.add(line);
            }
        }
        return result;
    }
    
    /**
     * Find the line index for a named line.
     * 
     * @param name The line name
     * @param idx  The nth occurrence (1-based, positive from start, negative from end)
     * @param axis ROW or COLUMN
     * @param end  START or END (for implicit line name lookup)
     * @param filter Optional filter to apply to lines (e.g., for named spans)
     * @return The 1-based line number
     */
    private int findLineIndex(String name, int idx, Axis axis, End end, 
                              Function<List<Integer>, List<Integer>> filter) {
        int explicitTrackCount = (axis == Axis.ROW) ? explicitRowCount : explicitColumnCount;
        
        // 0 means "no index specified", treat as 1
        if (idx == 0) {
            idx = 1;
        }
        
        // Get the line lookup for this axis
        Map<String, List<Integer>> lineLookup = (axis == Axis.ROW) ? rowLines : columnLines;
        
        // Try direct name lookup
        List<Integer> lines = lineLookup.get(name);
        if (lines != null && !lines.isEmpty()) {
            List<Integer> filteredLines = (filter != null) ? filter.apply(lines) : lines;
            if (!filteredLines.isEmpty()) {
                return getLineFromList(filteredLines, explicitTrackCount, idx);
            }
        }
        
        // Try implicit name lookup (for areas)
        // If looking for "header" at start position, also try "header-start"
        // If looking for "header" at end position, also try "header-end"
        String implicitName = name + ((end == End.START) ? "-start" : "-end");
        lines = lineLookup.get(implicitName);
        if (lines != null && !lines.isEmpty()) {
            List<Integer> filteredLines = (filter != null) ? filter.apply(lines) : lines;
            if (!filteredLines.isEmpty()) {
                return getLineFromList(filteredLines, explicitTrackCount, idx);
            }
        }
        
        // Fallback: non-existent named line goes to first implicit line after explicit grid
        // See: https://github.com/w3c/csswg-drafts/issues/966#issuecomment-277042153
        if (idx > 0) {
            return (explicitTrackCount + 1) + idx;
        } else {
            return -((explicitTrackCount + 1) + (-idx));
        }
    }
    
    /**
     * Get a line from a sorted list of lines by index.
     * Positive indices count from start (1-based), negative from end.
     */
    private int getLineFromList(List<Integer> lines, int explicitTrackCount, int idx) {
        int absIdx = Math.abs(idx);
        boolean enoughLines = absIdx <= lines.size();
        
        if (enoughLines) {
            if (idx > 0) {
                return lines.get(absIdx - 1);
            } else {
                return lines.get(lines.size() - absIdx);
            }
        } else {
            // Not enough lines - extend into implicit grid
            int remainingLines = (absIdx - lines.size()) * Integer.signum(idx);
            if (idx > 0) {
                return (explicitTrackCount + 1) + remainingLines;
            } else {
                return -((explicitTrackCount + 1) + remainingLines);
            }
        }
    }
    
    /**
     * Get the number of columns defined by grid areas.
     */
    public int getAreaColumnCount() {
        return areaColumnCount;
    }
    
    /**
     * Get the number of rows defined by grid areas.
     */
    public int getAreaRowCount() {
        return areaRowCount;
    }
    
    /**
     * Set the number of explicit columns (call after track expansion).
     */
    public void setExplicitColumnCount(int count) {
        this.explicitColumnCount = count;
        addRepeatedLineNames(columnLines, style.gridTemplateColumnsWithRepeat, count);
        sortAndDeduplicate(columnLines);
    }
    
    /**
     * Set the number of explicit rows (call after track expansion).
     */
    public void setExplicitRowCount(int count) {
        this.explicitRowCount = count;
        addRepeatedLineNames(rowLines, style.gridTemplateRowsWithRepeat, count);
        sortAndDeduplicate(rowLines);
    }
    
    /**
     * Get the number of explicit columns.
     */
    public int getExplicitColumnCount() {
        return explicitColumnCount;
    }
    
    /**
     * Get the number of explicit rows.
     */
    public int getExplicitRowCount() {
        return explicitRowCount;
    }
    
    /**
     * Check if there are any named lines or areas defined.
     */
    public boolean hasNamedLines() {
        return !rowLines.isEmpty() || !columnLines.isEmpty() || !areas.isEmpty();
    }

    /** Return a copy of row line names keyed by their one-based explicit line index. */
    public Map<Integer, List<String>> getRowLineNamesByIndex() {
        return lineNamesByIndex(rowLines);
    }

    /** Return a copy of column line names keyed by their one-based explicit line index. */
    public Map<Integer, List<String>> getColumnLineNamesByIndex() {
        return lineNamesByIndex(columnLines);
    }

    private static Map<Integer, List<String>> lineNamesByIndex(Map<String, List<Integer>> lineLookup) {
        Map<Integer, List<String>> result = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : lineLookup.entrySet()) {
            for (Integer index : entry.getValue()) {
                result.computeIfAbsent(index, ignored -> new ArrayList<>()).add(entry.getKey());
            }
        }
        for (List<String> names : result.values()) {
            Collections.sort(names);
        }
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NamedLineResolver {\n");
        
        if (!areas.isEmpty()) {
            sb.append("  Areas:\n");
            for (GridTemplateArea area : areas.values()) {
                sb.append("    ").append(area.getName())
                  .append(": row ").append(area.getRowStart()).append("/").append(area.getRowEnd())
                  .append(", col ").append(area.getColumnStart()).append("/").append(area.getColumnEnd())
                  .append("\n");
            }
        }
        
        if (!rowLines.isEmpty()) {
            sb.append("  Row Lines:\n");
            for (Map.Entry<String, List<Integer>> entry : rowLines.entrySet()) {
                sb.append("    ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        
        if (!columnLines.isEmpty()) {
            sb.append("  Column Lines:\n");
            for (Map.Entry<String, List<Integer>> entry : columnLines.entrySet()) {
                sb.append("    ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        
        sb.append("  Explicit: ").append(explicitColumnCount).append(" cols x ").append(explicitRowCount).append(" rows\n");
        sb.append("}");
        return sb.toString();
    }
}
