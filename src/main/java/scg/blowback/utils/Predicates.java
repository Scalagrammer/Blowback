package scg.blowback.utils;

import java.util.function.Function;
import java.util.function.Predicate;

public final class Predicates {

    private Predicates() {
        throw new UnsupportedOperationException();
    }

    public static <A, R> Predicate<A> at(Function<A, R> f, Predicate<R> p) {
        return a -> p.test(f.apply(a));
    }

}
