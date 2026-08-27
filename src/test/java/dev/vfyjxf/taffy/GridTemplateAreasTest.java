package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.CssParser;
import dev.vfyjxf.taffy.style.GridPlacement;
import dev.vfyjxf.taffy.style.GridTemplateArea;
import dev.vfyjxf.taffy.style.GridTemplateAreas;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.DetailedGridInfo;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GridTemplateAreasTest {
    @Test
    void parserPreservesUnnamedTemplateCells() {
        GridTemplateAreas areas = CssParser.parseGridTemplateAreas("\"hero hero .\" \"sidebar main main\"");
        assertEquals(2, areas.rowCount());
        assertEquals(3, areas.columnCount());
        assertEquals(3, areas.areas().size());
        GridTemplateArea hero = areas.areas().get(0);
        assertEquals("hero", hero.getName());
        assertEquals(1, hero.getRowStart());
        assertEquals(2, hero.getRowEnd());
        assertEquals(1, hero.getColumnStart());
        assertEquals(3, hero.getColumnEnd());
        assertThrows(IllegalArgumentException.class,
            () -> CssParser.parseGridTemplateAreas("\"a .\" \"a\""));
        assertThrows(IllegalArgumentException.class,
            () -> CssParser.parseGridTemplateAreas("\"a .\" \". a\""));
    }

    @Test
    void unnamedCellsCreateExplicitAutoTracks() {
        TaffyStyle gridStyle = new TaffyStyle();
        gridStyle.display = TaffyDisplay.GRID;
        gridStyle.size = new TaffySize<>(TaffyDimension.length(300f), TaffyDimension.length(100f));
        gridStyle.gridTemplateAreas = CssParser.parseGridTemplateAreas("\"hero . .\"");

        TaffyStyle childStyle = new TaffyStyle();
        childStyle.gridColumn = new TaffyLine<>(
            GridPlacement.namedLine("hero-start"), GridPlacement.namedLine("hero-end"));
        childStyle.gridRow = new TaffyLine<>(
            GridPlacement.namedLine("hero-start"), GridPlacement.namedLine("hero-end"));

        TaffyTree tree = new TaffyTree();
        NodeId child = tree.newLeaf(childStyle);
        NodeId root = tree.newWithChildren(gridStyle, child);
        tree.computeLayout(root, new TaffySize<>(AvailableSpace.definite(300f), AvailableSpace.definite(100f)));

        DetailedGridInfo details = tree.getDetailedLayoutInfo(root).grid();
        assertEquals(3, details.columns().counts().explicit);
        assertEquals(100f, tree.getLayout(child).size().width, 0.001f);
        assertEquals(0f, tree.getLayout(child).location().x, 0.001f);
    }
}
