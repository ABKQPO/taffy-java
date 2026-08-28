package dev.vfyjxf.taffy.style;

import java.util.Objects;

/** A named grid area with an application-defined identifier. */
public record GenericGridTemplateArea<S>(S name, int rowStart, int rowEnd, int columnStart, int columnEnd) {
    public GenericGridTemplateArea {
        Objects.requireNonNull(name, "name");
    }

    public GridTemplateArea toGridTemplateArea(CustomIdentCodec<S> codec) {
        return new GridTemplateArea(codec.encode(name), rowStart, rowEnd, columnStart, columnEnd);
    }
}
