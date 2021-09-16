package scg.blowback.utils.functions;

import org.reactivestreams.Publisher;
import org.springframework.web.reactive.function.server.ServerRequest;

@FunctionalInterface
public interface RequestHandler<T> {
    Publisher<T> onRequest(ServerRequest request);
}
