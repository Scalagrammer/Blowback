package scg.blowback.http.response;

import reactor.core.publisher.Mono;
import scg.blowback.http.response.Alert.ErrorAlert;

import java.util.function.Function;

import static reactor.core.publisher.Mono.error;

@FunctionalInterface
public interface AlertFold {

    <R> R fold(Function<ErrorAlert, R> onError, Function<Alert, R> onAlert);

    default <R> Mono<R> raiseOr(Function<Alert, Mono<R>> onAlert) {
        return this.fold(error -> error(error.throwable()), onAlert);
    }

}
