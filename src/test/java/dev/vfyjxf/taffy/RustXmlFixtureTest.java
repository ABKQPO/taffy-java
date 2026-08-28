package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.xml.XmlFixtureRunner;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class RustXmlFixtureTest {
    @TestFactory
    Stream<DynamicTest> runsConfiguredUpstreamXmlFixtures() throws IOException {
        String rootValue = System.getProperty("taffy.xml.root");
        if (rootValue == null || rootValue.isBlank()) return Stream.empty();
        Path root = Path.of(rootValue);
        String group = System.getProperty("taffy.xml.group");
        String name = System.getProperty("taffy.xml.name");
        Path searchRoot = group == null || group.isBlank() ? root : root.resolve(group);
        return Files.walk(searchRoot)
            .filter(path -> path.toString().endsWith(".xml"))
            .filter(path -> name == null || name.isBlank() || path.getFileName().toString().equals(name + ".xml"))
            .sorted()
            .map(path -> DynamicTest.dynamicTest(root.relativize(path).toString(), () -> XmlFixtureRunner.run(path)));
    }
}
