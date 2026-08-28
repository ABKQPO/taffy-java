package dev.vfyjxf.taffy.style;

/** Stable value representation of a {@link CompactLength} for serializers and external stores. */
public record CompactLengthData(int tag, float value, String calcKey) {
}
