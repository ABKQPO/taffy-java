package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;

import java.util.Arrays;

/**
 * A cache for storing layout computation results.
 *
 * <p>The keys and measurement lookup rules match Rust Taffy's cache. A layout result requires an
 * exact input key. Size measurements may reuse an entry with the same mixed known-dimension and
 * available-space key when the horizontal parent size and requested axis are compatible.</p>
 */
public class LayoutCache {
    private static final int CACHE_SIZE = 9;

    private CacheEntry<LayoutOutput> finalLayoutEntry;

    @SuppressWarnings("unchecked")
    private final CacheEntry<FloatSize>[] measureEntries = new CacheEntry[CACHE_SIZE];

    private boolean isEmpty = true;
    private int recentlyUsedEntries;
    private int nextMeasureEntry;

    private static class CacheEntry<T> {
        private final CacheKey key;
        private T content;

        private CacheEntry(CacheKey key, T content) {
            this.key = key;
            this.content = content;
        }
    }

    private record CacheKey(
        long knownDimensionsAndAvailableSpace,
        long parentSize,
        TaffySize<Boolean> knownDimensionsAreDefinite,
        RequestedAxis axis
    ) {
        private static CacheKey from(LayoutInput input) {
            return new CacheKey(
                mixedSizeKey(input.knownDimensions(), input.availableSpace()),
                optionalSizeKey(input.parentSize()),
                normalizedKnownDimensionsAreDefinite(input),
                input.axis()
            );
        }

        private boolean sizeIsValidFor(CacheKey requested) {
            return axis == RequestedAxis.BOTH || axis == requested.axis;
        }

        private boolean matchesMeasurement(CacheKey requested) {
            return knownDimensionsAndAvailableSpace == requested.knownDimensionsAndAvailableSpace
                && knownDimensionsAreDefinite.equals(requested.knownDimensionsAreDefinite)
                && horizontalParentSize() == requested.horizontalParentSize()
                && sizeIsValidFor(requested);
        }

        private long horizontalParentSize() {
            return parentSize & 0xFFFFFFFF00000000L;
        }

        private static TaffySize<Boolean> normalizedKnownDimensionsAreDefinite(LayoutInput input) {
            FloatSize knownDimensions = input.knownDimensions();
            TaffySize<Boolean> definiteness = input.knownDimensionsAreDefinite();
            return new TaffySize<>(
                Boolean.TRUE.equals(definiteness.width) || Float.isNaN(knownDimensions.width),
                Boolean.TRUE.equals(definiteness.height) || Float.isNaN(knownDimensions.height)
            );
        }

        private static long mixedSizeKey(FloatSize knownDimensions, TaffySize<AvailableSpace> availableSpace) {
            return pack(
                mixedDimensionKey(knownDimensions.width, availableSpace.width),
                mixedDimensionKey(knownDimensions.height, availableSpace.height)
            );
        }

        private static long optionalSizeKey(FloatSize size) {
            return pack(optionalDimensionKey(size.width), optionalDimensionKey(size.height));
        }

        private static int mixedDimensionKey(float knownDimension, AvailableSpace availableSpace) {
            return Float.isNaN(knownDimension)
                ? availableSpaceKey(availableSpace)
                : Float.floatToRawIntBits(knownDimension);
        }

        private static int optionalDimensionKey(float value) {
            return Float.isNaN(value)
                ? Float.floatToRawIntBits(Float.POSITIVE_INFINITY)
                : Float.floatToRawIntBits(value);
        }

        private static int availableSpaceKey(AvailableSpace availableSpace) {
            return switch (availableSpace.getType()) {
                case DEFINITE -> Float.floatToRawIntBits(-availableSpace.getValue());
                case MIN_CONTENT -> Float.floatToRawIntBits(Float.NEGATIVE_INFINITY);
                case MAX_CONTENT -> Float.floatToRawIntBits(Float.POSITIVE_INFINITY);
            };
        }

        private static long pack(int width, int height) {
            return ((long) width << 32) | Integer.toUnsignedLong(height);
        }
    }

    /**
     * Try to retrieve a cached result for the supplied layout input.
     */
    public LayoutOutput get(LayoutInput input) {
        if (isEmpty || input.runMode() == RunMode.PERFORM_HIDDEN_LAYOUT) {
            return null;
        }

        CacheKey key = CacheKey.from(input);
        if (input.runMode() == RunMode.PERFORM_LAYOUT) {
            return finalLayoutEntry != null && finalLayoutEntry.key.equals(key)
                ? finalLayoutEntry.content
                : null;
        }

        for (int index = 0; index < CACHE_SIZE; index++) {
            CacheEntry<FloatSize> entry = measureEntries[index];
            if (entry != null && entry.key.matchesMeasurement(key)) {
                recentlyUsedEntries |= 1 << index;
                return LayoutOutput.fromOuterSize(copySize(entry.content));
            }
        }
        return null;
    }

    /**
     * Store a layout result using the supplied layout input.
     */
    public void store(LayoutInput input, LayoutOutput layoutOutput) {
        if (input.runMode() == RunMode.PERFORM_HIDDEN_LAYOUT) {
            return;
        }

        CacheKey key = CacheKey.from(input);
        if (input.runMode() == RunMode.PERFORM_LAYOUT) {
            isEmpty = false;
            finalLayoutEntry = new CacheEntry<>(key, layoutOutput);
            return;
        }

        if (layoutOutput.marginsCanCollapseThrough()
            || !layoutOutput.topMargin().isZero()
            || !layoutOutput.bottomMargin().isZero()) {
            return;
        }

        isEmpty = false;
        for (int index = 0; index < CACHE_SIZE; index++) {
            CacheEntry<FloatSize> entry = measureEntries[index];
            if (entry != null && entry.key.equals(key)) {
                entry.content = copySize(layoutOutput.size());
                recentlyUsedEntries |= 1 << index;
                return;
            }
        }

        while ((recentlyUsedEntries & (1 << nextMeasureEntry)) != 0) {
            recentlyUsedEntries &= ~(1 << nextMeasureEntry);
            nextMeasureEntry = (nextMeasureEntry + 1) % CACHE_SIZE;
        }
        measureEntries[nextMeasureEntry] = new CacheEntry<>(key, copySize(layoutOutput.size()));
        recentlyUsedEntries |= 1 << nextMeasureEntry;
        nextMeasureEntry = (nextMeasureEntry + 1) % CACHE_SIZE;
    }

    /**
     * Clear all cache entries.
     *
     * @return the outcome of the clear operation
     */
    public ClearState clear() {
        if (isEmpty) {
            return ClearState.ALREADY_EMPTY;
        }
        finalLayoutEntry = null;
        Arrays.fill(measureEntries, null);
        recentlyUsedEntries = 0;
        nextMeasureEntry = 0;
        isEmpty = true;
        return ClearState.CLEARED;
    }

    /**
     * Check whether the cache has no stored entries.
     */
    public boolean isEmpty() {
        return isEmpty;
    }

    private static FloatSize copySize(FloatSize size) {
        return new FloatSize(size.width, size.height);
    }
}
