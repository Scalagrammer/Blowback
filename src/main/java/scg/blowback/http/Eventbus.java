package scg.blowback.http;

import com.fasterxml.jackson.databind.ser.std.ObjectArraySerializer;
import io.atlassian.fugue.Unit;
import reactor.core.publisher.Mono;
import org.springframework.context.ApplicationEventPublisher;

import java.util.EventObject;

import static scg.blowback.utils.Reactor.unit;

@FunctionalInterface
public interface Eventbus {

    Mono<Unit> fire(EventObject event);

    static Eventbus fromEventPublisher(ApplicationEventPublisher publisher) {
        return event -> unit(() -> publisher.publishEvent(event));
    }

}
