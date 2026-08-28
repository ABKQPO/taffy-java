package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.FloatRect;
import dev.vfyjxf.taffy.geometry.TaffyRect;
import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.CompactLength;
import dev.vfyjxf.taffy.style.CompactLengthData;
import dev.vfyjxf.taffy.style.CalcExpressionCodec;
import dev.vfyjxf.taffy.style.Contain;
import dev.vfyjxf.taffy.style.CoreStyle;
import dev.vfyjxf.taffy.style.CalcExpression;
import dev.vfyjxf.taffy.style.CalcValueResolver;
import dev.vfyjxf.taffy.style.CssParser;
import dev.vfyjxf.taffy.style.FromCss;
import dev.vfyjxf.taffy.style.CustomIdentCodec;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.GenericGridPlacement;
import dev.vfyjxf.taffy.style.GridPlacement;
import dev.vfyjxf.taffy.style.GenericGridRepetition;
import dev.vfyjxf.taffy.style.GenericGridTemplateComponent;
import dev.vfyjxf.taffy.style.GenericGridTemplateTracks;
import dev.vfyjxf.taffy.style.LengthPercentage;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.Style;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.style.TrackSizingFunction;
import dev.vfyjxf.taffy.tree.CacheTree;
import dev.vfyjxf.taffy.tree.CachedLayoutComputeFunc;
import dev.vfyjxf.taffy.tree.GenericDetailedGridInfo;
import dev.vfyjxf.taffy.tree.GenericLayoutPartialTree;
import dev.vfyjxf.taffy.tree.GenericLayoutBlockContainer;
import dev.vfyjxf.taffy.tree.GenericLayoutFlexboxContainer;
import dev.vfyjxf.taffy.tree.GenericLayoutGridContainer;
import dev.vfyjxf.taffy.tree.LayoutAlgorithms;
import dev.vfyjxf.taffy.tree.LayoutComputer;
import dev.vfyjxf.taffy.tree.LayoutInput;
import dev.vfyjxf.taffy.tree.LayoutOutput;
import dev.vfyjxf.taffy.tree.Layout;
import dev.vfyjxf.taffy.tree.LayoutBlockContainer;
import dev.vfyjxf.taffy.tree.LayoutFlexboxContainer;
import dev.vfyjxf.taffy.tree.LayoutGridContainer;
import dev.vfyjxf.taffy.tree.LayoutPartialTree;
import dev.vfyjxf.taffy.tree.TreeLayoutComputeFunc;
import dev.vfyjxf.taffy.tree.TreeCachedLayoutComputeFunc;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.Baselines;
import dev.vfyjxf.taffy.tree.RequestedAxis;
import dev.vfyjxf.taffy.tree.RunMode;
import dev.vfyjxf.taffy.tree.SizingMode;
import dev.vfyjxf.taffy.tree.TaffyTree;
import dev.vfyjxf.taffy.util.GenericNodeMeasureFunc;
import dev.vfyjxf.taffy.util.NodeLayoutMeasureFunc;
import dev.vfyjxf.taffy.util.NodeMeasureFunc;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class CurrentTaffyApiParityTest {
    @Test
    void genericStyleNormalizesCustomIdentifiersForTheRuntimeLayoutTree() {
        CustomIdentCodec<Identifier> codec = CustomIdentCodec.of(Identifier::value, Identifier::new);
        Style<Identifier> style = new Style<>(codec);
        style.base().display = TaffyDisplay.GRID;
        style.setGridColumn(new TaffyLine<>(GenericGridPlacement.namedLine(new Identifier("start")),
            GenericGridPlacement.namedSpan(new Identifier("end"), 2)));
        style.setGridTemplateColumnNameGroups(List.of(List.of(new Identifier("start")), List.of(new Identifier("end"))));

        TaffyStyle runtime = style.toTaffyStyle();

        assertEquals("start", runtime.getGridColumnStart().getLineName());
        assertEquals("end", runtime.getGridColumnEnd().getLineName());
        assertEquals(List.of(List.of("start"), List.of("end")), runtime.gridTemplateColumnNameGroups);
    }

    @Test
    void taffyTreeAcceptsGenericStyleDirectly() {
        Style<Identifier> style = new Style<>(CustomIdentCodec.of(Identifier::value, Identifier::new));
        style.setGridRow(new TaffyLine<>(GenericGridPlacement.namedLine(new Identifier("row")), GenericGridPlacement.auto()));
        TaffyTree tree = new TaffyTree();

        NodeId node = tree.newLeaf(style);

        assertEquals("row", tree.getStyle(node).getGridRowStart().getLineName());
    }

    @Test
    void lowLevelLayoutAcceptsATypedGenericStyleTree() {
        NodeId node = NodeId.of(41L);
        Style<Identifier> style = new Style<>(CustomIdentCodec.of(Identifier::value, Identifier::new));
        style.base().size = new TaffySize<>(TaffyDimension.length(23f), TaffyDimension.length(29f));
        GenericStyleTree tree = new GenericStyleTree(node, style);

        LayoutOutput output = LayoutAlgorithms.computeLeafLayout(tree, node, sizingInput());

        assertEquals(23f, output.size().width, 0.001f);
        assertEquals(29f, output.size().height, 0.001f);
    }

    @Test
    void typedTreeMeasureCallbacksReceiveTheCallersGenericStyle() {
        NodeId node = NodeId.of(42L);
        CustomIdentCodec<Identifier> codec = CustomIdentCodec.of(Identifier::value, Identifier::new);
        Style<Identifier> style = new Style<>(codec);
        style.setGridColumn(new TaffyLine<>(GenericGridPlacement.namedLine(new Identifier("typed")),
            GenericGridPlacement.auto()));
        GenericStyleTree tree = new GenericStyleTree(node, style);
        GenericNodeMeasureFunc<Identifier, Object> measure =
            (knownDimensions, availableSpace, measuredNode, context, measuredStyle) -> {
                assertSame(node, measuredNode);
                assertEquals(new Identifier("typed"), measuredStyle.gridColumn().start.identifier());
                return new FloatSize(31f, 37f);
            };

        LayoutOutput output = LayoutAlgorithms.computeLeafLayout(tree, node, sizingInput(), measure);

        assertEquals(31f, output.size().width, 0.001f);
        assertEquals(37f, output.size().height, 0.001f);
    }

    @Test
    void typedTreeRootLayoutRetainsGenericStylesInMeasureCallbacks() {
        NodeId node = NodeId.of(43L);
        CustomIdentCodec<Identifier> codec = CustomIdentCodec.of(Identifier::value, Identifier::new);
        Style<Identifier> style = new Style<>(codec);
        style.setGridRow(new TaffyLine<>(GenericGridPlacement.namedLine(new Identifier("root")),
            GenericGridPlacement.auto()));
        GenericStyleTree tree = new GenericStyleTree(node, style);
        GenericNodeMeasureFunc<Identifier, Object> measure =
            (knownDimensions, availableSpace, measuredNode, context, measuredStyle) -> {
                assertEquals(new Identifier("root"), measuredStyle.gridRow().start.identifier());
                return new FloatSize(41f, 43f);
            };

        LayoutOutput output = LayoutAlgorithms.computeRootLayout(tree, node, TaffySize.maxContent(), measure);

        assertEquals(41f, output.size().width, 0.001f);
        assertEquals(43f, output.size().height, 0.001f);
    }

    @Test
    void genericStyleIsAcceptedByEveryHighLevelNodeCreationEntryPoint() {
        CustomIdentCodec<Identifier> codec = CustomIdentCodec.of(Identifier::value, Identifier::new);
        Style<Identifier> style = new Style<>(codec);
        style.setGridColumn(new TaffyLine<>(GenericGridPlacement.namedLine(new Identifier("column")), GenericGridPlacement.auto()));
        TaffyTree tree = new TaffyTree();

        NodeId child = tree.newLeaf(style);
        NodeId parent = tree.newWithChildren(style, child);
        NodeId measured = tree.newLeafWithMeasure(style, (knownDimensions, availableSpace) -> new FloatSize(3f, 5f));
        NodeId contextual = tree.newLeafWithContext(style, "context");

        assertEquals("column", tree.getStyle(parent).getGridColumnStart().getLineName());
        assertEquals("column", tree.getStyle(measured).getGridColumnStart().getLineName());
        assertEquals("context", tree.getNodeContext(contextual));
    }

    @Test
    void genericStyleDecodesCustomIdentifiersFromDetailedGridInfo() {
        CustomIdentCodec<Identifier> codec = CustomIdentCodec.of(Identifier::value, Identifier::new);
        Style<Identifier> style = new Style<>(codec);
        style.base().display = TaffyDisplay.GRID;
        style.base().gridTemplateRows.add(TrackSizingFunction.fixed(10f));
        style.setGridTemplateColumnsWithRepeat(List.of(GenericGridTemplateComponent.repeat(
            GenericGridRepetition.count(1, List.of(TrackSizingFunction.fixed(10f)),
                List.of(List.of(new Identifier("column-start")), List.of(new Identifier("column-end")))))));
        TaffyTree tree = new TaffyTree();
        NodeId node = tree.newLeaf(style);

        tree.computeLayout(node, TaffySize.maxContent());
        GenericDetailedGridInfo<Identifier> detail = tree.getDetailedGridInfo(node, codec);

        assertEquals(List.of(new Identifier("column-start")), detail.columns().namesForLine(0));
        assertEquals(List.of(new Identifier("column-end")), detail.columns().namesForLine(1));
    }

    @Test
    void genericStyleNormalizesNamedLinesInsideGridRepeats() {
        CustomIdentCodec<Identifier> codec = CustomIdentCodec.of(Identifier::value, Identifier::new);
        Style<Identifier> style = new Style<>(codec);
        style.setGridTemplateColumnsWithRepeat(List.of(GenericGridTemplateComponent.repeat(
            GenericGridRepetition.count(2, List.of(TrackSizingFunction.fixed(10f)),
                List.of(List.of(new Identifier("repeat-start")), List.of(new Identifier("repeat-end")))))));

        TaffyStyle runtime = style.toTaffyStyle();

        assertEquals(List.of(List.of("repeat-start"), List.of("repeat-end")),
            runtime.gridTemplateColumnsWithRepeat.get(0).getRepeat().getLineNames());
    }

    @Test
    void genericCssParsingPreservesApplicationDefinedGridIdentifiers() {
        CustomIdentCodec<Identifier> codec = CustomIdentCodec.of(Identifier::value, Identifier::new);
        GenericGridPlacement<Identifier> placement = CssParser.parseGenericGridPlacement("header -2", codec);
        GenericGridTemplateTracks<Identifier> tracks = CssParser.parseGenericGridTemplateTracks(
            "[start] repeat(2, 10px [end]) [tail]", codec);
        Style<Identifier> style = new Style<>(codec);
        style.setGridTemplateColumns(tracks);

        assertEquals(new Identifier("header"), placement.identifier());
        assertEquals(-2, placement.nthIndex());
        assertEquals(List.of(new Identifier("start")), tracks.lineNames().get(0));
        assertEquals(List.of(new Identifier("tail")), tracks.lineNames().get(1));
        assertEquals(List.of(List.of("start"), List.of("tail")), style.toTaffyStyle().gridTemplateColumnNameGroups);
        assertEquals(List.of(List.of(), List.of("end")),
            style.toTaffyStyle().gridTemplateColumnsWithRepeat.get(0).getRepeat().getLineNames());
    }

    @Test
    void genericStyleRecoversCustomIdentifiersFromRuntimeStyle() {
        CustomIdentCodec<Identifier> codec = CustomIdentCodec.of(Identifier::value, Identifier::new);
        Style<Identifier> source = new Style<>(codec);
        source.setGridColumn(new TaffyLine<>(GenericGridPlacement.namedLine(new Identifier("start")),
            GenericGridPlacement.namedSpan(new Identifier("end"), 2)));
        source.setGridTemplateColumns(new GenericGridTemplateTracks<>(
            List.of(GenericGridTemplateComponent.repeat(GenericGridRepetition.count(2,
                List.of(TrackSizingFunction.fixed(10f)),
                List.of(List.of(new Identifier("repeat-start")), List.of(new Identifier("repeat-end")))))),
            List.of(List.of(new Identifier("outer-start")), List.of(new Identifier("outer-end")))));

        Style<Identifier> restored = Style.fromTaffyStyle(source.toTaffyStyle(), codec);

        assertEquals(new Identifier("start"), restored.gridColumn().start.identifier());
        assertEquals(new Identifier("end"), restored.gridColumn().end.identifier());
        assertEquals(List.of(new Identifier("outer-start")), restored.gridTemplateColumnNameGroups().get(0));
        assertEquals(List.of(new Identifier("repeat-start")),
            restored.gridTemplateColumnsWithRepeat().get(0).repeat().lineNames().get(0));
    }

    @Test
    void genericStyleHasTheRustDefaultIdentifierTypeAndDeepCopySemantics() {
        Style<String> defaultStyle = Style.defaultStyle();
        defaultStyle.setGridColumn(new TaffyLine<>(GenericGridPlacement.namedLine("start"),
            GenericGridPlacement.auto()));
        defaultStyle.base().display = TaffyDisplay.GRID;

        Style<String> copy = defaultStyle.copy();
        copy.base().display = TaffyDisplay.BLOCK;
        copy.setGridColumn(new TaffyLine<>(GenericGridPlacement.namedLine("changed"),
            GenericGridPlacement.auto()));

        assertEquals(TaffyDisplay.GRID, defaultStyle.base().display);
        assertEquals("start", defaultStyle.gridColumn().start.identifier());
        assertEquals(TaffyDisplay.BLOCK, copy.base().display);
        assertEquals("changed", copy.gridColumn().start.identifier());
    }

    @Test
    void treeCanRecoverAStoredGenericStyleWithACallersCodec() {
        CustomIdentCodec<Identifier> codec = CustomIdentCodec.of(Identifier::value, Identifier::new);
        Style<Identifier> source = new Style<>(codec);
        source.setGridRow(new TaffyLine<>(GenericGridPlacement.namedLine(new Identifier("header")),
            GenericGridPlacement.auto()));
        TaffyTree tree = new TaffyTree();
        NodeId node = tree.newLeaf(source);

        Style<Identifier> restored = tree.getStyle(node, codec);

        assertEquals(new Identifier("header"), restored.gridRow().start.identifier());
    }

    @Test
    void cssParserBuildsAGenericStyleFromIndividualCssDeclarations() {
        Style<String> style = CssParser.parseStyle(Map.of(
            "display", "grid",
            "width", "120px",
            "height", "50%",
            "grid-template-columns", "[start] 40px [end]",
            "grid-column", "start / end"), CustomIdentCodec.strings());

        assertEquals(TaffyDisplay.GRID, style.base().display);
        assertEquals(TaffyDimension.length(120f), style.base().size.width);
        assertEquals(TaffyDimension.percent(0.5f), style.base().size.height);
        assertEquals("start", style.gridColumn().start.identifier());
        assertEquals(List.of(List.of("start"), List.of("end")), style.gridTemplateColumnNameGroups());
    }

    @Test
    void compactLengthRoundTripsPublicSizingValues() {
        CompactLength length = CompactLength.length(12.5f);
        CompactLength percent = CompactLength.percent(0.25f);
        CompactLength fitContent = CompactLength.fitContentPercent(0.8f);

        assertEquals(CompactLength.LENGTH_TAG, length.tag());
        assertEquals(12.5f, length.value(), 0.001f);
        assertEquals(LengthPercentage.percent(0.25f), percent.toLengthPercentage());
        assertEquals(TaffyDimension.fitContent(LengthPercentage.percent(0.8f)), fitContent.toDimension());
    }

    @Test
    void compactLengthExposesTheCompleteTrackSizingClassificationContract() {
        CompactLength calc = CompactLength.calc(basis -> 17f);
        CompactLength auto = CompactLength.auto();
        CompactLength minContent = CompactLength.minContent();
        CompactLength maxContent = CompactLength.maxContent();
        CompactLength fitContent = CompactLength.fitContentPx(24f);
        CompactLength stretch = CompactLength.stretch();

        assertEquals(CompactLength.length(0f), CompactLength.ZERO);
        assertEquals(true, CompactLength.fr(1f).isFr());
        assertEquals(true, minContent.isMinOrMaxContent());
        assertEquals(true, maxContent.isMaxOrFitContent());
        assertEquals(true, fitContent.isMaxOrFitContent());
        assertEquals(true, auto.isMaxContentAlike());
        assertEquals(true, fitContent.isMaxContentAlike());
        assertEquals(true, minContent.isIntrinsic());
        assertEquals(true, fitContent.isIntrinsic());
        assertEquals(true, calc.usesPercentage());
        assertEquals(true, CompactLength.percent(0.5f).usesPercentage());
        assertEquals(true, stretch.isSizingKeyword());
        assertEquals(false, CompactLength.content().isSizingKeyword());
        assertEquals(50f, CompactLength.percent(0.5f).resolvedPercentageSize(100f, null), 0.001f);
        assertEquals(17f, calc.resolvedPercentageSize(100f, (expression, basis) -> 17f), 0.001f);
        assertEquals(null, CompactLength.length(10f).resolvedPercentageSize(100f, null));
    }

    @Test
    void compactLengthRoundTripsEveryPublicSizingValueFamily() {
        CalcExpression calc = basis -> basis * 0.25f;

        assertEquals(CompactLength.length(8f), CompactLength.from(LengthPercentage.length(8f)));
        assertEquals(CompactLength.percent(0.5f), CompactLength.from(LengthPercentageAuto.percent(0.5f)));
        assertEquals(CompactLength.calc(calc), CompactLength.from(TaffyDimension.calc(calc)));
        assertEquals(LengthPercentage.length(8f), CompactLength.length(8f).toLengthPercentage());
        assertEquals(LengthPercentageAuto.fitContent(LengthPercentage.percent(0.8f)),
            CompactLength.fitContentPercent(0.8f).toLengthPercentageAuto());
        assertEquals(TaffyDimension.stretch(), CompactLength.stretch().toDimension());
        assertEquals(CompactLength.length(8f), LengthPercentage.length(8f).toCompactLength());
        assertEquals(CompactLength.maxContent(), LengthPercentageAuto.maxContent().toCompactLength());
        assertEquals(CompactLength.content(), TaffyDimension.content().toCompactLength());
    }

    @Test
    void gridTrackCalcUsesTheTreeCalcResolutionHook() {
        NodeId node = NodeId.of(11L);
        NodeId child = NodeId.of(12L);
        TaffyStyle style = new TaffyStyle();
        style.display = TaffyDisplay.GRID;
        style.size = new TaffySize<>(TaffyDimension.length(100f), TaffyDimension.length(10f));
        style.gridTemplateColumns.add(TrackSizingFunction.fixed(LengthPercentage.calc(basis -> -1f)));
        style.gridTemplateRows.add(TrackSizingFunction.fixed(10f));
        GridCalcTree tree = new GridCalcTree(node, child, style, new TaffyStyle());

        new LayoutComputer(tree, null).computeLayoutWithOutput(node, new TaffySize<>(
            AvailableSpace.definite(100f), AvailableSpace.definite(10f)));

        assertEquals(42f, tree.getUnroundedLayout(child).size().width, 0.001f);
    }

    @Test
    void cachedLayoutCallbackReceivesTheCallersCacheInstance() {
        CallbackCache cache = new CallbackCache();
        AtomicReference<CacheTree> received = new AtomicReference<>();
        NodeId node = NodeId.of(7L);
        LayoutOutput expected = LayoutOutput.fromOuterSize(new FloatSize(9f, 11f));
        CachedLayoutComputeFunc<CallbackCache> compute = (ownedCache, ignoredNode, ignoredInput) -> {
            received.set(ownedCache);
            return expected;
        };

        LayoutOutput actual = LayoutAlgorithms.computeCachedLayout(cache, node, sizingInput(), compute);

        assertSame(cache, received.get());
        assertSame(expected, actual);
    }

    @Test
    void leafLayoutUsesTheTreeCalcResolutionHook() {
        NodeId node = NodeId.of(8L);
        TaffyStyle style = new TaffyStyle();
        style.size = new TaffySize<>(
            TaffyDimension.calc(basis -> -1f),
            TaffyDimension.length(5f));
        CalcTree tree = new CalcTree(node, style);

        LayoutOutput output = LayoutAlgorithms.computeLeafLayout(tree, node, sizingInput(new FloatSize(100f, 100f)));

        assertEquals(42f, output.size().width, 0.001f);
        assertEquals(5f, output.size().height, 0.001f);
    }

    @Test
    void lowLevelTreeCanAdaptAnApplicationOwnedCoreStyle() {
        NodeId node = NodeId.of(9L);
        TaffyStyle runtime = new TaffyStyle();
        runtime.size = new TaffySize<>(TaffyDimension.length(13f), TaffyDimension.length(17f));
        CoreStyle applicationStyle = () -> runtime;
        CoreStyleTree tree = new CoreStyleTree(node, applicationStyle);

        LayoutOutput output = LayoutAlgorithms.computeLeafLayout(tree, node, sizingInput());

        assertEquals(13f, output.size().width, 0.001f);
        assertEquals(17f, output.size().height, 0.001f);
    }

    @Test
    void rootLayoutUsesTheTreeCalcResolutionHook() {
        NodeId node = NodeId.of(10L);
        TaffyStyle style = new TaffyStyle();
        style.display = TaffyDisplay.BLOCK;
        style.size = new TaffySize<>(TaffyDimension.calc(basis -> -1f), TaffyDimension.length(5f));
        CalcTree tree = new CalcTree(node, style);

        LayoutOutput output = new LayoutComputer(tree, null)
            .computeLayoutWithOutput(node, TaffySize.maxContent());

        assertEquals(42f, output.size().width, 0.001f);
        assertEquals(5f, output.size().height, 0.001f);
    }

    @Test
    void highLevelMeasureEntryPointAcceptsNodeContextCallbacks() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle style = new TaffyStyle();
        FloatSize context = new FloatSize(19f, 23f);
        NodeId node = tree.newLeafWithContext(style, context);
        NodeMeasureFunc<FloatSize> measure = (knownDimensions, availableSpace, nodeId, nodeContext, nodeStyle) -> nodeContext.copy();

        tree.computeLayoutWithMeasure(node, TaffySize.maxContent(), measure);

        assertEquals(19f, tree.getLayout(node).size().width, 0.001f);
        assertEquals(23f, tree.getLayout(node).size().height, 0.001f);
    }

    @Test
    void highLevelMeasureEntryPointAcceptsCompleteLayoutOutputCallbacks() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle style = new TaffyStyle();
        style.display = TaffyDisplay.GRID;
        NodeId node = tree.newLeafWithContext(style, "context");
        NodeLayoutMeasureFunc<String> measure = (input, measuredNode, context, measuredStyle) -> {
            assertSame(node, measuredNode);
            assertEquals("context", context);
            assertEquals(RunMode.PERFORM_LAYOUT, input.runMode());
            return LayoutOutput.fromSizesAndOverflow(
                new FloatSize(19f, 23f),
                new FloatSize(31f, 37f),
                new FloatRect(-2f, 0f, 31f, 37f),
                new Baselines(5f, 17f));
        };

        tree.computeLayoutWithMeasure(node, TaffySize.maxContent(), measure);

        Layout layout = tree.getUnroundedLayout(node);
        assertEquals(19f, layout.size().width, 0.001f);
        assertEquals(31f, layout.contentSize().width, 0.001f);
        assertEquals(-2f, layout.scrollableOverflowRect().left, 0.001f);
        assertEquals(17f, layout.baselines().last(), 0.001f);
    }

    @Test
    void compactLengthHasAStableDataFormIncludingCallerOwnedCalcValues() {
        CalcExpression expression = basis -> basis * 0.25f;
        CalcExpressionCodec codec = CalcExpressionCodec.of(value -> "quarter", key -> expression);

        CompactLengthData numeric = CompactLength.percent(0.5f).toData();
        CompactLengthData calc = CompactLength.calc(expression).toData(codec);

        assertEquals(CompactLength.PERCENT_TAG, numeric.tag());
        assertEquals(0.5f, numeric.value(), 0.001f);
        assertEquals(CompactLength.percent(0.5f), CompactLength.fromData(numeric));
        assertEquals("quarter", calc.calcKey());
        assertEquals(CompactLength.calc(expression), CompactLength.fromData(calc, codec));
    }

    @Test
    void publicFromCssContractComposesExistingTypedParsers() {
        FromCss<TaffyDimension> dimensions = CssParser.dimensionParser();
        FromCss<Style<String>> styles = CssParser.styleParser(CustomIdentCodec.strings());

        assertEquals(TaffyDimension.fitContent(LengthPercentage.percent(0.5f)),
            dimensions.fromCss("fit-content(50%)"));
        assertEquals(TaffyDisplay.GRID, styles.fromCss("display: grid; grid-template-columns: [start] 10px [end]")
            .base().display);
    }

    @Test
    void publicFromCssContractCoversGridAndCommonStyleValues() {
        assertEquals(TaffyDisplay.GRID, CssParser.displayParser().fromCss("grid"));
        assertEquals(TrackSizingFunction.flex(2f), CssParser.trackSizingFunctionParser().fromCss("2fr"));
        assertEquals(GridPlacement.line(3), CssParser.gridPlacementParser().fromCss("3"));
        assertEquals(AvailableSpace.MAX_CONTENT, CssParser.availableSpaceParser().fromCss("max-content"));
        assertEquals(Contain.CONTENT, CssParser.containParser().fromCss("layout paint"));
    }

    @Test
    void lowLevelComputeCallbackRetainsTheConcreteTreeType() {
        NodeId node = NodeId.of(14L);
        TaffyStyle style = new TaffyStyle();
        CalcTree tree = new CalcTree(node, style);
        TreeLayoutComputeFunc<CalcTree> compute = (receivedTree, receivedNode, receivedInput) -> {
            assertSame(tree, receivedTree);
            assertSame(node, receivedNode);
            return LayoutOutput.fromOuterSize(new FloatSize(29f, 31f));
        };

        LayoutOutput output = LayoutAlgorithms.computeUncachedLayout(tree, node, sizingInput(), compute);

        assertEquals(29f, output.size().width, 0.001f);
        assertEquals(31f, output.size().height, 0.001f);
    }

    @Test
    void rootLayoutUsesTheCallersConcreteTreeDispatchCallback() {
        NodeId node = NodeId.of(16L);
        CalcTree tree = new CalcTree(node, new TaffyStyle());
        AtomicReference<LayoutInput> receivedInput = new AtomicReference<>();
        AtomicInteger dispatchCount = new AtomicInteger();
        TreeLayoutComputeFunc<CalcTree> dispatch = (receivedTree, receivedNode, input) -> {
            assertSame(tree, receivedTree);
            assertSame(node, receivedNode);
            receivedInput.set(input);
            dispatchCount.incrementAndGet();
            return LayoutOutput.fromOuterSize(new FloatSize(43f, 47f));
        };

        LayoutOutput output = LayoutAlgorithms.computeRootLayout(
            tree, node, TaffySize.maxContent(), dispatch);
        LayoutAlgorithms.computeRootLayout(tree, node, TaffySize.maxContent(), dispatch);

        assertEquals(RunMode.PERFORM_LAYOUT, receivedInput.get().runMode());
        assertEquals(1, dispatchCount.get());
        assertEquals(43f, output.size().width, 0.001f);
        assertEquals(47f, tree.getUnroundedLayout(node).size().height, 0.001f);
    }

    @Test
    void specializedAlgorithmsUseTheCallersConcreteTreeDispatchCallback() {
        NodeId root = NodeId.of(17L);
        NodeId child = NodeId.of(18L);
        TaffyStyle childStyle = new TaffyStyle();
        TreeLayoutComputeFunc<GridCalcTree> dispatch = (tree, node, input) -> {
            assertSame(child, node);
            return LayoutOutput.fromOuterSize(new FloatSize(11f, 13f));
        };

        TaffyStyle block = new TaffyStyle();
        block.display = TaffyDisplay.BLOCK;
        assertEquals(11f, LayoutAlgorithms.computeBlockLayout(
            new GridCalcTree(root, child, block, childStyle), root, sizingInput(), dispatch).size().width, 0.001f);

        TaffyStyle flex = new TaffyStyle();
        flex.display = TaffyDisplay.FLEX;
        assertEquals(11f, LayoutAlgorithms.computeFlexboxLayout(
            new GridCalcTree(root, child, flex, childStyle), root, sizingInput(), dispatch).size().width, 0.001f);

        TaffyStyle grid = new TaffyStyle();
        grid.display = TaffyDisplay.GRID;
        assertEquals(11f, LayoutAlgorithms.computeGridLayout(
            new GridCalcTree(root, child, grid, childStyle), root, sizingInput(), dispatch).size().width, 0.001f);
    }

    @Test
    void cachedLowLevelComputeCallbackRetainsTheConcreteTreeType() {
        NodeId node = NodeId.of(15L);
        TaffyStyle style = new TaffyStyle();
        CalcTree tree = new CalcTree(node, style);
        TreeCachedLayoutComputeFunc<CalcTree> compute = (receivedTree, receivedNode, receivedInput) -> {
            assertSame(tree, receivedTree);
            assertSame(node, receivedNode);
            assertEquals(42f, receivedTree.resolveCalcValue(basis -> basis, 1f), 0.001f);
            return LayoutOutput.fromOuterSize(new FloatSize(37f, 41f));
        };

        LayoutOutput first = LayoutAlgorithms.computeCachedLayout(tree, node, sizingInput(), compute);
        LayoutOutput second = LayoutAlgorithms.computeCachedLayout(tree, node, sizingInput(), compute);

        assertSame(first, second);
        assertEquals(37f, second.size().width, 0.001f);
    }

    @Test
    void leafLayoutAcceptsAnExplicitCalcResolver() {
        NodeId node = NodeId.of(19L);
        TaffyStyle style = new TaffyStyle();
        style.padding = TaffyRect.all(LengthPercentage.calc(basis -> basis * 0.1f));
        CalcTree tree = new CalcTree(node, style);
        CalcValueResolver resolver = (expression, basis) -> 3f;

        LayoutOutput output = LayoutAlgorithms.computeLeafLayout(
            tree, node, sizingInput(), resolver,
            (knownDimensions, availableSpace) -> new FloatSize(20f, 10f));

        assertEquals(26f, output.size().width, 0.001f);
        assertEquals(16f, output.size().height, 0.001f);
    }

    @Test
    void treeCalcHookDrivesFlexContainerAndItemStyles() {
        assertTreeCalcHookDrivesContainerAndItemStyles(TaffyDisplay.FLEX);
    }

    @Test
    void treeCalcHookDrivesBlockContainerAndItemStyles() {
        assertTreeCalcHookDrivesContainerAndItemStyles(TaffyDisplay.BLOCK);
    }

    @Test
    void treeCalcHookDrivesGridContainerAndItemStyles() {
        assertTreeCalcHookDrivesContainerAndItemStyles(TaffyDisplay.GRID);
    }

    @Test
    void genericSpecializedTreeContractsKeepTypedStylesAtAlgorithmBoundaries() {
        NodeId root = NodeId.of(20L);
        NodeId child = NodeId.of(21L);
        Style<Identifier> container = new Style<>(new TaffyStyle(), CustomIdentCodec.of(
            Identifier::value, Identifier::new));
        container.base().display = TaffyDisplay.GRID;
        container.setGridTemplateColumnsWithRepeat(List.of(GenericGridTemplateComponent.repeat(
            GenericGridRepetition.count(1, List.of(TrackSizingFunction.fixed(10f)),
                List.of(List.of(new Identifier("start")), List.of(new Identifier("end")))))));
        container.setGridTemplateRowsWithRepeat(List.of(GenericGridTemplateComponent.single(
            TrackSizingFunction.fixed(10f))));
        Style<Identifier> item = container.copy();
        GenericSpecializedTree tree = new GenericSpecializedTree(root, child, container, item);

        assertSame(container, tree.getGenericFlexboxContainerStyle(root));
        assertSame(item, tree.getGenericFlexboxChildStyle(child));
        assertSame(container, tree.getGenericGridContainerStyle(root));
        assertSame(item, tree.getGenericGridChildStyle(child));
        assertSame(container, tree.getGenericBlockContainerStyle(root));
        assertSame(item, tree.getGenericBlockChildStyle(child));
        assertEquals(10f, LayoutAlgorithms.computeGridLayout(tree, root, sizingInput()).size().width, 0.001f);
        assertEquals(List.of(new Identifier("start")), tree.detailedGridInfo.columns().namesForLine(0));
    }

    private LayoutInput sizingInput() {
        return sizingInput(FloatSize.none());
    }

    private LayoutInput sizingInput(FloatSize parentSize) {
        return new LayoutInput(
            RunMode.COMPUTE_SIZE,
            SizingMode.INHERENT_SIZE,
            RequestedAxis.BOTH,
            FloatSize.none(),
            new TaffySize<>(false, false),
            parentSize,
            TaffySize.maxContent(),
            new TaffyLine<>(false, false));
    }

    private void assertTreeCalcHookDrivesContainerAndItemStyles(TaffyDisplay display) {
        CalcTaffyTree tree = new CalcTaffyTree(42f);
        TaffyStyle itemStyle = new TaffyStyle();
        itemStyle.size = new TaffySize<>(TaffyDimension.calc(basis -> 1f), TaffyDimension.calc(basis -> 1f));

        TaffyStyle containerStyle = new TaffyStyle();
        containerStyle.display = display;
        containerStyle.size = new TaffySize<>(TaffyDimension.calc(basis -> 1f), TaffyDimension.calc(basis -> 1f));

        NodeId item = tree.newLeaf(itemStyle);
        NodeId container = tree.newWithChildren(containerStyle, item);
        tree.computeLayout(container, TaffySize.maxContent());

        assertEquals(42f, tree.getLayout(container).size().width, 0.001f);
        assertEquals(42f, tree.getLayout(container).size().height, 0.001f);
        assertEquals(42f, tree.getLayout(item).size().width, 0.001f);
        assertEquals(42f, tree.getLayout(item).size().height, 0.001f);
    }

    private record Identifier(String value) {
    }

    private static class CalcTaffyTree extends TaffyTree {
        private final float value;

        private CalcTaffyTree(float value) {
            this.value = value;
        }

        @Override
        public float resolveCalcValue(CalcExpression expression, float basis) {
            return value;
        }
    }

    private static class CallbackCache implements CacheTree {
        private LayoutInput input;
        private LayoutOutput output;

        @Override
        public LayoutOutput cacheGet(NodeId node, LayoutInput candidate) {
            return candidate.equals(input) ? output : null;
        }

        @Override
        public void cacheStore(NodeId node, LayoutInput candidate, LayoutOutput value) {
            input = candidate;
            output = value;
        }

        @Override
        public void cacheClear(NodeId node) {
            input = null;
            output = null;
        }
    }

    private static class CalcTree implements LayoutPartialTree, CacheTree {
        private final NodeId node;
        private final TaffyStyle style;
        private Layout layout = new Layout();
        private LayoutInput cacheInput;
        private LayoutOutput cacheOutput;

        private CalcTree(NodeId node, TaffyStyle style) {
            this.node = node;
            this.style = style;
        }

        @Override
        public List<NodeId> getChildren(NodeId parent) {
            return List.of();
        }

        @Override
        public int childCount(NodeId parent) {
            return 0;
        }

        @Override
        public TaffyStyle getStyle(NodeId candidate) {
            assertSame(node, candidate);
            return style;
        }

        @Override
        public void setUnroundedLayout(NodeId candidate, Layout value) {
            layout = value;
        }

        @Override
        public Layout getUnroundedLayout(NodeId candidate) {
            return layout;
        }

        @Override
        public float resolveCalcValue(CalcExpression expression, float basis) {
            return 42f;
        }

        @Override
        public LayoutOutput cacheGet(NodeId candidate, LayoutInput input) {
            return candidate.equals(node) && input.equals(cacheInput) ? cacheOutput : null;
        }

        @Override
        public void cacheStore(NodeId candidate, LayoutInput input, LayoutOutput output) {
            if (!candidate.equals(node)) throw new IllegalArgumentException("Unknown node: " + candidate);
            cacheInput = input;
            cacheOutput = output;
        }

        @Override
        public void cacheClear(NodeId candidate) {
            if (!candidate.equals(node)) throw new IllegalArgumentException("Unknown node: " + candidate);
            cacheInput = null;
            cacheOutput = null;
        }
    }

    private static class CoreStyleTree implements LayoutPartialTree {
        private final NodeId node;
        private final CoreStyle style;
        private Layout layout = new Layout();

        private CoreStyleTree(NodeId node, CoreStyle style) {
            this.node = node;
            this.style = style;
        }

        @Override
        public List<NodeId> getChildren(NodeId parent) {
            return List.of();
        }

        @Override
        public int childCount(NodeId parent) {
            return 0;
        }

        @Override
        public CoreStyle getCoreContainerStyle(NodeId candidate) {
            assertSame(node, candidate);
            return style;
        }

        @Override
        public void setUnroundedLayout(NodeId candidate, Layout value) {
            layout = value;
        }

        @Override
        public Layout getUnroundedLayout(NodeId candidate) {
            return layout;
        }
    }

    private static class GenericStyleTree implements GenericLayoutPartialTree<Identifier> {
        private final NodeId node;
        private final Style<Identifier> style;
        private Layout layout = new Layout();

        private GenericStyleTree(NodeId node, Style<Identifier> style) {
            this.node = node;
            this.style = style;
        }

        @Override
        public Style<Identifier> getGenericStyle(NodeId candidate) {
            assertSame(node, candidate);
            return style;
        }

        @Override
        public List<NodeId> getChildren(NodeId parent) {
            return List.of();
        }

        @Override
        public int childCount(NodeId parent) {
            return 0;
        }

        @Override
        public void setUnroundedLayout(NodeId candidate, Layout value) {
            layout = value;
        }

        @Override
        public Layout getUnroundedLayout(NodeId candidate) {
            return layout;
        }
    }

    private static class GridCalcTree implements LayoutPartialTree, LayoutBlockContainer, LayoutFlexboxContainer,
        LayoutGridContainer {
        private final NodeId root;
        private final NodeId child;
        private final TaffyStyle rootStyle;
        private final TaffyStyle childStyle;
        private Layout rootLayout = new Layout();
        private Layout childLayout = new Layout();

        private GridCalcTree(NodeId root, NodeId child, TaffyStyle rootStyle, TaffyStyle childStyle) {
            this.root = root;
            this.child = child;
            this.rootStyle = rootStyle;
            this.childStyle = childStyle;
        }

        @Override
        public List<NodeId> getChildren(NodeId node) {
            return root.equals(node) ? List.of(child) : List.of();
        }

        @Override
        public int childCount(NodeId node) {
            return root.equals(node) ? 1 : 0;
        }

        @Override
        public TaffyStyle getStyle(NodeId node) {
            return root.equals(node) ? rootStyle : childStyle;
        }

        @Override
        public void setUnroundedLayout(NodeId node, Layout layout) {
            if (root.equals(node)) {
                rootLayout = layout;
            } else if (child.equals(node)) {
                childLayout = layout;
            } else {
                throw new IllegalArgumentException("Unknown node: " + node);
            }
        }

        @Override
        public Layout getUnroundedLayout(NodeId node) {
            if (root.equals(node)) return rootLayout;
            if (child.equals(node)) return childLayout;
            throw new IllegalArgumentException("Unknown node: " + node);
        }

        @Override
        public float resolveCalcValue(CalcExpression expression, float basis) {
            return 42f;
        }
    }

    private static class GenericSpecializedTree implements GenericLayoutFlexboxContainer<Identifier>,
        GenericLayoutGridContainer<Identifier>, GenericLayoutBlockContainer<Identifier> {
        private final NodeId root;
        private final NodeId child;
        private final Style<Identifier> container;
        private final Style<Identifier> item;
        private Layout rootLayout = new Layout();
        private Layout childLayout = new Layout();
        private GenericDetailedGridInfo<Identifier> detailedGridInfo;

        private GenericSpecializedTree(
            NodeId root,
            NodeId child,
            Style<Identifier> container,
            Style<Identifier> item) {
            this.root = root;
            this.child = child;
            this.container = container;
            this.item = item;
        }

        @Override
        public Style<Identifier> getGenericStyle(NodeId node) {
            return root.equals(node) ? container : item;
        }

        @Override
        public Style<Identifier> getGenericFlexboxContainerStyle(NodeId node) {
            return container;
        }

        @Override
        public Style<Identifier> getGenericFlexboxChildStyle(NodeId node) {
            return item;
        }

        @Override
        public Style<Identifier> getGenericGridContainerStyle(NodeId node) {
            return container;
        }

        @Override
        public Style<Identifier> getGenericGridChildStyle(NodeId node) {
            return item;
        }

        @Override
        public Style<Identifier> getGenericBlockContainerStyle(NodeId node) {
            return container;
        }

        @Override
        public Style<Identifier> getGenericBlockChildStyle(NodeId node) {
            return item;
        }

        @Override
        public void setGenericDetailedGridInfo(NodeId node, GenericDetailedGridInfo<Identifier> info) {
            detailedGridInfo = info;
        }

        @Override
        public List<NodeId> getChildren(NodeId node) {
            return root.equals(node) ? List.of(child) : List.of();
        }

        @Override
        public int childCount(NodeId node) {
            return root.equals(node) ? 1 : 0;
        }

        @Override
        public void setUnroundedLayout(NodeId node, Layout layout) {
            if (root.equals(node)) rootLayout = layout;
            else childLayout = layout;
        }

        @Override
        public Layout getUnroundedLayout(NodeId node) {
            return root.equals(node) ? rootLayout : childLayout;
        }
    }
}
