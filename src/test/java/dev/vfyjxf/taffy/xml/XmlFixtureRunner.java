package dev.vfyjxf.taffy.xml;

import dev.vfyjxf.taffy.geometry.TaffyPoint;
import dev.vfyjxf.taffy.geometry.TaffyRect;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.CssParser;
import dev.vfyjxf.taffy.style.LengthPercentage;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.Overflow;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.Layout;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Runs an upstream Taffy XML fixture against the Java layout tree. */
public class XmlFixtureRunner {
    private static final float EPSILON = 0.01f;

    private XmlFixtureRunner() {
    }

    public static void run(Path fixture) throws IOException, SAXException, ParserConfigurationException {
        Document document = parse(fixture);
        Element test = document.getDocumentElement();
        TaffyTree tree = new TaffyTree();
        if (!Boolean.parseBoolean(attribute(test, "use-rounding", "true"))) {
            tree.disableRounding();
        }

        Element viewport = child(test, "viewport");
        Element input = child(test, "input");
        Element expectations = child(test, "expectations");
        Element inputRoot = firstElement(input);
        Element expectedRoot = firstElement(expectations);
        NodeId root = buildTree(tree, inputRoot, expectedRoot);
        tree.computeLayout(root, new TaffySize<>(
            CssParser.parseAvailableSpace(attribute(viewport, "width", "max-content")),
            CssParser.parseAvailableSpace(attribute(viewport, "height", "max-content"))
        ));
        assertNode(tree, root, expectedRoot, fixture.toString());
    }

    private static NodeId buildTree(TaffyTree tree, Element input, Element expected) {
        List<Element> inputChildren = children(input);
        List<Element> expectedChildren = children(expected);
        if (inputChildren.size() != expectedChildren.size()) {
            throw new IllegalArgumentException("Input and expectation child counts differ");
        }
        List<NodeId> childIds = new ArrayList<>();
        for (int index = 0; index < inputChildren.size(); index++) {
            childIds.add(buildTree(tree, inputChildren.get(index), expectedChildren.get(index)));
        }
        return tree.newWithChildren(style(input), childIds);
    }

    private static TaffyStyle style(Element element) {
        TaffyStyle style = new TaffyStyle();
        style.display = CssParser.parseDisplay(attribute(element, "display", "flex"));
        style.direction = CssParser.parseDirection(attribute(element, "direction", "inherit"));
        style.position = CssParser.parsePosition(attribute(element, "position", "static"));
        style.boxSizing = CssParser.parseBoxSizing(attribute(element, "box-sizing", "border-box"));
        style.contain = CssParser.parseContain(attribute(element, "contain", "none"));
        style.floatMode = CssParser.parseFloat(attribute(element, "float", "none"));
        style.clear = CssParser.parseClear(attribute(element, "clear", "none"));
        style.overflow = new TaffyPoint<>(
            CssParser.parseOverflow(attribute(element, "overflow-x", "visible")),
            CssParser.parseOverflow(attribute(element, "overflow-y", "visible"))
        );
        style.scrollbarWidth = number(element, "scrollbar-width", 0f);
        style.size = new TaffySize<>(dimension(element, "width"), dimension(element, "height"));
        style.minSize = new TaffySize<>(lengthPercentageAuto(element, "min-width", "auto"), lengthPercentageAuto(element, "min-height", "auto"));
        style.maxSize = new TaffySize<>(lengthPercentageAuto(element, "max-width", "auto"), lengthPercentageAuto(element, "max-height", "auto"));
        style.inset = rectAuto(element, "top", "right", "bottom", "left", "auto");
        style.margin = rectAuto(element, "margin-top", "margin-right", "margin-bottom", "margin-left", "0px");
        style.padding = rectLength(element, "padding-top", "padding-right", "padding-bottom", "padding-left");
        style.border = rectLength(element, "border-top", "border-right", "border-bottom", "border-left");
        style.gap = new TaffySize<>(lengthPercentage(element, "column-gap", "0px"), lengthPercentage(element, "row-gap", "0px"));
        if (element.hasAttribute("aspect-ratio")) style.aspectRatio = number(element, "aspect-ratio", Float.NaN);
        if (element.hasAttribute("flex-direction")) style.flexDirection = CssParser.parseFlexDirection(element.getAttribute("flex-direction"));
        if (element.hasAttribute("flex-wrap")) style.flexWrap = CssParser.parseFlexWrap(element.getAttribute("flex-wrap"));
        style.flexGrow = number(element, "flex-grow", 0f);
        style.flexShrink = number(element, "flex-shrink", 1f);
        style.flexBasis = CssParser.parseDimension(attribute(element, "flex-basis", "auto"));
        return style;
    }

