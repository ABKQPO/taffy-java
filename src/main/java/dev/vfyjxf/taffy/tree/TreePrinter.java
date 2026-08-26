package dev.vfyjxf.taffy.tree;

import java.io.IOException;

/** Writes the debug representation of a tree using the low-level PrintTree contract. */
public class TreePrinter {
    private TreePrinter() {
    }

    public static void writeTree(Appendable writer, PrintTree tree, NodeId root) throws IOException {
        writeNode(writer, tree, root, false, "");
    }

    private static void writeNode(
        Appendable writer,
        PrintTree tree,
        NodeId node,
        boolean hasSibling,
        String prefix) throws IOException {
        Layout layout = tree.getFinalLayout(node);
        String fork = hasSibling ? "|-- " : "`-- ";
        writer.append(prefix)
            .append(fork)
            .append(tree.getDebugLabel(node))
            .append(" [x: ").append(format(layout.location().x))
            .append(" y: ").append(format(layout.location().y))
            .append(" w: ").append(format(layout.size().width))
            .append(" h: ").append(format(layout.size().height))
            .append(" overflow: l:").append(format(layout.scrollableOverflowRect().left))
            .append(" r:").append(format(layout.scrollableOverflowRect().right))
            .append(" t:").append(format(layout.scrollableOverflowRect().top))
            .append(" b:").append(format(layout.scrollableOverflowRect().bottom))
            .append("] (").append(node.toString()).append(")\n");

        String childPrefix = prefix + (hasSibling ? "|   " : "    ");
        int childCount = tree.childCount(node);
        for (int index = 0; index < childCount; index++) {
            writeNode(writer, tree, tree.getChildId(node, index), index < childCount - 1, childPrefix);
        }
    }

    private static String format(float value) {
        return Float.isNaN(value) ? "NaN" : Float.toString(value);
    }
}
