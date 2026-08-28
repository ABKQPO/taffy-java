package dev.vfyjxf.taffy.style;

import dev.vfyjxf.taffy.style.GridPlacement.Type;

import java.util.Objects;

/** A grid placement whose named line is represented by an application-defined identifier. */
public record GenericGridPlacement<S>(Type type, int value, S identifier, int nthIndex) {
    public GenericGridPlacement {
        Objects.requireNonNull(type, "type");
        if ((type == Type.NAMED_LINE || type == Type.NAMED_SPAN) && identifier == null) {
            throw new IllegalArgumentException("Named placement requires an identifier");
        }
    }

    public static <S> GenericGridPlacement<S> auto() {
        return new GenericGridPlacement<>(Type.AUTO, 0, null, 0);
    }

    public static <S> GenericGridPlacement<S> line(int line) {
        return new GenericGridPlacement<>(Type.LINE, line, null, 0);
    }

    public static <S> GenericGridPlacement<S> namedLine(S name) {
        return namedLine(name, 1);
    }

    public static <S> GenericGridPlacement<S> namedLine(S name, int occurrence) {
        return new GenericGridPlacement<>(Type.NAMED_LINE, 0, name, occurrence == 0 ? 1 : occurrence);
    }

    public static <S> GenericGridPlacement<S> span(int span) {
        return new GenericGridPlacement<>(Type.SPAN, span, null, 0);
    }

    public static <S> GenericGridPlacement<S> namedSpan(S name, int count) {
        return new GenericGridPlacement<>(Type.NAMED_SPAN, count, name, 0);
    }

    public GridPlacement toGridPlacement(CustomIdentCodec<S> codec) {
        return switch (type) {
            case AUTO -> GridPlacement.auto();
            case LINE -> GridPlacement.line(value);
            case NAMED_LINE -> GridPlacement.namedLine(codec.encode(identifier), nthIndex);
            case SPAN -> GridPlacement.span(value);
            case NAMED_SPAN -> GridPlacement.namedSpan(codec.encode(identifier), value);
        };
    }
}
