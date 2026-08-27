package dev.vfyjxf.taffy.geometry;

import java.util.Objects;

/** A pair containing independently typed minimum and maximum values. */
public class MinMax<Min, Max> {
    public Min min;
    public Max max;

    public MinMax(Min min, Max max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MinMax<?, ?> other)) return false;
        return Objects.equals(min, other.min) && Objects.equals(max, other.max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }

    @Override
    public String toString() {
        return "MinMax{min=" + min + ", max=" + max + '}';
    }
}
