package scg.blowback.utils.functions;

import io.atlassian.fugue.Option;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;

import static io.atlassian.fugue.Option.option;

@FunctionalInterface
public interface SseFlowHandler extends RequestHandler<ServerSentEvent<Object>> {

    String LAST_EVENT_ID_HEADER = "Last-Event-ID";

    @Override
    Flux<ServerSentEvent<Object>> onRequest(ServerRequest request);

    static Option<String> findLastEventId(ServerRequest request) {
        return option((request.headers()).firstHeader(LAST_EVENT_ID_HEADER));
    }

}
