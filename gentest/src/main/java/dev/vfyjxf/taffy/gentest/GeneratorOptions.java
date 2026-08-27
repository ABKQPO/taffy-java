package dev.vfyjxf.taffy.gentest;

import java.nio.file.Path;

/**
 * Explicit filesystem and browser configuration for fixture generation.
 */
public record GeneratorOptions(
    Path fixturesRoot,
    Path outputRoot,
    Path chromeBinary,
    Path chromeDriver,
    String categoryFilter
) {
    /**
     * Parses generator options relative to the supplied working directory.
     */
    public static GeneratorOptions parse(Path workingDirectory, String[] args) {
        Path normalizedWorkingDirectory = workingDirectory.toAbsolutePath().normalize();
        Path fixturesRoot = normalizedWorkingDirectory.resolve("test_fixtures");
        Path outputRoot = normalizedWorkingDirectory.resolve("src/test/java/dev/vfyjxf/taffy/generated");
        Path chromeBinary = null;
        Path chromeDriver = null;
        String categoryFilter = null;

        for (int index = 0; index < args.length; index++) {
            String option = args[index];
            switch (option) {
                case "--fixturesRoot" -> fixturesRoot = resolvePath(normalizedWorkingDirectory, optionValue(args, ++index, option));
                case "--outputRoot" -> outputRoot = resolvePath(normalizedWorkingDirectory, optionValue(args, ++index, option));
                case "--chromeBinary" -> chromeBinary = resolvePath(normalizedWorkingDirectory, optionValue(args, ++index, option));
                case "--chromeDriver" -> chromeDriver = resolvePath(normalizedWorkingDirectory, optionValue(args, ++index, option));
                case "--category" -> categoryFilter = optionValue(args, ++index, option);
                default -> throw new IllegalArgumentException("Unknown generator option: " + option);
            }
        }

        return new GeneratorOptions(fixturesRoot, outputRoot, chromeBinary, chromeDriver, categoryFilter);
    }

    private static String optionValue(String[] args, int index, String option) {
        if (index >= args.length) {
            throw new IllegalArgumentException(option + " requires a value");
        }
        return args[index];
    }

    private static Path resolvePath(Path workingDirectory, String value) {
        Path path = Path.of(value);
        return (path.isAbsolute() ? path : workingDirectory.resolve(path)).normalize();
    }
}
