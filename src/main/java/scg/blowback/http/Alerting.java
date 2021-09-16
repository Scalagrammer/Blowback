package scg.blowback.http;

import io.atlassian.fugue.Unit;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import scg.blowback.http.response.Alert;

import static scg.blowback.utils.Reactor.unit;

@FunctionalInterface
public interface Alerting {

    Mono<Unit> push(Alert alert);

    static Alerting pushing(FluxSink<Alert> sink) {
        return alert -> unit(() -> sink.next(alert));
    }

}
