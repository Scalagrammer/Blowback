package scg.blowback.http.sse;

import reactor.core.publisher.Flux;

import java.util.function.Function;

@FunctionalInterface
public interface ServerSideEventStreaming<T, Q extends ServerSideEventQuery> extends Function<Q, Flux<ServerSideEvent<T>>> {

    Flux<ServerSideEvent<T>> stream(Q query);

    @Override
    default Flux<ServerSideEvent<T>> apply(Q query) {
        return this.stream(query);
    }

}