    private static void assertNode(TaffyTree tree, NodeId node, Element expected, String fixtureName) {
        Layout layout = tree.getLayout(node);
        assertEquals(number(expected, "x", 0f), layout.location().x, EPSILON, fixtureName + " x");
        assertEquals(number(expected, "y", 0f), layout.location().y, EPSILON, fixtureName + " y");
        assertEquals(number(expected, "width", 0f), layout.size().width, EPSILON, fixtureName + " width");
        assertEquals(number(expected, "height", 0f), layout.size().height, EPSILON, fixtureName + " height");
        List<Element> expectedChildren = children(expected);
        List<NodeId> childIds = tree.getChildren(node);
        assertEquals(expectedChildren.size(), childIds.size(), fixtureName + " child count");
        for (int index = 0; index < childIds.size(); index++) {
            assertNode(tree, childIds.get(index), expectedChildren.get(index), fixtureName);
        }
    }

    private static Document parse(Path fixture) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(fixture.toFile());
    }

    private static Element child(Element parent, String name) {
        for (Element element : children(parent)) if (element.getTagName().equals(name)) return element;
        throw new IllegalArgumentException("Missing XML element: " + name);
    }

    private static Element firstElement(Element parent) {
        List<Element> elements = children(parent);
        if (elements.isEmpty()) throw new IllegalArgumentException("Missing XML element child");
        return elements.get(0);
    }

    private static List<Element> children(Element parent) {
        NodeList nodes = parent.getChildNodes();
        List<Element> elements = new ArrayList<>();
        for (int index = 0; index < nodes.getLength(); index++) {
            Node node = nodes.item(index);
            if (node instanceof Element element) elements.add(element);
        }
        return elements;
    }

    private static String attribute(Element element, String name, String fallback) {
        return element.hasAttribute(name) ? element.getAttribute(name) : fallback;
    }

    private static float number(Element element, String name, float fallback) {
        return element.hasAttribute(name) ? Float.parseFloat(element.getAttribute(name).replace("px", "")) : fallback;
    }

    private static TaffyDimension dimension(Element element, String name) {
        return CssParser.parseDimension(attribute(element, name, "auto"));
    }

    private static LengthPercentage lengthPercentage(Element element, String name, String fallback) {
        return CssParser.parseLengthPercentage(attribute(element, name, fallback));
    }

    private static LengthPercentageAuto lengthPercentageAuto(Element element, String name, String fallback) {
        return CssParser.parseLengthPercentageAuto(attribute(element, name, fallback));
    }

    private static TaffyRect<LengthPercentageAuto> rectAuto(Element element, String top, String right, String bottom, String left, String fallback) {
        return TaffyRect.ltrb(lengthPercentageAuto(element, left, fallback), lengthPercentageAuto(element, top, fallback),
            lengthPercentageAuto(element, right, fallback), lengthPercentageAuto(element, bottom, fallback));
    }

    private static TaffyRect<LengthPercentage> rectLength(Element element, String top, String right, String bottom, String left) {
        return TaffyRect.ltrb(lengthPercentage(element, left, "0px"), lengthPercentage(element, top, "0px"),
            lengthPercentage(element, right, "0px"), lengthPercentage(element, bottom, "0px"));
    }
}
