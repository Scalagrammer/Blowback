package scg.blowback.http;

import reactor.core.publisher.Flux;
import scg.blowback.http.response.Alert;

import java.util.stream.Stream;

@FunctionalInterface
public interface AlertCollector {

    Stream<Alert> collectAll();

    static AlertCollector stream(Flux<Alert> alerts) {
        return alerts::toStream;
    }

}
