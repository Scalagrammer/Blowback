package scg.blowback.utils.functions;

import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface RequestFlowHandler extends RequestHandler<Object> {
    @Override
    Mono<Object> onRequest(ServerRequest request);
}
