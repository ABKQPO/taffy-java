package dev.vfyjxf.taffy.style;

/** Resolves a calc expression in the context of a caller-owned layout tree. */
@FunctionalInterface
public interface CalcValueResolver {
    float resolve(CalcExpression expression, float basis);
}
