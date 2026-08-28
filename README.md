# Taffy-Java

A pure Java implementation of the [Taffy](https://github.com/DioxusLabs/taffy) UI layout library, supporting CSS Flexbox, Grid, and Block layout algorithms.

The Java port delivers performance comparable to Rust in standard scenarios,
but experiences a noticeable drop under extreme conditions (based on benchmarks against the Rust version).

The Java port includes the upstream XML fixture runner. The configured Rust fixture audit passes all 4,420 fixtures using the same 0.1px comparison tolerance as the upstream Rust runner.

**This Java port was facilitated by AI.**

## JitPack

Add the JitPack repository and depend on the fork using the normal Maven coordinates:

```groovy
repositories {
    maven { url = 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.ABKQPO:taffy-java:1.1.4'
}
```

The library is built for the configured modern Java toolchain. Consumers should use a compatible Java runtime; the published artifact keeps the normal Maven dependency metadata.

## API Correspondence

`TaffyStyle` remains the direct runtime style type. Applications that use
non-string grid identifiers can use `Style<S>` with `CustomIdentCodec<S>` and
pass it to every `TaffyTree` creation entry point. `Style.fromTaffyStyle`
reconstructs that typed view after runtime-style handling. The tree normalizes names
only at the layout boundary. `TaffyTree.getDetailedGridInfo(node, codec)` exposes decoded
`GenericDetailedGridInfo<S>` diagnostics after Grid layout. `CompactLength` is
available as a tagged public sizing value with the same track-sizing
classification and percentage-resolution operations as Taffy. `CssParser`
can parse generic grid placements and complete grid templates through a
`CustomIdentCodec<S>`. Low-level applications can implement
`GenericLayoutPartialTree<S>` to expose `Style<S>` directly, and can override
`LayoutPartialTree.resolveCalcValue` for caller-owned calc evaluation,
including fixed grid tracks. `LayoutAlgorithms.computeRootLayout` and
`computeLeafLayout` accept `GenericNodeMeasureFunc<S, C>` for typed trees, so
application-defined grid identifiers remain available to measure callbacks.

Rust feature switches are not Java runtime features. `CssParser` is always
available as the parsing API in place of Rust's feature-gated `FromCss` and
`FromStr` implementations. JSON serialization is deliberately left to the
application's selected serialization library instead of imposing a runtime
dependency for Rust's optional `serde` support. `CompactLengthData` and
`CalcExpressionCodec` provide a stable serializer-facing representation for
the only compact internal value form. Java has no `no_std` equivalent, so the
library targets its configured Java runtime.

## Fixture Generation

The optional fixture generator uses the upstream Rust `test_fixtures` directory without assuming a sibling checkout. Point it at any Rust Taffy checkout using a Gradle property or environment variable:

```powershell
.\gradlew.bat :gentest:generateTaffyFixtures "-PtaffyFixtureRoot=<path-to-taffy>/test_fixtures"
```

Optional properties are `generatedTestRoot`, `taffyFixtureCategory`, `chromeBinary`, and `chromeDriver`. Their environment equivalents are `TAFFY_GENERATED_TEST_ROOT`, `TAFFY_FIXTURE_CATEGORY`, `CHROME_BINARY`, and `CHROME_DRIVER`.

Run upstream XML layout assertions directly with an explicit XML root. Restrict a run to one group while expanding the runner's supported style surface:

```powershell
.\gradlew.bat test "-PtaffyXmlRoot=<path-to-taffy>/tests/xml" -PtaffyXmlGroup=grid
```

## Credits

- Original [Taffy](https://github.com/DioxusLabs/taffy) library by DioxusLabs
- Python bindings [Stretchable](https://github.com/mortencombat/stretchable) for reference
