# Layout Cache Parity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Align Java layout-cache lookup, storage, and tree APIs with Rust Taffy v0.14 cache semantics.

**Architecture:** `LayoutInput` is the sole cache boundary. `LayoutCache` derives immutable cache keys from known dimensions, available space, parent size, definiteness, and requested axis. Final layout uses an exact key; size measurement uses Rust's horizontal-parent-size compatibility rule and axis validity rule.

**Tech Stack:** Java 21, Gradle, JUnit Jupiter.

---

### Task 1: Specify parent-size cache invalidation

**Files:**
- Create: `src/test/java/dev/vfyjxf/taffy/LayoutCacheParityTest.java`

- [x] **Step 1: Write the failing test**

```java
@Test
void percentageSizeMeasurementDoesNotReuseAResultForAnotherParentWidth() {
    TaffyTree tree = new TaffyTree();
    TaffyStyle style = new TaffyStyle();
    style.size = new TaffySize<>(TaffyDimension.percent(0.5f), TaffyDimension.AUTO);
    NodeId node = tree.newLeaf(style);

    LayoutOutput first = tree.computeChildLayout(node, input(100f));
    LayoutOutput second = tree.computeChildLayout(node, input(200f));

    assertEquals(50f, first.size().width, 0.01f);
    assertEquals(100f, second.size().width, 0.01f);
}
```

- [x] **Step 2: Run test to verify it fails**

Run: `./gradlew.bat test --tests dev.vfyjxf.taffy.LayoutCacheParityTest --no-daemon`

Expected: FAIL because the second cached size is reused despite the different parent width.

### Task 2: Port Rust cache key behavior

**Files:**
- Modify: `src/main/java/dev/vfyjxf/taffy/tree/LayoutCache.java`
- Modify: `src/main/java/dev/vfyjxf/taffy/tree/LayoutPartialTree.java`
- Modify: `src/main/java/dev/vfyjxf/taffy/tree/TaffyTree.java`
- Modify: `src/main/java/dev/vfyjxf/taffy/tree/LayoutComputer.java`
- Modify: `src/test/java/dev/vfyjxf/taffy/LowLevelLayoutApiTest.java`

- [x] **Step 1: Add failing direct cache behavior tests**

```java
assertNull(cache.get(inputWithParentWidth(200f)));
assertNotNull(cache.get(inputWithAxis(RequestedAxis.VERTICAL)));
```

- [x] **Step 2: Implement an immutable cache key derived from `LayoutInput`**

```java
private record CacheKey(
    FloatSize knownDimensions,
    TaffySize<AvailableSpace> availableSpace,
    FloatSize parentSize,
    TaffySize<Boolean> knownDimensionsAreDefinite,
    RequestedAxis axis
) {
}
```

- [x] **Step 3: Implement Rust-compatible lookup rules**

```java
boolean isValidFor(CacheKey requested) {
    return axis == RequestedAxis.BOTH || axis == requested.axis;
}
```

Final layout keys must compare exactly. Measurement entries must compare known dimensions, available space, normalized definiteness, horizontal parent dimension, and axis compatibility.

- [x] **Step 4: Pass one `LayoutInput` through cache interfaces**

```java
LayoutOutput getCacheEntry(NodeId node, LayoutInput input);
void storeCacheEntry(NodeId node, LayoutInput input, LayoutOutput output);
```

- [x] **Step 5: Run focused tests**

Run: `./gradlew.bat test --tests dev.vfyjxf.taffy.LayoutCacheParityTest --tests dev.vfyjxf.taffy.CacheDiagnosticTest --no-daemon`

Expected: PASS.

### Task 3: Verify integration and constraints

**Files:**
- Modify: files from Tasks 1-2 only as required by formatting or compilation.

- [x] **Step 1: Run all tests**

Run: `./gradlew.bat test --no-daemon`

Expected: BUILD SUCCESSFUL.

- [x] **Step 2: Check source constraints**

Run: `git diff --check` and `rg -n -- '//\\s*[-=]{4,}' src/main/java`

Expected: no whitespace errors and no separator comments.

- [ ] **Step 3: Commit and push the completed migration group**

```powershell
git add src/main/java src/test/java docs/superpowers/plans
git commit -m "align layout cache with Rust cache keys"
git push origin main
```
