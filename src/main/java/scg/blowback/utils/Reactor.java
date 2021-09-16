package scg.blowback.utils;

import io.atlassian.fugue.Unit;
import reactor.core.publisher.Mono;
import scg.blowback.utils.functions.Action;

import java.util.function.Function;

import static io.atlassian.fugue.Unit.VALUE;

public final class Reactor {

    public static final Mono<Unit> unit = Mono.just(VALUE);

    private Reactor() {
        throw new UnsupportedOperationException();
    }

    public static Mono<Unit> unit() {
        return unit;
    }

    public static Mono<Unit> unit(Action action) {
        return Mono.fromRunnable(action).thenReturn(VALUE);
    }

    public static <A, R> Function<A, Mono<A>> tap(Function<A, Mono<R>> f) {
        return a -> Mono.just(a).flatMap(f).thenReturn(a);
    }

    public static <A, B, R> Function<A, Mono<R>> bind(Function<A, Mono<B>> g, Function<B, Mono<R>> f) {
        return g.andThen(b -> b.flatMap(f));
    }

}
