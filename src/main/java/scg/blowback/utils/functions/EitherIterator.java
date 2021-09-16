package scg.blowback.utils.functions;

import io.atlassian.fugue.Either;

import java.util.function.Consumer;

@FunctionalInterface
public interface EitherIterator<L, R> {

    void forEach(Consumer<L> onLeft, Consumer<R> onRight);

    static <L, R> EitherIterator<L, R> on(Either<L, R> either) {
        return (onLeft, onRight) -> {

            for (R right : either.right()) onRight.accept(right);

            for (L left  :  either.left()) onLeft.accept(left);

        };
    }

}
