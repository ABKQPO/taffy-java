package dev.vfyjxf.taffy.style;

import java.util.Objects;

/** A grid-template component whose repeat line names use an application-defined identifier type. */
public class GenericGridTemplateComponent<S> {
    private final TrackSizingFunction single;
    private final GenericGridRepetition<S> repeat;

    private GenericGridTemplateComponent(TrackSizingFunction single, GenericGridRepetition<S> repeat) {
        this.single = single;
        this.repeat = repeat;
    }

    public static <S> GenericGridTemplateComponent<S> single(TrackSizingFunction track) {
        return new GenericGridTemplateComponent<>(Objects.requireNonNull(track, "track"), null);
    }

    public static <S> GenericGridTemplateComponent<S> repeat(GenericGridRepetition<S> repetition) {
        return new GenericGridTemplateComponent<>(null, Objects.requireNonNull(repetition, "repetition"));
    }

    public boolean isSingle() {
        return single != null;
    }

    public TrackSizingFunction single() {
        return single;
    }

    public GenericGridRepetition<S> repeat() {
        return repeat;
    }

    public GridTemplateComponent toGridTemplateComponent(CustomIdentCodec<S> codec) {
        return isSingle() ? GridTemplateComponent.single(single) : GridTemplateComponent.repeat(repeat.toGridRepetition(codec));
    }
}
